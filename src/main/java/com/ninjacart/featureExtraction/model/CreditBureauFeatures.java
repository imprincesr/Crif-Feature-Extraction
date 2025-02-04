package com.ninjacart.featureExtraction.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_bureau_features", indexes = {
        @Index(name = "idx_user_id", columnList = "userId")
        //With an index, the database uses a B-tree or Hash Index to quickly find records, improving performance.
})
public class CreditBureauFeatures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "score")
    private Integer score;

    @Column(name = "write_off_settled_l24m")
    private Integer writeOffSettledL24m;

    @Column(name = "dpd30_instances_l3m")
    private Integer dpd30InstancesL3m;

    @Column(name = "dpd60_instances_l12m")
    private Integer dpd60InstancesL12m;

    @Column(name = "dpd60_instances_l6m")
    private Integer dpd60InstancesL6m;

    @Column(name = "num_dpd30_instances_l3m_excl_gl_cc_kcc")
    private Integer numDpd30InstancesL3mExclGlCcKcc;

    @Column(name = "num_dpd60_instances_l12m_excl_gl_cc_kcc")
    private Integer numDpd60InstancesL12mExclGlCcKcc;

    @Column(name = "num_dpd60_instances_l6m_excl_gl_cc_kcc")
    private Integer numDpd60InstancesL6mExclGlCcKcc;

    @Column(name = "num_inquiries_last_3m_unsec_bl")
    private Integer numInquiriesLast3mUnsecBl;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "version")
    private Long version;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "non_nc_unsecured_loan_count")
    private Integer nonNcUnsecuredLoanCount;

    @Column(name = "non_nc_active_emi")
    private Double nonNcActiveEmi;

    @Column(name = "non_nc_max_emi_l24m")
    private Double nonNcMaxEmiL24m;

    @Column(name = "bureau_vintage")
    private Double bureauVintage;
}
