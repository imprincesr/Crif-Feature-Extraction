package com.ninjacart.featureExtraction.helper;


import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.ninjacart.featureExtraction.other.CreditReportResponseModel.CreditReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@Slf4j
@Component
@RequiredArgsConstructor
public class FeatureExtraction {

    private final FeatureExtractionHelper featureExtractionHelper;

    //2.
    /**
     * Extracts the user ID from the CreditReportResponse object.
     *
     * @param response The CreditReportResponse containing user data.
     * @return An Optional containing the user ID if present and valid, otherwise an empty Optional.
     */
    public Optional<Long> userId(CreditReportResponse response) {
        if (response == null || response.getData() == null || response.getData().getUserId() == null) {
            log.error("User ID is missing in the response data.");
            return Optional.empty();
        }
        try {
            Long userId = Long.valueOf(response.getData().getUserId());
            return Optional.of(userId);
        } catch (NumberFormatException e) {
            log.error("Error occurred while converting userId: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //3.
    /**
     * Extracts the score value from a JSON report using JsonPath.
     *
     * @param report The JSON string containing the score data.
     * @return An Optional containing the extracted score if present and valid, otherwise an empty Optional.
     */
    public Optional<Integer> score(String report) {
        try {
            String score = JsonPath.read(report, "$['B2C-REPORT'].SCORES[0].SCORE['SCORE-VALUE']");
            if (score != null) {
                return Optional.of(Integer.valueOf(score));
            } else {
                log.warn("Score value not found in the report.");
                return Optional.empty();
            }
        } catch (JsonPathException | NumberFormatException e) {
            log.error("Error occurred while extracting or parsing score: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //4.
    /**
     * Extracts and counts the number of write-offs settled in the last 24 months based on the given status.
     *
     * @param report The JSON string containing the credit report data.
     * @param status The account status to filter loan details.
     * @return An Optional containing the count of write-offs settled in the last 24 months if successful, otherwise an empty Optional.
     */
    public Optional<Integer> writeOffSettledL24m(String report, String status) {
        try {
            // Extract the creation date (DATE-OF-ISSUE from HEADER)
            String creationDateStr = JsonPath.read(report, "$['B2C-REPORT'].HEADER['DATE-OF-ISSUE']");
            LocalDate creationDate = LocalDate.parse(creationDateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            // Calculate start date (24 months before creation date)
            LocalDate startDate = creationDate.minusMonths(24);

            // Extract all loan details
            List<Object> loanDetails = JsonPath.read(report, "$['B2C-REPORT'].RESPONSES[*].RESPONSE['LOAN-DETAILS']");

//            // Early exit if no loan details found
//            if (loanDetails == null || loanDetails.isEmpty()) {
//                log.warn("No loan details found in the credit report.");
//                return Optional.empty();
//            }

            int totalFlagCount = 0;

            // Process each loan detail
            for (Object loanDetail : loanDetails) {
                String accountStatus = JsonPath.read(loanDetail, "$['ACCOUNT-STATUS']");
                String dateReported = JsonPath.read(loanDetail, "$['DISBURSED-DT']");

                // Skip if status doesn't match
                if (!status.equals(accountStatus)) {
                    continue;
                }

                LocalDate reportDate = LocalDate.parse(dateReported, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

                // Check if the account is within the date range (24 months before the creation date)
                if (reportDate.isAfter(startDate) && !reportDate.isAfter(creationDate)) {
                    totalFlagCount++;
                }
            }

            log.info("Total write-offs settled within the last 24 months: {}", totalFlagCount);
            return Optional.of(totalFlagCount);

        } catch (JsonPathException | DateTimeParseException e) {
            log.error("Error occurred while parsing the credit report or date format: {}", e.getMessage(), e);
            return Optional.empty();  // Return an empty Optional in case of error
        } catch (Exception e) {
            log.error("Unexpected error while extracting write-off settled data: {}", e.getMessage(), e);
            return Optional.empty();  // Return an empty Optional for any other unexpected errors
        }
    }

    //5 and 6 and 7.
    /**
     * Counts the number of Delinquency Past Due (DPD) instances that meet or exceed a target value
     * within a specified time frame from the payment history in a credit report.
     *
     * @param report      The JSON string containing the credit report data.
     * @param monthsLimit The maximum number of months to analyze from the payment history.
     * @param targetValue The DPD threshold value; only values greater than or equal to this are counted.
     * @return An Optional containing the count of DPD instances meeting the criteria, or 0 if all records are "XXX" or "000",
     *         otherwise an empty Optional in case of an error.
     */
    public Optional<Integer> dpdInstances(String report, int monthsLimit, int targetValue) {
        try {
            // Extract all payment histories
            List<String> paymentHistories = JsonPath.read(report,
                    "$['B2C-REPORT'].RESPONSES[*].RESPONSE['LOAN-DETAILS']['COMBINED-PAYMENT-HISTORY']");

            if (paymentHistories == null || paymentHistories.isEmpty()) {
                log.warn("No payment history found in the credit report.");
                return Optional.empty();
            }

            int dpdCount = 0;
            int nullOrZeroCount = 0;
            int totalRecords = 0;

            // Regex pattern to extract DPD values
            Pattern pattern = Pattern.compile("\\d+:\\d+,(\\d+)/");

            for (String paymentHistory : paymentHistories) {
                if (paymentHistory == null || paymentHistory.isEmpty()) {
                    continue;
                }

                // Split the payment history into monthly records
                String[] monthlyRecords = paymentHistory.split("\\|");

                // Process only up to `monthsLimit`
                int monthsToProcess = Math.min(monthsLimit, monthlyRecords.length);

                for (int i = 0; i < monthsToProcess; i++) {
                    String record = monthlyRecords[i];
                    if (record.isEmpty()) continue;

                    Matcher matcher = pattern.matcher(record);
                    totalRecords++;

                    if (matcher.find()) {
                        String dpdValue = matcher.group(1);

                        if ("XXX".equals(dpdValue) || "000".equals(dpdValue)) {
                            nullOrZeroCount++;
                        } else {
                            int dpd = Integer.parseInt(dpdValue);
                            if (dpd >= targetValue) {
                                dpdCount++;
                            }
                        }
                    } else {
                        nullOrZeroCount++;
                    }
                }
            }

            // If all records are "XXX" or "000", return 0
            if (totalRecords == nullOrZeroCount) {
                return Optional.of(0);
            }

            return Optional.of(dpdCount);

        } catch (Exception e) {
            log.error("Error processing DPD instances: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //8 and 9 and 10.
    /**
     * Counts the number of Delinquency Past Due (DPD) instances that meet or exceed a target value
     * within a specified time frame, excluding specified account types.
     *
     * @param report      The JSON string containing the credit report data.
     * @param monthsLimit The maximum number of months to analyze from the payment history.
     * @param targetValue The DPD threshold value; only values greater than or equal to this are counted.
     * @param typeList    A list of account types to be excluded from the analysis.
     * @return An Optional containing the count of DPD instances meeting the criteria, or 0 if all records are "null", "nodues", "XXX", or "000",
     *         otherwise an empty Optional in case of an error.
     */
    public Optional<Integer> numDpdInstancesExclGlCcKcc(String report, int monthsLimit, int targetValue, List<String> typeList) {
        try {
            // Extract all loan details using JsonPath
            List<Map<String, Object>> loanDetails;
            try {
                loanDetails = JsonPath.read(report, "$['B2C-REPORT'].RESPONSES[*].RESPONSE['LOAN-DETAILS']");
            } catch (JsonPathException e) {
                log.warn("No loan details found in the JSON data.");
                return Optional.empty();
            }

            if (loanDetails.isEmpty()) {
                log.warn("Loan details list is empty.");
                return Optional.empty();
            }

            int dpdInstances = 0;
            int nullInstances = 0;
            int totalInstances = 0;

            // Process each loan detail
            for (Map<String, Object> loanDetail : loanDetails) {
                String accountType = (String) loanDetail.get("ACCT-TYPE");
                String combinedPaymentHistory = (String) loanDetail.get("COMBINED-PAYMENT-HISTORY");

                if (accountType == null || combinedPaymentHistory == null || combinedPaymentHistory.isEmpty()) {
                    log.debug("Skipping loan record due to missing account type or payment history.");
                    continue;
                }

                // Exclude specified account types
                if (typeList.contains(accountType)) {
                    continue;
                }

                // Split the payment history into monthly records
                String[] monthlyRecords = combinedPaymentHistory.split("\\|");
                int monthsToProcess = Math.min(monthsLimit, monthlyRecords.length);

                for (int i = 0; i < monthsToProcess; i++) {
                    String record = monthlyRecords[i].trim();
                    if (record.isEmpty()) continue;

                    // Extract DPD value using split
                    String[] parts = record.split(",");
                    if (parts.length < 2) continue;

                    String valueStr = parts[1].split("/")[0];
                    totalInstances++;

                    if (Arrays.asList("null", "nodues", "XXX", "000").contains(valueStr)) {
                        nullInstances++;
                    } else {
                        try {
                            int value = Integer.parseInt(valueStr);
                            if (value >= targetValue) {
                                dpdInstances++;
                            }
                        } catch (NumberFormatException e) {
                            log.warn("Skipping invalid DPD value: {}", valueStr);
                            nullInstances++;
                        }
                    }
                }
            }

            // Check if all instances were null/no dues
            if (totalInstances == 0 || totalInstances == nullInstances) {
                log.debug("All instances were null/nodues.");
                return Optional.of(0);
            }

            log.debug("Extracted {} DPD instances.", dpdInstances);
            return Optional.of(dpdInstances);

        } catch (Exception e) {
            log.error("Error processing DPD instances: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //11.
    /**
     * Counts the number of inquiries made in the last 3 months from the credit report.
     *
     * @param report The JSON string containing the credit report data.
     * @return An Optional containing the count of inquiries made in the last 3 months,
     *         or an empty Optional in case of errors.
     */
    public Optional<Integer> numInquiriesLast3mUnsecBl(String report) {
        try {
            // Extract 'DATE-OF-ISSUE' from the JSON
            String dateOfIssueStr = JsonPath.read(report, "$['B2C-REPORT'].HEADER['DATE-OF-ISSUE']");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate dateOfIssue = LocalDate.parse(dateOfIssueStr, formatter);

            // Calculate start date (3 months ago)
            LocalDate startDate = dateOfIssue.minusMonths(3);

            // Extract inquiry history
            List<String> inquiryHistory = JsonPath.read(report, "$['B2C-REPORT'].RESPONSES[*].RESPONSE['LOAN-DETAILS']['INQUIRY-HISTORY']");

            int inquiryCount = 0;

            for (String inquiryDateStr : inquiryHistory) {
                LocalDate inquiryDate = LocalDate.parse(inquiryDateStr, formatter);

                if ((inquiryDate.isAfter(startDate) && inquiryDate.isBefore(dateOfIssue)) || inquiryDate.isEqual(dateOfIssue)) {
                    inquiryCount++;
                }
            }
            return Optional.of(inquiryCount);

        } catch (JsonPathException e) {
            log.error("Error extracting data from CRIF JSON: {}", e.getMessage(), e);
        } catch (DateTimeParseException e) {
            log.error("Error parsing date: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error in numInquiriesLast3mUnsecBl: {}", e.getMessage(), e);
        }

        return Optional.empty();
    }

    //12.
    /**
     * Extracts the reference ID from the CreditReportResponse object.
     *
     * @param response The CreditReportResponse containing the reference ID data.
     * @return An Optional containing the reference ID if present, otherwise an empty Optional.
     */
    public Optional<String> referenceId(CreditReportResponse response) {
        if (response == null || response.getData() == null || response.getData().getInitiatorReferenceId() == null) {
            log.warn("Reference ID is missing in the response data.");
            return Optional.empty();
        }

        String referenceId = response.getData().getInitiatorReferenceId();
        log.debug("Extracted reference ID: {}", referenceId);
        return Optional.of(referenceId);
    }

    //13.
    /**
     * Extracts the version (createdAt) from the CreditReportResponse object and converts it to a Long.
     *
     * @param response The CreditReportResponse containing the version data.
     * @return An Optional containing the version as a Long if present and valid, otherwise an empty Optional.
     */
    public Optional<Long> version(CreditReportResponse response) {
        if (response == null || response.getData() == null || response.getData().getCreatedAt() == null) {
            log.error("Version is missing in the response data.");
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(response.getData().getCreatedAt()));
        } catch (NumberFormatException e) {
            log.error("Error occurred while extracting version: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //14.
    /**
     * Extracts the createdBy value from the CreditReportResponse object and converts it to a Long.
     *
     * @param response The CreditReportResponse containing the createdBy data.
     * @return An Optional containing the createdBy value as a Long if present and valid, otherwise an empty Optional.
     */
    public Optional<Long> createdBy(CreditReportResponse response) {
        if (response == null || response.getData() == null || response.getData().getCreatedBy() == null) {
            log.error("CreatedBy is missing in the response data.");
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(response.getData().getCreatedBy()));
        } catch (NumberFormatException e) {
            log.error("Error occurred while extracting createdBy: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //15.
    /**
     * Extracts the createdAt timestamp from the CreditReportResponse, converts it from epoch seconds
     * to a LocalDateTime in UTC.
     *
     * @param response The CreditReportResponse containing the createdAt timestamp.
     * @return An Optional containing the createdAt value as LocalDateTime if valid, otherwise an empty Optional.
     */
    public Optional<LocalDateTime> createdAt(CreditReportResponse response) {
        if (response == null || response.getData() == null || response.getData().getCreatedAt() == null) {
            log.error("CreatedAt is missing in the response data.");
            return Optional.empty();
        }
        try {
            long epochSeconds = Long.parseLong(response.getData().getCreatedAt());
            Instant instant = Instant.ofEpochSecond(epochSeconds);
            return Optional.of(LocalDateTime.ofInstant(instant, ZoneId.of("UTC")));
        } catch (NumberFormatException e) {
            log.error("Error occurred while converting createdAt: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //16.
    /**
     * Extracts the report date (Credit Pull Date) from the CreditReportResponse and converts it to a LocalDate.
     *
     * @param response The CreditReportResponse containing the Credit Pull Date.
     * @return An Optional containing the report date as LocalDate if valid, otherwise an empty Optional.
     */
    public Optional<LocalDate> reportDate(CreditReportResponse response) {
        if (response == null || response.getData() == null || response.getData().getCreditPullDate() == null) {
            log.error("Credit Pull Date is missing in the response data.");
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(response.getData().getCreditPullDate(), DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (DateTimeParseException e) {
            log.error("Error occurred while parsing reportDate: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //17.
    /**
     * Extracts and counts the number of non-NC unsecured loans from the credit report
     * based on specific conditions.
     *
     * @param report The JSON string containing the credit report data.
     * @return An Optional containing the count of non-NC unsecured loans if successfully extracted,
     *         otherwise an empty Optional in case of errors.
     */
    public Optional<Integer> nonNcUnsecuredLoanCount(String report) {
        if (report == null || report.isBlank()) {
            log.error("Invalid or empty JSON input.");
            return Optional.empty();
        }

        try {
            List<String> accountTypes = JsonPath.read(report, "$['B2C-REPORT'].RESPONSES[*].RESPONSE['LOAN-DETAILS']['ACCT-TYPE']");
            List<String> accountStatuses = JsonPath.read(report, "$['B2C-REPORT'].RESPONSES[*].RESPONSE['LOAN-DETAILS']['ACCOUNT-STATUS']");
            List<String> sanctionAmounts = JsonPath.read(report, "$['B2C-REPORT'].RESPONSES[*].RESPONSE['LOAN-DETAILS']['DISBURSED-AMT']");
            List<String> accountNumbers = JsonPath.read(report, "$['B2C-REPORT'].RESPONSES[*].RESPONSE['LOAN-DETAILS']['ACCT-NUMBER']");

            int unsecLoanCount = 0;
//            if (accountTypes.isEmpty() || accountStatuses.isEmpty() || sanctionAmounts.isEmpty() || accountNumbers.isEmpty()) {
//                log.warn("Missing required fields in JSON.");
//                return Optional.empty();
//            }



            for (int i = 0; i < accountTypes.size(); i++) {
                String accountType = accountTypes.get(i);
                String accountStatus = accountStatuses.get(i);
                String sanctionAmountStr = sanctionAmounts.get(i);
                String accountNumber = accountNumbers.get(i);

                try {
                    double sanctionAmount = Double.parseDouble(sanctionAmountStr.replace(",", ""));

                    if (featureExtractionHelper.getUNSECURED_LOAN_TYPES().contains(accountType)
                            && "Active".equalsIgnoreCase(accountStatus)
                            && sanctionAmount > 100000
                            && !accountNumber.startsWith("NIN")) {
                        unsecLoanCount++;
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid sanction amount format for account: {} - Error: {}", accountNumber, e.getMessage());
                }
            }

            log.debug("Extracted non-NC unsecured loan count: {}", unsecLoanCount);
            return Optional.of(unsecLoanCount);

        } catch (Exception e) {
            log.error("Error extracting non-NC unsecured loan count: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //18 and 19.
    /**
     * Extracts and calculates the active EMI and maximum EMI for non-NC accounts from the credit report.
     *
     * @param report The JSON string containing the credit report data.
     * @return An Optional containing an array of two Double values:
     *         - [0] Active EMI (sum of EMIs for active accounts)
     *         - [1] Maximum EMI (highest EMI across all accounts)
     *         Returns an empty Optional in case of errors.
     */
    public Optional<Double[]> nonNCActiveEmiAndMaxEmi(String report) {
        try {
            DocumentContext jsonContext = JsonPath.parse(report);

            // Get all loan details
            List<Map<String, Object>> loanDetails = jsonContext.read("$['B2C-REPORT'].RESPONSES[*].RESPONSE['LOAN-DETAILS']");

            double maxEmi = 0.0;
            double activeEmi = 0.0;

            for (Map<String, Object> loan : loanDetails) {
                String accountType = featureExtractionHelper.getStringValue(loan, "ACCT-TYPE");
                String accountStatus = featureExtractionHelper.getStringValue(loan, "ACCOUNT-STATUS");

                if (!featureExtractionHelper.getACCOUNT_TYPES().contains(accountType)) {
                    continue;
                }

                // Get the maximum of SANCTIONED-AMOUNT or CREDIT-LIMIT
                double disbursedAmount = featureExtractionHelper.parseAmount(featureExtractionHelper.getStringValue(loan, "DISBURSED-AMT"));
                double creditLimit = featureExtractionHelper.parseAmount(featureExtractionHelper.getStringValue(loan, "CREDIT-LIMIT"));
                double maxDisbursal = Math.max(disbursedAmount, creditLimit);

                // Calculate EMI
                double emi = featureExtractionHelper.calculateEMI(accountType, maxDisbursal);

                // Check for actual installment amount
                double actualPayment = featureExtractionHelper.parseAmount(featureExtractionHelper.getStringValue(loan, "ACTUAL-PAYMENT"));

                double finalEmi = actualPayment > 0 ? actualPayment : emi;

                // Update maxEmi
                maxEmi = Math.max(maxEmi, finalEmi);

                // Update activeEmi if account is active
                if ("Active".equals(accountStatus)) {
                    activeEmi += finalEmi;
                }
            }

            return Optional.of(new Double[] { activeEmi, maxEmi });
        } catch (Exception e) {
            log.error("Error occurred while extracting EMI and Max EMI: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //20.
    /**
     * Calculates the bureau vintage, which represents the number of years since the oldest loan disbursement date.
     *
     * @param report The JSON string containing the credit report data.
     * @return An Optional containing the bureau vintage as a Double (years since the oldest loan disbursement),
     *         or an empty Optional in case of errors.
     */
    public Optional<Double> bureauVintage(String report) {
        try {
            // Extract 'DATE OPENED' from the JSON
            List<String> dateOpenedList = JsonPath.read(report, "$['B2C-REPORT'].RESPONSES[*].RESPONSE['LOAN-DETAILS']['DISBURSED-DT']");
            String createdDate = LocalDate.now().toString();

            // Convert dates to LocalDate objects
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate dateOpened = dateOpenedList.stream()
                    .map(s -> LocalDate.parse(s, formatter))
                    .min(Comparator.naturalOrder())
                    .orElse(LocalDate.now());
            LocalDate createdDateObj = LocalDate.parse(createdDate, formatter2);

            // Calculate vintage
            long daysDiff = ChronoUnit.DAYS.between(dateOpened, createdDateObj);
            double vintage = (double) daysDiff / 365;

            return Optional.of(vintage);
        } catch (Exception e) {
            log.error("Error occurred while calculating bureau vintage: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}



