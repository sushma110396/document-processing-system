package io.documentprocessing.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.documentprocessing.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByType(String type); 
    
    Page<Document> findByOwnerId(Long userId, Pageable pageable);

}

 