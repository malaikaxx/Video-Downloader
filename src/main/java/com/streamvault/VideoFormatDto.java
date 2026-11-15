package com.streamvault;

public class VideoFormatDto {
    private String formatId;
    private String extension;
    private String resolution;
    private String fileSizeText;
    private boolean audioOnly;
    private String label;

    public VideoFormatDto() {}

    public VideoFormatDto(String formatId, String extension, String resolution, String fileSizeText, boolean audioOnly, String label) {
        this.formatId = formatId;
        this.extension = extension;
        this.resolution = resolution;
        this.fileSizeText = fileSizeText;
        this.audioOnly = audioOnly;
        this.label = label;
    }

    public String getFormatId() { return formatId; }
    public void setFormatId(String formatId) { this.formatId = formatId; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getFileSizeText() { return fileSizeText; }
    public void setFileSizeText(String fileSizeText) { this.fileSizeText = fileSizeText; }

    public boolean isAudioOnly() { return audioOnly; }
    public void setAudioOnly(boolean audioOnly) { this.audioOnly = audioOnly; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}