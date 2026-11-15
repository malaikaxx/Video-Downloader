package com.streamvault;

import com.streamvault.DownloadHistory;
import com.streamvault.DownloadHistoryRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncDownloadLogger {

    private final DownloadHistoryRepository historyRepository;

    public AsyncDownloadLogger(DownloadHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Async
    public void logDownloadAsync(String username, String videoUrl, String videoTitle, String formatSelected) {
        try {
            DownloadHistory history = new DownloadHistory(username, videoUrl, videoTitle, formatSelected);
            historyRepository.save(history);
            System.out.println("--> [ASYNC THREAD " + Thread.currentThread().getName() + "] Download successfully logged for user: " + username);
        } catch (Exception e) {
            System.err.println("Failed to log download history asynchronously: " + e.getMessage());
        }
    }
}