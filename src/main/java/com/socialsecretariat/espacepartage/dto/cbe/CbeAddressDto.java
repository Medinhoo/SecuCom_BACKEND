package com.socialsecretariat.espacepartage.dto.cbe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbeAddressDto {
    private String street;
    
    @JsonProperty("street_number")
    private String streetNumber;
    
    private String box;
    
    @JsonProperty("post_code")
    private String postCode;
    
    private String city;
    
    @JsonProperty("full_address")
    private String fullAddress;
}
