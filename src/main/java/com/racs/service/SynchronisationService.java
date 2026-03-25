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

import java.time.LocalDateTime;
import java.util.*;

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

    @Value("${external.sources.batch-size:100}")
    private int batchSize;

    @Value("${external.sources.base-url:http://localhost:8081}")
    private String externalSourcesBaseUrl;

    private RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public void demarrerSynchronisation() {
        log.info("=== Début du processus de synchronisation ===");
        
        List<Source> sourcesActives = sourceRepository.findByActifTrue();
        log.info("Sources actives: {}", sourcesActives.size());
        
        List<FIDDTO> fidsActifsDTO = externalFIDService.getFIDsActifs();
        log.info("fIDs actifs récupérés du RSU: {}", fidsActifsDTO.size());
        
        List<String> fidsList = fidsActifsDTO.stream()
                .map(FIDDTO::getFid)
                .toList();
        
        List<List<String>> lotsFIDs = decouperEnLots(fidsList, batchSize);
        log.info("Nombre de lots de fIDs: {}", lotsFIDs.size());
        
        for (int i = 0; i < lotsFIDs.size(); i++) {
            List<String> lotFIDs = lotsFIDs.get(i);
            traiterLot(lotFIDs, sourcesActives, i + 1);
        }
        
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
    public void traiterLot(List<String> fidsLot, List<Source> sources, int numeroLot) {
        log.info("Traitement du lot {} avec {} fIDs et {} sources", numeroLot, fidsLot.size(), sources.size());
        
        Lot lot = Lot.builder()
                .referenceLot("LOT_" + LocalDateTime.now() + "_" + numeroLot)
                .dateEnvoi(LocalDateTime.now())
                .statut("EN_COURS")
                .taille(fidsLot.size() * sources.size())
                .build();
        lot = lotRepository.save(lot);
        
        int succesCount = 0;
        int erreurCount = 0;
        
        for (Source source : sources) {
            log.info("Récupération des données de la source {} pour le lot {}", source.getNom(), numeroLot);
            
            List<Map<String, Object>> donneesSource = recupererDonneesSource(source.getNom(), fidsLot);
            
            for (Map<String, Object> donnees : donneesSource) {
                Object fidObj = donnees.get("fid");
                if (fidObj != null) {
                    String fid = fidObj.toString();
                    String payload = convertMapToJson(donnees);
                    try {
                        traiterDonnees(fid, source, lot, payload);
                        succesCount++;
                    } catch (Exception e) {
                        log.error("Erreur lors du traitement des données pour fID {}: {}", fid, e.getMessage());
                        erreurCount++;
                    }
                }
            }
        }
        
        lot.setStatut(erreurCount > 0 && succesCount == 0 ? "ERREUR" : "TERMINE");
        lot.setDateFin(LocalDateTime.now());
        lotRepository.save(lot);
        
        log.info("Lot {} terminé: {} succès, {} erreurs", numeroLot, succesCount, erreurCount);
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
}
