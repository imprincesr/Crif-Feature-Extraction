package com.ninjacart.featureExtraction.other.featureModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditReportResponse {

    CreditReportResponseData data;

}
