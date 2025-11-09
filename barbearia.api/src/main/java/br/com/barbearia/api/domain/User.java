package br.com.barbearia.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true) // @Indexed(unique = true) -> garante que o e-mail seja único
    private String email;

    private Boolean emailVerified;
    private String image; // imagem em URL


    private Instant createdAt;
    private Instant updatedAt;

}