package com.tibco.dovetail.daml.event;

import java.util.List;

public class Filter {
	String party;
	List<Template> templates;
	public String getParty() {
		return party;
	}
	public void setParty(String party) {
		this.party = party;
	}
	public List<Template> getTemplates() {
		return templates;
	}
	public void setTemplates(List<Template> templates) {
		this.templates = templates;
	}
}
