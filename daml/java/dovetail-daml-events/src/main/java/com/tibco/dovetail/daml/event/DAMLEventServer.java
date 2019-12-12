package com.tibco.dovetail.daml.event;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import com.daml.ledger.javaapi.data.*;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.google.gson.Gson;
import com.tibco.dovetail.messaging.Publisher;

import io.reactivex.schedulers.Schedulers;

public class DAMLEventServer {
	public static void main(String[] args) {
	
		DefaultParser cmdParser = new DefaultParser();
		Options opts = new Options();
		opts.addRequiredOption("c", "config", true, "path to configuration yaml file");
		opts.addRequiredOption("s", "server", true, "ledger host");
		opts.addRequiredOption("p", "port", true, "ledger port");
		opts.addOption("h", "help", false, "print help");
		 
		 String header = "DAML event server \n\n";
		 String footer = "";
		 
		 HelpFormatter formatter = new HelpFormatter();

		try {
			CommandLine cmds = cmdParser.parse(opts, args, false);
			if(cmds.hasOption("h") || cmds.hasOption("help")){
				 formatter.printHelp("java -jar <path to daml event server jar>", header, opts, footer, true);
				 System.exit(0);
			}
			
			String configfile = cmds.getOptionValue("c");
			String host = cmds.getOptionValue("s");
			String port = cmds.getOptionValue("p");
			
			AppConfig config = AppConfig.parse(configfile);
			
			DamlLedgerClient client = DamlLedgerClient.forHostWithLedgerIdDiscovery(host, Integer.valueOf(port), Optional.empty());
	        client.connect();
	        System.out.println("Starting Dovetail DAML Event Server ...");
	        
	        subscribeTransactionEvents(client, config);
	        subscribeCommandEvents(client, config);
	        
	     // Run until user terminates
	        while (true)
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        
		} catch (Throwable e) {
			e.printStackTrace();
			 formatter.printHelp("java -jar <path to daml event server jar>", header, opts, footer, true);
		}
		
	}
	
	
	private static void subscribeTransactionEvents(DamlLedgerClient client, AppConfig config) throws Exception {
        Publisher pub = getPublisher(config, config.getAppId() + "_transaction");
		//Publisher pub = getPublisher(config, null);
		 Gson gson = new Gson();
		
		 client.getTransactionsClient().getTransactions(getCheckpoint(), getTxnFilter(config), true)
		 .subscribeOn(Schedulers.io())
		 .subscribe( txn -> {
			 pub.beginTransaction();
			 try {
				 Map<String, String> headers = new HashMap<String, String>();
				 headers.put("APPID", config.getAppId());
				 headers.put("FLOWID", txn.getWorkflowId());
				 headers.put("TXNID", txn.getTransactionId());
				 headers.put("CMDID", txn.getCommandId());
				 
				 for (Event event : txn.getEvents()) {
					
	                 if (event instanceof CreatedEvent) {
	                     CreatedEvent createdEvent = (CreatedEvent) event;
	                     ContractCreatedEvent e = new ContractCreatedEvent(createdEvent);
	                     pub.publish(gson.toJson(e), headers, e.getTemplateId().getEventTopic() + ".created");
	                 } else if (event instanceof ArchivedEvent) {
	                     ArchivedEvent archivedEvent = (ArchivedEvent) event;
	                     ContractArchivedEvent e = new ContractArchivedEvent(archivedEvent);
	                     pub.publish(gson.toJson(e), headers, e.getTemplateId().getEventTopic() + ".archived");
	                 }
				 }
			 }catch(Exception e) {
				 pub.abortTransaction();
				 throw e;
			 }
			pub.commitTransaction();
		 });
		 
		 System.out.println("Subscribed to transactions stream");
		 
	}
	
	private static void subscribeCommandEvents(DamlLedgerClient client, AppConfig config) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Publisher pub = getPublisher(config, config.getAppId() + "_command");
		//Publisher pub = getPublisher(config, null);
		 Gson gson = new Gson();
		
		 client.getCommandCompletionClient().completionStream(config.getAppId(), getCheckpoint(), getCommandFilter(config))
		 .subscribeOn(Schedulers.io())
		 .subscribe( cmd -> {
			 pub.beginTransaction();
			 if(cmd.getCompletions().size() > 0) {
				 cmd.getCompletions().forEach(c -> {
					 Map<String, String> headers = new HashMap<String, String>();
					 headers.put("APPID", config.getAppId());
					 headers.put("CMDID", c.getCommandId());
					 headers.put("TXNID", c.getTransactionId());
					 
					 CommandEvent cmdevt = new CommandEvent(c);
					 pub.publish(gson.toJson(cmdevt), headers,  "command");
				 });
			 }
			
			 pub.commitTransaction();
		 });
		 System.out.println("Subscribed to command completion stream");
	}
	
	private static LedgerOffset getCheckpoint() {
		return LedgerOffset.LedgerBegin.getInstance();
	}
	
	private static FiltersByParty getTxnFilter(AppConfig config) {
		Map<String, com.daml.ledger.javaapi.data.Filter> filters = new HashMap<String, com.daml.ledger.javaapi.data.Filter>();
		config.getFilters().forEach(f -> {
			Set<Identifier> ids = new java.util.HashSet<Identifier>();
	        f.getTemplates().forEach(t -> {
	        	ids.add(new Identifier(t.getPackageId(), t.getModuleName(), t.getEntityName()));
	        });
	        InclusiveFilter inclusiveFilter = new InclusiveFilter(ids);
	        filters.put(f.getParty(), inclusiveFilter);
		});
		return new FiltersByParty(filters);
	}
	
	private static Set<String> getCommandFilter(AppConfig config) {
		Set<String> filter = new HashSet<String>();
		config.getFilters().forEach(f -> {
	        filter.add(f.getParty());
		});
		return filter;
	}
	
	private static Publisher getPublisher(AppConfig config, String txnId) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String className = config.getMessaging().getClassName();
		Constructor pcon = Class.forName(className).getConstructor();
		Publisher pub = (Publisher) pcon.newInstance();
		
		Properties pp = new Properties();
		config.getMessaging().getProperties().forEach((k,v) -> pp.setProperty(k, v));
		if(txnId == null)
			pub.connect(pp);
		else
			pub.connect(pp, txnId);
		return pub;
	}
	
	
}
