package com.nchain.headerSV.service.propagation;

import com.nchain.headerSV.service.propagation.buffer.BufferedMessage;
import com.nchain.headerSV.service.propagation.buffer.MessageBufferService;
import com.nchain.headerSV.service.propagation.persistence.BufferedMessagePersistenceService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 01/07/2020
 */
@Component
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BufferConsumerTask implements Runnable {

    private final MessageBufferService messageBufferService;
    private final BufferedMessagePersistenceService bufferedMessagePersistenceService;

    @Setter
    private String name;

    private boolean running = false;
    private boolean shouldStop = false;
    private Stats stats;

    public BufferConsumerTask(MessageBufferService messageBufferService, BufferedMessagePersistenceService bufferedMessagePersistenceService) {
        this.messageBufferService = messageBufferService;
        this.bufferedMessagePersistenceService = bufferedMessagePersistenceService;
    }

    @Override
    public void run() {
        try {
            running = true;
            stats = new Stats( name + "(" + Thread.currentThread().getName() + ")", System.currentTimeMillis());
            while (!shouldStop) {
                final BufferedMessage bufferedMessage = messageBufferService.blockingGet();
                try{
                    bufferedMessagePersistenceService.persist(bufferedMessage);
                    stats.processedMessageOk(bufferedMessage);
                }  catch (Exception e) {
                    stats.processedMessageError(bufferedMessage);
                    log.error("Error persisting message " + bufferedMessage, e);
                }
            }
            bufferedMessagePersistenceService.stop();
        } catch (InterruptedException e) {
            // Interrupted means the jvm is closing during
        } catch (Exception e) {
            log.error("Task error", e);
        } finally {
            running = false;
        }

    }

    public boolean isRunning() {
        return running;
    }

    public void stop() { shouldStop = true; }

    public Stats getStats() {
        return stats;
    }

    public class Stats {
        private String name;
        private long start;
        @Getter
        private long messages = 0;
        private long errors = 0;

        long getDuration() {
            return (System.currentTimeMillis() - start) / 1000;
        }

        long getMsgsPerSecond() {
            return messages / getDuration();
        }

        public Stats(String name, long start) {
            this.name = name;
            this.start = start;
        }

        public void processedMessageOk(BufferedMessage bufferedMessage) {
            messages++;
        }

        public void processedMessageError(BufferedMessage bufferedMessage) {
            messages++;
            errors++;
        }

        @Override
        public String toString() {
            return "- " + name + ": Uptime: " + getDuration() + "s, messages: " + messages + " (" + getMsgsPerSecond() + " msgs/s), errors: " + errors;
        }
    }
}
