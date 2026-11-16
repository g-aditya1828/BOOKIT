package com.example.ticketbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.ticketbooking.model.SeatCategory;
import com.example.ticketbooking.model.SeatState;

public class SeatDto {
    private Long id;

    @NotBlank(message = "Row label is required")
    private String rowLabel;

    @NotNull(message = "Seat number is required")
    private Integer seatNumber;

    @NotNull(message = "Category is required")
    private SeatCategory category;

    @NotNull(message = "Price is required")
    private Double price;

    @NotNull(message = "State is required")
    private SeatState state;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }
    public SeatCategory getCategory() { return category; }
    public void setCategory(SeatCategory category) { this.category = category; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public SeatState getState() { return state; }
    public void setState(SeatState state) { this.state = state; }
}
