package com.racs.repository;

import com.racs.entity.Validation;
import com.racs.entity.DonneesBrutes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, Integer> {
    Optional<Validation> findByDonneesBrutes(DonneesBrutes donneesBrutes);
}
