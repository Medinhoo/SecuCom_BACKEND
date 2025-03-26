package com.socialsecretariat.espacepartage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Data
public class CreateDimonaRequest {
    @NotNull(message = "Type is required")
    private String type;

    @NotNull(message = "Entry date is required")
    private Date entryDate;

    private Date exitDate;

    private String exitReason;

    private String onssReference;

    @NotNull(message = "Collaborator ID is required")
    private UUID collaboratorId;

    @NotNull(message = "Company ID is required")
    private UUID companyId;
}
