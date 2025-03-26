package com.socialsecretariat.espacepartage.dto;

import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Data
public class DimonaDto {
    private UUID id;
    private String type;
    private Date entryDate;
    private Date exitDate;
    private String exitReason;
    private String status;
    private String onssReference;
    private String errorMessage;
    private UUID collaboratorId;
    private UUID companyId;
}
