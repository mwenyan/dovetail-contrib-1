/*
* Copyright © 2018. TIBCO Software Inc.
* This file is subject to the license terms contained
* in the license file that is distributed with this file.
 */
package dovetail_general.activity.collection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.tibco.dovetail.core.runtime.engine.Context;
import com.tibco.dovetail.function.array;
import com.tibco.dovetail.core.runtime.activity.IActivity;
import com.tibco.dovetail.core.runtime.util.CompareUtil;
import com.tibco.dovetail.core.runtime.util.JsonUtil;

public class collection implements IActivity {
	@Override
	public void eval(Context context) throws IllegalArgumentException {
		String op = context.getInput("operation").toString();
		
		switch(op) {
			case "DISTINCT":
				distinct(context);
				break;
			case "COUNT":
				count(context);
				break;
			case "MERGE":
				merge(context);
				break;
			case "FILTER":
				filter(context);
				break;
			default:
				throw new IllegalArgumentException("Unsupported operation:" + op);
		}
	}

	private DocumentContext getInput(Context context) {
		//String dataType = context.getInput("dataType").toString();
		Object obj = context.getInput("input");

		if(obj != null) {
			DocumentContext input = (DocumentContext)obj;
			return input;
		}
		
		throw new IllegalArgumentException("Input data is not set");
	}
	private void distinct(Context context){
		Set<String> result = new HashSet<String>();
		DocumentContext input = getInput(context);
	
		List<Object> data = input.read("$.dataset[*].field");
	//	result = array.distinct(data.stream().map(d -> d.toString()).collect(Collectors.toList()));
		result = array.distinct(data);
		
		
		List values = new ArrayList();
		
		result.forEach(r -> {
			LinkedHashMap<String, Object> field = new LinkedHashMap<String, Object>();
			field.put("field", r);
			values.add(field);
		});
		
		DocumentContext doc = JsonUtil.getJsonParser().parse("{}");
		doc.put("$", "dataset", values);
		doc.put("$", "size", result.size());
	
		context.setOutput("output", doc);
	}
	
	private void join(Context context) {
		String delimiter = context.getInput("delimiter").toString();
		
		DocumentContext input = getInput(context);
		
		List<Object> data = input.read("$..data");
		String result = data.stream().map(d -> d.toString()).collect(Collectors.joining(delimiter));
		
		DocumentContext doc = JsonUtil.getJsonParser().parse("{}");
		doc.put("$", "result", result);
		context.setOutput("output", doc);
	}
	
	private void indexing(Context context) {
		DocumentContext input = getInput(context);
		List<Object> data = input.json();
		for (int i=0; i<data.size(); i++) {
			LinkedHashMap<String, Object> d = (LinkedHashMap<String, Object>) data.get(i);
			d.put("_index_", i);
		}
		context.setOutput("output", input);
	}
	
	private void count(Context context) {
		DocumentContext input = getInput(context);
		List<Object> data = input.json();
		
		DocumentContext doc = JsonUtil.getJsonParser().parse("{}");
		doc.put("$", "size", data.size());
		context.setOutput("output", doc);
	}
	
	private void merge(Context context) {
		DocumentContext input = getInput(context);
		DocumentContext doc = JsonUtil.getJsonParser().parse("{}");
		List<Object> values = new ArrayList<Object>();
		
		LinkedHashMap<String, Object> value = input.json();
		Object input1 = value.get("dataset1");
		Object input2 = value.get("dataset2");
		int size = 0;
		
		if (input1 != null ) {
			List<Object> lsinput1 = (List<Object>)input1;
			values.addAll(lsinput1);
			size = size + lsinput1.size();
			
		}
		
		if (input2 != null ) {
			List<Object> lsinput2 = (List<Object>)input2;
			values.addAll(lsinput2);
			size = size + lsinput2.size();
		}
	
		doc.put("$", "dataset", values);
		doc.put("$", "size", size);
		context.setOutput("output", doc);
	}
	
	private void filter(Context context) {
		DocumentContext input = getInput(context);
		DocumentContext doc = JsonUtil.getJsonParser().parse("{}");
		List<Object> trueset = new ArrayList<Object>();
		List<Object> falseset = new ArrayList<Object>();
		
		LinkedHashMap<String, Object> data = input.json();
		Object filterValue = data.get("filterExpression");
		Object values = data.get("dataset");
		/*
		if (values != null) {
			//$dataset.path.to.field
			String[] tokens = field.split("\\.");
			if(tokens.length < 2 || !field.startsWith("$dataset")) {
				throw new IllegalArgumentException("collection filter field " + field + " should be in the format of $dataset.path.to.field");
			}
			
			List<LinkedHashMap<String, Object>> dataset = (List<LinkedHashMap<String, Object>>)values;
			dataset.forEach(in -> {
				Object v = in.get(tokens[1]);
				for(int i= 2; i<tokens.length; i++) {
					if(v != null) {
						v = ((LinkedHashMap<String, Object>)v).get(tokens[i]);
					} else {
						break;
					}
				}
				
				if (v == null || !CompareUtil.compare(v, filterValue, op)) {
					falseset.add(in);
				} else {
					trueset.add(in);
				}
			});
		}
		
		doc.put(JsonPath.compile("$"), "true_dataset", trueset);
		doc.put(JsonPath.compile("$"), "false_dataset", falseset);
		doc.put(JsonPath.compile("$"), "true_dataset_size", trueset.size());
		doc.put(JsonPath.compile("$"), "false_dataset_size", falseset.size()); */
		context.setOutput("output", doc);
	}
}
