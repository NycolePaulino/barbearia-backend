package br.com.barbearia.api.controller;

import br.com.barbearia.api.domain.Barbershop;
import br.com.barbearia.api.domain.BarbershopService;
import br.com.barbearia.api.domain.Booking;
import br.com.barbearia.api.domain.User;
import br.com.barbearia.api.dto.BookingRequest;
import br.com.barbearia.api.dto.BookingResponse; // 1. IMPORTAR O NOVO DTO
import br.com.barbearia.api.repository.BarbershopRepository; // 2. IMPORTAR O REPO DA BARBEARIA
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
import java.util.stream.Collectors; // 3. IMPORTAR O COLLECTORS

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingRepository bookingRepository;
    private final BarbershopServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final BarbershopRepository barbershopRepository; // 4. INJECTAR O REPO DA BARBEARIA

    // === MÉTODO ATUALIZADO PARA DEVOLVER O "OBJETO RICO" ===
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(@AuthenticationPrincipal UserDetails userDetails) {

        // Validação (igual)
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado.");
        }
        User user = userOpt.get();

        // 1. Busca os agendamentos normais (com IDs)
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        // 2. "Transforma" a lista de Booking em uma lista de BookingResponse
        List<BookingResponse> richBookings = bookings.stream().map(booking -> {
            // Para cada agendamento, busca os detalhes
            BarbershopService service = serviceRepository.findById(booking.getServiceId()).orElse(null);
            Barbershop barbershop = (service != null)
                    ? barbershopRepository.findById(service.getBarbershopId()).orElse(null)
                    : null;

            // 3. Monta o DTO de Resposta
            return new BookingResponse(
                    booking.getId(),
                    booking.getDate(),
                    booking.getCancelled(),
                    service,
                    barbershop
            );
        }).collect(Collectors.toList());

        // 4. Devolve a lista "rica"
        return ResponseEntity.ok(richBookings);
    }


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

        Booking newBooking = new Booking(
                null,
                request.getServiceId(),
                service.getBarbershopId(),
                user.getId(),
                request.getDate(),
                false,
                null
        );

        bookingRepository.save(newBooking);

        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }
}