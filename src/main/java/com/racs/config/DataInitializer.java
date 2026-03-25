package com.racs.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.racs.entity.Role;
import com.racs.entity.Source;
import com.racs.entity.User;
import com.racs.repository.RoleRepository;
import com.racs.repository.SourceRepository;
import com.racs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SourceRepository sourceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SourceValidationConfig validationConfig;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeAdminUser();
        initializeSources();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            log.info("Initialisation des rôles...");
            
            Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
            Role userRole = roleRepository.save(Role.builder().name("USER").build());
            
            log.info("Rôles initialisés: ADMIN, USER");
        }
    }

    private void initializeAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            log.info("Création de l'utilisateur admin...");
            
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            Role userRole = roleRepository.findByName("USER").orElseThrow();
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            
            User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@racs.com")
                .enabled(true)
                .roles(roles)
                .build();
            
            userRepository.save(admin);
            log.info("Utilisateur admin créé: username=admin, password=admin123");
        }
    }

    private void initializeSources() {
        if (sourceRepository.count() == 0) {
            log.info("Initialisation des sources de données...");
            
            List<Source> sources = new ArrayList<>();
            
            for (String sourceNom : validationConfig.getToutesLesSources()) {
                List<String> champsObligatoires = validationConfig.getChampsObligatoires(sourceNom);
                Map<String, String> typesChamps = validationConfig.getTypesChamps(sourceNom);
                
                String champsJson = toJson(champsObligatoires);
                String typesJson = toJson(typesChamps);
                
                Source source = Source.builder()
                    .nom(sourceNom)
                    .apiUrl("/api/" + sourceNom.toLowerCase())
                    .typeObjet(getTypeObjet(sourceNom))
                    .actif(true)
                    .champsObligatoiresJson(champsJson)
                    .typesChampsJson(typesJson)
                    .build();
                
                sources.add(source);
            }
            
            sourceRepository.saveAll(sources);
            log.info("Sources initialisées: {} - {}", sources.size(), validationConfig.getToutesLesSources());
        }
    }

    private String getTypeObjet(String sourceNom) {
        return switch (sourceNom) {
            case "CNSS" -> "donnees_salariales";
            case "CARFO" -> "pension_retraite";
            case "SONABEL" -> "consommation_electricite";
            case "ONEA" -> "consommation_eau";
            case "DGTTM" -> "vehicules_permis";
            default -> "inconnu";
        };
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Erreur lors de la conversion en JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
