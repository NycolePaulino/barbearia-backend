package br.com.barbearia.api.repository;

import br.com.barbearia.api.domain.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    Optional<Booking> findByBarbershopIdAndDate(String barbershopId, Instant date);

    List<Booking> findByUserId(String userId);

    List<Booking> findByBarbershopIdAndDateBetween(String barbershopId, Instant start, Instant end);
}