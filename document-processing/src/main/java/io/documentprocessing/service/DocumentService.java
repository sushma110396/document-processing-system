package io.documentprocessing.service;

import io.documentprocessing.model.Document;
import io.documentprocessing.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document saveDocument(MultipartFile file, String name, String type) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty, cannot save.");
        }

        Document document = new Document();
        document.setName(name);
        document.setType(type);
        document.setData(file.getBytes()); // Convert file to byte array

        return documentRepository.save(document);
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
