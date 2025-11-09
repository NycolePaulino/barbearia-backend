package br.com.barbearia.api.repository;

import br.com.barbearia.api.domain.BarbershopService;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarbershopServiceRepository extends MongoRepository<BarbershopService, String> {

    // método p/ encontrar todos os serviços q tenham o barbershopId
    List<BarbershopService> findByBarbershopId(String barbershopId);
}