package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileQueueService implements QueueService {

    @Override
    public void push(String queueName, QueueMessage message) throws InterruptedException, IOException {
        File messages = this.getMessagesFile(queueName);
        File lock = this.getLockFile(queueName);

        this.lock(lock);
        try (PrintWriter pw = new PrintWriter(new FileWriter(messages, true))) {  // append
            pw.println(message.stringEncode());
            // write out message as expected
        } finally {
            unlock(lock);
        }
    }

    @Override
    public QueueMessage pull(String queueName, long visibilityTimeout) {
        return null;
    }

    @Override
    public void delete(String queueName, QueueMessage message) {

    }

    private File getMessagesFile(String queueName) {
        // Hash the queue + salt to get something slightly unique
        // and more importantly a legal directory name
        File tempDir = this.createQueueDirectory(queueName);
        File messagesFile = new File(tempDir, "messages");

        return messagesFile;
    }

    private File createQueueDirectory(String queueName) {
        String baseName = org.apache.commons.codec.digest.DigestUtils.md5Hex(queueName + "FileQueue");
        File baseDir = new File(System.getProperty("java.io.tmpdir"));

        File tempDir = new File(baseDir, baseName);

        if(!tempDir.exists()) {
            tempDir.mkdir();
        }
        return tempDir;
    }

    private File getLockFile(String queueName) {
        File tempDir = this.createQueueDirectory(queueName);
        File lockFile = new File(tempDir, "lock");

        return lockFile;
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
