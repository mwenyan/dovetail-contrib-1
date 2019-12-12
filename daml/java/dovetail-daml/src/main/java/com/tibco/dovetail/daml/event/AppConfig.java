package com.tibco.dovetail.daml.event;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import com.tibco.dovetail.daml.event.Messaging;

public class AppConfig {
	String appId;
	List<Filter> filters;
	Messaging messaging;
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public List<Filter> getFilters() {
		return filters;
	}
	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}
	public Messaging getMessaging() {
		return messaging;
	}
	public void setMessaging(Messaging messaging) {
		this.messaging = messaging;
	}
	
	public static AppConfig parse(String configFile) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		return yaml.loadAs(new FileInputStream(configFile), AppConfig.class);
	}
}
