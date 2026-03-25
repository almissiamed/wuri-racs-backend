package com.racs.controller;

import com.racs.dto.*;
import com.racs.entity.*;
import com.racs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donnees")
@RequiredArgsConstructor
public class DonneesSocioEcoController {

    private final DonneesSocioEcoRepository donneesSocioEcoRepository;
    private final DonneesBrutesRepository donneesBrutesRepository;
    private final LotRepository lotRepository;
    private final AlerteRepository alerteRepository;

    @GetMapping
    public ResponseEntity<List<DonneesSocioEco>> getAllDonnees() {
        return ResponseEntity.ok(donneesSocioEcoRepository.findAll());
    }

    @GetMapping("/fid/{fid}")
    public ResponseEntity<List<DonneesSocioEco>> getDonneesByFid(@PathVariable String fid) {
        return ResponseEntity.ok(donneesSocioEcoRepository.findByFid(fid));
    }

    @GetMapping("/brutes")
    public ResponseEntity<List<DonneesBrutes>> getDonneesBrutes() {
        return ResponseEntity.ok(donneesBrutesRepository.findAll());
    }

    @GetMapping("/lots")
    public ResponseEntity<List<Lot>> getLots() {
        return ResponseEntity.ok(lotRepository.findAll());
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<Alerte>> getAlertes() {
        return ResponseEntity.ok(alerteRepository.findAll());
    }
}
