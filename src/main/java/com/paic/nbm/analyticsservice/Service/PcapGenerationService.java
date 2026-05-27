package com.paic.nbm.analyticsservice.Service;

import com.google.gson.GsonBuilder;
import com.paic.nbm.analyticsservice.Entities.DownloadJob;
import com.paic.nbm.analyticsservice.Entities.DownloadJobStatus;
import com.paic.nbm.analyticsservice.PcapGenerator.PcapDownloaderInfo;
import com.paic.nbm.analyticsservice.PcapGenerator.PcapFileFrame;
import com.paic.nbm.analyticsservice.PcapGenerator.SerializableController;
import com.paic.nbm.analyticsservice.ProtocolBuilder.ConsolidatedProtocolAnalytics;
import com.paic.nbm.analyticsservice.Repositories.DownloadJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PcapGenerationService {
    @Value("${merge.pcap.binary}")
    private String mergePcapBinary;

    @Value("${tshark.frames.separator}")
    private String tsharkFramesSeparator;

    @Autowired
    private ConsolidatedProtocolAnalytics consolidatedAnalytics;

    @Autowired
    private JobProgressService progressService;

    @Autowired
    private DownloadJobRepository jobRepository;

    /**
     * Main entrypoint for PCAP generation (Queued Job will call this)
     */
    public String generate(Map<String, Object> jsonRequest, String fileName) {
        try {
            String encodedPcapInfo = jsonRequest.get("frames").toString();
            List<PcapDownloaderInfo> infos = SerializableController.decode(encodedPcapInfo);

            String jobIdStr = (String) jsonRequest.get("jobId");
            UUID jobId = (jobIdStr != null) ? UUID.fromString(jobIdStr) : null;

            String workspacePrefix = createWorkspace();
            File mergedFile = getMergedFile(workspacePrefix, fileName);
            buildPcapUsingFrames(new File("/tmp/nbm_" + workspacePrefix), mergedFile, infos, jobId);

            if (jobId != null) {
                DownloadJob job = jobRepository.findByJobId(jobId).orElse(null);
                if (job != null && (job.isCancelled() || job.getStatus() == DownloadJobStatus.CANCELLED)) {
                    return null;
                }
            }

            return mergedFile.exists() ? workspacePrefix : null;
        } catch (Exception e) {
            log.error("Error generating PCAP", e);
            return null;
        }
    }

    /**
     * Alternative method for generating PCAP using paginated requests
     */
    public String generateFromParts(HashMap<String, Object> fields, String fileName) {
        try {
            List<Integer> requestedPages = Arrays.stream(((String) fields.get("page")).split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            List<PcapDownloaderInfo> infos = new ArrayList<>();
            for (Integer page : requestedPages) {
                if (fields.containsKey("jobId")) {
                    String jId = (String) fields.get("jobId");
                    DownloadJob job = jobRepository.findByJobId(UUID.fromString(jId)).orElse(null);
                    if (job != null && job.isCancelled()) {
                        log.info("Job ID: {} - Cancellation detected Stopping JobID. ", jId);
                        return null;
                    }
                }
                fields.put("page", String.valueOf(page));
                HashMap<String, Object> diagram = consolidatedAnalytics.buildSequenceDiagramAll(fields);
                infos.addAll(SerializableController.decode((String) diagram.get("pcapData")));
            }

            HashMap<String, Object> payload = new HashMap<>();
            String strEncoded = SerializableController.encode(new GsonBuilder().disableHtmlEscaping().create().toJson(infos));
            payload.put("frames", strEncoded);

            if (fields.containsKey("jobId")) {
                payload.put("jobId", fields.get("jobId"));
            }
            return generate(payload,fileName);
        } catch (Exception e) {
            log.error("Error generating PCAP from parts", e);
            return null;
        }
    }

    public void buildPcapUsingFrames(File workspacePath, File mergedFile, List<PcapDownloaderInfo> infos, UUID jobId) {
        long totalFrames = infos.stream().mapToLong(info -> info.getPcapFileFrameList().size()).sum();
        long processedCount = 0;

        int i = 0;
        for (PcapDownloaderInfo info : infos) {
            for (PcapFileFrame frame : info.getPcapFileFrameList()) {

                if (jobId != null) {
                    Optional<DownloadJob> jobOpt = jobRepository.findByJobId(jobId);
                    if (jobOpt.isPresent()) {
                        DownloadJob job = jobOpt.get();
                        if (job.isCancelled() || job.getStatus() == DownloadJobStatus.CANCELLED) {
                            log.info("Job ID: {} - Cancellation JobID: ", jobId);
                            return;
                        }
                    }
                }

                String fileIndex = frame.getFileName().endsWith(".gz") ? frame.getFileName() : frame.getFileName() + ".gz";

                if (!new File(fileIndex).exists()) {
                    // Removing .gz from the filename and check if the file exists
                    fileIndex = fileIndex.substring(0, fileIndex.length() - 3);
                    if (!new File(fileIndex).exists()) {
                        log.error("PCAP file NOT FOUND in both names: {}, {}.gz", fileIndex, fileIndex);
                        continue;
                    }
                }

                String frameName = workspacePath.getAbsolutePath() + "/selection_" + i + ".pcap";
                String query = info.getTsharkQuery().isEmpty() ? getFrameQuery(frame.getFrames()) : info.getTsharkQuery();
                runBashCmd("tshark -r " + fileIndex + " -Y " + query + " -w " + frameName);

                processedCount++;
                if (jobId != null && totalFrames > 0) {
                    int progress = (int) ((processedCount * 90) / totalFrames);
                    progressService.sendProgress(jobId, progress, "IN_PROGRESS");
                }

                i++;
            }
        }

        // Final check before merging
        if (jobId != null) {
            DownloadJob job = jobRepository.findByJobId(jobId).orElse(null);
            if (job != null && (job.isCancelled() || job.getStatus() == DownloadJobStatus.CANCELLED)) {
                log.info("Job ID: {} - Cancellation detected.", jobId);
                return;
            }
        }

        runBashCmd(mergePcapBinary + " " + mergedFile.getAbsolutePath() + " " + workspacePath.getAbsolutePath());

        if (jobId != null) {
            progressService.sendProgress(jobId, 100, "COMPLETED");
        }
    }

    private String createWorkspace() {
        String prefix = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        File workspace = new File("/tmp/nbm_" + prefix);
        if (!workspace.mkdirs()) {
            log.error("Failed to create workspace at {}", workspace.getAbsolutePath());
            return null;
        }
        return prefix;
    }

    private File getMergedFile(String workspacePrefix, String fileName) {
        return new File("/tmp/nbm_" + workspacePrefix + "/" + fileName + ".pcap");
    }

    public String getFrameQuery(List<String> frames) {
        String delimiter = "COMMA".equals(tsharkFramesSeparator) ? "," : " ";
        String framesGroup = String.join(delimiter, frames);
        return "\"frame.number in {" + framesGroup + "}\"";
    }

    public int runBashCmd(String cmd) {
        try {

            ProcessBuilder builder;
            log.info("cmd {}", cmd);
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows
                builder = new ProcessBuilder("cmd.exe", "/c", cmd);
            } else {
                // Linux / Mac
                builder = new ProcessBuilder("/bin/bash", "-c", cmd);
            }
            builder.environment().put("JAVA_HOME", System.getProperty("java.home"));
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("Command exited with non-zero status: {}", exitCode);
            }
            return exitCode;
        } catch (Exception e) {
            log.error("Error running cmd [{}]: {}", cmd, e.getMessage());
            return -1;
        }
    }
}
