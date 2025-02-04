package com.ninjacart.featureExtraction.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@Component
public class FeatureExtractionHelper {

    private final List<String> EXCLUDED_TYPES = List.of("Credit Card", "Gold Loan", "Kisan Credit Card");

    private final List<String> UNSECURED_LOAN_TYPES = Arrays.asList(
            "Business Loan Priority Sector  Agriculture",
            "Business Loan Priority Sector  Others",
            "Business Loan Priority Sector  Small Business",
            "Business Loan Unsecured",
            "Business Non-Funded Credit Facility General",
            "Business Non-Funded Credit Facility-Priority Sector- Small Business",
            "Business Non-Funded Credit Facility-Priority Sector-Others",
            "GECL Loan unsecured",
            "Loan to Professional",
            "Microfinance Business Loan",
            "Microfinance Others",
            "Microfinance Personal Loan",
            "Mudra Loans - Shishu / Kishor / Tarun",
            "Personal Loan",
            "SHG Individual"
    );

    private final List<String> ACCOUNT_TYPES = List.of(
            "AutoLoan(Personal)",
            "BusinessLoan-Secured",
            "BusinessLoanGeneral",
            "BusinessLoanPrioritySectorAgriculture",
            "BusinessLoanPrioritySectorOthers",
            "BusinessLoanPrioritySectorSmallBusiness",
            "BusinessLoanUnsecured",
            "CommercialEquipmentLoan",
            "CommercialVehicleLoan",
            "ConstructionEquipmentLoan",
            "ConsumerLoan",
            "EducationLoan",
            "GECLLoansecured",
            "GECLLoanunsecured",
            "HousingLoan",
            "LoanonCreditCard",
            "LoantoProfessional",
            "MicrofinanceBusinessLoan",
            "MicrofinanceHousingLoan",
            "MicrofinancePersonalLoan",
            "MudraLoans-Shishu/Kishor/Tarun",
            "PersonalLoan",
            "PradhanMantriAwasYojana-CLSS",
            "PropertyLoan",
            "TractorLoan",
            "Two-WheelerLoan",
            "UsedCarLoan"
    );

