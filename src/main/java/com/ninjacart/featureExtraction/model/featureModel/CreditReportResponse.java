package com.ninjacart.featureExtraction.model.featureModel;

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
