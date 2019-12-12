package com.tibco.dovetail.daml.event;
import com.daml.ledger.javaapi.data.*;

public class Template {
	String packageId;
	String moduleName;
	String entityName;
	
	
	public Template() {
		
	}
	
	public Template(Identifier id) {
		this.packageId = id.getPackageId();
		this.moduleName = id.getModuleName();
		this.entityName = id.getEntityName();
	}
	public String getPackageId() {
		return packageId;
	}
	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	
	public String getEventTopic() {
		return this.moduleName + "." + this.entityName;
	}
}
