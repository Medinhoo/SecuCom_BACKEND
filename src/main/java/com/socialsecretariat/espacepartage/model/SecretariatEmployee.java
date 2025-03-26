package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = { "secretariat" })
@ToString(callSuper = true, exclude = { "secretariat" })
@DiscriminatorValue("SecretariatEmployee")
public class SecretariatEmployee extends User {

    private String position;
    private String specialization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretariat_id")
    private SocialSecretariat secretariat;
}
