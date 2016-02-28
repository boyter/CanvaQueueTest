package com.example;

public class InMemoryQueueService implements QueueService {

    public InMemoryQueueService(){}

    public InMemoryQueueService(int queueLength) {}
    
    public void push(QueueMessage message) {

    }

    public QueueMessage pull() {
        return null;
    }

    public void delete(QueueMessage message) {

    }
}
