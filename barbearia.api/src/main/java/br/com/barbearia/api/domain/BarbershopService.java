package br.com.barbearia.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarbershopService {

    @Id
    private String id;

    private String name;
    private String description;
    private String imageUrl;

    private String barbershopId;

    private Integer priceInCents;
}