package com.emlogis.model.structurelevel;

import com.emlogis.model.AOMEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()
public abstract class StructureLevel extends AOMEntity implements Serializable {
	    
    @Column(nullable = false)
	private	String name = "no name set";
    	        
	private	String description;
	
	// AOM relationship related attributes
	@JsonIgnore
    @OneToMany(mappedBy = "dst", targetEntity=AOMRelationship.class, fetch = FetchType.LAZY)
    private Set<AOMRelationship> srcRels = new HashSet<>();
    
	@JsonIgnore
    @OneToMany(mappedBy = "src", targetEntity=AOMRelationship.class, fetch = FetchType.LAZY)
    private Set<AOMRelationship> dstRels = new HashSet<>();

	public StructureLevel() {
		super();
	}

	public StructureLevel(PrimaryKey primaryKey) {
		super(primaryKey);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<AOMRelationship> getSrcRels() {
		return srcRels;
	}

	public void setSrcRels(Set<AOMRelationship> srcRels) {
		this.srcRels = srcRels;
	}
	
	public void addSrcRel(AOMRelationship r) {
		srcRels.add(r);		
	}

	public void removeSrcRel(AOMRelationship r) {
		srcRels.remove(r);		
	}

	public Set<AOMRelationship> getDstRels() {
		return dstRels;
	}

	public void setDstRels(Set<AOMRelationship> dstRels) {
		this.dstRels = dstRels;
	}
	
	public void addDstRel(AOMRelationship r) {
		dstRels.add(r);		
	}

	public void removeDstRel(AOMRelationship r) {
		dstRels.remove(r);		
	}

	public String toString() {
		
		String s = this.getClName() + ": " + getTenantId() + ":" + getId() + " " + name + " (path:" + getPath() + ") ";
		if (description != null) {
			s += (" (" + description + ")");
		}
		return s;
	}
    
}
