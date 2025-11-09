package br.com.barbearia.api.config;

import br.com.barbearia.api.domain.Barbershop;
import br.com.barbearia.api.domain.BarbershopService;
import br.com.barbearia.api.repository.BarbershopRepository;
import br.com.barbearia.api.repository.BarbershopServiceRepository;
import br.com.barbearia.api.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList; // array list
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final BarbershopRepository barbershopRepository;
    private final BarbershopServiceRepository barbershopServiceRepository;
    private final BookingRepository bookingRepository;

    @Override
    public void run(String... args) throws Exception {
        // limopando o banco
        barbershopRepository.deleteAll();
        barbershopServiceRepository.deleteAll();
        bookingRepository.deleteAll();


        // gerando as barbearias
        Barbershop b1 = new Barbershop(null, "Barbearia Vintage", "Rua da Barbearia, 123", "Tradição e estilo clássico.", "https://utfs.io/f/c97a2dc9-cf62-468b-a851-bfd2bdde775f-16p.png", List.of("(11) 99999-9991"), null);
        Barbershop b2 = new Barbershop(null, "Corte Moderno", "Avenida Principal, 456", "As últimas tendências de corte.", "https://utfs.io/f/45331760-899c-4b4b-910e-e00babb6ed81-16q.png", List.of("(21) 98888-8882"), null);
        Barbershop b3 = new Barbershop(null, "Navalha de Ouro", "Praça Central, 789", "Especialistas em barba e navalha.", "https://utfs.io/f/5832df58-cfd7-4b3f-b102-42b7e150ced2-16r.png", List.of("(31) 97777-7773"), null);
        Barbershop b4 = new Barbershop(null, "Stilo Urbano", "Rua das Tesouras, 101", "Visual jovem e descolado.", "https://utfs.io/f/7e309eaa-d722-465b-b8b6-76217404a3d3-16s.png", List.of("(41) 96666-6664"), null);
        Barbershop b5 = new Barbershop(null, "Barba & Cia", "Beco do Corte, 202", "O melhor happy hour do homem.", "https://utfs.io/f/178da6b6-6f9a-424a-be9d-a2feb476eb36-16t.png", List.of("(51) 95555-5555"), null);

        // salvando as barbearias (springboot atualiza os IDS delas)
        List<Barbershop> barbershopsSalvas = barbershopRepository.saveAll(List.of(b1, b2, b3, b4, b5));

        // criando lista vazia pros serviços
        List<BarbershopService> todosOsServicos = new ArrayList<>();

        // p/ cada barbearia, cria e salva seus respectivos serviços
        for (Barbershop shop : barbershopsSalvas) {

            BarbershopService corte = new BarbershopService(
                    null,
                    "Corte de Cabelo",
                    "Estilo personalizado com as últimas tendências.",
                    "https://utfs.io/f/0ddfbd26-a424-43a0-aaf3-c3f1dc6be6d1-1kgxo7.png",
                    shop.getId(),
                    6000
            );

            BarbershopService barba = new BarbershopService(
                    null,
                    "Barba",
                    "Modelagem completa para destacar sua masculinidade.",
                    "https://utfs.io/f/e6bdffb6-24a9-455b-aba3-903c2c2b5bde-1jo6tu.png",
                    shop.getId(),
                    4000
            );

            // add os serviços na lista geral
            todosOsServicos.add(corte);
            todosOsServicos.add(barba);
        }

        // salva todos os serviços de uma vez
        barbershopServiceRepository.saveAll(todosOsServicos);

        System.out.println("BANCO DE DADOS POPULADO COM SUCESSO!");
    }
}