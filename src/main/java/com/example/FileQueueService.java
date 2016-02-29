package com.example;

import java.io.File;

public class FileQueueService implements QueueService {
    public void push(QueueMessage message) {
    }

    public QueueMessage pull() {
        return null;
    }

    public void delete(QueueMessage message) {
    }

    private void lock(File lock) throws InterruptedException {
        while (!lock.mkdir()) {
            Thread.sleep(50);
        }
    }

    private void unlock(File lock) {
        lock.delete();
    }
}
