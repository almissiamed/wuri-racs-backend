package com.racs.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.racs.config.SourceValidationConfig;
import com.racs.dto.SourceDTO;
import com.racs.entity.Source;
import com.racs.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceRepository sourceRepository;
    private final ObjectMapper objectMapper;
    private final SourceValidationConfig validationConfig;

    @GetMapping
    public ResponseEntity<List<SourceDTO>> getAllSources() {
        List<SourceDTO> sources = sourceRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(sources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceDTO> getSourceById(@PathVariable Integer id) {
        return sourceRepository.findById(id)
            .map(source -> ResponseEntity.ok(toDTO(source)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/validation")
    public ResponseEntity<?> getSourceValidationConfig(@PathVariable Integer id) {
        return sourceRepository.findById(id)
            .map(source -> {
                String sourceNom = source.getNom().toUpperCase();
                return ResponseEntity.ok(Map.of(
                    "source", sourceNom,
                    "champsObligatoires", validationConfig.getChampsObligatoires(sourceNom),
                    "typesChamps", validationConfig.getTypesChamps(sourceNom),
                    "estSourceConnue", validationConfig.isSourceConnue(sourceNom)
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SourceDTO> createSource(@RequestBody Source source) {
        if (validationConfig.isSourceConnue(source.getNom().toUpperCase())) {
            List<String> champs = validationConfig.getChampsObligatoires(source.getNom().toUpperCase());
            Map<String, String> types = validationConfig.getTypesChamps(source.getNom().toUpperCase());
            source.setChampsObligatoiresJson(toJson(champs));
            source.setTypesChampsJson(toJson(types));
        }
        return ResponseEntity.ok(toDTO(sourceRepository.save(source)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SourceDTO> updateSource(@PathVariable Integer id, @RequestBody Source source) {
        return sourceRepository.findById(id)
            .map(existing -> {
                existing.setNom(source.getNom());
                existing.setApiUrl(source.getApiUrl());
                existing.setTypeObjet(source.getTypeObjet());
                existing.setActif(source.getActif());
                
                if (validationConfig.isSourceConnue(source.getNom().toUpperCase())) {
                    List<String> champs = validationConfig.getChampsObligatoires(source.getNom().toUpperCase());
                    Map<String, String> types = validationConfig.getTypesChamps(source.getNom().toUpperCase());
                    existing.setChampsObligatoiresJson(toJson(champs));
                    existing.setTypesChampsJson(toJson(types));
                }
                
                return ResponseEntity.ok(toDTO(sourceRepository.save(existing)));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSource(@PathVariable Integer id) {
        if (sourceRepository.existsById(id)) {
            sourceRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    private SourceDTO toDTO(Source source) {
        List<String> champs = parseJsonList(source.getChampsObligatoiresJson());
        Map<String, String> types = parseJsonMap(source.getTypesChampsJson());
        
        return SourceDTO.builder()
            .id(source.getId())
            .nom(source.getNom())
            .apiUrl(source.getApiUrl())
            .typeObjet(source.getTypeObjet())
            .actif(source.getActif())
            .dateCreation(source.getDateCreation())
            .champsObligatoires(champs)
            .typesChamps(types)
            .build();
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private Map<String, String> parseJsonMap(String json) {
        if (json == null || json.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
