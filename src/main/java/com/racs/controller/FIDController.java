package com.racs.controller;

import com.racs.dto.FIDDTO;
import com.racs.service.ExternalFIDService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fids")
@RequiredArgsConstructor
public class FIDController {

    private final ExternalFIDService externalFIDService;

    @GetMapping
    public ResponseEntity<List<FIDDTO>> getAllFIDs() {
        return ResponseEntity.ok(externalFIDService.getAllFIDs());
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<FIDDTO>> getActiveFIDs() {
        return ResponseEntity.ok(externalFIDService.getFIDsActifs());
    }
}
