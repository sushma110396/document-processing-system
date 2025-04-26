package io.documentprocessing.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.documentprocessing.model.Document;
import io.documentprocessing.model.DocumentMetadata;
import io.documentprocessing.model.User;
import io.documentprocessing.repository.DocumentMetadataRepository;
import io.documentprocessing.repository.DocumentRepository;
import io.documentprocessing.repository.UserRepository;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMetadataRepository documentMetadataRepository;
    
    //For docx or txt files text extraction
    private final TextExtractionService textExtractionService;
    
    //For pdf, jpeg and png files text extraction
    private final LambdaService lambdaService;
    private final S3StorageService s3StorageService;

    @Value("${aws.s3.bucket}")
    private String bucket;
    
    @Autowired
    public DocumentService(DocumentRepository documentRepository, DocumentMetadataRepository documentMetadataRepository, 
    		LambdaService lambdaService, S3StorageService s3StorageService, TextExtractionService textExtractionService) {
        this.documentRepository = documentRepository;
        this.documentMetadataRepository = documentMetadataRepository;
        this.lambdaService = lambdaService;
        this.s3StorageService = s3StorageService;
        this.textExtractionService = textExtractionService;
    }

	
    
    public Document saveDocument(MultipartFile file, String name, String type, User currentUser) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty.");
        }

        // Upload to S3 and get the object key
        String s3Key;
        try {
            s3Key = s3StorageService.uploadFile(file);
        } catch (Exception e) {
            throw new IOException("S3 upload failed", e);
        }

        // Save document info to DB
        Document document = new Document();
        document.setName(file.getOriginalFilename()); 
        document.setType(type);
        document.setS3Key(s3Key);
        document.setOwner(currentUser);
        Document savedDocument = documentRepository.save(document);

        // Save metadata
        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setDocument(savedDocument);
        metadata.setStatus("Pending");
        metadata.setUploadTimestamp(LocalDateTime.now());
        documentMetadataRepository.save(metadata);

        // Trigger text extraction asynchronously        
        if (type.equals("application/pdf") || type.equals("image/png") || type.equals("image/jpeg")) {
        	String lambdaResult = lambdaService.triggerTextExtraction(bucket, s3Key, metadata);
        	System.out.println("Lambda response: " + lambdaResult);
        } else {
        	textExtractionService.processDocument(savedDocument, metadata); 
        }

        return savedDocument;
    }



    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }


    public List<Document> getAllDocuments() {
        return documentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }


    public void deleteDocument(Long id, User currentUser) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOwner().getId().equals(currentUser.getId())) {
            throw new SecurityException("You are not authorized to delete this document.");
        }

        documentRepository.delete(document);
    }

    
    //Download doc from S3 service
    public byte[] downloadDocument(Long id) throws IOException {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return s3StorageService.downloadFile(doc.getS3Key());
    }
    
    public List<DocumentMetadata> searchDocuments(String query, Long userId) {
        return documentMetadataRepository.searchByNameOrText(query, userId);
    }


}
