package com.example;

public class InMemoryQueueService implements QueueService {

    private int readQueueLocation = 0;
    private int pushQueueLocation = 0;
    private QueueMessage[] ringBufferQueue;

    public InMemoryQueueService(){
        this.ringBufferQueue = new QueueMessage[10000];
    }

    public InMemoryQueueService(int queueLength) {
        this.ringBufferQueue = new QueueMessage[queueLength];
    }

    public void push(QueueMessage message) {
        this.ringBufferQueue[this.pushQueueLocation] = message;
        this.pushQueueLocation++;
    }

    public QueueMessage pull() {
        QueueMessage message = this.ringBufferQueue[this.readQueueLocation];
        return message;
    }

    public void delete(QueueMessage message) {

    }
}
