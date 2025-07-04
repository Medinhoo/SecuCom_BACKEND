package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDto {
    private boolean emailEnabled;
    private String defaultSubject;
    private String defaultBody;
    private List<String> defaultRecipients;
    private List<String> defaultCcRecipients;
}
