package com.paic.nbm.analyticsservice.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobProgressService {
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID jobId) {
        SseEmitter emitter = new SseEmitter(3600000L);
        emitters.put(jobId, emitter);


        Runnable removeEmitter = () -> emitters.remove(jobId);
        emitter.onCompletion(removeEmitter);
        emitter.onTimeout(removeEmitter);
        emitter.onError((e) -> removeEmitter.run());

        return emitter;
    }

    public void sendProgress(UUID jobId, int progress, String status) {
        SseEmitter emitter = emitters.get(jobId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(Map.of("progress", progress, "status", status)));
            } catch (IOException e) {
                emitters.remove(jobId);
            }
        }
    }

}