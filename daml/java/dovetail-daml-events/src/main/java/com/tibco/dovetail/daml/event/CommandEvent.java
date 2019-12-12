package com.tibco.dovetail.daml.event;

import org.checkerframework.checker.nullness.qual.NonNull;

public class CommandEvent {
	String commandId;
	int statusCode;
	String statusMessage;
	
	public CommandEvent(com.digitalasset.ledger.api.v1.CompletionOuterClass.@NonNull Completion c) {
		this.commandId = c.getCommandId();
		this.statusCode = c.getStatus().getCode();
		this.statusMessage = c.getStatus().getMessage();
	}
}
