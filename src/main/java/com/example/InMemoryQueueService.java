package com.example;

import com.example.exceptions.QueueFullException;

public class InMemoryQueueService implements QueueService {

    private int itemsInQueue = 0;
    private int readQueueLocation = 0;
    private int pushQueueLocation = 0;
    private QueueMessage[] ringBufferQueue;

    public InMemoryQueueService(){
        this.ringBufferQueue = new QueueMessage[10000];
    }

    public InMemoryQueueService(int queueLength) {
        this.ringBufferQueue = new QueueMessage[queueLength];
    }

    public void push(QueueMessage message) throws QueueFullException {
        if(this.itemsInQueue == this.ringBufferQueue.length) {
            throw new QueueFullException("The queue is full");
        }

        message.setQueueLocation(this.pushQueueLocation);
        this.ringBufferQueue[this.pushQueueLocation] = message;

        this.pushQueueLocation = this.pushQueueLocation == this.ringBufferQueue.length ? 0 : this.pushQueueLocation++;

        this.itemsInQueue++;
    }

    public QueueMessage pull() {
        QueueMessage message = this.ringBufferQueue[this.readQueueLocation];

        this.readQueueLocation = this.readQueueLocation == this.ringBufferQueue.length ? 0 : this.readQueueLocation++;

        return message;
    }

    public void delete(QueueMessage message) {
        this.ringBufferQueue[message.getQueueLocation()] = null;
        this.itemsInQueue--;
    }
}
