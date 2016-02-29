package com.example;

import com.example.exceptions.QueueFullException;

public class FileQueueService implements QueueService {

    @Override
    public void push(String queueName, QueueMessage message) throws QueueFullException {

    }

    @Override
    public QueueMessage pull(String queueName, long visibilityTimeout) {
        return null;
    }

    @Override
    public void delete(String queueName, QueueMessage message) {

    }
}
