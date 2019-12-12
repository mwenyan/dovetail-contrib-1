package com.tibco.dovetail.messaging;

import java.util.Map;
import java.util.Properties;

public interface Publisher {
	public  void connect(Properties properties );
	public  void connect(Properties properties, String txnId);
	public void beginTransaction();
	public void commitTransaction();
	public void abortTransaction();
	public  void publish(String message, Map<String, String>headers, String event);
	public  void disconnect();
}
