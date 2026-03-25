package com.racs.repository;

import com.racs.entity.Lot;
import com.racs.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LotRepository extends JpaRepository<Lot, Integer> {
    Optional<Lot> findByReferenceLot(String referenceLot);
    List<Lot> findBySource(Source source);
    List<Lot> findByStatut(String statut);
}
