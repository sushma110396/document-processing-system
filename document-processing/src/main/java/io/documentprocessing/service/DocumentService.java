package io.documentprocessing.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.documentprocessing.model.Document;
import io.documentprocessing.model.DocumentMetadata;
import io.documentprocessing.repository.DocumentMetadataRepository;
import io.documentprocessing.repository.DocumentRepository;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMetadataRepository documentMetadataRepository;
    private final TextExtractionService textExtractionService;
    private final S3StorageService s3StorageService;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, DocumentMetadataRepository documentMetadataRepository, 
    		TextExtractionService textExtractionService, S3StorageService s3StorageService ) {
        this.documentRepository = documentRepository;
        this.documentMetadataRepository = documentMetadataRepository;
        this.textExtractionService = textExtractionService;
        this.s3StorageService = s3StorageService;
    }

	/*
	 * public Document saveDocument(MultipartFile file, String name, String type)
	 * throws IOException { if (file.isEmpty()) { throw new
	 * IOException("File is empty, cannot save."); }
	 * 
	 * // Save Document Document document = new Document(); document.setName(name);
	 * document.setType(type); document.setData(file.getBytes()); Document
	 * savedDocument = documentRepository.save(document);
	 * 
	 * // Save Metadata DocumentMetadata metadata = new DocumentMetadata();
	 * metadata.setDocument(savedDocument);
	 * metadata.setUploadTimestamp(LocalDateTime.now());
	 * documentMetadataRepository.save(metadata);
	 * 
	 * //Start document processing asynchronously
	 * textExtractionService.processDocument(savedDocument, metadata); return
	 * savedDocument; }
	 */
    
    public Document saveDocument(MultipartFile file, String name, String type) throws IOException {
        if (file.isEmpty()) throw new IOException("File is empty.");

        String s3Key;
        try {
            s3Key = s3StorageService.uploadFile(file); // Upload to S3
        } catch (Exception e) {
            throw new IOException("S3 upload failed", e);
        }

        Document document = new Document();
        document.setName(name);
        document.setType(type);
        document.setS3Key(s3Key); // New field to store key instead of binary
        return documentRepository.save(document);
    }


    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }


    public List<Document> getAllDocuments() {
        return documentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }


    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
    
    //Download doc from S3 service
    public byte[] downloadDocument(Long id) throws IOException {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return s3StorageService.downloadFile(doc.getS3Key());
    }

}
