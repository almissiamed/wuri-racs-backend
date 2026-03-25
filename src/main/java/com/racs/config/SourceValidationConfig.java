package com.racs.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Getter
public class SourceValidationConfig {

    private static final Map<String, Map<String, String>> TYPES_CHAMPS = Map.of(
        "CNSS", Map.of(
            "salaire_base", "NUMERIC",
            "prime", "NUMERIC",
            "devise", "STRING",
            "employeur", "STRING",
            "secteur", "STRING",
            "anciennete", "NUMERIC",
            "statut", "STRING"
        ),
        "CARFO", Map.of(
            "montant_mensuel", "NUMERIC",
            "type_pension", "STRING",
            "date_effet", "DATE",
            "indice", "NUMERIC",
            "ayants_droit", "NUMERIC"
        ),
        "SONABEL", Map.of(
            "consommation_mensuelle", "NUMERIC",
            "unite", "STRING",
            "puissance_souscrite", "NUMERIC",
            "facture_moyenne", "NUMERIC",
            "statut_paiement", "STRING",
            "compteur", "STRING"
        ),
        "ONEA", Map.of(
            "consommation_mensuelle", "NUMERIC",
            "unite", "STRING",
            "facture_moyenne", "NUMERIC",
            "statut_paiement", "STRING",
            "mois_impaye", "NUMERIC"
        ),
        "DGTTM", Map.of(
            "vehicules", "LIST",
            "permis_categories", "LIST",
            "carte_transporteur", "STRING"
        )
    );

    public List<String> getChampsObligatoires(String sourceNom) {
        Map<String, String> champs = TYPES_CHAMPS.get(sourceNom.toUpperCase());
        return champs != null ? List.copyOf(champs.keySet()) : List.of();
    }

    public Map<String, String> getTypesChamps(String sourceNom) {
        return TYPES_CHAMPS.getOrDefault(sourceNom.toUpperCase(), Map.of());
    }

    public boolean isSourceConnue(String sourceNom) {
        return TYPES_CHAMPS.containsKey(sourceNom.toUpperCase());
    }

    public List<String> getToutesLesSources() {
        return List.copyOf(TYPES_CHAMPS.keySet());
    }
}
