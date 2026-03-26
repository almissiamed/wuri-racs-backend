package com.racs.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        
        return emitter;
    }

    public void sendProgress(String message, int current, int total, String status) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("progress")
                    .data(new SyncProgress(message, current, total, status)));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public void sendComplete(String message, int success, int errors) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(new SyncComplete(message, success, errors)));
                emitter.complete();
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @Getter
    public static class SyncProgress {
        private final String message;
        private final int current;
        private final int total;
        private final String status;
        private final int percentage;

        public SyncProgress(String message, int current, int total, String status) {
            this.message = message;
            this.current = current;
            this.total = total;
            this.status = status;
            this.percentage = total > 0 ? (current * 100) / total : 0;
        }
    }

    @Getter
    public static class SyncComplete {
        private final String message;
        private final int success;
        private final int errors;
        private final boolean successBool;

        public SyncComplete(String message, int success, int errors) {
            this.message = message;
            this.success = success;
            this.errors = errors;
            this.successBool = errors == 0;
        }
    }
}
