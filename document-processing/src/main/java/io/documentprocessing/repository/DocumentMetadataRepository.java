package io.documentprocessing.repository;

import io.documentprocessing.model.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {
    
    // Find metadata by status
    List<DocumentMetadata> findByStatus(String status);

    // Find documents uploaded after a certain date
    List<DocumentMetadata> findByUploadTimestampAfter(LocalDateTime timestamp);
}
