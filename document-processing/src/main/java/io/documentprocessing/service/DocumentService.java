package io.documentprocessing.service;

import io.documentprocessing.model.Document;
import io.documentprocessing.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    public Document getDocumentById(Long id) {
        Optional<Document> document = documentRepository.findById(id);
        return document.orElse(null);  // Returns null if document not found
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}
