package com.ms.webflux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequestMapping("/campaigns")
@RestController
@SpringBootApplication
public class WebFluxApplication implements CommandLineRunner {
    private final CampaignRepository campaignRepository;

    public WebFluxApplication(CampaignRepository campaignRepository) {this.campaignRepository = campaignRepository;}

    public static void main(String[] args) {
        SpringApplication.run(WebFluxApplication.class, args);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(){
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> String.format("%s - %s", i, UUID.randomUUID().toString()));
    }


    @GetMapping
    public Flux<Campaign> getCampaigns() {
        return this.campaignRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Campaign> getCampaign(@PathVariable String id) {
        return this.campaignRepository.findById(id);
    }

    @PostMapping
    public Mono<String> createCampaign(@RequestBody Campaign campaign) {
        return this.campaignRepository.save(campaign)
                .map(Campaign::getId);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteCampaign(@PathVariable String id) {
        return this.campaignRepository.deleteById(id);
    }

    @Override
    public void run(String... args) {
        Flux<Campaign> campaigns = Flux.just(
                new Campaign("리니지"),
                new Campaign("넷마블"),
                new Campaign("SNOW"),
                new Campaign("현대자동차")
        );

        this.campaignRepository.saveAll(campaigns)
                .subscribe();
    }
}

@Data
@Document
class Campaign {
    @Id
    private String id;
    private String name;
    private LocalDateTime startTime;

    public Campaign() {
        this.startTime = LocalDateTime.now();
    }

    public Campaign(String name) {
        this();
        this.name = name;
    }
}

interface CampaignRepository extends ReactiveCrudRepository<Campaign, String> {}
