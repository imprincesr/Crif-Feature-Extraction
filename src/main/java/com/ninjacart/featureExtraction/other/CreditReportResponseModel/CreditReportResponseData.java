package com.ninjacart.featureExtraction.other.CreditReportResponseModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditReportResponseData {
    private String id;
    private String userId;
    private String realmId;
    private String providerReferenceId;
    private String initiatorReferenceId;
    private String analyzedReferenceId;
    private Provider provider;
    private List<MediaDetail> mediaDetails;
    private List<RejectionDetails> rejectionDetails;
    private List<AdditionalDetails> additionalDetails;
    private String creditStatus;
    private String statusComment;
    private String creditPullDate;

    //metadata
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;
    private String createdByTool;
    private String updatedByTool;

}
