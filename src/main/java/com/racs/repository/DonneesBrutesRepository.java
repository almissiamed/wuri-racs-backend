package com.racs.repository;

import com.racs.entity.DonneesBrutes;
import com.racs.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DonneesBrutesRepository extends JpaRepository<DonneesBrutes, Integer> {
    List<DonneesBrutes> findByLot(Lot lot);
    List<DonneesBrutes> findByStatut(String statut);
    List<DonneesBrutes> findByFid(String fid);
}
