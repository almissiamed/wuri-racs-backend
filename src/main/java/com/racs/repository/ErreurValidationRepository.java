package com.racs.repository;

import com.racs.entity.ErreurValidation;
import com.racs.entity.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ErreurValidationRepository extends JpaRepository<ErreurValidation, Integer> {
    List<ErreurValidation> findByValidation(Validation validation);
}
