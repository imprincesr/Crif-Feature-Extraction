package com.ninjacart.featureExtraction.model.featureModel;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdditionalDetails {

    private AdditionalDetailKeys key;
    private String value;
    private String comment;
    private String timestamp;

}