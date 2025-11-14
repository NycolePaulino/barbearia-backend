package br.com.barbearia.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    private String id;

    private String serviceId;
    private String barbershopId;

    private String userId;

    private Instant date;
    private Boolean cancelled;
    private Instant cancelledAt;

    private String stripeChargeId;
}