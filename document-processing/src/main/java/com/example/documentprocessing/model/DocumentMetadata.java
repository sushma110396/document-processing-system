package com.example.documentprocessing.model;

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
	 
	 @Column
	 private LocalDateTime processedAt;
	 
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

}
