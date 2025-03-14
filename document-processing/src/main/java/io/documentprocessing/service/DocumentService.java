package io.documentprocessing.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public DocumentService(DocumentRepository documentRepository, DocumentMetadataRepository documentMetadataRepository) {
        this.documentRepository = documentRepository;
        this.documentMetadataRepository = documentMetadataRepository;
    }

    public Document saveDocument(MultipartFile file, String name, String type) throws IOException {
    	  if (file.isEmpty()) {
    	        throw new IOException("File is empty, cannot save.");
    	    }

    	    // Save Document
    	    Document document = new Document();
    	    document.setName(name);
    	    document.setType(type);
    	    document.setData(file.getBytes());
    	    Document savedDocument = documentRepository.save(document);

    	    // Save Metadata 
    	    DocumentMetadata metadata = new DocumentMetadata();
    	    metadata.setDocument(savedDocument); 
    	    metadata.setUploadTimestamp(LocalDateTime.now());
    	    documentMetadataRepository.save(metadata);

    	    return savedDocument;
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}
