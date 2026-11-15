package com.streamvault;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "download_history")
public class DownloadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, length = 1000)
    private String videoUrl;

    @Column(nullable = false)
    private String videoTitle;

    @Column(nullable = false)
    private String formatSelected;

    @Column(nullable = false)
    private LocalDateTime downloadedAt = LocalDateTime.now();

    public DownloadHistory() {}

    public DownloadHistory(String username, String videoUrl, String videoTitle, String formatSelected) {
        this.username = username;
        this.videoUrl = videoUrl;
        this.videoTitle = videoTitle;
        this.formatSelected = formatSelected;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getVideoTitle() { return videoTitle; }
    public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }
    public String getFormatSelected() { return formatSelected; }
    public void setFormatSelected(String formatSelected) { this.formatSelected = formatSelected; }
    public LocalDateTime getDownloadedAt() { return downloadedAt; }
    public void setDownloadedAt(LocalDateTime downloadedAt) { this.downloadedAt = downloadedAt; }
}