package com.racs.controller;

import com.racs.entity.*;
import com.racs.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/synchronisation")
@RequiredArgsConstructor
public class SynchronisationController {

    private final SynchronisationService synchronisationService;
    private final AlerteService alerteService;

    @PostMapping("/demarrer")
    public ResponseEntity<String> demarrerSynchronisation() {
        synchronisationService.demarrerSynchronisation();
        return ResponseEntity.ok("Synchronisation démarrée");
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<Alerte>> getAlertes() {
        return ResponseEntity.ok(alerteService.getAlertesNonTraitees());
    }

    @GetMapping("/alertes/fid/{fid}")
    public ResponseEntity<List<Alerte>> getAlertesParFID(@PathVariable String fid) {
        return ResponseEntity.ok(alerteService.getAlertesParFID(fid));
    }

    @GetMapping("/alertes/type/{type}")
    public ResponseEntity<List<Alerte>> getAlertesParType(@PathVariable String type) {
        return ResponseEntity.ok(alerteService.getAlertesParType(type));
    }

    @PostMapping("/alertes/{id}/traiter")
    public ResponseEntity<String> traiterAlerte(@PathVariable Integer id) {
        alerteService.traiterAlerte(id);
        return ResponseEntity.ok("Alerte traitée");
    }

    @PostMapping("/alertes/diffuser")
    public ResponseEntity<String> diffuserAlertes() {
        alerteService.diffuserAlertes();
        return ResponseEntity.ok("Alertes diffusées");
    }
}
