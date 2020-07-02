package com.nchain.headerSV.service.propagation;

import com.nchain.headerSV.service.propagation.buffer.MessageBufferService;
import com.nchain.headerSV.service.propagation.persistence.BufferedMessagePersistenceService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.IntStream;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 */
@Service
@Slf4j
@ConfigurationProperties(prefix = "headersv.listener-app.propagation.buffer")
public class PropagationDBService {

    private final MessageBufferService messageBufferService;
    private final BufferedMessagePersistenceService bufferedMessagePersistenceService;
    private final ApplicationContext applicationContext;

    private ThreadPoolExecutor executor;
    private final List<BufferConsumerTask> tasks = new LinkedList<>();

    @Setter
    boolean enabled;

    @Setter
    int threads;

    @Setter
    int bufferSize;

    private boolean shouldStop = false;

    public PropagationDBService(MessageBufferService messageBufferService, BufferedMessagePersistenceService bufferedMessagePersistenceService, ApplicationContext applicationContext) {
        this.messageBufferService = messageBufferService;
        this.bufferedMessagePersistenceService = bufferedMessagePersistenceService;
        this.applicationContext = applicationContext;
    }

    public void start() {
        if(!enabled) return;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads + 1);
        this.messageBufferService.configure(bufferSize);
        log.info("Launching " + threads + " threads to consume network messages. Buffer size is " + bufferSize);
        IntStream.rangeClosed(1, threads).forEach(thread -> {
            final BufferConsumerTask bufferConsumerTask = applicationContext.getBean(BufferConsumerTask.class);
            bufferConsumerTask.setName("BufferConsumerTask#" + thread);
            tasks.add(bufferConsumerTask);
            executor.submit(bufferConsumerTask);

        });
        executor.submit(this::showStatsLoopTask);

    }

    public void stop() {
        if (!enabled) return;
        tasks.forEach(BufferConsumerTask::stop);
        shouldStop = true;
    }


    private static final Format PCT = new DecimalFormat("0.0%");

    private void showStatsLoopTask() {
        try {
            while (!shouldStop) {
                Thread.sleep(10000);
                final int currentSize = messageBufferService.queueSize();
                if (currentSize > (bufferSize * 0.8)) {
                    log.error("Buffer almost full!! please, improve the performance or increase the threads/buffers size");
                }
                final double percent = ((double) currentSize) / bufferSize;
                final long messages = tasks.stream().map(BufferConsumerTask::getStats).mapToLong(BufferConsumerTask.Stats::getMessages).sum();
                log.trace("- " + threads + " consumers, " + messages + " messages processed. Producer queue has " + currentSize+ " items of " + bufferSize + " (" + PCT.format(percent) + " full)");
                tasks.stream().map(BufferConsumerTask::getStats).map(BufferConsumerTask.Stats::toString).forEach(log::trace);
            }
        } catch (InterruptedException e) {

        }
    }}
