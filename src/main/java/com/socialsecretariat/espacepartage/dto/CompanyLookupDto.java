package com.socialsecretariat.espacepartage.dto;

import com.socialsecretariat.espacepartage.model.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLookupDto {
    private String bceNumber;
    private String bceNumberFormatted;
    private String name;
    private String companyName; // denomination with legal form
    private String legalForm;
    private String legalFormShort;
    private String email;
    private String phoneNumber;
    private String website;
    private Address address;
    private String startDate;
    private String status;
}
