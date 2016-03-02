package com.example;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Implementation of a inter process file backed persistent queue.
 * This queue will create a directory in the OS's nominated temp directory
 * to place the queue. A lock file is used to ensure multi thread/process
 * operations work as expected so long as they use the same queue name.
 *
 * Note that if a queue operation fails without calling finally (E.G. process crash)
 * that the lock file will remain and operations will block until it is cleared.
 * Generally OS's clear the temp directory on reboot so this queue will not
 * persist reboots.
 * It does not check the integrity of the messages or attempt to repair them, thus
 * corrupted messaged will be discarded at some point and lost.
 * This solution does not scale well beyond a few processes or thousand messages.
 */
public class FileQueueService implements QueueService {

    /**
     * Push QueueMessage onto the queue for processing.
     * @param queueName
     * @param message
     * @throws InterruptedException
     * @throws IOException
     */
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

    /**
     * Pulls the next available message from the queue to be processed.
     * Generally FIFO
     * @param queueName
     * @param visibilityTimeout
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
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
                    returnQueueMessage = queueMessage.clone();
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

    /**
     * Delete a message passed in. Duplicate messages with
     * duplicate timeout values will have the first message found will
     * be deleted. In theory this sounds bad, but in practive they are
     * duplicates so it shouldn't matter.
     * @param queueName
     * @param message
     * @throws InterruptedException
     */
    @Override
    public void delete(String queueName, QueueMessage message) throws InterruptedException {
        File messages = this.getMessagesFile(queueName);
        File lock = this.getLockFile(queueName);
        File tempMessages = this.getTempMessagesFile(queueName);
        boolean haveDeleted = false;

        this.lock(lock);
        try (PrintWriter pw = new PrintWriter(new FileWriter(tempMessages, true))) {
            List<String> messageList = Files.readLines(messages, Charsets.UTF_8);
            QueueMessage queueMessage = new QueueMessage("");

            for (String msg : messageList) {
                queueMessage.stringDecode(msg);

                if(haveDeleted || !queueMessage.areSame(message)) {
                    pw.println(queueMessage.stringEncode());
                }
                else {
                    haveDeleted = true;
                }
            }

            Files.move(tempMessages, messages);
        }
        catch(IOException ex) {
            // If we have no messages to read IE readLines failed
            // we can safely ignore the exception because it may be just that
            // the queue has not had anything written yet
        } finally {
            this.unlock(lock);
        }
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
