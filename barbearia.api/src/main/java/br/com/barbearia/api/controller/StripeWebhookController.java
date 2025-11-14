package br.com.barbearia.api.controller;

import br.com.barbearia.api.domain.Booking;
import br.com.barbearia.api.repository.BookingRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionRetrieveParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final BookingRepository bookingRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/stripe")
    public ResponseEntity<?> handleStripeWebhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        String payload;

        try {
            payload = new String(request.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

        } catch (SignatureVerificationException e) {
            System.out.println("Webhook error :: Assinatura inválida: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Assinatura inválida.");
        } catch (Exception e) {
            System.out.println("Webhook error :: Erro ao processar evento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro no webhook.");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            try {
                JsonObject dataObject = JsonParser.parseString(event.getData().toJson())
                        .getAsJsonObject()
                        .getAsJsonObject("object");

                Session session = ApiResource.GSON.fromJson(dataObject, Session.class);

                if (session == null || session.getId() == null) {
                    System.out.println("Webhook error :: Sessão inválida ou nula.");
                    System.out.println("JSON recebido: " + dataObject.toString());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sessão inválida.");
                }

                System.out.println("=== Processando Session ===");
                System.out.println("Session ID: " + session.getId());
                System.out.println("Payment Status: " + session.getPaymentStatus());

                Map<String, String> metadata = new HashMap<>();
                if (dataObject.has("metadata") && !dataObject.get("metadata").isJsonNull()) {
                    JsonObject metadataJson = dataObject.getAsJsonObject("metadata");
                    for (String key : metadataJson.keySet()) {
                        metadata.put(key, metadataJson.get(key).getAsString());
                    }
                }

                System.out.println("Metadata: " + metadata);

                String serviceId = metadata.get("serviceId");
                String barbershopId = metadata.get("barbershopId");
                String userId = metadata.get("userId");
                String dateStr = metadata.get("date");

                if (serviceId == null || barbershopId == null || userId == null || dateStr == null) {
                    System.out.println("Webhook error :: Metadados em falta.");
                    System.out.println("  - serviceId: " + serviceId);
                    System.out.println("  - barbershopId: " + barbershopId);
                    System.out.println("  - userId: " + userId);
                    System.out.println("  - date: " + dateStr);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Metadados em falta.");
                }

                String chargeId = null;
                try {
                    SessionRetrieveParams params = SessionRetrieveParams.builder()
                            .addExpand("payment_intent")
                            .build();

                    Session retrievedSession = Session.retrieve(session.getId(), params, null);

                    if (retrievedSession.getPaymentIntent() != null) {
                        if (retrievedSession.getPaymentIntentObject() != null) {
                            PaymentIntent paymentIntent = retrievedSession.getPaymentIntentObject();
                            chargeId = paymentIntent.getLatestCharge();
                            System.out.println("Charge ID obtido via PaymentIntentObject: " + chargeId);
                        } else {
                            String paymentIntentId = retrievedSession.getPaymentIntent();
                            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                            chargeId = paymentIntent.getLatestCharge();
                            System.out.println("Charge ID obtido via PaymentIntent separado: " + chargeId);
                        }
                    }
                } catch (StripeException e) {
                    System.out.println("Webhook warning :: Falha ao buscar payment_intent/charge: " + e.getMessage());
                }

                // Criar o agendamento
                Booking newBooking = new Booking(
                        null,
                        serviceId,
                        barbershopId,
                        userId,
                        Instant.parse(dateStr),
                        false,
                        null,
                        chargeId
                );

                Booking savedBooking = bookingRepository.save(newBooking);

                System.out.println("✓ SUCESSO: Agendamento criado via Webhook do Stripe!");
                System.out.println("  - ID do Agendamento: " + savedBooking.getId());
                System.out.println("  - Serviço: " + serviceId);
                System.out.println("  - Barbearia: " + barbershopId);
                System.out.println("  - Usuário: " + userId);
                System.out.println("  - Data: " + dateStr);
                System.out.println("  - Charge ID: " + (chargeId != null ? chargeId : "N/A"));

            } catch (Exception e) {
                System.out.println("Webhook error :: Erro ao processar checkout.session.completed: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro ao processar o agendamento: " + e.getMessage());
            }
        }
        return ResponseEntity.ok().build();
    }
}