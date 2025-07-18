package io.documentprocessing.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import io.documentprocessing.model.SearchResult;
import io.documentprocessing.model.User;
import io.documentprocessing.repository.DocumentMetadataRepository;
import io.documentprocessing.repository.UserRepository;
import io.documentprocessing.service.DocumentService;
import io.documentprocessing.service.LuceneService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMetadataRepository metadataRepository;
    private final UserRepository userRepository;
    private final LuceneService luceneService;
    private final AuthController authController;

    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    private static final long MULTIPART_UPLOAD_THRESHOLD = 10L * 1024 * 1024; //100MB limit-(Change to 100mb later)
    private static final String CORS_ORIGIN_URL = "http://localhost:5173";

    public DocumentController(DocumentService documentService, DocumentMetadataRepository metadataRepository, UserRepository userRepository, 
    		LuceneService luceneService, AuthController authController) {
        this.documentService = documentService;
        this.metadataRepository = metadataRepository;
        this.userRepository = userRepository;
        this.luceneService = luceneService;
        this.authController = authController;
    }
    

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file,@RequestParam("name") String name,
    		@RequestParam("type") String type,@RequestParam("userId") Long userId) throws IOException, InterruptedException {

    	 if (file.isEmpty()) {
    		 throw new IllegalArgumentException("File cannot be empty.");
    	 }

    	 User user = userRepository.findById(userId).orElse(null);

    	 if (user == null) {
    		 throw new IllegalArgumentException("Invalid user ID.");
    	 }
    	    
    	 String finalName = documentService.getAvailableFileName(user.getId(), name);
    	 Document savedDocument;

    	 if (file.getSize() > MULTIPART_UPLOAD_THRESHOLD) { // Limit: 100MB
    	        // Multipart upload for large file
    	    savedDocument = documentService.saveLargeDocument(file, finalName, type, user);
    	 } else {
    	        // Simple upload for small file
    	    savedDocument = documentService.saveDocument(file, finalName, type, user);
    	  }
    	
    	 System.out.println("Saving file as: " + finalName);

    	 if (!type.equalsIgnoreCase("application/pdf")) {
    		    String extractedText = metadataRepository.findByDocumentId(savedDocument.getId())
    		        .map(meta -> meta.getExtractedText())
    		        .orElse("");

    		    if (!extractedText.isBlank()) {
    		        luceneService.indexDocument(
    		            savedDocument.getId().toString(),
    		            savedDocument.getName(),
    		            savedDocument.getType(),
    		            extractedText,
    		            userId.toString()
    		        );
    		    } else {
    		        System.out.println("Skipping Lucene indexing - extracted text not available yet.");
    		    }
    		}


    	  return ResponseEntity.ok(savedDocument);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable("id") Long id) {

        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok) 
                .orElseGet(() -> ResponseEntity.notFound().build()); 
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


	
    @GetMapping("/list") 
    public ResponseEntity<?> getUserDocuments(

    @RequestParam(name = "userId") Long userId,

    @RequestParam(name = "page", defaultValue = "0") int page,

    @RequestParam(name = "size", defaultValue = "10") int size,

    @RequestParam(name = "type", required = false) String type) {

    	if (userId == null) { return ResponseEntity.status(403).body("Forbidden"); }
    	Map<String, String> mimeTypes = Map.of( "pdf", "application/pdf", "docx",
    			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    			"image", "image/png" );

    	Page<DocumentMetadata> metadataPage;

    	if (type == null || type.equalsIgnoreCase("all")) { metadataPage =
    			metadataRepository.findByDocumentOwnerId( userId, PageRequest.of(page, size,
    					Sort.by("uploadTimestamp").descending())); } else { String mappedType =
    					mimeTypes.getOrDefault(type.toLowerCase(), type); // fallback to raw type
    					metadataPage = metadataRepository.findByDocumentOwnerIdAndType( userId,
    							mappedType, PageRequest.of(page, size,
    									Sort.by("uploadTimestamp").descending())); }

    	List<Map<String, Object>> responseDocs =
    			metadataPage.getContent().stream().map(meta -> { Document doc =
    			meta.getDocument(); Map<String, Object> map = new HashMap<>(); map.put("id",
    					doc.getId()); map.put("name", doc.getName()); map.put("type", doc.getType());
    					map.put("status", meta.getStatus()); map.put("uploadTimestamp",
    							meta.getUploadTimestamp());

    					Map<String, Object> ownerMap = new HashMap<>(); ownerMap.put("id",
    							doc.getOwner().getId()); ownerMap.put("username",
    									doc.getOwner().getUsername()); map.put("owner", ownerMap);

    									return map; }).collect(Collectors.toList());

    	Map<String, Object> response = new HashMap<>(); response.put("documents",
    			responseDocs); response.put("currentPage", page); response.put("totalPages",
    					metadataPage.getTotalPages()); response.put("totalDocuments",
    							metadataPage.getTotalElements());

    					return ResponseEntity.ok(response); 
    }

    //View extracted text
    @GetMapping("/extracted-text/{id}")
    public ResponseEntity<String> getExtractedText(@PathVariable("id") Long documentId) {
        return metadataRepository.findByDocumentId(documentId).map(metadata -> ResponseEntity.ok(metadata.getExtractedText()))
        		.orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDocuments(
            @RequestParam("q") String q,
            @RequestParam("userId") Long userId) throws Exception {
        
        //List<DocumentMetadata> results = documentService.searchDocuments(q, userId);
    	List<SearchResult> results = luceneService.fuzzySearch(q, userId.toString());
    	 //List<SearchResult> results = luceneService.searchByName(q, userId.toString());
    	 
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

    public void updateMetadataAfterLambda(Document document, String extractedText) {
        Optional<DocumentMetadata> metadataOpt = metadataRepository.findById(document.getId());
        if (metadataOpt.isPresent()) {
            DocumentMetadata metadata = metadataOpt.get();
            metadata.setExtractedText(extractedText);
            metadataRepository.save(metadata);

            try {
                luceneService.indexDocument(
                    document.getId().toString(),
                    document.getName(),
                    document.getType(),
                    extractedText,
                    document.getOwner().getId().toString()
                );
 
            } catch (IOException e) {
                System.err.println("Failed to index document in Lucene: " + e.getMessage());
            }
        } else {
            System.err.println("Metadata not found for document ID " + document.getId());
        }
    }
    
    @GetMapping("/metadata/{id}")
    public ResponseEntity<?> getDocumentMetadata(@PathVariable("id") Long id) {
        return metadataRepository.findByDocumentId(id).map(metadata -> {
            Map<String, Object> meta = new HashMap<>();
            meta.put("name", metadata.getDocument().getName());
            meta.put("type", metadata.getDocument().getType());
            meta.put("status", metadata.getStatus());
            meta.put("uploadedOn", metadata.getUploadTimestamp());
            meta.put("uploadedBy", metadata.getDocument().getOwner().getUsername());
            return ResponseEntity.ok(meta);
        }).orElse(ResponseEntity.notFound().build());
    }  
}


