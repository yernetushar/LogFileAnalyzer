package org.example;

import java.util.List;

public class LogProcessorTask implements Runnable {

    private final int  threadId;
    private final List<String> lines;
    private int infoCount    = 0;
    private int errorCount   = 0;
    private int warningCount = 0;

    public LogProcessorTask(int threadId, List<String> lines) {
        this.threadId = threadId;
        this.lines    = lines;
    }

    @Override
    public void run() {
        for (String line : lines) {
            countLogLevel(line);
        }
        LogAnalyzer.addCounts(infoCount, errorCount, warningCount);
    }

    private void countLogLevel(String line) {
        if (line == null || line.trim().isEmpty()) {
            return;
        }

        String[] parts = line.trim().split("\\s+", 3);
        if (parts.length < 2) {
            return;
        }

        String level = parts[1].toUpperCase();

        switch (level) {
            case "INFO":
                infoCount++;
                break;
            case "ERROR":
                errorCount++;
                break;
            case "WARNING":
                warningCount++;
                break;
            default:
                break;
        }
    }
}