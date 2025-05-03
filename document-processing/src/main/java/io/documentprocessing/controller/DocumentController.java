package io.documentprocessing.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import io.documentprocessing.model.User;
import io.documentprocessing.repository.DocumentMetadataRepository;
import io.documentprocessing.repository.UserRepository;
import io.documentprocessing.service.DocumentService;


@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMetadataRepository metadataRepository;
    private final UserRepository userRepository;

    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    private static final long MULTIPART_UPLOAD_THRESHOLD = 10L * 1024 * 1024; //100MB limit-(Change to 100mb later)

    public DocumentController(DocumentService documentService, DocumentMetadataRepository metadataRepository, UserRepository userRepository) {
        this.documentService = documentService;
        this.metadataRepository = metadataRepository;
        this.userRepository = userRepository;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file,@RequestParam("name") String name,
    		@RequestParam("type") String type,@RequestParam("userId") Long userId) throws IOException {

    	 if (file.isEmpty()) {
    		 throw new IllegalArgumentException("File cannot be empty.");
    	 }

    	 User user = userRepository.findById(userId).orElse(null);

    	 if (user == null) {
    		 throw new IllegalArgumentException("Invalid user ID.");
    	 }
    	    
    	 Document savedDocument;

    	 if (file.getSize() > MULTIPART_UPLOAD_THRESHOLD) { // Limit: 100MB
    	        // Multipart upload for large file
    	    savedDocument = documentService.saveLargeDocument(file, name, type, user);
    	 } else {
    	        // Simple upload for small file
    	    savedDocument = documentService.saveDocument(file, name, type, user);
    	  }

    	  return ResponseEntity.ok(savedDocument);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable("id") Long id) {
        System.out.println("Fetching document with ID: " + id);

        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok) 
                .orElseGet(() -> ResponseEntity.notFound().build()); 
    }

    
    // Helps users know which files exist before downloading
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

    
    @GetMapping("/list")
    public ResponseEntity<?> getUserDocuments(@RequestParam("userId") Long userId) {
        List<Document> documents = documentService.getAllDocuments()
                .stream()
                .filter(doc -> doc.getOwner() != null && doc.getOwner().getId().equals(userId))
                .collect(Collectors.toList());

        if (documents.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }


        List<Map<String, Object>> metadataList = documents.stream().map(doc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId());
            map.put("name", doc.getName());
            map.put("type", doc.getType());
            
            if (doc.getOwner() != null) {
                Map<String, Object> ownerMap = new HashMap<>();
                ownerMap.put("id", doc.getOwner().getId());
                ownerMap.put("username", doc.getOwner().getUsername());
                map.put("owner", ownerMap);
            }
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(metadataList);
    }

    @GetMapping("/extracted-text/{id}")
    public ResponseEntity<String> getExtractedText(@PathVariable("id") Long documentId) {
        return metadataRepository.findByDocumentId(documentId)
                .map(metadata -> ResponseEntity.ok(metadata.getExtractedText()))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/upload/bulk")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,  @AuthenticationPrincipal User user, UserRepository userRepository ) {
        List<String> success = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String name = file.getOriginalFilename();
                String type = file.getContentType();
                documentService.saveDocument(file, name, type, user);
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
    public ResponseEntity<?> searchDocuments(
            @RequestParam("q") String q,
            @RequestParam("userId") Long userId) {
        
        List<DocumentMetadata> results = documentService.searchDocuments(q, userId);

        if (results.isEmpty()) {
            return ResponseEntity.ok("No results found.");
        }

        return ResponseEntity.ok(results);
    }




    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable("id") Long id, @RequestParam("userId") Long userId) {
    	 	User user = userRepository.findById(userId).orElse(null);
    		if (user == null) {
    			user = userRepository.findByUsername("testuser"); 
    		}

    		documentService.deleteDocument(id, user);
    		return ResponseEntity.noContent().build();
    }

    
}


