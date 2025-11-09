package br.com.barbearia.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "barbershops")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Barbershop {

    @Id
    private String id;

    private String name;
    private String address;
    private String description;
    private String imageUrl;
    private List<String> phones;

    // @Transient fala pro banco não salvar esse campo na coleção
    @Transient
    private List<BarbershopService> services;
}