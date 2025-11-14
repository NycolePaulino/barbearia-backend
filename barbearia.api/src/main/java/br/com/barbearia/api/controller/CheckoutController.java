package br.com.barbearia.api.controller;

import br.com.barbearia.api.domain.Barbershop;
import br.com.barbearia.api.domain.BarbershopService;
import br.com.barbearia.api.domain.User;
import br.com.barbearia.api.dto.BookingRequest;
import br.com.barbearia.api.dto.CheckoutResponse;
import br.com.barbearia.api.repository.BarbershopRepository;
import br.com.barbearia.api.repository.BarbershopServiceRepository;
import br.com.barbearia.api.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final UserRepository userRepository;
    private final BarbershopServiceRepository serviceRepository;
    private final BarbershopRepository barbershopRepository;

    // Lemos as URLs de sucesso/cancelamento do application.properties
    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @PostMapping("/create-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody BookingRequest request,
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

        Barbershop barbershop = barbershopRepository.findById(service.getBarbershopId()).orElse(null);
        if (barbershop == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Barbearia não encontrada.");
        }

        String formattedDate = request.getDate().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd 'de' MMM HH:mm"));

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)

                    .putMetadata("serviceId", service.getId())
                    .putMetadata("barbershopId", service.getBarbershopId())
                    .putMetadata("userId", user.getId())
                    .putMetadata("date", request.getDate().toString())

                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("brl")
                                                    .setUnitAmount((long) service.getPriceInCents())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(String.format("%s - %s em %s", barbershop.getName(), service.getName(), formattedDate))
                                                                    .setDescription(service.getDescription())
                                                                    .addImage(service.getImageUrl())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            return ResponseEntity.ok(new CheckoutResponse(session.getUrl()));

        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar sessão de pagamento: " + e.getMessage());
        }
    }
}