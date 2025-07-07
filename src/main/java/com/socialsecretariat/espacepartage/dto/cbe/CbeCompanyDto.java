package com.socialsecretariat.espacepartage.dto.cbe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbeCompanyDto {
    @JsonProperty("cbe_number")
    private String cbeNumber;
    
    @JsonProperty("cbe_number_formatted")
    private String cbeNumberFormatted;
    
    private String denomination;
    
    @JsonProperty("denomination_with_legal_form")
    private String denominationWithLegalForm;
    
    private CbeAddressDto address;
    
    @JsonProperty("juridical_form")
    private String juridicalForm;
    
    @JsonProperty("juridical_form_short")
    private String juridicalFormShort;
    
    @JsonProperty("juridical_form_id")
    private Integer juridicalFormId;
    
    @JsonProperty("start_date")
    private String startDate;
    
    private String status;
    
    @JsonProperty("contact_infos")
    private CbeContactInfosDto contactInfos;
    
    @JsonProperty("nace_activities")
    private java.util.List<Object> naceActivities;
}
