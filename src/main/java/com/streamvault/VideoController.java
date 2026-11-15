package com.streamvault;

import com.streamvault.DownloadHistory;
import com.streamvault.User;
import com.streamvault.VideoMetadataDto;
import com.streamvault.DownloadHistoryRepository;
import com.streamvault.AsyncDownloadLogger;
import com.streamvault.VideoDownloadService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Controller
public class VideoController {

    private final VideoDownloadService videoDownloadService;
    private final DownloadHistoryRepository historyRepository;
    private final AsyncDownloadLogger asyncDownloadLogger;

    public VideoController(VideoDownloadService videoDownloadService,
                           DownloadHistoryRepository historyRepository,
                           AsyncDownloadLogger asyncDownloadLogger) {
        this.videoDownloadService = videoDownloadService;
        this.historyRepository = historyRepository;
        this.asyncDownloadLogger = asyncDownloadLogger;
    }

    @GetMapping("/")
    public String showHomePage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);

        // Fetch user's download history from SQL Server database
        List<DownloadHistory> historyList = historyRepository.findByUsernameIgnoreCaseOrderByDownloadedAtDesc(currentUser.getUsername());
        model.addAttribute("historyList", historyList);

        return "index";
    }

    /**
     * Real-time AJAX endpoint: receives pasted URL and returns available video resolutions & formats
     */
    @PostMapping("/api/video/analyze")
    @ResponseBody
    public ResponseEntity<?> analyzeVideo(@RequestBody Map<String, String> payload, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Please sign in to analyze and download videos."));
        }

        String url = payload.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please paste a valid video URL."));
        }

        try {
            VideoMetadataDto metadata = videoDownloadService.analyzeVideoUrl(url.trim());
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Instant Browser Download Endpoint: downloads chosen video format, logs history asynchronously via threads,
     * and streams file directly into user browser "Downloads" folder.
     */
    @GetMapping("/api/video/download")
    public ResponseEntity<Resource> downloadVideo(@RequestParam("url") String url,
                                                  @RequestParam("formatId") String formatId,
                                                  HttpSession session) throws Exception {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Execute download via yt-dlp service
        Path downloadedFile = videoDownloadService.downloadVideoToTempFile(url, formatId);
        String filename = downloadedFile.getFileName().toString();
        long fileLength = Files.size(downloadedFile);

        String videoTitle = filename.replace("video_", "").replace(".mp4", "").replace(".m4a", "").replace(".mp3", "");

        // Multithreading execution: log download history asynchronously in background thread
        asyncDownloadLogger.logDownloadAsync(currentUser.getUsername(), url, videoTitle, formatId);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(downloadedFile.toFile()) {
            @Override
            public void close() throws java.io.IOException {
                super.close();
                Files.deleteIfExists(downloadedFile);
                Files.deleteIfExists(downloadedFile.getParent());
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("StreamVault_" + filename).build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(fileLength);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}