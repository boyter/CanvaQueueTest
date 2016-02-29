package com.example;

import com.example.exceptions.QueueFullException;

public class InMemoryQueueService implements QueueService {

    private int itemsInQueue = 0;
    private int readQueueLocation = 0;
    private int pushQueueLocation = 0;
    private long visibilityTimeout = 10; // 10 seconds by default
    private QueueMessage[] ringBufferQueue;

    public InMemoryQueueService(){
        this.ringBufferQueue = new QueueMessage[10000];
    }

    public InMemoryQueueService(long visibilitytimeout){
        this.visibilityTimeout = visibilitytimeout;
    }

    public InMemoryQueueService(int queueLength) {
        if(queueLength == 0) {
            throw new IllegalArgumentException("Queue length must be greater than 0");
        }
        this.ringBufferQueue = new QueueMessage[queueLength];
    }

    public InMemoryQueueService(int queueLength, long visibilitytimeout) {
        if(queueLength == 0) {
            throw new IllegalArgumentException("Queue length must be greater than 0");
        }

        this.ringBufferQueue = new QueueMessage[queueLength];
        this.visibilityTimeout = visibilitytimeout;
    }

    public synchronized void push(QueueMessage message) throws QueueFullException {
        if(this.itemsInQueue == this.ringBufferQueue.length) {
            throw new QueueFullException("The queue is full");
        }

        int pushLocation = this.pushQueueLocation;
        boolean search = true;

        // Find the first free space starting from the last location
        while(search) {
            if(this.ringBufferQueue[pushLocation] == null) {
                search = false;
            }
            else {
                pushLocation = this.incrementPosition(pushLocation);
            }
        }

        message.setQueueLocation(pushLocation);
        this.ringBufferQueue[pushLocation] = message;

        // Move the next push location to where we inserted as anything before is filled
        this.pushQueueLocation = this.incrementPosition(pushLocation);
        this.itemsInQueue++;
    }

    public synchronized QueueMessage pull() {
        int startReadLocation = this.readQueueLocation;
        QueueMessage message = null;
        boolean search = true;

        while(search) {
            message = this.ringBufferQueue[startReadLocation];

            // If we have a message and it's not 'locked' thats who we want
            if(message != null && message.getTimeout() <= System.currentTimeMillis()) {
                message.setTimeout(System.currentTimeMillis() + (this.visibilityTimeout * 1000L));
                search = false;
            }
            // Wrap around search
            startReadLocation = this.incrementPosition(startReadLocation);

            // We have looked at every element found nothing to do
            if(startReadLocation == this.pushQueueLocation && search) {
                message = null;
                search = false;
            }
        }

        return message;
    }

    public synchronized void delete(QueueMessage message) {
        if(message == null) {
            return;
        }

        // If we process FIFO this makes it slightly faster
        // to find the next message to process
        this.readQueueLocation = message.getQueueLocation();

        this.ringBufferQueue[message.getQueueLocation()] = null;
        this.itemsInQueue--;
    }

    private int incrementPosition(int position) {
        return position == (this.ringBufferQueue.length - 1) ? 0 : position + 1;
    }
}
