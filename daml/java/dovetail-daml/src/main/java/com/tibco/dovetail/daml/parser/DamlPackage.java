package com.tibco.dovetail.daml.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DamlPackage {
	String packageId;
	public String getPackageId() {
		return packageId;
	}

	Map<String, DamlTemplate> templates = new HashMap<String, DamlTemplate>();
	Map<String, DamlDataObject> dataObjects = new HashMap<String, DamlDataObject>();
	
	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}

	public Collection<DamlTemplate> getTemplates() {
		return templates.values();
	}

	public void addTemplate(String name, DamlTemplate template) {
		this.templates.put(name, template);
	}

	public DamlTemplate getTempate(String entity) {
		return templates.get(entity);
	}
	
	public Collection<DamlDataObject> getDataObjects() {
		return dataObjects.values();
	}

	public void addDataObject(String name, DamlDataObject data) {
		this.dataObjects.put(name, data);
	}

	public DamlDataObject getDataObject(String entity) {
		return dataObjects.get(entity);
	}
	
	public DamlDataObject removeDataObject(String entity) {
		return dataObjects.remove(entity);
	}
	
	static class DamlTemplate{
		
		List<DamlChoice> choices = new ArrayList<DamlChoice>();
		
		public List<DamlChoice> getChoices() {
			return choices;
		}
		public void setChoices(List<DamlChoice> choices) {
			this.choices = choices;
		}
		public void addChoice(DamlChoice choice) {
			this.choices.add(choice);
		}
	}

	static class Argument {
		String name;
		String type;
		boolean isList = false;
		boolean isMap = false;
		boolean isOptional = false;
		String parentType;
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public boolean isOptional() {
			return isOptional;
		}
		public void setOptional(boolean isOptional) {
			this.isOptional = isOptional;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean isList() {
			return isList;
		}
		public void setList(boolean isList) {
			this.isList = isList;
		}
		public boolean isMap() {
			return isMap;
		}
		public void setMap(boolean isMap) {
			this.isMap = isMap;
		}
		public String getParentType() {
			return parentType;
		}
		public void setParentType(String parentModule) {
			this.parentType = parentModule;
		}
	}
	
	static class DamlChoice extends DamlBaseObject{
		/*List<Argument> returns =  new ArrayList<Argument>();
		public List<Argument> getReturns() {
			return returns;
		}
		public void setReturns(List<Argument> returns) {
			this.returns = returns;
		}*/
	}
	
	static class DamlBaseObject {
		String module;
		String name;
		List<Argument> arguments = new ArrayList<Argument>();
		
		public String getModule() {
			return module;
		}
		public void setModule(String module) {
			this.module = module;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String entity) {
			this.name = entity;
		}
		public List<Argument> getArguments() {
			return arguments;
		}
		public void setArguments(List<Argument> arguments) {
			this.arguments = arguments;
		}
	}
	
	static class DamlDataObject extends DamlBaseObject{
		
		boolean isEnum = false;
		boolean isVariant = false;
		
		
		Map<String, String> varTypes = new HashMap<String, String>();
		
		
		public boolean isEnum() {
			return isEnum;
		}
		public void setEnum(boolean isEnum) {
			this.isEnum = isEnum;
		}
		public boolean isVariant() {
			return isVariant;
		}
		public void setVariant(boolean isVariant) {
			this.isVariant = isVariant;
		}
		public void addVarType(String n, String varType) {
			this.varTypes.put(n, varType);
		}
		public Map<String, String> getVarTypes() {
			return varTypes;
		}
		public void setVarTypes(Map<String, String> varTypes) {
			this.varTypes = varTypes;
		}
		
	}
}




