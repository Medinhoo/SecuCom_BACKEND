package com.socialsecretariat.espacepartage.dto;

/**
 * DTO for Company update responses with additional metadata
 * Extends CompanyDto to include information about confirmation requirements
 */
public class CompanyUpdateResponseDto extends CompanyDto {
    
    private boolean needsConfirmation;
    private boolean wasJustCompleted;
    
    public CompanyUpdateResponseDto() {
        super();
    }
    
    /**
     * Indicates if the company data needs confirmation from the company contact
     * @return true if confirmation is required, false otherwise
     */
    public boolean isNeedsConfirmation() {
        return needsConfirmation;
    }
    
    public void setNeedsConfirmation(boolean needsConfirmation) {
        this.needsConfirmation = needsConfirmation;
    }
    
    /**
     * Indicates if the company was just completed (went from incomplete to complete)
     * @return true if the company was just completed, false otherwise
     */
    public boolean isWasJustCompleted() {
        return wasJustCompleted;
    }
    
    public void setWasJustCompleted(boolean wasJustCompleted) {
        this.wasJustCompleted = wasJustCompleted;
    }
}
