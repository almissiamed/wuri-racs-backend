package com.racs.repository;

import com.racs.entity.FID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FIDRepository extends JpaRepository<FID, String> {
    @Query("SELECT f FROM FID f WHERE f.statut = 'actif'")
    List<FID> findAllActive();
    
    List<FID> findByStatut(String statut);
}
