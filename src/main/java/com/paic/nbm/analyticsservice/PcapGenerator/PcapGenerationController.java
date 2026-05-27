package com.paic.nbm.analyticsservice.PcapGenerator;

import com.google.gson.GsonBuilder;
import com.paic.nbm.analyticsservice.ProtocolBuilder.ConsolidatedProtocolAnalytics;
import com.paic.nbm.analyticsservice.ProtocolBuilder.HTTPAnalytics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class PcapGenerationController {
    @Value("${process.cmd.debug}")
    private boolean processDebug;

    @Value("${logging.file.name}")
    private String logFile;

    @Value("${merge.pcap.binary}")
    String mergePcapBinary;

    @Value("${tshark.frames.separator}")
    private String tsharkFramesSeparator;

    @Autowired
    ConsolidatedProtocolAnalytics consolidatedAnalytics;

    HTTPAnalytics httpQueryBuilder;
    ApplicationContext applicationContext;

    public PcapGenerationController(HTTPAnalytics httpQueryBuilder, ApplicationContext applicationContext) {
        this.httpQueryBuilder = httpQueryBuilder;
        this.applicationContext = applicationContext;
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getPcap", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Object> getPcap(@RequestBody HashMap<String, Object> jsonRequest) {
        List<HashMap<String, String>> selection = (List<HashMap<String, String>>) jsonRequest.get("payload");
        String encodePcapDownloaderInfo = jsonRequest.get("frames").toString();
        List<PcapDownloaderInfo> pcapDownloaderInfos = SerializableController.decode(encodePcapDownloaderInfo);

        log.info("Received PCAP request.");
        String workspacePrefix = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        File workspacePath = new File("/tmp/nbm_" + workspacePrefix);
        File mergedFile = new File(workspacePath.getAbsolutePath() + "/merged_" + workspacePrefix + ".pcap");
        if (!workspacePath.mkdirs())
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);

        //Create pcap file
        buildPcapUsingFrames(workspacePath, mergedFile, pcapDownloaderInfos);

        if (!mergedFile.exists()) {
            log.error("Merged file NOT FOUND. Returning failed status for: [" + mergedFile.getAbsolutePath() + "]");
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(Collections.singletonMap("Result", workspacePrefix), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getPcapPart", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json")
    public ResponseEntity<Object> getPcapPart(@RequestBody Object fields) {
        log.info("Received download PCAP file by parts request, fields: " + fields );

        try {
            HashMap<String, Object> requestFields = (HashMap<String, Object>) fields;

            List<Integer> requestedPages = Arrays.stream(((String)requestFields.get("page")).split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            List<PcapDownloaderInfo> downloaderInfos = new ArrayList<>();

            for (Integer currentPage : requestedPages) {
                requestFields.put("page", Integer.toString(currentPage));
                HashMap<String, Object> resultDiagram = (consolidatedAnalytics.buildSequenceDiagramAll(requestFields));
                downloaderInfos.addAll(SerializableController.decode((String) resultDiagram.get("pcapData")));
            }
            String strEncondeInfos = SerializableController.encode(new GsonBuilder().disableHtmlEscaping().create().toJson(downloaderInfos));

            HashMap<String, Object> resultDiagramFinal = new HashMap<>();
            resultDiagramFinal.put("frames", strEncondeInfos);

            return getPcap(resultDiagramFinal);

        } catch (Exception ex) {
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("/download/{id}")
    public void download(HttpServletResponse response, @PathVariable String id) throws Exception {
        File workspacePath = new File("/tmp/nbm_" + id);
        String mergedFileStr = workspacePath.getAbsolutePath() + "/merged_" + id + ".pcap";
        File mergedFile = new File(mergedFileStr);
        log.info("Downloading pcap file {}", mergedFileStr);

        if (!mergedFile.exists()) {
            log.error("Cannot download PCAP, File DOES NOT EXIST:  [" + mergedFile.getAbsolutePath() + "]");
        }

        //File file = new File("/tmp/output.pcap");
        FileInputStream fis = new FileInputStream(mergedFile);
        response.setContentType("application/force-download");
        response.addHeader("Content-disposition", "attachment;fileName=" + "output.pcap");
        OutputStream os = response.getOutputStream();

        byte[] buf = new byte[1024];
        int len;
        while ((len = fis.read(buf)) != -1) {
            os.write(buf, 0, len);
        }
        fis.close();
    }

    public void buildPcapUsingFrames(File workspacePath, File generatedFile, List<PcapDownloaderInfo> filesWithFrames) {
        log.info("Building pcap file from local archive.");
        StringBuilder filesToMerge = new StringBuilder("");
        int i = 0;
        for (PcapDownloaderInfo pcapDownloaderInfo : filesWithFrames) {

            for (PcapFileFrame fileFrame : pcapDownloaderInfo.getPcapFileFrameList()) {
                String[] fileFormat = fileFrame.getFileName().split("\\.");
                String fileIndex = "";
                if (Arrays.asList(fileFormat).contains("gz"))
                    fileIndex = fileFrame.getFileName();
                else
                    fileIndex = fileFrame.getFileName() + ".gz";

                if (!new File(fileIndex).exists()) {
                    log.error("Archived file: [" + fileIndex + "] NOT FOUND");
                    break;
                }

                String frameName = workspacePath.getAbsolutePath() + "/selection_" + i + ".pcap";
                String tsharkQuery = pcapDownloaderInfo.getTsharkQuery().isEmpty()
                        ? getFrameQuery(fileFrame.getFrames())
                        : pcapDownloaderInfo.getTsharkQuery();
                String cmd = "tshark -r " + fileIndex + " -Y " + tsharkQuery + " -w " + frameName;
                runBashCmd(cmd);
                filesToMerge.append(" ").append(frameName);
                i++;
            }
        }

        //Merging frames
        runBashCmd("pwd");
        runBashCmd("sh " + mergePcapBinary + " " + generatedFile.getAbsolutePath() + " " + workspacePath.getAbsolutePath());
        //runBashCmd(" mergecap -w " + " " + generatedFile.getAbsolutePath() + " " + workspacePath.getAbsolutePath()+"/*");
    }

    @Deprecated
    public void buildPcap(List<HashMap<String, String>> selection, HashMap<String, String> parameters,
                          File workspacePath, File generatedFile) {
        log.info("Building pcap file from local archive.");
        StringBuilder filesToMerge = new StringBuilder();
        HashMap<String, PcapSlice> pcapSlices = new HashMap<>();

        //Collecting files and frames
        for (HashMap<String, String> frame : selection) {
            String fileIndex = frame.get("file_index") + ".gz";

            if (!new File(fileIndex).exists()) {
                log.error("Archived file: [{}] NOT FOUND", fileIndex);
                break;
            }

            PcapSlice slice = pcapSlices.getOrDefault(fileIndex, new PcapSlice());
            slice.getFrameList().add(frame.get("frames_list"));
            slice.setProtocolName(frame.get("protocol_name"));
            slice.setParameters(parameters);

            pcapSlices.put(fileIndex, slice);
        }

        //Extracting frames from files
        int i = 0;
        for (String key : pcapSlices.keySet()) {
            String frameName = workspacePath.getAbsolutePath() + "/selection_" + i + ".pcap";
            PcapSlice pcapFrame = pcapSlices.get(key);

            String cmd = "tshark -r " + key + " -Y " + pcapFrame.getFrameQuery(applicationContext) + " -w " + frameName;
            runBashCmd(cmd);
            filesToMerge.append(" ").append(frameName);
            i++;
        }

        //Merging frames
        runBashCmd("pwd");
        runBashCmd(mergePcapBinary + " " + generatedFile.getAbsolutePath() + " " + workspacePath.getAbsolutePath());
    }

    public String getFrameQuery(List<String> frameList) {
        String delimiter = "COMMA".equals(tsharkFramesSeparator) ? "," : " ";
        String frameQuery = null;
        String framesGroup = String.join(delimiter, frameList);
        frameQuery = "\"frame.number in {" + framesGroup + "\"}";
        return frameQuery;
    }

    public int runBashCmd(String cmd) {
        log.info("About to run: [" + cmd + "]");

        int exitCode = 0;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.environment().put("JAVA_HOME", System.getProperty("java.home"));
            processBuilder.command("/bin/bash", "-c", cmd + (!processDebug ? " >/dev/null 2>&1" : " >>" + logFile));
            Process process = processBuilder.start();

            exitCode = process.waitFor();
            log.info("Exited with code : {}", exitCode);

        } catch (Exception e) {
            log.error("Could not run [{}] with cause: {}", cmd, Arrays.toString(e.getStackTrace()));
            log.error(e.getMessage());
        }
        return exitCode;
    }
}