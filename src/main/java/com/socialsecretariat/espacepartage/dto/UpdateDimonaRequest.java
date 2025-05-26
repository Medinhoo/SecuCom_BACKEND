package com.socialsecretariat.espacepartage.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Data
public class UpdateDimonaRequest {
    @NotNull(message = "Type is required")
    private String type;
    
    @NotNull(message = "Entry date is required")
    private Date entryDate;
    
    private Date exitDate;
    
    private String exitReason;
    
    private String onssReference;
    
    private String errorMessage;
}
