package com.tibco.dovetail.corda.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tibco.dovetail.container.cordapp.AppContainer;

import net.corda.finance.contracts.asset.Cash.State;

public class CashSerializer extends StdSerializer<State> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CashSerializer() {
		this(null);
	}
	protected CashSerializer(Class<State> t) {
		super(t);
	}

	@Override
	public void serialize(State value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		
		gen.writeStartObject();
		gen.writeStringField("owner",AppContainer.partyToString(value.getOwner()));
		gen.writeStringField("issuer", AppContainer.partyToString(value.getAmount().getToken().getIssuer().getParty()));
		gen.writeObjectFieldStart("amt");
		gen.writeNumberField("quantity", value.getAmount().getQuantity());
		gen.writeStringField("currency", value.getAmount().getToken().getProduct().getCurrencyCode());
		gen.writeEndObject();
		gen.writeEndObject();
	}

}