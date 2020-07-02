package com.nchain.headerSV.service.propagation.buffer;

import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class MessageBufferService {
    private BlockingQueue<BufferedMessage> buffer;

    public void configure(int capacity) {
        buffer = new LinkedBlockingDeque<>(capacity);
    }

    public void queue(BufferedMessage message) {
        buffer.add(message);
    }

    public int queueSize() {
        return buffer.size();
    }

    public BufferedMessage blockingGet() throws InterruptedException {
        return buffer.take();
    }

}
