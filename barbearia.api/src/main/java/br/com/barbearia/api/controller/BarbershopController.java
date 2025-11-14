package br.com.barbearia.api.controller;

import br.com.barbearia.api.domain.Barbershop;
import br.com.barbearia.api.domain.BarbershopService;
import br.com.barbearia.api.domain.Booking;
import br.com.barbearia.api.repository.BarbershopRepository;
import br.com.barbearia.api.repository.BarbershopServiceRepository;
import br.com.barbearia.api.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import br.com.barbearia.api.dto.ServiceSearchResponse;

@RestController
@RequestMapping("/api/barbershops")
@RequiredArgsConstructor
public class BarbershopController {

    private final BarbershopRepository barbershopRepository;
    private final BarbershopServiceRepository barbershopServiceRepository;
    private final BookingRepository bookingRepository;

    // lista de horários
    private static final List<String> TIME_SLOTS = List.of(
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
            "17:00", "17:30", "18:00"
    );
    // usando fuso horário do sistema
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    // Endpoint p/ horários livres
    @GetMapping("/{id}/available-times")
    public ResponseEntity<List<String>> getAvailableTimeSlots(
            @PathVariable String id,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // convertendo a data p/ intervalo (inicio e fim do dia em UTC (2025-11-10))
        Instant startOfDay = date.atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();

        // buscando agendamentos do dia p/ barbearia "x"
        List<Booking> bookings = bookingRepository.findByBarbershopIdAndDateBetween(id, startOfDay, endOfDay);

        // extraindo horários ocupados
        List<String> occupiedSlots = bookings.stream()
                .map(booking -> TIME_FORMATTER.format(booking.getDate()))
                .collect(Collectors.toList());

        // filtrando a lista
        List<String> availableTimeSlots = TIME_SLOTS.stream()
                .filter(slot -> !occupiedSlots.contains(slot))
                .collect(Collectors.toList());

        // retornando lista de horários livres
        return ResponseEntity.ok(availableTimeSlots);
    }



    @GetMapping("/recommended")
    public List<Barbershop> getRecommendedBarbershops() {
        return barbershopRepository.findAllByOrderByNameAsc();
    }

    @GetMapping("/popular")
    public List<Barbershop> getPopularBarbershops() {
        return barbershopRepository.findAllByOrderByNameDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Barbershop> getBarbershopById(@PathVariable String id) {
        Optional<Barbershop> barbershopOpt = barbershopRepository.findById(id);
        if (barbershopOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Barbershop barbershop = barbershopOpt.get();
        List<BarbershopService> services = barbershopServiceRepository.findByBarbershopId(id);
        barbershop.setServices(services);
        return ResponseEntity.ok(barbershop);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ServiceSearchResponse>> searchServices(
            @RequestParam("q") String searchTerm) {

        // encontra os serviços buscados
        List<BarbershopService> matchingServices = barbershopServiceRepository.findByNameContainingIgnoreCase(searchTerm);

        List<ServiceSearchResponse> response = matchingServices.stream().map(service -> {
            // p/ cada serviço, busca sua barbearia
            Barbershop barbershop = barbershopRepository.findById(service.getBarbershopId())
                    .orElse(null);
            return new ServiceSearchResponse(service, barbershop);

        }).collect(Collectors.toList());

        List<ServiceSearchResponse> finalResponse = response.stream()
                .filter(res -> res.getBarbershop() != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(finalResponse);
    }
}