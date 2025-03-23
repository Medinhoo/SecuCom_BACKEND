package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Size(max = 100)
    private String street;

    @Size(max = 10)
    private String number;

    @Size(max = 10)
    private String box;

    @Size(max = 10)
    private String postalCode;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;
}
