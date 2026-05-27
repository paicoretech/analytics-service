package com.paic.nbm.analyticsservice.Service;

import com.paic.nbm.analyticsservice.Entities.DownloadJob;
import com.paic.nbm.analyticsservice.Entities.DownloadJobStatus;
import com.paic.nbm.analyticsservice.Repositories.DownloadJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GenericQueueService {
    private final DownloadJobRepository jobRepository;
    private final PcapJobProcessorImpl pcapProcessor;
    // Temporary in-memory store for payloads
    private final Map<UUID, Map<String, Object>> jobPayloads = new ConcurrentHashMap<>();

    private final ThreadPoolTaskExecutor pcapExecutor;
    @Autowired
    public GenericQueueService(DownloadJobRepository jobRepository, PcapJobProcessorImpl pcapProcessor, @Qualifier("pcapExecutor") ThreadPoolTaskExecutor pcapExecutor) {
        this.jobRepository = jobRepository;
        this.pcapProcessor = pcapProcessor;
        this.pcapExecutor = pcapExecutor;
    }

    @PostConstruct
    public void cleanupStuckJobs() {
        log.info("STARTUP: Cleaning up stuck jobs from previous session");
        try {
            List<DownloadJob> stuckJobs = jobRepository.findByStatusIn(
                    Arrays.asList(DownloadJobStatus.IN_PROGRESS, DownloadJobStatus.QUEUED)
            );

            for (DownloadJob job : stuckJobs) {
                log.warn("Marking stuck job [{}] as FAILED", job.getJobId());
                job.setStatus(DownloadJobStatus.FAILED);
                job.setUpdatedAt(LocalDateTime.now());
                jobRepository.save(job);
            }
            log.info("STARTUP: Cleanup finished. {} jobs marked as FAILED", stuckJobs.size());
        } catch (Exception e) {
            log.error("Failed to cleanup stuck jobs on startup", e);
        }
    }

    public DownloadJob addToQueue(String type, Map<String, Object> payload) {
        String fileName = "nbm-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        DownloadJob job = DownloadJob.builder()
                .jobId(UUID.randomUUID())
                .status(DownloadJobStatus.QUEUED)
                .fileName(fileName) // default
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        DownloadJob savedJob = jobRepository.save(job);
        jobRepository.flush(); // To ensure ID + UUID are persisted
        payload.put("type", type); // type can be "PCAP" or "PCAP_PARTS"
        payload.put("jobId", job.getJobId().toString());
        payload.put("fileName", fileName);

        jobPayloads.put(job.getJobId(), payload);
        return savedJob;
    }

    public DownloadJob addToQueueWithPayload(String type, Map<String, Object> payload) {
        String fileName = "nbm-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        DownloadJob job = DownloadJob.builder()
                .jobId(UUID.randomUUID())
                .status(DownloadJobStatus.QUEUED)
                .fileName(fileName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        DownloadJob savedJob = jobRepository.save(job);
        jobRepository.flush();

        // This will let the processor know if it's parts-based
        payload.put("type", type); // type can be "PCAP" or "PCAP_PARTS"
        payload.put("jobId", job.getJobId().toString());
        payload.put("fileName", fileName);
        log.info(payload.get("fileName").toString());

        jobPayloads.put(job.getJobId(), payload);

        return savedJob;
    }

    public void processQueue() {
        List<DownloadJob> jobs = jobRepository.findByStatusIn(List.of(DownloadJobStatus.QUEUED));
        for (DownloadJob job : jobs) {
            if (job.isCancelled()) {
                continue;
            }
            UUID jobId = job.getJobId();

            if (!jobPayloads.containsKey(jobId)) {
                log.warn("Skipping Job {}: Payload not found ", jobId);
                continue;
            }

            int maxSlots = pcapExecutor.getMaxPoolSize();
            int activeDownloads = pcapExecutor.getActiveCount();
            int availableSlots = maxSlots - activeDownloads;

            log.info("Preparing to add Job {}. Total slots: {}. Active downloads: {}. Available slots: {}",
                    job.getJobId(), maxSlots, activeDownloads, availableSlots);

            if (availableSlots <= 0) {
                log.warn("Total download slots exhausted (0 left). Job {} will be queued and wait for a slot.", job.getJobId());
            } else {
                log.info("Slot available. Job {} will be added immediately. Total slots left after this: {}",
                        job.getJobId(), (availableSlots - 1));
            }

            HashMap<String, Object> payload = new HashMap<> (jobPayloads.getOrDefault(jobId, Collections.emptyMap()));
            pcapProcessor.process(job, payload);
            jobPayloads.remove(jobId); // cleanup after processing
        }
    }

    public void cancelJob(UUID jobId) {
        jobRepository.findByJobId(jobId).ifPresent(job -> {
            log.info("Request received to cancel Job ID: {}", jobId);
            job.setCancelled(true);
            job.setStatus(DownloadJobStatus.CANCELLED);
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
            processQueue();
        });
    }

    public void clearJob(UUID jobId) {
        jobRepository.findByJobId(jobId).ifPresent(job -> {
            log.info("Clearing job [{}] ", jobId);
            job.setStatus(DownloadJobStatus.DELETED);
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
        });
    }

    public Page<DownloadJob> listDownloads(DownloadJobStatus status, LocalDateTime startDate,
                                           LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobRepository.findDownloadsWithFilters(
                status, startDate, endDate, pageable
        );
    }
}
