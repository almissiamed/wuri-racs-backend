package com.racs.repository;

import com.racs.entity.ArchiveAttributsDonneesSocioEco;
import com.racs.entity.DonneesSocioEco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArchiveAttributsDonneesSocioEcoRepository extends JpaRepository<ArchiveAttributsDonneesSocioEco, Integer> {
    List<ArchiveAttributsDonneesSocioEco> findByDonneesSocioEco(DonneesSocioEco donneesSocioEco);
}
