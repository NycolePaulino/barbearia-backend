package br.com.barbearia.api.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class BookingRequest {
    private String serviceId;
    private Instant date;
}