package br.com.barbearia.api.dto;

import br.com.barbearia.api.domain.Barbershop;
import br.com.barbearia.api.domain.BarbershopService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    // detalhes do próprio agendamento
    private String id;
    private Instant date;
    private Boolean cancelled;

    private BarbershopService service;
    private Barbershop barbershop;
}