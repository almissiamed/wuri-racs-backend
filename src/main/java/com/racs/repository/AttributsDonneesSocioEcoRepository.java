package com.racs.repository;

import com.racs.entity.AttributsDonneesSocioEco;
import com.racs.entity.DonneesSocioEco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttributsDonneesSocioEcoRepository extends JpaRepository<AttributsDonneesSocioEco, Integer> {
    List<AttributsDonneesSocioEco> findByDonneesSocioEco(DonneesSocioEco donneesSocioEco);
    List<AttributsDonneesSocioEco> findByDonneesSocioEcoId(Integer donneesSocioEcoId);
}
