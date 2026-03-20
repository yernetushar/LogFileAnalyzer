package org.example;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class LogAnalyzer {

    private static final AtomicInteger totalInfo    = new AtomicInteger(0);
    private static final AtomicInteger totalError   = new AtomicInteger(0);
    private static final AtomicInteger totalWarning = new AtomicInteger(0);
    private static final int THREAD_COUNT = 4;

    public static void main(String[] args) {

        String filePath = (args.length > 0) ? args[0] : "src/main/resources/application.log";

        List<String> allLines = readLogFile(filePath);
        if (allLines == null) {
            System.err.println("Failed to read log file. Exiting.");
            return;
        }

        List<List<String>> chunks = partitionLines(allLines, THREAD_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < chunks.size(); i++) {
            final int threadId       = i + 1;
            final List<String> chunk = chunks.get(i);
            executor.submit(new LogProcessorTask(threadId, chunk));
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        printResults();
    }

    private static List<String> readLogFile(String filePath) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: File not found — " + filePath);
            return null;
        } catch (IOException e) {
            System.err.println("ERROR: Could not read file — " + e.getMessage());
            return null;
        }

        return lines;
    }

    private static List<List<String>> partitionLines(List<String> lines, int n) {
        List<List<String>> chunks = new ArrayList<>();
        int total     = lines.size();
        int chunkSize = (int) Math.ceil((double) total / n);

        for (int start = 0; start < total; start += chunkSize) {
            int end = Math.min(start + chunkSize, total);
            chunks.add(lines.subList(start, end));
        }
        return chunks;
    }

    private static void printResults() {
        System.out.println("INFO Count: "    + totalInfo.get());
        System.out.println("ERROR Count: "   + totalError.get());
        System.out.println("WARNING Count: " + totalWarning.get());
    }

    static void addCounts(int info, int error, int warning) {
        totalInfo   .addAndGet(info);
        totalError  .addAndGet(error);
        totalWarning.addAndGet(warning);
    }
}