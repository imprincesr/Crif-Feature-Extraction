package com.ninjacart.featureExtraction.other.CreditReportResponseModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectionDetails implements Serializable {

    private String key;
    private String comment;
    private String classification;
    private String subClassification;
    private String timestamp;
}