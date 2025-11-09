package br.com.barbearia.api.repository;

import br.com.barbearia.api.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Importar

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // método p/ buscar o user pelo e-mail
    Optional<User> findByEmail(String email);
}