package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateAnalysisResult {
    private List<String> variables;
    private String originalFileName;
}
