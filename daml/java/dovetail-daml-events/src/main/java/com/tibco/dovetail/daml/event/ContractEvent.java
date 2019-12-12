package com.tibco.dovetail.daml.event;

import java.util.List;
import com.daml.ledger.javaapi.data.*;

public class ContractEvent {
	String eventId;
	List<String> witnessParties;
	Template templateId;
	String contractId;
	
	public ContractEvent() {
		
	}
	
	public ContractEvent(Event e) {
		this.eventId = e.getEventId();
		this.contractId = e.getContractId();
		this.templateId = new Template(e.getTemplateId());
		this.witnessParties = e.getWitnessParties();
	}
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public List<String> getWitnessParties() {
		return witnessParties;
	}
	public void setWitnessParties(List<String> witnessParties) {
		this.witnessParties = witnessParties;
	}
	public Template getTemplateId() {
		return templateId;
	}
	public void setTemplateId(Template templateId) {
		this.templateId = templateId;
	}
	public String getContractId() {
		return contractId;
	}
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}
}
