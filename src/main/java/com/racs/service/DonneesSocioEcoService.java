package com.racs.service;

import com.racs.entity.*;
import com.racs.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonneesSocioEcoService {

    private final DonneesSocioEcoRepository donneesSocioEcoRepository;
    private final AttributsDonneesSocioEcoRepository attributsRepository;
    private final ArchiveAttributsDonneesSocioEcoRepository archiveRepository;
    private final JournalModificationRepository journalRepository;
    private final FIDRepository fidRepository;
    private final ObjectMapper objectMapper;
    private final AlerteService alerteService;

    @Transactional
    public void enregistrerDonnees(String fid, Source source, String payload, Lot lot) {
        log.debug("Enregistrement des données pour fID: {} source: {}", fid, source.getNom());
        
        DonneesSocioEco donnees = donneesSocioEcoRepository
                .findByFidAndSourceIdAndDateValeurWithAttributs(fid, source.getId(), LocalDate.now())
                .orElse(null);
        
        if (donnees == null) {
            insererNouvellesDonnees(fid, source, payload);
        } else {
            mettreAJourDonnees(donnees, fid, source, payload, lot);
        }
    }

    private void insererNouvellesDonnees(String fid, Source source, String payload) {
        log.debug("Insertion de nouvelles données pour fID: {}", fid);
        
        ensureFidExists(fid);
        
        DonneesSocioEco donnees = DonneesSocioEco.builder()
                .fid(fid)
                .source(source)
                .dateValeur(LocalDate.now())
                .version(1)
                .build();
        
        donnees = donneesSocioEcoRepository.save(donnees);
        
        Map<String, Object> attributsMap = parsePayload(payload);
        for (Map.Entry<String, Object> entry : attributsMap.entrySet()) {
            AttributsDonneesSocioEco attribut = createAttribut(donnees, entry.getKey(), entry.getValue(), 1);
            attributsRepository.save(attribut);
            
            journalRepository.save(JournalModification.builder()
                    .donneesSocioEcoId(donnees.getId())
                    .action("INSERT")
                    .avant(null)
                    .apres(toJson(entry.getValue()))
                    .processus("SYNCHRONISATION")
                    .build());
        }
        
        log.info("Nouvelles données insérées pour fID: {}", fid);
    }

    private void mettreAJourDonnees(DonneesSocioEco donnees, String fid, Source source, String payload, Lot lot) {
        log.debug("Mise à jour des données pour fID: {}", fid);
        
        Map<String, Object> nouveauxAttributs = parsePayload(payload);
        
        Map<String, Object> anciennesDonneesMap = new HashMap<>();
        Map<String, AttributsDonneesSocioEco> anciensAttributsMap = new HashMap<>();
        for (AttributsDonneesSocioEco attr : donnees.getAttributs()) {
            anciennesDonneesMap.put(attr.getNom(), getValeurTypée(attr));
            anciensAttributsMap.put(attr.getNom(), attr);
        }
        
        boolean aDesChangements = false;
        for (Map.Entry<String, Object> entry : nouveauxAttributs.entrySet()) {
            String nomChamp = entry.getKey();
            Object nouvelleValeur = entry.getValue();
            Object ancienneValeur = anciennesDonneesMap.get(nomChamp);
            
            if (!Objects.equals(ancienneValeur, nouvelleValeur)) {
                aDesChangements = true;
                break;
            }
        }
        
        if (!aDesChangements) {
            log.debug("Données identiques, aucune mise à jour nécessaire pour fID: {}", fid);
            return;
        }
        
        alerteService.analyserEtGenererAlertes(fid, source, anciennesDonneesMap, nouveauxAttributs, lot);
        
        int oldVersion = donnees.getVersion();
        int nouvelleVersion = oldVersion + 1;
        
        donnees.setVersion(nouvelleVersion);
        donnees.setDateMiseAJour(LocalDateTime.now());
        donneesSocioEcoRepository.save(donnees);
        
        for (Map.Entry<String, Object> entry : nouveauxAttributs.entrySet()) {
            String nomChamp = entry.getKey();
            Object nouvelleValeur = entry.getValue();
            Object ancienneValeur = anciennesDonneesMap.get(nomChamp);
            AttributsDonneesSocioEco ancienAttr = anciensAttributsMap.get(nomChamp);
            
            if (ancienAttr != null && !Objects.equals(ancienneValeur, nouvelleValeur)) {
                ArchiveAttributsDonneesSocioEco archive = ArchiveAttributsDonneesSocioEco.builder()
                        .attributOrigineId(ancienAttr.getId())
                        .donneesSocioEco(donnees)
                        .nom(ancienAttr.getNom())
                        .valeurText(ancienAttr.getValeurText())
                        .valeurNumerique(ancienAttr.getValeurNumerique())
                        .valeurDate(ancienAttr.getValeurDate())
                        .valeurBoolean(ancienAttr.getValeurBoolean())
                        .version(oldVersion)
                        .raisonArchivage("MISE_A_JOUR")
                        .build();
                archiveRepository.save(archive);
                
                journalRepository.save(JournalModification.builder()
                        .donneesSocioEcoId(donnees.getId())
                        .action("UPDATE")
                        .avant(toJson(ancienneValeur))
                        .apres(toJson(nouvelleValeur))
                        .processus("SYNCHRONISATION")
                        .build());
                
                ancienAttr.setValeurText(null);
                ancienAttr.setValeurNumerique(null);
                ancienAttr.setValeurDate(null);
                ancienAttr.setValeurBoolean(null);
                ancienAttr.setVersion(nouvelleVersion);
                
                if (nouvelleValeur instanceof Number) {
                    ancienAttr.setValeurNumerique(BigDecimal.valueOf(((Number) nouvelleValeur).doubleValue()));
                } else if (nouvelleValeur instanceof String) {
                    ancienAttr.setValeurText((String) nouvelleValeur);
                } else if (nouvelleValeur instanceof Boolean) {
                    ancienAttr.setValeurBoolean((Boolean) nouvelleValeur);
                } else if (nouvelleValeur != null) {
                    ancienAttr.setValeurText(nouvelleValeur.toString());
                }
                
                attributsRepository.save(ancienAttr);
            } else if (ancienAttr == null) {
                AttributsDonneesSocioEco newAttr = createAttribut(donnees, nomChamp, nouvelleValeur, nouvelleVersion);
                attributsRepository.save(newAttr);
                
                journalRepository.save(JournalModification.builder()
                        .donneesSocioEcoId(donnees.getId())
                        .action("INSERT")
                        .avant(null)
                        .apres(toJson(nouvelleValeur))
                        .processus("SYNCHRONISATION")
                        .build());
            }
        }
        
        log.info("Données mises à jour pour fID: {} vers version {}", fid, nouvelleVersion);
    }

    private void ensureFidExists(String fid) {
        if (!fidRepository.existsById(fid)) {
            FID newFid = FID.builder()
                    .fid(fid)
                    .statut("actif")
                    .build();
            fidRepository.save(newFid);
        }
    }

    private Map<String, Object> parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Erreur lors du parsing du payload: {}", e.getMessage());
            return Map.of();
        }
    }

    private AttributsDonneesSocioEco createAttribut(DonneesSocioEco donnees, String nom, Object valeur, int version) {
        AttributsDonneesSocioEco attribut = AttributsDonneesSocioEco.builder()
                .donneesSocioEco(donnees)
                .nom(nom)
                .version(version)
                .build();
        
        if (valeur instanceof Number) {
            attribut.setValeurNumerique(BigDecimal.valueOf(((Number) valeur).doubleValue()));
        } else if (valeur instanceof String) {
            attribut.setValeurText((String) valeur);
        } else if (valeur instanceof Boolean) {
            attribut.setValeurBoolean((Boolean) valeur);
        } else if (valeur != null) {
            attribut.setValeurText(valeur.toString());
        }
        
        return attribut;
    }

    private Object getValeurTypée(AttributsDonneesSocioEco attribut) {
        if (attribut.getValeurText() != null) {
            return attribut.getValeurText();
        }
        if (attribut.getValeurNumerique() != null) {
            return attribut.getValeurNumerique();
        }
        if (attribut.getValeurDate() != null) {
            return attribut.getValeurDate();
        }
        if (attribut.getValeurBoolean() != null) {
            return attribut.getValeurBoolean();
        }
        return null;
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
