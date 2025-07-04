package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableMapping {
    private String variableName;
    private String displayName;
    private String entity; // "Company", "Collaborator", "manual"
    private String field; // null if manual
    private String type;
    private boolean required;
    private String description;
}
