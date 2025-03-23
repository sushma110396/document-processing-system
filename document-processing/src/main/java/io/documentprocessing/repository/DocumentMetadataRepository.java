package io.documentprocessing.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.documentprocessing.model.DocumentMetadata;

@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {
    
    // Find metadata by status
    List<DocumentMetadata> findByStatus(String status);

    // Find documents uploaded after a certain date
    List<DocumentMetadata> findByUploadTimestampAfter(LocalDateTime timestamp);
    
    Optional<DocumentMetadata> findByDocumentId(Long documentId);
}
