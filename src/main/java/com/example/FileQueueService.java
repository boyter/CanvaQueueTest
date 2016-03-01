package com.example;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FileQueueService implements QueueService {

    @Override
    public void push(String queueName, QueueMessage message) throws InterruptedException, IOException {
        File messages = this.getMessagesFile(queueName);
        File lock = this.getLockFile(queueName);

        this.lock(lock);
        try (PrintWriter pw = new PrintWriter(new FileWriter(messages, true))) {  // append
            pw.println(message.stringEncode());
        } finally {
            this.unlock(lock);
        }
    }

    @Override
    public QueueMessage pull(String queueName, long visibilityTimeout) throws InterruptedException, IOException {
        File messages = this.getMessagesFile(queueName);
        File lock = this.getLockFile(queueName);
        File tempMessages = this.getTempMessagesFile(queueName);
        QueueMessage returnQueueMessage = null;

        this.lock(lock);

        try (PrintWriter pw = new PrintWriter(new FileWriter(tempMessages, true))) {
            List<String> messageList = Files.readLines(messages, Charsets.UTF_8);
            QueueMessage queueMessage = new QueueMessage("");

            for (String message : messageList) {
                queueMessage.stringDecode(message);

                if (queueMessage.getTimeout() <= System.currentTimeMillis() && returnQueueMessage == null) {
                    queueMessage.setTimeout(System.currentTimeMillis() + visibilityTimeout);
                    returnQueueMessage = queueMessage;
                }

                pw.println(queueMessage.stringEncode());
            }

            Files.move(tempMessages, messages);
        }
        catch(IOException ex) {
            // If we have no messages to read IE readLines failed
            // we can safely ignore the exception because it may be just that
            // the queue has not had anything written yet
        }
        finally {
            this.unlock(lock);
        }

        return returnQueueMessage;
    }

    @Override
    public void delete(String queueName, QueueMessage message) {

    }

    /**
     * Clean up method mostly used for tests
     * Will purge the queue in question
     * @param queueName
     */
    public void empty(String queueName) throws InterruptedException {
        File lock = this.getLockFile(queueName);
        this.lock(lock);
        try {
            File messages = this.getMessagesFile(queueName);
            messages.delete();
        }
        finally {
            this.unlock(lock);
        }
    }

    private File getMessagesFile(String queueName) {
        return this.createFile(queueName, "messages");
    }

    private File getTempMessagesFile(String queueName) {
        return this.createFile(queueName, "temp.messages");
    }

    private File getLockFile(String queueName) {
        return this.createFile(queueName, "lock");
    }

    private File createFile(String queueName, String filename) {
        File tempDir = this.createQueueDirectory(queueName);
        File file = new File(tempDir, filename);

        return file;
    }

    private File createQueueDirectory(String queueName) {
        // Hash the queue + salt to get something slightly unique
        // and more importantly a legal directory name
        String baseName = org.apache.commons.codec.digest.DigestUtils.md5Hex(queueName + "FileQueue");
        File baseDir = new File(System.getProperty("java.io.tmpdir"));

        File tempDir = new File(baseDir, baseName);

        if(!tempDir.exists()) {
            tempDir.mkdir();
        }
        return tempDir;
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