    public String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "0";
    }

    public double parseAmount(String amount) {
        try {
            return Double.parseDouble(amount.replaceAll(",", ""));
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0;
        }
    }

    public double calculateEMI(String accountType, double amount) {
        if (accountType == null) return 0;

        // Credit Card and similar products
        if (isGroup1(accountType)) {
            return amount * 0.01;
        }
        // Business Loans and Property Loans (12% for 120 months)
        else if (isGroup2(accountType)) {
            return calculateEMIWithTerms(amount, 0.12, 120);
        }
        // Auto and Education Loans (12% for 60 months)
        else if (isGroup3(accountType)) {
            return calculateEMIWithTerms(amount, 0.12, 60);
        }
        // Commercial Loans (15% for 60 months)
        else if (isGroup4(accountType)) {
            return calculateEMIWithTerms(amount, 0.15, 60);
        }
        // Two-Wheeler and Used Car Loans (15% for 48 months)
        else if (isGroup5(accountType)) {
            return calculateEMIWithTerms(amount, 0.15, 48);
        }
        // Personal and Consumer Loans (15% for 36 months)
        else if (isGroup6(accountType)) {
            return calculateEMIWithTerms(amount, 0.15, 36);
        }
        // SHG
        else if (isGroup7(accountType)) {
            return calculateEMIWithTerms(amount, 0.15, 24);
        }
        //Business loan unsecured
        else if (isGroup8(accountType)) {
            return calculateEMIWithTerms(amount, 0.12, 36);
        }
        //Business Loan Priority sector Agriculture
        else if (isGroup9(accountType)) {
            return (amount * (0.10 / 12)) * 6;
        }
        //Microfinance Business loan
        else if (isGroup10(accountType)) {
            return calculateEMIWithTerms(amount, 0.20, 36);
        }
        // Gold Loan
        else if (isGroup11(accountType)) {
            return (amount * (0.10 / 12)) * 4;
        }
        // Tractor Loan
        else if (isGroup12(accountType)) {
            return calculateEMIWithTerms(amount, 0.09, 72);
        }
        // JLG Group
        else if (isGroup13(accountType)) {
            return calculateEMIWithTerms(amount, 0.20, 24);
        }
        // Housing Loan
        else if (isGroup14(accountType)) {
            return calculateEMIWithTerms(amount, 0.09, 240);
        } else if (isGroup15(accountType)) {
            return 0;
        }
        return 0;
    }

    public double calculateEMIWithTerms(double principal, double annualRate, int months) {
        double monthlyRate = annualRate / 12;
        double term1 = principal * monthlyRate * Math.pow(1 + monthlyRate, months);
        double term2 = Math.pow(1 + monthlyRate, months - 1);
        return term1 / term2;
    }

    public boolean isGroup1(String accountType) {
        return accountType.equals("Business Loan Against Bank Deposits") || accountType.equals("Corporate Credit Card")
                || accountType.equals("Credit Card") || accountType.equals("Loan Against Shares / Securities")
                || accountType.equals("Loan on Credit Card") || accountType.equals("Other") || accountType.equals("Overdraft")
                || accountType.equals("Prime Minister Jaan Dhan Yojana - Overdraft") || accountType.equals("Secured Credit Card") ||
                accountType.equals("Fleet Card") || accountType.equals("Loan Against Bank Deposits");
    }

    public boolean isGroup2(String accountType) {
        return accountType.equals("Business Loan - Secured") || accountType.equals("Business Loan General") ||
                accountType.equals("Business Loan Priority Sector  Others") || accountType.equals("Business Loan Priority Sector  Small Business") ||
                accountType.equals("Microfinance Housing Loan") || accountType.equals("Microfinance Others") || accountType.equals("Microfinance Personal Loan")
                || accountType.equals("Mudra Loans - Shishu / Kishor / Tarun") || accountType.equals("Pradhan Mantri Awas Yojana - CLSS") || accountType.equals("Property Loan");
    }

    public boolean isGroup3(String accountType) {
        return accountType.equals("Auto Loan (Personal)") || accountType.equals("Education Loan");
    }

    public boolean isGroup4(String accountType) {
        return accountType.equals("Commercial Equipment Loan") || accountType.equals("Commercial Vehicle Loan") ||
                accountType.equals("Construction Equipment Loan") || accountType.equals("Loan to Professional");
    }

    public boolean isGroup5(String accountType) {
        return accountType.equals("Two-Wheeler Loan") || accountType.equals("Used Car Loan");
    }

    public boolean isGroup6(String accountType) {
        return accountType.equals("Consumer Loan") || accountType.equals("Personal Loan");
    }

    public boolean isGroup7(String accountType) {
        return accountType.equals("SHG Individual");
    }

    public boolean isGroup8(String accountType) {
        return accountType.equals("Business Loan Unsecured");
    }

    public boolean isGroup9(String accountType) {
        return accountType.equals("Business Loan Priority Sector  Agriculture") || accountType.equals("Kisan Credit Card");
    }

    public boolean isGroup10(String accountType) {
        return accountType.equals("Microfinance Business Loan") || accountType.equals("Individual");
    }

    public boolean isGroup11(String accountType) {
        return accountType.equals("Gold Loan");
    }

    public boolean isGroup12(String accountType) {
        return accountType.equals("Tractor Loan");
    }

    public boolean isGroup13(String accountType) {
        return accountType.equals("JLG Group") || accountType.equals("JLG Individual");
    }

    public boolean isGroup14(String accountType) {
        return accountType.equals("Housing Loan");
    }

    public boolean isGroup15(String accountType) {
        return accountType.equals("Business Non-Funded Credit Facility General") || accountType.equals("Business Non-Funded Credit Facility-Priority Sector- Small Business")
                || accountType.equals("Business Non-Funded Credit Facility-Priority Sector-Others") || accountType.equals("GECL Loan secured")
                || accountType.equals("GECL Loan unsecured") || accountType.equals("Non-Funded Credit Facility")
                || accountType.equals("Telco Wireless");
    }
}
