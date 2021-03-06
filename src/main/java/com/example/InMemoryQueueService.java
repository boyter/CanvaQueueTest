package com.example;

import com.example.exceptions.QueueFullException;

public class InMemoryQueueService implements QueueService {

    private int itemsInQueue = 0;
    private int readQueueLocation = 0;
    private int pushQueueLocation = 0;
    private QueueMessage[] ringBufferQueue;

    /**
     * Create an in memory queue with size of 10000 elements
     */
    public InMemoryQueueService(){
        this.ringBufferQueue = new QueueMessage[10000];
    }

    /**
     * Create in memory queue with custom length (must be greater than 0)
     * @param queueLength
     */
    public InMemoryQueueService(int queueLength) {
        if(queueLength == 0) {
            throw new IllegalArgumentException("Queue length must be greater than 0");
        }
        this.ringBufferQueue = new QueueMessage[queueLength];
    }

    /**
     * Push QueueMessage onto the queue for processing. Queue name is ignored
     * for this implementation of the queue. To use a separate queue create
     * a new instance of this class.
     * @param queueName
     * @param message
     * @throws QueueFullException
     */
    @Override
    public synchronized void push(String queueName, QueueMessage message) throws QueueFullException {
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

    /**
     * Pulls the next available message from the queue.
     * No guarantee on order but attempts to be FIFO
     * Assuming processing time is the same for jobs
     * this should hold true.
     * Parameter queueName is ignored
     * @param queueName
     * @param visibilityTimeout
     * @return
     */
    @Override
    public synchronized QueueMessage pull(String queueName, long visibilityTimeout) {
        int startReadLocation = this.readQueueLocation;
        QueueMessage message = null;
        boolean search = true;

        while(search) {
            message = this.ringBufferQueue[startReadLocation];

            // If we have a message and it's not 'locked' thats who we want
            if(message != null && message.getTimeout() <= System.currentTimeMillis()) {
                message.setTimeout(System.currentTimeMillis() + visibilityTimeout);
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

    /**
     * Deletes a message from the queue. Messages
     * that are not deleted will after the default time of
     * 10 seconds be ready to be consumed again.
     * @param queueName
     * @param message
     */
    @Override
    public synchronized void delete(String queueName, QueueMessage message) {
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
