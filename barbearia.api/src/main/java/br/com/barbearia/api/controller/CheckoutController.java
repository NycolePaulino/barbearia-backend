package br.com.barbearia.api.controller;

import br.com.barbearia.api.domain.Barbershop;
import br.com.barbearia.api.domain.BarbershopService;
import br.com.barbearia.api.domain.Booking; // Importar Booking
import br.com.barbearia.api.domain.User;
import br.com.barbearia.api.dto.BookingRequest;
import br.com.barbearia.api.dto.CheckoutResponse;
import br.com.barbearia.api.repository.BarbershopRepository;
import br.com.barbearia.api.repository.BarbershopServiceRepository;
import br.com.barbearia.api.repository.BookingRepository; // Importar Repo
import br.com.barbearia.api.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final UserRepository userRepository;
    private final BarbershopServiceRepository serviceRepository;
    private final BarbershopRepository barbershopRepository;
    private final BookingRepository bookingRepository; // Injetar o repo de bookings

    // MUDANÇA 1: A URL de sucesso agora aponta para ESTE Back-end, não para o Front-end
    // O {CHECKOUT_SESSION_ID} é substituído automaticamente pelo Stripe
    private final String backendSuccessUrl = "http://localhost:8080/api/checkout/success?session_id={CHECKOUT_SESSION_ID}";

    // A URL final para onde mandamos o user depois de salvar tudo (Front-end)
    private final String frontendFinalUrl = "http://localhost:3000/bookings";

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @PostMapping("/create-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody BookingRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        BarbershopService service = serviceRepository.findById(request.getServiceId()).orElse(null);
        if (service == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Barbershop barbershop = barbershopRepository.findById(service.getBarbershopId()).orElse(null);

        String formattedDate = request.getDate().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd 'de' MMM HH:mm"));

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(backendSuccessUrl) // MUDANÇA 2: Usamos a URL do Back-end
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro Stripe: " + e.getMessage());
        }
    }

    // MUDANÇA 3: Novo Endpoint que substitui o Webhook
    @GetMapping("/success")
    public ResponseEntity<?> confirmPayment(@RequestParam("session_id") String sessionId) {
        try {
            // 1. Validar a sessão com o Stripe (Segurança)
            Session session = Session.retrieve(sessionId);

            if (!"paid".equals(session.getPaymentStatus())) {
                // Se não foi pago, manda para a home
                return ResponseEntity.status(HttpStatus.FOUND).header("Location", "http://localhost:3000").build();
            }

            // 2. Extrair dados (Igual ao Webhook)
            Map<String, String> metadata = session.getMetadata();
            String serviceId = metadata.get("serviceId");
            String barbershopId = metadata.get("barbershopId");
            String userId = metadata.get("userId");
            String dateStr = metadata.get("date");

            // 3. Pegar o ID da transação (opcional)
            String chargeId = null;
            if (session.getPaymentIntent() != null) {
                PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
                chargeId = paymentIntent.getLatestCharge();
            }

            // 4. SALVAR NO BANCO (A parte importante!)
            Booking newBooking = new Booking(
                    null, serviceId, barbershopId, userId,
                    Instant.parse(dateStr), false, null, chargeId
            );
            bookingRepository.save(newBooking);

            // 5. REDIRECIONAR para o Front-end (Página de Agendamentos)
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", frontendFinalUrl).build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao confirmar pagamento");
        }
    }
}