package com.racs.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.racs.dto.FIDDTO;
import com.racs.entity.*;
import com.racs.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PreDestroy;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SynchronisationService {

    private final ExternalFIDService externalFIDService;
    private final SourceRepository sourceRepository;
    private final LotRepository lotRepository;
    private final DonneesBrutesRepository donneesBrutesRepository;
    private final ValidationService validationService;
    private final DonneesSocioEcoService donneesSocioEcoService;
    private final AlerteService alerteService;
    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @Value("${external.sources.batch-size:100}")
    private int batchSize;

    @Value("${external.sources.base-url:http://localhost:8081}")
    private String externalSourcesBaseUrl;

    private RestTemplate restTemplate = new RestTemplate();
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Transactional
    public void demarrerSynchronisation() {
        log.info("=== Début du processus de synchronisation ===");
        sseService.sendProgress("Démarrage de la synchronisation...", 0, 100, "STARTING");
        
        List<Source> sourcesActives = sourceRepository.findByActifTrue();
        log.info("Sources actives: {}", sourcesActives.size());
        sseService.sendProgress("Récupération des sources actives: " + sourcesActives.size(), 5, 100, "FETCHING_SOURCES");
        
        List<FIDDTO> fidsActifsDTO = externalFIDService.getFIDsActifs();
        log.info("fIDs actifs récupérés du RSU: {}", fidsActifsDTO.size());
        sseService.sendProgress("Récupération des fIDs actifs: " + fidsActifsDTO.size(), 10, 100, "FETCHING_FIDS");
        
        List<String> fidsList = fidsActifsDTO.stream()
                .map(FIDDTO::getFid)
                .toList();
        
        List<List<String>> lotsFIDs = decouperEnLots(fidsList, batchSize);
        log.info("Nombre de lots de fIDs: {}", lotsFIDs.size());
        
        int totalOperations = lotsFIDs.size() * sourcesActives.size();
        int completedOperations = 0;
        int totalSuccess = 0;
        int totalErrors = 0;
        
        for (int i = 0; i < lotsFIDs.size(); i++) {
            List<String> lotFIDs = lotsFIDs.get(i);
            int[] result = traiterLot(lotFIDs, sourcesActives, i + 1, lotsFIDs.size(), completedOperations, totalOperations);
            totalSuccess += result[0];
            totalErrors += result[1];
            completedOperations += lotFIDs.size() * sourcesActives.size();
            int progress = 10 + (int)((completedOperations * 90.0) / Math.max(totalOperations, 1));
            sseService.sendProgress("Lot " + (i + 1) + "/" + lotsFIDs.size() + " terminé", progress, 100, "PROCESSING");
        }
        
        sseService.sendProgress("Synchronisation terminée", 100, 100, "COMPLETED");
        sseService.sendComplete("Synchronisation terminée: " + totalSuccess + " succès, " + totalErrors + " erreurs", totalSuccess, totalErrors);
        log.info("=== Fin du processus de synchronisation ===");
    }

    private List<List<String>> decouperEnLots(List<String> fids, int tailleLot) {
        List<List<String>> lots = new ArrayList<>();
        for (int i = 0; i < fids.size(); i += tailleLot) {
            lots.add(new ArrayList<>(fids.subList(i, Math.min(i + tailleLot, fids.size()))));
        }
        return lots;
    }

    @Transactional
    public int[] traiterLot(List<String> fidsLot, List<Source> sources, int numeroLot, int totalLots, int completedOps, int totalOps) {
        log.info("Traitement du lot {} avec {} fIDs et {} sources", numeroLot, fidsLot.size(), sources.size());
        
        int succesCount = 0;
        int erreurCount = 0;
        
        List<CompletableFuture<int[]>> futures = sources.stream()
            .map(source -> CompletableFuture.supplyAsync(() -> {
                int sc = 0;
                int er = 0;
                try {
                    Lot lot = Lot.builder()
                            .source(source)
                            .referenceLot("LOT_" + LocalDateTime.now() + "_" + numeroLot + "_" + source.getNom())
                            .dateEnvoi(LocalDateTime.now())
                            .statut("EN_COURS")
                            .taille(fidsLot.size())
                            .build();
                    lot = lotRepository.save(lot);
                    
                    log.info("Récupération des données de la source {} pour le lot {}", source.getNom(), numeroLot);
                    List<Map<String, Object>> donneesSource = recupererDonneesSource(source.getNom(), fidsLot);
                    
                    Map<String, Map<String, Object>> donneesParFid = new LinkedHashMap<>();
                    for (Map<String, Object> donnees : donneesSource) {
                        Object fidObj = donnees.get("fid");
                        if (fidObj != null) {
                            String fid = fidObj.toString();
                            donneesParFid.putIfAbsent(fid, donnees);
                        }
                    }
                    
                    for (Map.Entry<String, Map<String, Object>> entry : donneesParFid.entrySet()) {
                        String fid = entry.getKey();
                        Map<String, Object> donnees = entry.getValue();
                        String payload = convertMapToJson(donnees);
                        try {
                            traiterDonnees(fid, source, lot, payload);
                            sc++;
                        } catch (Exception e) {
                            log.error("Erreur lors du traitement des données pour fID {}: {}", fid, e.getMessage());
                            er++;
                        }
                    }
                    
                    lot.setStatut(er > 0 && sc == 0 ? "ERREUR" : "TERMINE");
                    lot.setDateFin(LocalDateTime.now());
                    lotRepository.save(lot);
                } catch (Exception e) {
                    log.error("Erreur pour la source {}: {}", source.getNom(), e.getMessage());
                    er = fidsLot.size();
                }
                return new int[]{sc, er};
            }, executorService))
            .collect(Collectors.toList());
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        for (CompletableFuture<int[]> future : futures) {
            try {
                int[] result = future.get();
                succesCount += result[0];
                erreurCount += result[1];
            } catch (Exception e) {
                erreurCount++;
            }
        }
        
        log.info("Lot {} terminé: {} succès, {} erreurs", numeroLot, succesCount, erreurCount);
        return new int[]{succesCount, erreurCount};
    }

    private List<Map<String, Object>> recupererDonneesSource(String nomSource, List<String> fids) {
        try {
            String endpoint = getEndpointPourSource(nomSource);
            String url = externalSourcesBaseUrl + endpoint + "/batch";
            
            log.info("Appel API: {} avec fids: {}", url, fids);
            
            String jsonResponse = restTemplate.postForObject(url, fids, String.class);
            
            List<Map<String, Object>> result = objectMapper.readValue(jsonResponse, 
                    new TypeReference<List<Map<String, Object>>>() {});
            
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des données de {}: {}", nomSource, e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getEndpointPourSource(String nomSource) {
        return switch (nomSource.toUpperCase()) {
            case "CNSS" -> "/api/cnss";
            case "CARFO" -> "/api/carfo";
            case "SONABEL" -> "/api/sonabel";
            case "ONEA" -> "/api/onea";
            case "DGTTM" -> "/api/dgttm";
            default -> "/api/" + nomSource.toLowerCase();
        };
    }

    private String convertMapToJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("Erreur conversion JSON: {}", e.getMessage());
            return "{}";
        }
    }

    public void traiterDonnees(String fid, Source source, Lot lot, String payload) {
        log.debug("Traitement des données pour fID: {}, source: {}", fid, source.getNom());
        
        DonneesBrutes donneesBrutes = DonneesBrutes.builder()
                .lot(lot)
                .fid(fid)
                .payload(payload)
                .statut("RECUE")
                .build();
        donneesBrutesRepository.save(donneesBrutes);
        
        boolean valide = validationService.validerDonnees(donneesBrutes, source);
        
        if (valide) {
            donneesSocioEcoService.enregistrerDonnees(fid, source, payload, lot);
            log.debug("Données validées et enregistrées pour fID: {}", fid);
        } else {
            donneesBrutes.setStatut("INVALIDE");
            donneesBrutesRepository.save(donneesBrutes);
            log.debug("Données invalides pour fID: {}", fid);
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}
