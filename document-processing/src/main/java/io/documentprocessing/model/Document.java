package io.documentprocessing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(name = "s3_key")
    private String s3Key; // S3 object key (path/filename)
	
	/*
	 * @Column(columnDefinition = "BYTEA") private byte[] data;
	 */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true) 
    private User owner;


    
    // Getters and Setters
    public Long getId() { 
    	return id; 
    }
    
    public void setId(Long id) { 
    	this.id = id; 
    }

    public String getName() { 
    	return name; 
    }
    
    public void setName(String name) { 
    	this.name = name; 
    }

    public String getType() { 
    	return type; 
    }
    
    public void setType(String type) { 
    	this.type = type; 
    }
    
    public String getS3Key() {
		return s3Key;
	}
	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}
	
	public User getOwner() { 
		return owner; 
	}
    public void setOwner(User owner) { 
    	this.owner = owner; 
    }
}
