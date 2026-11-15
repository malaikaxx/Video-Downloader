package com.streamvault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamvault.VideoFormatDto;
import com.streamvault.VideoMetadataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class VideoDownloadService {

    @Value("${streamvault.ytdlp.path:yt-dlp}")
    private String ytDlpPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Inspects any pasted social video URL (Tumblr, YouTube, TikTok, Instagram, X, Facebook, etc.)
     * and returns real-time metadata + selectable download options.
     */
    public VideoMetadataDto analyzeVideoUrl(String videoUrl) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                ytDlpPath,
                "--dump-single-json",
                "--no-playlist",
                "--no-warnings",
                videoUrl
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(45, TimeUnit.SECONDS);
        if (!finished || process.exitValue() != 0) {
            // Print the real yt-dlp error to your IntelliJ console
            System.err.println("--- YT-DLP ERROR OUTPUT ---");
            System.err.println(output.toString());
            throw new IllegalStateException("yt-dlp failed: " + (output.length() > 200 ? output.substring(0, 200) + "..." : output.toString()));
        }

        String rawOut = output.toString().trim();
        int jsonStart = rawOut.indexOf('{');
        if (jsonStart > 0) {
            rawOut = rawOut.substring(jsonStart);
        }

        JsonNode root = objectMapper.readTree(rawOut);

        VideoMetadataDto metadata = new VideoMetadataDto();
        metadata.setSourceUrl(videoUrl);
        metadata.setTitle(root.path("title").asText("Social Media Video"));
        metadata.setThumbnail(root.path("thumbnail").asText(""));
        metadata.setExtractor(root.path("extractor_key").asText("Universal").toUpperCase());

        int durationSec = root.path("duration").asInt(0);
        metadata.setDurationText(formatDuration(durationSec));

        List<VideoFormatDto> formatOptions = new ArrayList<>();
        formatOptions.add(new VideoFormatDto("best_mp4", "mp4", "Best Available HD", "Auto", false, "MP4 - Best Quality (HD Video + Audio)"));
        formatOptions.add(new VideoFormatDto("720p_mp4", "mp4", "720p HD", "Standard HD", false, "MP4 - 720p HD Quality"));
        formatOptions.add(new VideoFormatDto("480p_mp4", "mp4", "480p SD", "Data Saver", false, "MP4 - 480p Standard Quality"));
        formatOptions.add(new VideoFormatDto("best_audio", "mp3", "Audio Only (192kbps)", "Audio Track", true, "MP3 / M4A - Audio Only"));

        JsonNode formatsNode = root.path("formats");
        if (formatsNode.isArray()) {
            Set<String> seenResolutions = new HashSet<>();
            for (JsonNode fmt : formatsNode) {
                String formatId = fmt.path("format_id").asText("");
                String ext = fmt.path("ext").asText("mp4");
                String vcodec = fmt.path("vcodec").asText("none");
                String acodec = fmt.path("acodec").asText("none");
                int height = fmt.path("height").asInt(0);
                long filesize = fmt.path("filesize").asLong(fmt.path("filesize_approx").asLong(0));

                if (!"none".equals(vcodec) && !"none".equals(acodec) && height > 0) {
                    String resKey = height + "p_" + ext;
                    if (!seenResolutions.contains(resKey)) {
                        seenResolutions.add(resKey);
                        String sizeStr = filesize > 0 ? String.format("%.1f MB", filesize / (1024.0 * 1024.0)) : "Direct Stream";
                        formatOptions.add(new VideoFormatDto(
                                formatId,
                                ext,
                                height + "p",
                                sizeStr,
                                false,
                                ext.toUpperCase() + " - " + height + "p Direct (" + sizeStr + ")"
                        ));
                    }
                }
            }
        }

        metadata.setFormats(formatOptions);
        return metadata;
    }

    /**
     * Downloads the requested format to a temporary file so the controller can stream it
     * directly to the user's browser Downloads folder with Content-Disposition: attachment.
     */
    public Path downloadVideoToTempFile(String videoUrl, String selectedFormatId) throws Exception {
        Path tempDir = Files.createTempDirectory("streamvault_dl_");
        String outputTemplate = tempDir.resolve("video_%(id)s.%(ext)s").toString();

        List<String> command = new ArrayList<>();
        command.add(ytDlpPath);
        command.add("--no-playlist");
        command.add("--concurrent-fragments");
        command.add("8"); // Multi-threaded chunk downloading
        command.add("--socket-timeout");
        command.add("15");

        switch (selectedFormatId) {
            case "best_mp4":
                command.add("-f");
                command.add("best[ext=mp4]/best");
                break;
            case "720p_mp4":
                command.add("-f");
                command.add("best[height<=720][ext=mp4]/best[height<=720]/best");
                break;
            case "480p_mp4":
                command.add("-f");
                command.add("best[height<=480][ext=mp4]/best[height<=480]/best");
                break;
            case "best_audio":
                command.add("-f");
                command.add("bestaudio[ext=m4a]/bestaudio/best");
                break;
            default:
                command.add("-f");
                command.add(selectedFormatId + "/best");
                break;
        }

        command.add("-o");
        command.add(outputTemplate);
        command.add(videoUrl);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                // Consume stream rapidly
            }
        }

        boolean completed = process.waitFor(2, TimeUnit.MINUTES);
        if (!completed || process.exitValue() != 0) {
            throw new IllegalStateException("Failed to download the selected video stream.");
        }

        File[] files = tempDir.toFile().listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalStateException("Downloaded file not found.");
        }
        return files[0].toPath();
    }

    private String formatDuration(int totalSeconds) {
        if (totalSeconds <= 0) return "Live / Short Clip";
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}