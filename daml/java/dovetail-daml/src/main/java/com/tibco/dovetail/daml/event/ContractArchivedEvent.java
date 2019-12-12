package com.tibco.dovetail.daml.event;

import com.daml.ledger.javaapi.data.ArchivedEvent;

public class ContractArchivedEvent extends ContractEvent{
	public ContractArchivedEvent() {
		super();
	}
	
	public ContractArchivedEvent(ArchivedEvent e) {
		super(e);
	}
}
