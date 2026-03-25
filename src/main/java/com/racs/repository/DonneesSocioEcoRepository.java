package com.racs.repository;

import com.racs.entity.DonneesSocioEco;
import com.racs.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonneesSocioEcoRepository extends JpaRepository<DonneesSocioEco, Integer> {
    List<DonneesSocioEco> findByFid(String fid);
    List<DonneesSocioEco> findBySource(Source source);
    
    boolean existsByFidAndSourceIdAndDateValeur(String fid, Integer sourceId, LocalDate dateValeur);
    
    @Query("SELECT d FROM DonneesSocioEco d WHERE d.fid = :fid AND d.source.id = :sourceId ORDER BY d.version DESC")
    List<DonneesSocioEco> findByFidAndSourceIdOrderByVersionDesc(String fid, Integer sourceId);
    
    @Query("SELECT d FROM DonneesSocioEco d WHERE d.fid = :fid AND d.source.id = :sourceId AND d.version = (SELECT MAX(d2.version) FROM DonneesSocioEco d2 WHERE d2.fid = :fid AND d2.source.id = :sourceId)")
    Optional<DonneesSocioEco> findLatestByFidAndSourceId(String fid, Integer sourceId);
    
    @Query("SELECT d FROM DonneesSocioEco d LEFT JOIN FETCH d.attributs WHERE d.fid = :fid AND d.source.id = :sourceId AND d.dateValeur = :dateValeur")
    Optional<DonneesSocioEco> findByFidAndSourceIdAndDateValeurWithAttributs(@Param("fid") String fid, @Param("sourceId") Integer sourceId, @Param("dateValeur") LocalDate dateValeur);
    
    Optional<DonneesSocioEco> findByFidAndSourceIdAndDateValeur(String fid, Integer sourceId, LocalDate dateValeur);
}
