package com.ninjacart.featureExtraction.service;

import com.ninjacart.featureExtraction.helper.FeatureExtractionHelper;
import com.ninjacart.featureExtraction.helper.FeatureExtraction;
import com.ninjacart.featureExtraction.model.CreditBureauFeatures;
import com.ninjacart.featureExtraction.other.featureModel.CreditReportResponse;
import com.ninjacart.featureExtraction.repository.FeatureExtractionRepository;
import com.ninjacart.featureExtraction.exception.ResourceNotFoundException;
import com.ninjacart.featureExtraction.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureExtractionService {

    private final FeatureExtractionRepository featureExtractionRepository;
    private final FeatureExtraction featureExtraction;
    private final FeatureExtractionHelper featureExtractionHelper;

    @Transactional
    public CreditBureauFeatures saveFeatures(String creditReport, CreditReportResponse creditReportResponse) {
        log.info("Starting feature extraction for user: {}", creditReportResponse.getData().getUserId());

        try {
            CreditBureauFeatures features = CreditBureauFeatures.builder()
                    .userId(featureExtraction.userId(creditReportResponse).orElseThrow(() -> {
                        log.error("User ID is missing in response.");
                        return new InvalidRequestException("User ID is required.");
                    }))
                    .score(featureExtraction.score(creditReport).orElse(null))
                    .writeOffSettledL24m(featureExtraction.writeOffSettledL24m(creditReport, "Written Off").orElse(null))
                    .dpd30InstancesL3m(featureExtraction.dpdInstances(creditReport, 3, 30).orElse(null))
                    .dpd60InstancesL12m(featureExtraction.dpdInstances(creditReport, 12, 60).orElse(null))
                    .dpd60InstancesL6m(featureExtraction.dpdInstances(creditReport, 6, 60).orElse(null))
                    .numDpd30InstancesL3mExclGlCcKcc(featureExtraction.numDpdInstancesExclGlCcKcc(creditReport,3, 30, featureExtractionHelper.getEXCLUDED_TYPES()).orElse(null))
                    .numDpd60InstancesL12mExclGlCcKcc(featureExtraction.numDpdInstancesExclGlCcKcc(creditReport,12, 60, featureExtractionHelper.getEXCLUDED_TYPES()).orElse(null))
                    .numDpd60InstancesL6mExclGlCcKcc(featureExtraction.numDpdInstancesExclGlCcKcc(creditReport, 6, 60, featureExtractionHelper.getEXCLUDED_TYPES()).orElse(null))
                    .numInquiriesLast3mUnsecBl(featureExtraction.numInquiriesLast3mUnsecBl(creditReport).orElse(null))
                    .referenceId(featureExtraction.referenceId(creditReportResponse).orElse(""))
                    .version(featureExtraction.version(creditReportResponse).orElse(null))
                    .createdBy(featureExtraction.createdBy(creditReportResponse).orElse(null))
                    .createdAt(featureExtraction.createdAt(creditReportResponse).orElse(null))
                    .reportDate(featureExtraction.reportDate(creditReportResponse).orElse(null))
                    .nonNcUnsecuredLoanCount(featureExtraction.nonNcUnsecuredLoanCount(creditReport).orElse(null))
                    .nonNcActiveEmi(featureExtraction.nonNCActiveEmiAndMaxEmi(creditReport)
                            .map(values -> values[0])
                            .orElse(null))
                    .nonNcMaxEmiL24m(featureExtraction.nonNCActiveEmiAndMaxEmi(creditReport)
                            .map(values -> values[1])
                            .orElse(null))
                    .bureauVintage(featureExtraction.bureauVintage(creditReport).orElse(null))
                    .build();

            log.info("Feature extraction completed for user: {}. Saving to database...", features.getUserId());
            return featureExtractionRepository.save(features);

        } catch (Exception e) {
            log.error("Error during feature extraction: {}", e.getMessage(), e);
            throw new RuntimeException("Feature extraction failed", e);
        }
    }

    public List<CreditBureauFeatures> getAllCreditBureauFeatures(Long userId) {
        if (userId == null || userId <= 0) {
            log.error("Invalid userId: {}", userId);
            throw new InvalidRequestException("Invalid userId: " + userId);
        }

        List<CreditBureauFeatures> features = featureExtractionRepository.findAllByUserId(userId);
        if (features.isEmpty()) {
            log.warn("No CreditBureauFeatures found for userId: {}", userId);
            throw new ResourceNotFoundException("No data found for userId: " + userId);
        }

        return features;
    }

    public CreditBureauFeatures getLatestCreditBureauFeature(Long userId) {
        return featureExtractionRepository.findTopByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No data found for userId: " + userId));
    }
}
