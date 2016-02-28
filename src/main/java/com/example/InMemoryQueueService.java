package com.example;

public class InMemoryQueueService implements QueueService {

    private QueueMessage[] ringBufferQueue;


    public InMemoryQueueService(){
        this.ringBufferQueue = new QueueMessage[10000];
    }

    public InMemoryQueueService(int queueLength) {
        this.ringBufferQueue = new QueueMessage[queueLength];
    }

    public void push(QueueMessage message) {

    }

    public QueueMessage pull() {
        return null;
    }

    public void delete(QueueMessage message) {

    }
}
