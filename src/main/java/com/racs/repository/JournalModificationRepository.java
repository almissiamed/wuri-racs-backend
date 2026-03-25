package com.racs.repository;

import com.racs.entity.JournalModification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JournalModificationRepository extends JpaRepository<JournalModification, Integer> {
    List<JournalModification> findByDonneesSocioEcoIdOrderByDateActionDesc(Integer donneesSocioEcoId);
    List<JournalModification> findByAttributIdOrderByDateActionDesc(Integer attributId);
}
