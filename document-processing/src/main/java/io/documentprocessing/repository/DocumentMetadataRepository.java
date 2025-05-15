package io.documentprocessing.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    	       "WHERE d.owner.id = :userId AND " +
    	       "(LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
    	       "OR LOWER(m.extractedText) LIKE LOWER(CONCAT('%', :query, '%')))")
    	List<DocumentMetadata> searchByNameOrText(@Param("query") String query, @Param("userId") Long userId);

    Page<DocumentMetadata> findByDocumentOwnerId(Long userId, Pageable pageable); // if you want user-specific docs
    
    @Query("SELECT dm FROM DocumentMetadata dm WHERE dm.document.owner.id = :userId")
    List<DocumentMetadata> findByUserId(@Param("userId") Long userId);

    @Query("SELECT dm FROM DocumentMetadata dm WHERE dm.document.owner.id = :userId AND dm.document.type = :type")
    Page<DocumentMetadata> findByDocumentOwnerIdAndType(@Param("userId") Long userId, @Param("type") String type, Pageable pageable);



}
