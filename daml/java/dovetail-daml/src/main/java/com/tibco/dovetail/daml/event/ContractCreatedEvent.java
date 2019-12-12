package com.tibco.dovetail.daml.event;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.daml.ledger.javaapi.data.*;

public class ContractCreatedEvent extends ContractEvent{
	Map<String, Object> arguments;
	String argumentText;
	Set<String> signatories;
	Set<String> observers;
	String contractKey;
	
	public ContractCreatedEvent() {
		
	}
	
	public ContractCreatedEvent(CreatedEvent e) {
		super(e);
		this.argumentText = e.getAgreementText().orElse(null);
		this.signatories = e.getSignatories();
		this.observers = e.getObservers();
		this.arguments = new HashMap<String, Object>();
		
		e.getArguments().getFieldsMap().forEach((k,v) -> {
			arguments.put(k, getValue(v));
				
		//	System.out.println("k=" +k + ", v=" +v.toString() + "vt =" + v.getClass().getName());
		});
	}
	
	public Map<String, Object> getArguments() {
		return arguments;
	}
	public void setArguments(Map<String, Object> arguments) {
		this.arguments = arguments;
	}
	public String getArgumentText() {
		return argumentText;
	}
	public void setArgumentText(String argumentText) {
		this.argumentText = argumentText;
	}
	public Set<String> getSignatories() {
		return signatories;
	}
	public void setSignatories(Set<String> signatories) {
		this.signatories = signatories;
	}
	public Set<String> getObservers() {
		return observers;
	}
	public void setObservers(Set<String> observers) {
		this.observers = observers;
	}
	public String getContractKey() {
		return contractKey;
	}
	public void setContractKey(String contractKey) {
		this.contractKey = contractKey;
	}
	
	private Object getValue(Value v) {
		if(v instanceof Party) {
			if (v.asParty().isPresent())
				return v.asParty().get().getValue();
			else 
				return null;
		}
		else if(v instanceof Text) {
			if (v.asText().isPresent())
				return v.asText().get().getValue();
			else 
				return null;
		}
		else if(v instanceof Numeric) {
			if (v.asNumeric().isPresent())
				return v.asNumeric().get().getValue();
			else 
				return null;
		}
		else if(v instanceof Bool) {
			if (v.asBool().isPresent())
				return v.asBool().get().getValue();
			else 
				return null;
		}
		else if(v instanceof ContractId) {
			if (v.asContractId().isPresent())
				return v.asContractId().get().getValue();
			else 
				return null;
		}
		else if(v instanceof Date) {
			if (v.asDate().isPresent())
				return v.asDate().get().getValue().toString();
			else 
				return null;
		}
		else if(v instanceof Int64) {
			if (v.asInt64().isPresent())
				return v.asInt64().get().getValue();
			else 
				return null;
		} else if (v instanceof Timestamp){
			if (v.asTimestamp().isPresent())
				return v.asTimestamp().get().getValue().toString();
			else 
				return null;
		}
		else if(v instanceof DamlList) {
			List<Object> values = new ArrayList<Object>();
			v.asList().ifPresent(l -> l.getValues().forEach(lv -> {
				values.add(getValue(lv));
			}));
			return values;
		} else if(v instanceof Record) {
			if (v.asRecord().isPresent()) {
				Map<String, Object> rv = new HashMap<String, Object>();
				v.asRecord().get().getFieldsMap().forEach((f,fv) -> {
					rv.put(f, getValue(fv));
				});
				return rv;
			}
			else 
				return null;
		}
		
		return null;
	}
}
