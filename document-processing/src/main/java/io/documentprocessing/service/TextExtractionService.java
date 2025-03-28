package io.documentprocessing.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.documentprocessing.model.Document;
import io.documentprocessing.model.DocumentMetadata;
import io.documentprocessing.repository.DocumentMetadataRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
public class TextExtractionService {

    private final Tika tika = new Tika();
    private final DocumentMetadataRepository metadataRepository;
    private final S3Client s3Client;


    public TextExtractionService(DocumentMetadataRepository metadataRepository,  S3Client s3Client) {
        this.metadataRepository = metadataRepository;
        this.s3Client = s3Client;
    }

	/*
	 * public String extractText(Document document) { InputStream inputStream = new
	 * ByteArrayInputStream(document.getData()); // Process text-based files using
	 * Apache Tika
	 * 
	 * try (inputStream) { BodyContentHandler handler = new BodyContentHandler(-1);
	 * Metadata metadata = new Metadata(); AutoDetectParser parser = new
	 * AutoDetectParser(); parser.parse(inputStream, handler, metadata, new
	 * ParseContext()); return handler.toString().trim(); } catch (Exception e) {
	 * throw new RuntimeException("Text extraction failed", e); } }
	 */
    
    public String extractText(Document document) {
        // Load the file from S3
        try (InputStream inputStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket("your-bucket-name")
                .key(document.getS3Key()) // Use s3Key field in Document model
                .build())) {

            // Use Apache Tika to extract text
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(inputStream, handler, metadata, new ParseContext());

            return handler.toString().trim();

        } catch (Exception e) {
            throw new RuntimeException("Text extraction from S3 failed", e);
        }
    }

    
    @Transactional
    public void processDocument(Document document, DocumentMetadata metadata) {
        // Extract text from the document
        String extractedText = extractText(document);

        // Update metadata with extracted text and processing timestamp
        metadata.setExtractedText(extractedText);
        metadata.setProcessedAt(LocalDateTime.now());
        metadata.setStatus("Processed");

        // Save updated metadata to the database
        metadataRepository.save(metadata);
    }
}
