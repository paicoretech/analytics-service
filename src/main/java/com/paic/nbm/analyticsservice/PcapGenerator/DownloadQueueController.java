package com.paic.nbm.analyticsservice.PcapGenerator;

import com.paic.nbm.analyticsservice.Entities.DownloadJob;
import com.paic.nbm.analyticsservice.Entities.DownloadJobStatus;
import com.paic.nbm.analyticsservice.Repositories.DownloadJobRepository;
import com.paic.nbm.analyticsservice.Service.GenericQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import com.paic.nbm.analyticsservice.Service.JobProgressService;

@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class DownloadQueueController {
    private final GenericQueueService queueService;
    private final DownloadJobRepository jobRepository;
    private final JobProgressService progressService;

    @PostMapping(value = "/add",consumes = "application/json",produces = "application/json")
    public Mono<ResponseEntity<DownloadJob>> addToQueue(@RequestBody Map<String, Object> fields) {
        DownloadJob job = queueService.addToQueue("PCAP", fields);
        Mono.fromRunnable(queueService::processQueue)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        return Mono.just(ResponseEntity.ok(job));
    }

    @PostMapping(value = "/add/parts",consumes = "application/json",produces = "application/json")
    public Mono<ResponseEntity<DownloadJob>> addToQueueParts(@RequestBody Map<String, Object> fields) {
        DownloadJob job = queueService.addToQueueWithPayload("PCAP_PARTS", fields);
        Mono.fromRunnable(queueService::processQueue)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        return Mono.just(ResponseEntity.ok(job));
    }

    @GetMapping("/{jobId}")
    public Mono<ResponseEntity<DownloadJob>> getProgress(@PathVariable UUID jobId) {
        return Mono.fromCallable(() ->
                jobRepository.findByJobId(jobId)
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build())
        ).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    @PostMapping("/{jobId}/cancel")
    public Mono<ResponseEntity<Void>> cancelJob(@PathVariable UUID jobId) {
        queueService.cancelJob(jobId);
        return Mono.just(ResponseEntity.ok().build());
    }

    @PostMapping("/{jobId}/clear")
    public Mono<ResponseEntity<Void>> clearJob(@PathVariable UUID jobId) {
        return Mono.fromRunnable(() -> queueService.clearJob(jobId))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @GetMapping(value = "/stream/{jobId}", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamJobProgress(@PathVariable UUID jobId) {
        return progressService.subscribe(jobId);
    }

    @GetMapping("/list")
    public Mono<Page<DownloadJob>> listDownloads(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        DownloadJobStatus jobStatus = status == null || status.isEmpty() ? null : DownloadJobStatus.valueOf(status);
        LocalDateTime start = startDate == null || startDate.isEmpty() ? null : LocalDateTime.parse(startDate);
        LocalDateTime end = endDate == null || endDate.isEmpty() ? null : LocalDateTime.parse(endDate);
        return Mono.fromSupplier(() ->
                queueService.listDownloads(jobStatus, start, end, page, size)
        ).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/download/{jobId}")
    public Mono<ResponseEntity<Resource>> download(@PathVariable UUID jobId) {
        return Mono.fromSupplier(() -> {
            DownloadJob job = jobRepository.findByJobId(jobId).orElse(null);
            if (job == null || job.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }
            try {
                File file = new File(job.getFilePath());
                InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
                return ResponseEntity.ok()
                        .contentLength(file.length())
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + job.getFileName())
                        .body(resource);
            } catch (Exception e) {
                log.error("Download error", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        });
    }
}