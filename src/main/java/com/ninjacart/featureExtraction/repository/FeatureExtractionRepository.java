package com.ninjacart.featureExtraction.repository;

import com.ninjacart.featureExtraction.model.CreditBureauFeatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureExtractionRepository extends JpaRepository<CreditBureauFeatures, Long> {

    // Fetch all records associated with the userId
    List<CreditBureauFeatures> findAllByUserId(Long userId);

    @Query("SELECT c FROM CreditBureauFeatures c WHERE c.userId = :userId ORDER BY c.id DESC LIMIT 1")
    Optional<CreditBureauFeatures> findTopByUserIdOrderByIdDesc(Long userId);
}
