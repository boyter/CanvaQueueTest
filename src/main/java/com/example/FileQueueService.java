package com.example;

import com.example.exceptions.QueueFullException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileQueueService implements QueueService {

    public void push(QueueMessage message) throws InterruptedException, IOException {
//        String queue = fromUrl(queueUrl);
        File messages = getMessagesFile("");
        File lock = getLockFile("");
//        long visibileFrom = (delaySeconds != null) ? now() + TimeUnit.SECONDS.toMillis(delaySeconds) : 0L;

        lock(lock);
        try (PrintWriter pw = new PrintWriter(new FileWriter(messages, true))) {  // append
            pw.println(message);
        } finally {
            unlock(lock);
        }

    }

    private File getMessagesFile(String queueName) {
        return null;
    }

    private File getLockFile(String queueName) {
        return null;
    }

    private void lock(File lock) throws InterruptedException {
        while (!lock.mkdir()) {
            Thread.sleep(50);
        }
    }

    private void unlock(File lock) {
        lock.delete();
    }

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
