package br.com.barbearia.api.controller;

import br.com.barbearia.api.domain.Barbershop;
import br.com.barbearia.api.domain.BarbershopService;
import br.com.barbearia.api.repository.BarbershopRepository;
import br.com.barbearia.api.repository.BarbershopServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/barbershops")
@RequiredArgsConstructor
public class BarbershopController {

    private final BarbershopRepository barbershopRepository;
    private final BarbershopServiceRepository barbershopServiceRepository;

    // Endpoint para: recommendedBarbershops
    @GetMapping("/recommended")
    public List<Barbershop> getRecommendedBarbershops() {
        return barbershopRepository.findAllByOrderByNameAsc();
    }

    // Endpoint para: popularBarbershops
    @GetMapping("/popular")
    public List<Barbershop> getPopularBarbershops() {
        return barbershopRepository.findAllByOrderByNameDesc();
    }

    // Endpoint para: buscar babearia e seus serviços
    @GetMapping("/{id}")
    public ResponseEntity<Barbershop> getBarbershopById(@PathVariable String id) {

        Optional<Barbershop> barbershopOpt = barbershopRepository.findById(id);

        if (barbershopOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // pega a barbearia
        Barbershop barbershop = barbershopOpt.get();

        // busca os serviços dessa barbearia
        List<BarbershopService> services = barbershopServiceRepository.findByBarbershopId(id);

        // junta os serviços no objeto da barbearia
        barbershop.setServices(services);

        // retorna o obj completo
        return ResponseEntity.ok(barbershop);
    }
}