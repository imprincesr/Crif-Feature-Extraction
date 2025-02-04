package com.ninjacart.featureExtraction.model.featureModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDetail {

    private String mediaUrl;
    private ReportType reportType;
    private String fileCreationType;
    private String storageProvider;
    private List<Tag> tags;
    private String presignedUrl;
    private String createdAt;
}