package br.com.barbearia.api.config;

import br.com.barbearia.api.domain.Barbershop;
import br.com.barbearia.api.domain.BarbershopService;
import br.com.barbearia.api.repository.BarbershopRepository;
import br.com.barbearia.api.repository.BarbershopServiceRepository;
import br.com.barbearia.api.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final BarbershopRepository barbershopRepository;
    private final BarbershopServiceRepository barbershopServiceRepository;
    private final BookingRepository bookingRepository;

    @Override
    public void run(String... args) throws Exception {
        // limpamndo tudo
        barbershopRepository.deleteAll();
        barbershopServiceRepository.deleteAll();
        bookingRepository.deleteAll();

        // criando barbearias
        Barbershop b1 = new Barbershop(null, "Barbearia Corleone", "Rua Doutor Renato Paes de Barros, 390", "A Barbearia Corleone é inspirado nas antigas barbearias nova-iorquinas típicas de filmes da máfia das décadas 40, 50 e 60 e possui a intenção de resgatar a cultura masculina, que os homens se encontravam para fazer a barba à navalha e cortas os cabelos enquanto fumavam seus charutos, bebiam e conversavam.", "https://www.baressp.com.br/bares/fotos2/barbearia-corleone-baressp-1-min.jpg", List.of("(11) 99999-9991"), null);
        Barbershop b2 = new Barbershop(null, "WM Vintage Clube", "Avenida Principal, 456", "As últimas tendências de corte.", "https://qlxfzjbgzjfecfxlhqwe.supabase.co/storage/v1/object/public/barber-photos/AnyConv.com__DSCF6796.webp", List.of("(21) 98888-8882"), null);
        Barbershop b3 = new Barbershop(null, "Barbearia Torres", "Praça Central, 789", "Especialistas em barba e navalha.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSXDUbxbTN2z-jiCACdPyiYq0K4KA01w8f_hw&s", List.of("(31) 97777-7773"), null);
        Barbershop b4 = new Barbershop(null, "Black Wood", "Rua das Tesouras, 101", "Visual jovem e descolado.", "https://www.nemerfornaciari.com/wp-content/uploads/Screen-Shot-2021-05-03-at-16.29.13.png", List.of("(41) 96666-6664"), null);
        Barbershop b5 = new Barbershop(null, "Barba & Cia", "Beco do Corte, 202", "O melhor happy hour do homem.", "https://utfs.io/f/178da6b6-6f9a-424a-be9d-a2feb476eb36-16t.png", List.of("(51) 95555-5555"), null);

        // salva as barbearias e gera o ID de cada uma
        barbershopRepository.saveAll(List.of(b1, b2, b3, b4, b5));


        List<BarbershopService> servicos = new ArrayList<>();

        // CRIANDO SERVIÇOS
        // b1
        servicos.add(criarServico("Corte Clássico", "Tesoura e máquina.", 4500, "https://utfs.io/f/0ddfbd26-a424-43a0-aaf3-c3f1dc6be6d1-1kgxo7.png", b1));
        servicos.add(criarServico("Barba Tradicional", "Toalha quente e navalha.", 3500, "https://utfs.io/f/e6bdffb6-24a9-455b-aba3-903c2c2b5bde-1jo6tu.png", b1));
        servicos.add(criarServico("Pézinho", "Acabamento na nuca.", 1500, "https://utfs.io/f/8a457cda-f768-411d-a737-cdb23ca6b9b5-b3pegf.png", b1));

        servicos.add(criarServico("Pigmentação", "Correção de falhas na barba ou cabelo.", 3500, "https://plus.unsplash.com/premium_photo-1677444546739-21b8aad351d4?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", b1));
        servicos.add(criarServico("Corte na Tesoura", "Técnica inteiramente manual.", 5000, "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8YmFyYmVhcmlhfGVufDB8fDB8fHww", b1));
        servicos.add(criarServico("Lavagem Especial", "Shampoo mentolado e massagem.", 1500, "https://images.unsplash.com/photo-1647140655214-e4a2d914971f?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Nnx8YmFyYmVhcmlhfGVufDB8fDB8fHww", b1));

        // b2
        servicos.add(criarServico("Corte Degradê", "O corte do momento.", 6000, "https://utfs.io/f/0ddfbd26-a424-43a0-aaf3-c3f1dc6be6d1-1kgxo7.png", b2));
        servicos.add(criarServico("Barba Modelada", "Design moderno.", 4500, "https://utfs.io/f/e6bdffb6-24a9-455b-aba3-903c2c2b5bde-1jo6tu.png", b2));
        servicos.add(criarServico("Luzes", "Clareamento capilar.", 9000, "https://utfs.io/f/5788be0e-2307-4bb4-b603-d9dd237950a2-17l.png", b2));

        servicos.add(criarServico("Platinado", "Descoloração global com matização.", 12000, "https://images.unsplash.com/photo-1599351431202-1e0f0137899a?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTB8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b2));
        servicos.add(criarServico("Freestyle", "Desenhos e riscos com navalha.", 2000, "https://plus.unsplash.com/premium_photo-1677444491957-ab1e8b9a80fd?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OXx8YmFyYmVhcmlhfGVufDB8fDB8fHww", b2));
        servicos.add(criarServico("Pomada Modeladora", "Aplicação e finalização.", 1000, "https://images.unsplash.com/photo-1596728325488-58c87691e9af?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTJ8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b2));

        // b3
        servicos.add(criarServico("Barba Real", "Tratamento completo.", 5500, "https://utfs.io/f/e6bdffb6-24a9-455b-aba3-903c2c2b5bde-1jo6tu.png", b3));
        servicos.add(criarServico("Corte Simples", "Apenas máquina.", 3000, "https://utfs.io/f/0ddfbd26-a424-43a0-aaf3-c3f1dc6be6d1-1kgxo7.png", b3));

        servicos.add(criarServico("Barboterapia", "Vapor de ozônio e óleos essenciais.", 6500, "https://plus.unsplash.com/premium_photo-1677444398670-4f5aaaef65eb?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTN8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b3));
        servicos.add(criarServico("Corte Pai e Filho", "Combo para família.", 7500, "https://images.unsplash.com/photo-1598524374912-6b0b0bab43dd?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTh8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b3));
        servicos.add(criarServico("Limpeza de Pele", "Remoção de cravos e impurezas.", 7000, "https://images.unsplash.com/photo-1693755807658-17ce5331aacb?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjB8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b3));

        // b4
        servicos.add(criarServico("Corte Rápido", "Sem hora marcada.", 2500, "https://utfs.io/f/0ddfbd26-a424-43a0-aaf3-c3f1dc6be6d1-1kgxo7.png", b4));
        servicos.add(criarServico("Sobrancelha", "Na navalha.", 1000, "https://utfs.io/f/2118f76e-89e4-43e6-87c9-8f157500c333-b0ps0b.png", b4));

        servicos.add(criarServico("Máquina Zero", "Raspado completo uniforme.", 2000, "https://images.unsplash.com/photo-1605497788044-5a32c7078486?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjJ8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b4));
        servicos.add(criarServico("Bigode", "Aparo e alinhamento.", 1500, "https://images.unsplash.com/photo-1517832606299-7ae9b720a186?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mjh8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b4));
        servicos.add(criarServico("Acabamento", "Apenas contornos (pezinho).", 1200, "https://plus.unsplash.com/premium_photo-1677098575994-400ea6e05303?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mjl8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b4));

        // b5
        servicos.add(criarServico("Combo Completo", "Cabelo, barba e sobrancelha.", 8500, "https://utfs.io/f/0ddfbd26-a424-43a0-aaf3-c3f1dc6be6d1-1kgxo7.png", b5));
        servicos.add(criarServico("Hidratação", "Produtos importados.", 4000, "https://utfs.io/f/c4919193-a675-4c47-9f21-ebd86d1c8e6a-4oen2a.png", b5));

        servicos.add(criarServico("Selagem", "Alisamento e redução de volume.", 9000, "https://plus.unsplash.com/premium_photo-1661420297394-a8a9590e93a8?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MzN8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b5));
        servicos.add(criarServico("Relaxamento", "Soltura dos cachos.", 6000, "https://plus.unsplash.com/premium_photo-1677098574666-8f97d913d9cd?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mzd8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b5));
        servicos.add(criarServico("Dia do Noivo", "Pacote completo exclusivo.", 35000, "https://images.unsplash.com/photo-1589985494639-69e60c82cab2?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NDB8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b5));
        servicos.add(criarServico("Máscara Black", "Remoção de células mortas.", 2500, "https://plus.unsplash.com/premium_photo-1661493935776-a76a3e33dddf?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NDl8fGJhcmJlYXJpYXxlbnwwfHwwfHx8MA%3D%3D", b5));

        barbershopServiceRepository.saveAll(servicos);

        System.out.println("BANCO DE DADOS POPULADO COM SUCESSO!");
    }

    private BarbershopService criarServico(String nome, String descricao, Integer preco, String imagem, Barbershop loja) {
        return new BarbershopService(
                null,
                nome,
                descricao,
                imagem,
                loja.getId(),
                preco
        );
    }
}