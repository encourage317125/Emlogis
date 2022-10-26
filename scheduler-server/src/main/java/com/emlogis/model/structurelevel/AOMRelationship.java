package com.emlogis.model.structurelevel;

import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class AOMRelationship {
	
	@EmbeddedId
    private PrimaryKey primaryKey;

	@JsonIgnore
    @ManyToOne(targetEntity = StructureLevel.class)
    @JoinColumns({
		@JoinColumn(name = "src_tenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "src_id", referencedColumnName = "id")
    })
    private StructureLevel src;

	@JsonIgnore
    @ManyToOne(targetEntity=StructureLevel.class)
    @JoinColumns({
		@JoinColumn(name = "dst_tenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "dst_id", referencedColumnName = "id")
    })
    private StructureLevel dst;
	
	@Column
	private String type;

	public AOMRelationship() {
		super();
	}

	public AOMRelationship(String tenantId, StructureLevel src, StructureLevel dst, String type) {
		super();
		String id = "[" + src.getTenantId() + "." + type + "]" 
				+ src.getClass().getSimpleName() + ":" + src.getId()
				+ "_"
				+ dst.getClass().getSimpleName() + ":" + dst.getId();
		primaryKey = new PrimaryKey(tenantId, id);
		setType(type);
	}

	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getId() {
		return primaryKey.getId();
	}

	public String getTenantId() {
		return primaryKey.getTenantId();
	}
	
	public StructureLevel getSrc() {
		return src;
	}

	public void setSrc(StructureLevel src) {
		this.src = src;
	}

	public StructureLevel getDst() {
		return dst;
	}

	public void setDst(StructureLevel dst) {
		this.dst = dst;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
