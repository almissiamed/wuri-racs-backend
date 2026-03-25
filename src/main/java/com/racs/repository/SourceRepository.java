package com.racs.repository;

import com.racs.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SourceRepository extends JpaRepository<Source, Integer> {
    Optional<Source> findByNom(String nom);
    List<Source> findByActifTrue();
}
