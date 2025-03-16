package io.documentprocessing.service;

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


@Service
public class TextExtractionService {

    private final DocumentMetadataRepository metadataRepository;
    private final Tika tika = new Tika();

    public TextExtractionService(DocumentMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public String extractText(Document document) {
    	InputStream inputStream = new java.io.ByteArrayInputStream(document.getData());
        try (inputStream) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(inputStream, handler, metadata, new ParseContext());

            return handler.toString().trim();  //Extracted text
        } catch (Exception e) {
            throw new RuntimeException("Text extraction failed", e);
        }
    }

    @Transactional
    public void processDocument(Document document, DocumentMetadata metadata) {
        // Extract text
        String extractedText = extractText(document);
        
        // Update metadata with extracted text
        metadata.setExtractedText(extractedText);
        metadata.setProcessedAt(LocalDateTime.now());
        metadata.setStatus("Processed");

        // Save updated metadata
        metadataRepository.save(metadata);
    }
}
