package com.example;

import com.example.exceptions.QueueFullException;

public class InMemoryQueueService implements QueueService {

    private int itemsInQueue = 0;
    private int readQueueLocation = 0;
    private int pushQueueLocation = 0;
    private long defaultTimeout = 1000; // 10 seconds
    private QueueMessage[] ringBufferQueue;

    public InMemoryQueueService(){
        this.ringBufferQueue = new QueueMessage[10000];
    }

    public InMemoryQueueService(int queueLength) {
        this.ringBufferQueue = new QueueMessage[queueLength];
    }

    public InMemoryQueueService(int queueLength, long messageTimeout) {
        this.ringBufferQueue = new QueueMessage[queueLength];
        this.defaultTimeout = messageTimeout;
    }

    public void push(QueueMessage message) throws QueueFullException {
        if(this.itemsInQueue == this.ringBufferQueue.length) {
            throw new QueueFullException("The queue is full");
        }

        // Assumes that the next element is free, which is currently
        // wrong
        message.setQueueLocation(this.pushQueueLocation);
        this.ringBufferQueue[this.pushQueueLocation] = message;

        this.pushQueueLocation = this.pushQueueLocation == (this.ringBufferQueue.length - 1) ? 0 : pushQueueLocation + 1;
        this.itemsInQueue++;
    }

    public QueueMessage pull() {
        int startReadLocation = this.readQueueLocation;
        QueueMessage message = null;
        boolean search = true;

        while(search) {
            message = this.ringBufferQueue[startReadLocation];

            // If we have a message and it's not 'locked' thats who we want
            if(message != null && message.getTimeout() <= System.currentTimeMillis()) {
                message.setTimeout(System.currentTimeMillis() + (this.defaultTimeout * 1000L));
                search = false;
            }
            // Wrap around search
            startReadLocation = startReadLocation == (this.ringBufferQueue.length - 1) ? 0 : startReadLocation + 1;

            // We have looked at every element found nothing to do
            if(startReadLocation == this.pushQueueLocation) {
                search = false;
            }
        }

        return message;
    }

    public void delete(QueueMessage message) {
        if(message == null) {
            return;
        }

        // If we process FIFO this makes it slightly faster
        // to find the next message to process
        this.readQueueLocation = message.getQueueLocation();

        this.ringBufferQueue[message.getQueueLocation()] = null;
        this.itemsInQueue--;
    }
}
