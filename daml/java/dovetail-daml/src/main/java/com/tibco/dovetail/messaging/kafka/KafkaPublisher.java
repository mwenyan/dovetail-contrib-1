package com.tibco.dovetail.messaging.kafka;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringSerializer;

import com.tibco.dovetail.messaging.Publisher;

public class KafkaPublisher implements Publisher {
	private KafkaProducer<String, String> producer = null;
	Properties properties;
	
	@Override
	public void publish(String message, Map<String, String> headers, String event) {

		String topic = properties.getProperty(event);
		String key = properties.getProperty(topic + ".key", "none");
		ProducerRecord<String, String> rec = null;
		
		if(key == "none") {
			rec = new ProducerRecord<String, String>(topic, message);
		} else {
			String kv = headers.get(key);
			rec = new ProducerRecord<String, String>(topic, kv, message);
		}
		
		Headers msgheaders = rec.headers();
		headers.forEach((h,v) -> msgheaders.add(h, v.getBytes()));
		this.producer.send(rec, new KafkaCallback());
		
	}

	@Override
	public void connect(Properties properties) {
		Properties kp = new Properties();
		kp.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap.servers"));
		kp.setProperty(ProducerConfig.CLIENT_ID_CONFIG, properties.getProperty("clientId"));
		kp.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kp.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		this.producer = new KafkaProducer<String,String>(kp);
		
		this.properties = properties;
		
	}
	
	@Override
	public void connect(Properties properties, String txnId) {
		Properties kp = new Properties();
		kp.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap.servers"));
		kp.setProperty(ProducerConfig.CLIENT_ID_CONFIG, properties.getProperty("clientId"));
		kp.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kp.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		
		kp.setProperty("enable.idempotence", "true");
		kp.setProperty("transactional.id", txnId);
		//	kp.setProperty("offsets.topic.replication.factor",  properties.getProperty("offsets.topic.replication.factor", "1"));
		//	kp.setProperty("transaction.state.log.replication.factor",  properties.getProperty("transaction.state.log.replication.factor", "1"));
		//	kp.setProperty("transaction.state.log.min.isr",  properties.getProperty("transaction.state.log.min.isr", "1"));
		
		
		this.producer = new KafkaProducer<String,String>(kp);
		this.producer.initTransactions();
		
		this.properties = properties;
		
	}


	@Override
	public void disconnect() {
		this.producer.close();
	}

	@Override
	public void beginTransaction() {
		this.producer.beginTransaction();
		
	}

	@Override
	public void commitTransaction() {
		this.producer.commitTransaction();
	}

	@Override
	public void abortTransaction() {
		this.producer.abortTransaction();
	}
	
	static class KafkaCallback implements Callback {

		@Override
		public void onCompletion(RecordMetadata arg0, Exception arg1) {
			if(arg1 != null)
				arg1.printStackTrace();
			
			System.out.printf("completed: topic=%s,  partition=%d, offset=%d, timestamp=%4$td.%4$tm.%4$ty %4tT:%4$tL\n", arg0.topic(), arg0.partition(), arg0.offset(), new Date(arg0.timestamp()));
		}
		
	}

}
