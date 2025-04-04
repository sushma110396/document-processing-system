package io.documentprocessing.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.documentprocessing.model.DocumentMetadata;

@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {
    
    // Find metadata by status
    List<DocumentMetadata> findByStatus(String status);

    // Find documents uploaded after a certain date
    List<DocumentMetadata> findByUploadTimestampAfter(LocalDateTime timestamp);
    
    Optional<DocumentMetadata> findByDocumentId(Long documentId);
    
    // Search in both document name and extracted text
    @Query("SELECT m FROM DocumentMetadata m " +
    	       "JOIN m.document d " +
    	       "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
    	       "OR LOWER(m.extractedText) LIKE LOWER(CONCAT('%', :query, '%'))")
    	List<DocumentMetadata> searchByNameOrText(@Param("query") String query);

}
