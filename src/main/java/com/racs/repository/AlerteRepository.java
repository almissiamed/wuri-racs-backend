package com.racs.repository;

import com.racs.entity.Alerte;
import com.racs.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Integer> {
    List<Alerte> findByLot(Lot lot);
    List<Alerte> findByTraiteeFalse();
    List<Alerte> findByTypeAndTraiteeFalse(String type);
}
