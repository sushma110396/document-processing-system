package io.documentprocessing.controller;

import java.io.IOException;
import java.util.ArrayList;
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
import io.documentprocessing.model.DocumentMetadata;
import io.documentprocessing.repository.DocumentMetadataRepository;
import io.documentprocessing.service.DocumentService;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMetadataRepository metadataRepository;

    public DocumentController(DocumentService documentService, DocumentMetadataRepository metadataRepository) {
        this.documentService = documentService;
        this.metadataRepository = metadataRepository;
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

    	    /*if (!type.equals("application/pdf") && !type.equals("application/msword")) {
    	        throw new IllegalArgumentException("Only PDF and Word documents are allowed.");
    	    }*/

    	    Document savedDocument = documentService.saveDocument(file, name, type);
    	    return ResponseEntity.ok(savedDocument);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable("id") Long id) {
        System.out.println("Fetching document with ID: " + id);

        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok) // If present, return 200 OK
                .orElseGet(() -> ResponseEntity.notFound().build()); // If not found, return 404
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable("id") Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable("id") Long id) throws IOException {
    	Document document = documentService.getDocumentById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        byte[] fileBytes = documentService.downloadDocument(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                .contentType(MediaType.parseMediaType(document.getType()))
                .body(fileBytes);
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

    // Helps users know which files exist before downloading
    @GetMapping("/list")
    public ResponseEntity<List<Document>> listDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/extracted-text/{id}")
    public ResponseEntity<String> getExtractedText(@PathVariable("id") Long documentId) {
        return metadataRepository.findByDocumentId(documentId)
                .map(metadata -> ResponseEntity.ok(metadata.getExtractedText()))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/upload/bulk")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> success = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String name = file.getOriginalFilename();
                String type = file.getContentType();
                documentService.saveDocument(file, name, type);
                success.add(name);
            } catch (Exception e) {
                failed.add(file.getOriginalFilename() + " - " + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("successfulUploads", success);
        response.put("failedUploads", failed);
        response.put("totalFiles", files.length);

        return ResponseEntity.ok(response);
    }
	/* Needs to be corrected, holding it for now, check if time permits
	 * @GetMapping("/download/all") public ResponseEntity<byte[]>
	 * downloadAllDocuments() throws IOException { List<Document> documents =
	 * documentService.getAllDocuments();
	 * 
	 * ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream
	 * zipOut = new ZipOutputStream(baos);
	 * 
	 * for (Document doc : documents) { ZipEntry entry = new
	 * ZipEntry(doc.getName()); zipOut.putNextEntry(entry);
	 * zipOut.write(doc.getData()); zipOut.closeEntry(); }
	 * 
	 * zipOut.close();
	 * 
	 * return ResponseEntity.ok() .header(HttpHeaders.CONTENT_DISPOSITION,
	 * "attachment; filename=\"all_documents.zip\"")
	 * .contentType(MediaType.APPLICATION_OCTET_STREAM) .body(baos.toByteArray()); }
	 */
    @GetMapping("/search")
    public ResponseEntity<List<DocumentMetadata>> searchDocuments(@RequestParam("q") String query) {
        List<DocumentMetadata> results = documentService.searchDocuments(query);
        return ResponseEntity.ok(results);
    }


}
