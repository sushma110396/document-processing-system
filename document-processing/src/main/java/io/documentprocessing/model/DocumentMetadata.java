package io.documentprocessing.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "document_metadata")
public class DocumentMetadata {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@Column(nullable = false)
	private String status;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false, updatable = false)
    private LocalDateTime uploadTimestamp; 
	
	@Column
	private LocalDateTime processedAt;

	@Column(columnDefinition = "TEXT")
	private String extractedText;
	
	public DocumentMetadata() {
		 this.uploadTimestamp = LocalDateTime.now(); 
		 this.createdAt = LocalDateTime.now();
		 this.status = "PENDING"; 
	}
	 
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	public LocalDateTime getUploadTimestamp() { 
		return uploadTimestamp; 
	}

    public void setUploadTimestamp(LocalDateTime uploadTimestamp) { 
    	this.uploadTimestamp = uploadTimestamp; 
    }

    public LocalDateTime getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(LocalDateTime processedAt) {
		this.processedAt = processedAt;
	}

	public String getExtractedText() {
		return extractedText;
	}

	public void setExtractedText(String extractedText) {
		this.extractedText = extractedText;
	}

}
