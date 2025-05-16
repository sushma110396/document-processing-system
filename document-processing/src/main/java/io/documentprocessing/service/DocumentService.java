package io.documentprocessing.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import io.documentprocessing.model.Document;
import io.documentprocessing.model.DocumentMetadata;
import io.documentprocessing.model.User;
import io.documentprocessing.repository.DocumentMetadataRepository;
import io.documentprocessing.repository.DocumentRepository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMetadataRepository documentMetadataRepository;
    
    //For docx or txt files text extraction
    private final TextExtractionService textExtractionService;
    
    //For pdf, jpeg and png files text extraction
    private final LambdaService lambdaService;
    private final LuceneService luceneService;
    private final S3StorageService s3StorageService;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    @Autowired
    public DocumentService(DocumentRepository documentRepository, DocumentMetadataRepository documentMetadataRepository, 
    		LambdaService lambdaService, S3StorageService s3StorageService, TextExtractionService textExtractionService, S3Client s3Client, 
    		LuceneService luceneService) {
        this.documentRepository = documentRepository;
        this.documentMetadataRepository = documentMetadataRepository;
        this.lambdaService = lambdaService;
        this.luceneService = luceneService;
        this.s3StorageService = s3StorageService;
        this.textExtractionService = textExtractionService;
        this.s3Client = s3Client;
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
        	String lambdaResult = lambdaService.triggerTextExtraction(bucketName, s3Key, metadata);
        	System.out.println("Lambda response: " + lambdaResult);
        } else {
        	textExtractionService.processDocument(savedDocument, metadata); 
        }
        
        luceneService.indexDocument(
        	    savedDocument.getId().toString(),
        	    savedDocument.getName(),
        	    savedDocument.getType(),
        	    metadata.getExtractedText(),  
        	    currentUser.getId().toString()
        	);


        return savedDocument;
    }

    public Document saveLargeDocument(MultipartFile file, String name, String type, User user) throws IOException {
    	//Generate a unique key for the uploaded document
        String key = "documents/" + UUID.randomUUID();

        //Start a multipart upload session
        String uploadId = s3Client.createMultipartUpload(CreateMultipartUploadRequest.builder().bucket(bucketName).key(key).contentType(type).build()).uploadId();

        List<CompletedPart> completedParts = new ArrayList<>();
        InputStream inputStream = file.getInputStream();
        byte[] buffer = new byte[5 * 1024 * 1024]; // 5MB buffer size
        int bytesRead;
        int partNumber = 1;

        //Read file as a chunk and upload each part as a chunk
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] partData = new byte[bytesRead];
            System.arraycopy(buffer, 0, partData, 0, bytesRead);

            UploadPartResponse uploadPartResponse = s3Client.uploadPart(
                    UploadPartRequest.builder().bucket(bucketName).key(key).uploadId(uploadId).partNumber(partNumber).contentLength((long) bytesRead).build(),
                    RequestBody.fromBytes(partData));

            completedParts.add(CompletedPart.builder().partNumber(partNumber).eTag(uploadPartResponse.eTag()).build());
            partNumber++;
        }

        //Complete the upload
        s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder().bucket(bucketName).key(key).uploadId(uploadId).
        		multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build()).build());

        Document document = new Document();
        document.setName(name);
        document.setType(type);
        document.setOwner(user);
        document.setS3Key(key);

        return documentRepository.save(document);
    }


    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }
    
    public Page<Document> getDocumentsByUserIdPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadTime").descending());
        return documentRepository.findByOwnerId(userId, pageable);
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
