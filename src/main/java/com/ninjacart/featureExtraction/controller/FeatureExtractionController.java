package com.ninjacart.featureExtraction.controller;

import com.ninjacart.featureExtraction.other.ObjectCreation;
import com.ninjacart.featureExtraction.model.CreditBureauFeatures;
import com.ninjacart.featureExtraction.other.featureModel.CreditReportResponse;
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

    /**
     * Retrieves all credit bureau features associated with a given user.
     *
     * @param userId The unique identifier of the user whose credit bureau features need to be fetched.
     * @return A ResponseEntity containing a list of CreditBureauFeatures if found, otherwise an appropriate HTTP error response.
     * @throws ResponseStatusException If the userId is invalid or no features are found.
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
     * Retrieves the latest credit bureau feature entry for a given user ID.
     *
     * @param userId The unique identifier of the user.
     * @return ResponseEntity containing the latest CreditBureauFeatures object if found.
     * @throws ResponseStatusException If the userId is invalid or no data is found.
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





    //We are using this Object Creation class for the testing purpose only
    //We will not use this objectCreation class in the main code we are just using it to make creditReport and creditReportResponse metadata by hard coding the input value
    private final ObjectCreation objectCreation;

    //We are calling this api for the testing purpose only
    //In the main code we will directly implement the processAndSaveFeatures function by providing the creditReport and creditReportResponse
    //That will be done while we are saving the creditReport and CreditReport metadata to the mongo repository
    @PostMapping("/api/features/save")
    public ResponseEntity<CreditBureauFeatures> saveCreditBureauFeatures() {

        try {
            CreditBureauFeatures savedFeatures = processAndSaveFeatures();
            log.info("Feature extraction completed successfully. Returning response.");
            return ResponseEntity.ok(savedFeatures);
        } catch (Exception e) {
            log.error("Error saving credit bureau features: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Feature extraction failed");
        }
    }


    //we will directly implement this method in the middle of the code
    private CreditBureauFeatures processAndSaveFeatures() {
        // Generate credit report data
        String creditReport = objectCreation.createCreditReport();
        CreditReportResponse creditReportResponse = objectCreation.createCreditReportResponse();

        // Save and return the extracted features
        return featureExtractionService.saveFeatures(creditReport, creditReportResponse);
    }
}