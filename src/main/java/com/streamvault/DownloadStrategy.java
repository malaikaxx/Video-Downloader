package com.streamvault;

import com.streamvault.VideoFormatDto;
import java.util.List;

public interface DownloadStrategy {
    void applyFormatting(List<VideoFormatDto> formats);
}

class StandardVideoStrategy implements DownloadStrategy {
    @Override
    public void applyFormatting(List<VideoFormatDto> formats) {
        formats.add(0, new VideoFormatDto("best_mp4", "mp4", "Best HD", "Instant", false, "MP4 - Maximum Speed & HD Quality"));
    }
}