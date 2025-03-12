package io.documentprocessing.controller;

import io.documentprocessing.model.Document;
import io.documentprocessing.service.DocumentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("type") String type) throws IOException {

    	 if (file.isEmpty()) {
    	        throw new IllegalArgumentException("File cannot be empty.");
    	    }

    	    if (file.getSize() > 5_000_000) { // Limit: 5MB
    	        throw new IllegalArgumentException("File size exceeds the limit (5MB).");
    	    }

    	    if (!type.equals("application/pdf") && !type.equals("application/msword")) {
    	        throw new IllegalArgumentException("Only PDF and Word documents are allowed.");
    	    }

    	    Document savedDocument = documentService.saveDocument(file, name, type);
    	    return ResponseEntity.ok(savedDocument);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable("id") Long id) {
    	 System.out.println("Fetching document with ID: " + id);
    	 
        Document document = documentService.getDocumentById(id);
        
        if (document == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(document);
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
