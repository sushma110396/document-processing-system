package io.documentprocessing.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.documentprocessing.model.Document;
import io.documentprocessing.service.DocumentService;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable("id") Long id) 
 {
    	Document document = documentService.getDocumentById(id);
        
        if (document == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
            .contentType(MediaType.parseMediaType(document.getType()))
            .body(document.getData());
    }
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();

        //Return only metadata, not binary data
        List<Map<String, Object>> metadataList = documents.stream().map(doc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId());
            map.put("name", doc.getName());
            map.put("type", doc.getType());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(metadataList);
    }


}
