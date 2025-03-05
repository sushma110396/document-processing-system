package io.documentprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.documentprocessing.model.Document;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByType(String type); 
}

