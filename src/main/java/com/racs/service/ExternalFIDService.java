package com.racs.service;

import com.racs.dto.FIDDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class ExternalFIDService {

    private final RestTemplate restTemplate;
    
    @Value("${external.rsu.base-url:http://localhost:8082}")
    private String rsuBaseUrl;

    public ExternalFIDService() {
        this.restTemplate = new RestTemplate();
    }

    public List<FIDDTO> getFIDsActifs() {
        try {
            String url = rsuBaseUrl + "/api/fids/actifs";
            log.info("Récupération des fIDs actifs depuis: {}", url);
            FIDDTO[] fids = restTemplate.getForObject(url, FIDDTO[].class);
            return List.of(fids != null ? fids : new FIDDTO[0]);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des fIDs depuis RSU: {}", e.getMessage());
            return List.of();
        }
    }

    public List<FIDDTO> getAllFIDs() {
        try {
            String url = rsuBaseUrl + "/api/fids";
            log.info("Récupération de tous les fIDs depuis: {}", url);
            FIDDTO[] fids = restTemplate.getForObject(url, FIDDTO[].class);
            return List.of(fids != null ? fids : new FIDDTO[0]);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des fIDs depuis RSU: {}", e.getMessage());
            return List.of();
        }
    }
}
