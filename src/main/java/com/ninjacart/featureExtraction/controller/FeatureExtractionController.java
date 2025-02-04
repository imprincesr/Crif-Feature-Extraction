package com.ninjacart.featureExtraction.controller;

import com.ninjacart.featureExtraction.ObjectCreation;
import com.ninjacart.featureExtraction.model.CreditBureauFeatures;
import com.ninjacart.featureExtraction.model.featureModel.CreditReportResponse;
import com.ninjacart.featureExtraction.service.FeatureExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class FeatureExtractionController {

    private final FeatureExtractionService featureExtractionService;
    private final ObjectCreation objectCreation;

    /**
     * Saves extracted credit bureau features.
     *
     * @return ResponseEntity containing saved features or error response.
     */
    @PostMapping("/api/features/save")
    public ResponseEntity<CreditBureauFeatures> saveCreditBureauFeatures() {
        log.info("Starting credit bureau feature extraction and saving...");

        try {
//            CreditBureauFeatures savedFeatures = featureExtractionService.processAndSaveFeatures();
            CreditBureauFeatures savedFeatures = processAndSaveFeatures();
            log.info("Feature extraction completed successfully. Returning response.");
            return ResponseEntity.ok(savedFeatures);
        } catch (Exception e) {
            log.error("Error saving credit bureau features: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Feature extraction failed");
        }
    }

    /**
     * Fetches all credit bureau features associated with a user.
     *
     * @param userId Unique identifier of the user.
     * @return List of CreditBureauFeatures or error if not found.
     */
    @GetMapping("/api/features/users/{userId}/v1")
    public ResponseEntity<List<CreditBureauFeatures>> getAllCreditBureauFeatures(@PathVariable Long userId) {
        log.info("Fetching all credit bureau features for userId: {}", userId);

        if (userId == null || userId <= 0) {
            log.error("Invalid userId received: {}", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId: " + userId);
        }

        List<CreditBureauFeatures> features = featureExtractionService.getAllCreditBureauFeatures(userId);
        if (features.isEmpty()) {
            log.warn("No credit bureau features found for userId: {}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No data found for userId: " + userId);
        }

        return ResponseEntity.ok(features);
    }

    /**
     * Fetches the latest credit bureau feature entry for a user.
     *
     * @param userId Unique identifier of the user.
     * @return The latest CreditBureauFeatures or error if not found.
     */
    @GetMapping("/api/features/users/{userId}")
    public ResponseEntity<CreditBureauFeatures> getLatestCreditBureauFeature(@PathVariable Long userId) {
        log.info("Fetching latest credit bureau feature for userId: {}", userId);

        if (userId == null || userId <= 0) {
            log.error("Invalid userId received: {}", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId: " + userId);
        }

        CreditBureauFeatures latestFeature = featureExtractionService.getLatestCreditBureauFeature(userId);
        return ResponseEntity.ok(latestFeature);
    }

    private CreditBureauFeatures processAndSaveFeatures() {
        // Generate credit report data
        String creditReport = objectCreation.createCreditReport();
        CreditReportResponse creditReportResponse = objectCreation.createCreditReportResponse();

        // Save and return the extracted features
        return featureExtractionService.saveFeatures(creditReport, creditReportResponse);
    }
}