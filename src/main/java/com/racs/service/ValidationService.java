package com.racs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.racs.config.SourceValidationConfig;
import com.racs.entity.*;
import com.racs.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final ValidationRepository validationRepository;
    private final ErreurValidationRepository erreurValidationRepository;
    private final ObjectMapper objectMapper;
    private final SourceValidationConfig validationConfig;

    @Transactional
    public boolean validerDonnees(DonneesBrutes donneesBrutes, Source source) {
        log.debug("Validation des données pour fID: {} - Source: {}", donneesBrutes.getFid(), source.getNom());
        
        List<ErreurValidation> erreurs = new ArrayList<>();
        List<String> rapportDetails = new ArrayList<>();
        
        List<String> champsObligatoires = getChampsObligatoires(source);
        Map<String, String> typesChamps = getTypesChamps(source);
        
        if (champsObligatoires.isEmpty() && typesChamps.isEmpty()) {
            log.warn("Configuration de validation non trouvée pour la source: {}", source.getNom());
            erreurs.add(ErreurValidation.builder()
                    .validation(null)
                    .champ("source")
                    .codeErreur("SOURCE_INCONNUE")
                    .message("Configuration de validation non définie pour la source: " + source.getNom())
                    .niveau("WARNING")
                    .build());
            rapportDetails.add("Source inconnue: " + source.getNom());
        } else {
            Map<String, Object> donnees = parserPayload(donneesBrutes.getPayload());
            
            if (donnees == null) {
                erreurs.add(ErreurValidation.builder()
                        .validation(null)
                        .champ("payload")
                        .codeErreur("FORMAT_INVALIDE")
                        .message("Le format des données n'est pas un JSON valide")
                        .niveau("ERROR")
                        .build());
                rapportDetails.add("Format JSON invalide");
            } else {
                List<ErreurValidation> erreursFormat = validerFormat(donnees, typesChamps);
                List<ErreurValidation> erreursChamps = validerChampsObligatoires(donnees, champsObligatoires);
                
                erreurs.addAll(erreursFormat);
                erreurs.addAll(erreursChamps);
                
                for (ErreurValidation err : erreursFormat) {
                    rapportDetails.add("Format: " + err.getMessage());
                }
                for (ErreurValidation err : erreursChamps) {
                    rapportDetails.add("Champ: " + err.getMessage());
                }
            }
        }
        
        boolean formatValide = erreurs.stream().noneMatch(e -> e.getCodeErreur().equals("FORMAT_INVALIDE"));
        boolean champsValides = erreurs.stream().noneMatch(e -> e.getCodeErreur().equals("CHAMPS_OBLIGATOIRES_MANQUANTS"));
        
        Validation validation = Validation.builder()
                .donneesBrutes(donneesBrutes)
                .formatValide(formatValide)
                .champsObligatoiresValides(champsValides)
                .rapport(genererRapport(formatValide, champsValides, rapportDetails))
                .build();
        
        validation = validationRepository.save(validation);
        
        for (ErreurValidation erreur : erreurs) {
            erreur.setValidation(validation);
            erreurValidationRepository.save(erreur);
        }
        
        boolean resultat = formatValide && champsValides;
        log.debug("Validation terminée pour fID: {} - Résultat: {}", donneesBrutes.getFid(), resultat);
        
        return resultat;
    }

    private List<String> getChampsObligatoires(Source source) {
        if (source.getChampsObligatoiresJson() != null && !source.getChampsObligatoiresJson().isEmpty()) {
            try {
                return objectMapper.readValue(source.getChampsObligatoiresJson(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                log.warn("Impossible de parser champsObligatoiresJson pour {}: {}", source.getNom(), e.getMessage());
            }
        }
        return validationConfig.getChampsObligatoires(source.getNom());
    }

    private Map<String, String> getTypesChamps(Source source) {
        if (source.getTypesChampsJson() != null && !source.getTypesChampsJson().isEmpty()) {
            try {
                return objectMapper.readValue(source.getTypesChampsJson(), new TypeReference<Map<String, String>>() {});
            } catch (JsonProcessingException e) {
                log.warn("Impossible de parser typesChampsJson pour {}: {}", source.getNom(), e.getMessage());
            }
        }
        return validationConfig.getTypesChamps(source.getNom());
    }

    private Map<String, Object> parserPayload(String payload) {
        try {
            return objectMapper.readValue(payload, Map.class);
        } catch (JsonProcessingException e) {
            log.warn("Impossible de parser le payload: {}", e.getMessage());
            return null;
        }
    }

    private List<ErreurValidation> validerFormat(Map<String, Object> donnees, Map<String, String> typesAttendus) {
        List<ErreurValidation> erreurs = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : typesAttendus.entrySet()) {
            String champ = entry.getKey();
            String typeAttendu = entry.getValue();
            
            if (donnees.containsKey(champ)) {
                Object valeur = donnees.get(champ);
                String erreurType = validerType(valeur, typeAttendu, champ);
                
                if (erreurType != null) {
                    erreurs.add(ErreurValidation.builder()
                            .validation(null)
                            .champ(champ)
                            .codeErreur("FORMAT_INVALIDE")
                            .message(erreurType)
                            .niveau("ERROR")
                            .build());
                }
            }
        }
        
        return erreurs;
    }

    private String validerType(Object valeur, String typeAttendu, String champ) {
        if (valeur == null) {
            return null;
        }
        
        return switch (typeAttendu) {
            case "STRING" -> validerString(valeur, champ);
            case "NUMERIC" -> validerNumeric(valeur, champ);
            case "DATE" -> validerDateType(valeur, champ);
            case "BOOLEAN" -> validerBoolean(valeur, champ);
            case "LIST" -> validerList(valeur, champ);
            default -> null;
        };
    }

    private String validerString(Object valeur, String champ) {
        if (valeur instanceof String) {
            return null;
        }
        return "Le champ '" + champ + "' doit être une chaîne de caractères";
    }

    private String validerNumeric(Object valeur, String champ) {
        if (valeur instanceof Number) {
            return null;
        }
        if (valeur instanceof String) {
            try {
                new BigDecimal((String) valeur);
                return null;
            } catch (NumberFormatException e) {
                return "Le champ '" + champ + "' doit être une valeur numérique";
            }
        }
        return "Le champ '" + champ + "' doit être une valeur numérique";
    }

    private String validerDateType(Object valeur, String champ) {
        if (!validerDate(valeur)) {
            return "Le champ '" + champ + "' doit être une date valide (format: YYYY-MM-DD)";
        }
        return null;
    }

    private String validerBoolean(Object valeur, String champ) {
        if (valeur instanceof Boolean) {
            return null;
        }
        if (valeur instanceof String) {
            String val = ((String) valeur).toLowerCase();
            if (val.equals("true") || val.equals("false")) {
                return null;
            }
        }
        return "Le champ '" + champ + "' doit être un booléen";
    }

    private String validerList(Object valeur, String champ) {
        if (valeur instanceof List) {
            return null;
        }
        return "Le champ '" + champ + "' doit être une liste";
    }

    private boolean validerDate(Object valeur) {
        if (valeur == null) return false;
        
        String dateStr = valeur instanceof String ? (String) valeur : valeur.toString();
        
        String[] formats = {"yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd", "dd-MM-yyyy"};
        
        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDate.parse(dateStr, formatter);
                return true;
            } catch (DateTimeParseException ignored) {
            }
        }
        return false;
    }

    private List<ErreurValidation> validerChampsObligatoires(Map<String, Object> donnees, List<String> champsObligatoires) {
        List<ErreurValidation> erreurs = new ArrayList<>();
        
        for (String champ : champsObligatoires) {
            if (!donnees.containsKey(champ)) {
                erreurs.add(ErreurValidation.builder()
                        .validation(null)
                        .champ(champ)
                        .codeErreur("CHAMPS_OBLIGATOIRES_MANQUANTS")
                        .message("Le champ obligatoire '" + champ + "' est absent")
                        .niveau("ERROR")
                        .build());
            } else {
                Object valeur = donnees.get(champ);
                if (valeur == null) {
                    erreurs.add(ErreurValidation.builder()
                            .validation(null)
                            .champ(champ)
                            .codeErreur("CHAMPS_OBLIGATOIRES_MANQUANTS")
                            .message("Le champ obligatoire '" + champ + "' est null")
                            .niveau("ERROR")
                            .build());
                }
            }
        }
        
        return erreurs;
    }

    private String genererRapport(boolean formatValide, boolean champsValides, List<String> details) {
        StringBuilder rapport = new StringBuilder();
        rapport.append("Validation: ");
        
        if (formatValide && champsValides) {
            rapport.append("OK - Toutes les données sont valides");
        } else {
            rapport.append("ECHEC");
            List<String> problems = new ArrayList<>();
            if (!formatValide) {
                problems.add("Format invalide");
            }
            if (!champsValides) {
                problems.add("Champs obligatoires manquants");
            }
            rapport.append(" (").append(String.join(", ", problems)).append(")");
        }
        
        if (!details.isEmpty()) {
            rapport.append("\nDétails: ").append(String.join("; ", details));
        }
        
        return rapport.toString();
    }
}
