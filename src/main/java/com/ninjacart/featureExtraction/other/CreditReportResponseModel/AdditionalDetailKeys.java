package com.ninjacart.featureExtraction.other.CreditReportResponseModel;

import lombok.Getter;

/**
 * Predefined Additional Detail Keys
 */
@Getter
public enum AdditionalDetailKeys {

    // List of completely processed Finbox account IDs
    FINBOX_COMPLETED_ACCOUNT_DETAILS,

    // List of fetched failed Finbox account IDs
    FETCHED_FINBOX_FAILED_ACCOUNT_DETAILS,

    // List of not fetched failed Finbox account IDs
    NOT_FETCHED_FINBOX_FAILED_ACCOUNT_DETAILS,

    // List of failed processed Finbox account IDs
    FINBOX_FAILED_ACCOUNT_DETAILS,

    // Count of Finbox accounts with completed status
    FINBOX_COMPLETED_ACCOUNT_COUNTS,

    // Count of fetched failed Finbox accounts
    FETCHED_FINBOX_FAILED_ACCOUNT_COUNTS,

    // Count of not fetched failed Finbox accounts
    NOT_FETCHED_FINBOX_FAILED_ACCOUNT_COUNTS,

    // Count of Finbox accounts with failed status
    FINBOX_FAILED_ACCOUNT_COUNTS,

    // for storing any remarks
    REMARKS
}
