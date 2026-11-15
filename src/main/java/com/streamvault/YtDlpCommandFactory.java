package com.streamvault;

import java.util.ArrayList;
import java.util.List;

public class YtDlpCommandFactory {

    public static List<String> createDownloadCommand(String ytDlpPath, String outputTemplate, String videoUrl, String formatId) {
        List<String> command = new ArrayList<>();
        command.add(ytDlpPath);
        command.add("--no-playlist");
        command.add("--no-check-certificates");
        command.add("-N");
        command.add("16"); // 16-thread multi-threaded chunk downloader
        command.add("--socket-timeout");
        command.add("10");

        switch (formatId) {
            case "best_mp4":
                command.add("-f");
                command.add("best[ext=mp4]/best[height<=1080]/best");
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
                command.add(formatId + "/best");
                break;
        }

        command.add("-o");
        command.add(outputTemplate);
        command.add(videoUrl);
        return command;
    }
}