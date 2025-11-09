package br.com.barbearia.api.controller;

import br.com.barbearia.api.domain.Barbershop;
import br.com.barbearia.api.domain.BarbershopService;
import br.com.barbearia.api.domain.Booking;
import br.com.barbearia.api.domain.User;
import br.com.barbearia.api.dto.BookingRequest;
import br.com.barbearia.api.dto.BookingResponse;
import br.com.barbearia.api.repository.BarbershopRepository;
import br.com.barbearia.api.repository.BarbershopServiceRepository;
import br.com.barbearia.api.repository.BookingRepository;
import br.com.barbearia.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.Instant;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingRepository bookingRepository;
    private final BarbershopServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final BarbershopRepository barbershopRepository;

    // Endpoint p/ meus agendamentos
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(@AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        // verifica se o user está no banco de dados
        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado.");
        }
        User user = userOpt.get();

        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        List<BookingResponse> richBookings = bookings.stream().map(booking -> {
            BarbershopService service = serviceRepository.findById(booking.getServiceId()).orElse(null);
            Barbershop barbershop = (service != null)
                    ? barbershopRepository.findById(service.getBarbershopId()).orElse(null)
                    : null;
            return new BookingResponse(booking.getId(), booking.getDate(), booking.getCancelled(), service, barbershop);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(richBookings);
    }

    // Endpoint para criar agendamentos
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request,
                                           @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado.");
        }
        User user = userOpt.get();

        Optional<BarbershopService> serviceOpt = serviceRepository.findById(request.getServiceId());
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Serviço não encontrado.");
        }
        BarbershopService service = serviceOpt.get();

        Optional<Booking> existingBooking = bookingRepository.findByBarbershopIdAndDate(
                service.getBarbershopId(),
                request.getDate()
        );
        if (existingBooking.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Já existe um agendamento para esta data.");
        }

        Booking newBooking = new Booking(null, request.getServiceId(), service.getBarbershopId(), user.getId(), request.getDate(), false, null);
        bookingRepository.save(newBooking);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }

    // Endpoint p/ cancelamento de reserva
    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // verifica se o user está logado
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado.");
        }

        // encontra a reserva
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva não encontrada.");
        }

        // verifica se o user é o dono da reserva
        if (!booking.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para cancelar esta reserva.");
        }

        // verifica se já está cancelada
        if (booking.getCancelled() != null && booking.getCancelled()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Esta reserva já foi cancelada.");
        }

        // não pode cancelar reservas passadas
        if (booking.getDate().isBefore(Instant.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Não é possível cancelar reservas passadas.");
        }

        // atualizando a reserva
        booking.setCancelled(true);
        booking.setCancelledAt(Instant.now());
        bookingRepository.save(booking);

        return ResponseEntity.ok(booking);
    }
}