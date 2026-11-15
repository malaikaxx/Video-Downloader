package com.streamvault;

import java.util.ArrayList;
import java.util.List;

public class VideoMetadataDto {
    private String sourceUrl;
    private String title;
    private String thumbnail;
    private String durationText;
    private String extractor;
    private List<VideoFormatDto> formats = new ArrayList<>();

    public VideoMetadataDto() {}

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getDurationText() { return durationText; }
    public void setDurationText(String durationText) { this.durationText = durationText; }

    public String getExtractor() { return extractor; }
    public void setExtractor(String extractor) { this.extractor = extractor; }

    public List<VideoFormatDto> getFormats() { return formats; }
    public void setFormats(List<VideoFormatDto> formats) { this.formats = formats; }
}