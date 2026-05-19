package com.project.retailproject.dto;

import jakarta.validation.constraints.*;

public class InvoiceRequestDTO {

    @NotNull(message = "Sale ID is required")
    private Long saleId;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount cannot be negative")
    private Double amount;

    private String status;

    // Getters & Setters
    public Long getSaleId() { return saleId; }
    public void setSaleId(Long saleId) { this.saleId = saleId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}