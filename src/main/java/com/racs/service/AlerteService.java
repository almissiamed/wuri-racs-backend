package com.racs.service;

import com.racs.entity.*;
import com.racs.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlerteService {

    private final AlerteRepository alerteRepository;

    @Transactional
    public void genererAlerte(String type, String niveau, String message, Lot lot, Validation validation) {
        log.debug("Génération d'alerte: type={}, niveau={}", type, niveau);
        
        Alerte alerte = Alerte.builder()
                .type(type)
                .niveau(niveau)
                .message(message)
                .lot(lot)
                .validation(validation)
                .traitee(false)
                .build();
        
        alerteRepository.save(alerte);
        
        log.info("Alerte générée: {} - {}", type, message);
    }

    @Transactional
    public void genererAlerteVariation(String type, String niveau, String message, String fid, 
            String attribut, BigDecimal valeurAvant, BigDecimal valeurApres, BigDecimal variation, Lot lot) {
        
        if (alerteRepository.existsByFidAndAttributAndType(fid, attribut, type)) {
            log.debug("Alerte déjà existante pour fid={}, attribut={}, type={}", fid, attribut, type);
            return;
        }
        
        Alerte alerte = Alerte.builder()
                .type(type)
                .niveau(niveau)
                .message(message)
                .fid(fid)
                .attribut(attribut)
                .valeurAvant(valeurAvant)
                .valeurApres(valeurApres)
                .variation(variation)
                .lot(lot)
                .traitee(false)
                .build();
        
        alerteRepository.save(alerte);
        
        log.info("Alerte de variation générée: {} - {}", type, message);
    }

    public void analyserEtGenererAlertes(String fid, Source source, Map<String, Object> anciennesDonnees, 
            Map<String, Object> nouvellesDonnees, Lot lot) {
        
        for (Map.Entry<String, Object> entry : nouvellesDonnees.entrySet()) {
            String attribut = entry.getKey();
            Object nouvelleValeur = entry.getValue();
            Object ancienneValeur = anciennesDonnees.get(attribut);

            if (ancienneValeur != null && nouvelleValeur != null) {
                if (estNumerique(ancienneValeur) && estNumerique(nouvelleValeur)) {
                    BigDecimal avant = new BigDecimal(ancienneValeur.toString());
                    BigDecimal apres = new BigDecimal(nouvelleValeur.toString());
                    BigDecimal variation = apres.subtract(avant);
                    BigDecimal pourcentageVariation = BigDecimal.ZERO;
                    
                    if (avant.compareTo(BigDecimal.ZERO) != 0) {
                        pourcentageVariation = variation.multiply(new BigDecimal("100"))
                                .divide(avant, 2, RoundingMode.HALF_UP);
                    }

                    if (variation.compareTo(BigDecimal.ZERO) > 0) {
                        String message = String.format("Augmentation de %s: %s -> %s (+%s%%)", 
                                attribut, avant, apres, pourcentageVariation);
                        genererAlerteVariation("AUGMENTATION", "WARNING", message, fid, attribut, 
                                avant, apres, variation, lot);
                    } else if (variation.compareTo(BigDecimal.ZERO) < 0) {
                        String message = String.format("Diminution de %s: %s -> %s (%s%%)", 
                                attribut, avant, apres, pourcentageVariation);
                        genererAlerteVariation("DIMINUTION", "WARNING", message, fid, attribut, 
                                avant, apres, variation, lot);
                    }
                } else if (!Objects.equals(ancienneValeur.toString(), nouvelleValeur.toString())) {
                    String message = String.format("Changement de %s: '%s' -> '%s'", 
                            attribut, ancienneValeur, nouvelleValeur);
                    genererAlerteVariation("CHANGEMENT", "INFO", message, fid, attribut, 
                            null, null, null, lot);
                }
            }
        }
    }

    private boolean estNumerique(Object valeur) {
        try {
            new BigDecimal(valeur.toString());
            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public List<Alerte> getAlertesNonTraitees() {
        return alerteRepository.findByTraiteeFalse();
    }

    public List<Alerte> getAlertesParType(String type) {
        return alerteRepository.findByTypeAndTraiteeFalse(type);
    }

    public List<Alerte> getAlertesParFID(String fid) {
        return alerteRepository.findAll().stream()
                .filter(a -> fid.equals(a.getFid()))
                .toList();
    }

    @Transactional
    public void traiterAlerte(Integer alerteId) {
        alerteRepository.findById(alerteId).ifPresent(alerte -> {
            alerte.setTraitee(true);
            alerte.setDateTraitement(LocalDateTime.now());
            alerteRepository.save(alerte);
            log.info("Alerte {} traitée", alerteId);
        });
    }

    @Transactional
    public void diffuserAlertes() {
        List<Alerte> alertes = getAlertesNonTraitees();
        log.info("Diffusion de {} alertes non traitées", alertes.size());
        
        for (Alerte alerte : alertes) {
            log.info("Alerte à diffuser: {} - {}", alerte.getType(), alerte.getMessage());
        }
    }
}
