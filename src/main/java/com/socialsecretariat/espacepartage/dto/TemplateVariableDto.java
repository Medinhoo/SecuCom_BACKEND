package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVariableDto {
    private String name;
    private String displayName;
    private String entity;
    private String field;
    private String type;
    private boolean required;
    private String description;
    private Object defaultValue;
}
