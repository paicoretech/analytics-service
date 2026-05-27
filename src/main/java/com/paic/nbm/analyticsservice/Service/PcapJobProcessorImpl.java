package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.DownloadJob;
import com.paic.nbm.analyticsservice.Entities.DownloadJobStatus;
import com.paic.nbm.analyticsservice.Repositories.DownloadJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@Service
public class PcapJobProcessorImpl {

    private final DownloadJobRepository jobRepository;
    private final PcapGenerationService pcapGenerationService;
    private final ThreadPoolTaskExecutor pcapExecutor;

    @Autowired
    public PcapJobProcessorImpl(DownloadJobRepository jobRepository, PcapGenerationService pcapGenerationService, @Qualifier("pcapExecutor") ThreadPoolTaskExecutor pcapExecutor) {
        this.jobRepository = jobRepository;
        this.pcapGenerationService = pcapGenerationService;
        this.pcapExecutor = pcapExecutor;
    }

    /**
     * Main processor to handle job execution based on the type of payload
     */
    @Async("pcapExecutor")
    public void process(DownloadJob job, HashMap<String, Object> payload) {
        log.info("Slot available. Starting processing for Job ID: {}", job.getJobId());
        DownloadJob currentJobState = jobRepository.findByJobId(job.getJobId()).orElse(job);
        if (currentJobState.isCancelled() || currentJobState.getStatus() == DownloadJobStatus.CANCELLED) {
            log.info("Job {} was cancelled. Skipping execution.", job.getJobId());
            return;
        }

        job.setStatus(DownloadJobStatus.IN_PROGRESS);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        try {
            String type = (String) payload.getOrDefault("type", "PCAP");
            String fileName = (String) payload.getOrDefault("fileName", "nbm-default");
            String workspaceId;

            if ("PCAP_PARTS".equals(type)) {
                workspaceId = pcapGenerationService.generateFromParts(payload, fileName);
            } else {
                workspaceId = pcapGenerationService.generate(payload, fileName);
            }

            if (workspaceId == null) {
                DownloadJob latestState = jobRepository.findByJobId(job.getJobId()).orElse(job);
                if (latestState.isCancelled() || latestState.getStatus() == DownloadJobStatus.CANCELLED) {
                    job.setStatus(DownloadJobStatus.CANCELLED);
                    log.info("Job {} finalized as CANCELLED.", job.getJobId());
                } else {
                    job.setStatus(DownloadJobStatus.FAILED);
                    log.error("Job {} returned null workspace but was not cancelled. Marking FAILED.", job.getJobId());
                }
            } else {
//                String fileN
                job.setStatus(DownloadJobStatus.COMPLETED);
                job.setFileName(job.getFileName()+ ".pcap");
                job.setFilePath("/tmp/nbm_" + workspaceId + "/"+job.getFileName());
            }

        } catch (Exception e) {
            job.setStatus(DownloadJobStatus.FAILED);
            log.error("Job {} failed with exception", job.getJobId(), e);
        } finally {
            int maxSlots = pcapExecutor.getMaxPoolSize();
            int activeDownloads = pcapExecutor.getActiveCount();

            int availableAfterExit = maxSlots - (activeDownloads - 1);
            if (availableAfterExit > maxSlots) availableAfterExit = maxSlots;

            log.info("Job ID: {} finished. Total slots: {}. Total Slots available : {}",
                    job.getJobId(), maxSlots, availableAfterExit);

            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }
}
