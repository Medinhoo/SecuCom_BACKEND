package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityFieldInfo {
    private String fieldPath;
    private String displayName;
    private String type;
}
