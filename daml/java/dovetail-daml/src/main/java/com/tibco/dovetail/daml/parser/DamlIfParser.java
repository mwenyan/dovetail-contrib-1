package com.tibco.dovetail.daml.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;

import com.digitalasset.daml.lf.archive.Dar;
import com.digitalasset.daml.lf.archive.DarReader;
import com.digitalasset.daml.lf.data.Ref.QualifiedName;
import com.digitalasset.daml.lf.iface.DataType;
import com.digitalasset.daml.lf.iface.DefTemplate;
import com.digitalasset.daml.lf.iface.Interface;
import com.digitalasset.daml.lf.iface.Record;
import com.digitalasset.daml.lf.iface.TypeCon;
import com.digitalasset.daml.lf.iface.Type;
import com.digitalasset.daml.lf.iface.TypeNumeric;
import com.digitalasset.daml.lf.iface.TypePrim;
import com.digitalasset.daml.lf.iface.Variant;
import com.digitalasset.daml.lf.iface.reader.Errors;
import com.digitalasset.daml.lf.iface.reader.InterfaceReader;
import com.digitalasset.daml.lf.iface.reader.InterfaceReader.InvalidDataTypeDefinition;
import com.digitalasset.daml_lf_dev.DamlLf;
import com.digitalasset.daml_lf_dev.DamlLf.Archive;
import com.digitalasset.daml_lf_dev.DamlLf.ArchivePayload;
import com.google.gson.Gson;
import com.tibco.dovetail.daml.parser.DamlPackage.Argument;
import com.tibco.dovetail.daml.parser.DamlPackage.DamlDataObject;
import com.tibco.dovetail.daml.parser.DamlPackage.DamlTemplate;

import scala.Tuple2;
import scala.util.Try;

public class DamlIfParser {
	public static void main(String[] args) {
		DefaultParser cmdParser = new DefaultParser();
		Options opts = new Options();
		opts.addRequiredOption("a", "dar", true, "path to dar file");
		opts.addOption("o", "output", true, "path to output directory, default to current");
		try {
			CommandLine cmds = cmdParser.parse(opts, args, false);
			String dar = cmds.getOptionValue("a");
			
			Path darpath = FileSystems.getDefault().getPath(dar);
			if(!darpath.isAbsolute())
				darpath = darpath.toAbsolutePath();
			String darfile = darpath.getFileName().toString();
			DamlPackage pkg = ParseDar(darpath.toString());
			Gson gson = new Gson();
		    String json = gson.toJson(pkg); 
		    
		    String output = "";
			if(cmds.hasOption("o")){
				output = cmds.getOptionValue("o");
			}
		    String outputpath = FileSystems.getDefault().getPath(output).toAbsolutePath().toString() + "/" + darfile.replace(".dar", ".json");
	
		    BufferedWriter writer = new BufferedWriter(new FileWriter(outputpath));
		    writer.write(json);
		     
		    writer.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static DamlPackage ParseDar(String darFile) throws Throwable {
		File lfFile = new java.io.File(darFile);
		Try<Dar<Tuple2<String, ArchivePayload>>> dar = DarReader.apply().readArchiveFromFile(lfFile);
		
		if(dar.isFailure()) {
			throw dar.failed().get();
		}
			 
		Archive archive = DamlLf.Archive.parser().parseFrom(dar.get().main()._2.toByteArray());
		Tuple2<Errors<Object, InvalidDataTypeDefinition>, Interface> out = InterfaceReader.readInterface(dar.get().main());
	
		if(!out._1.toString().contains("(Tip)") ){
			throw new RuntimeException(out._1.toString());
		}
		
		
		DamlPackage damlPackage = new DamlPackage();
		damlPackage.setPackageId(out._2.packageId());
		
		Map<String, Boolean> dataObjects = new HashMap<String, Boolean>();
		
		//process all data object types: template, choice, data
		out._2.getTypeDecls().forEach((qn, intf) -> {
			DamlDataObject dobj = new DamlDataObject();
			
			String module = qn.module().dottedName();
			String name = qn.name().dottedName();
			List<Argument> args = null;
			if(intf.getType().dataType() instanceof Record)
				args = processArguments((Record)intf.getType().dataType());
			else if(intf.getType().dataType() instanceof Variant){
				dobj.setVariant(true);
				Variant var = (Variant)intf.getType().dataType();
				var.getFields().forEach(f -> {
					Tuple2<String, TypeCon> ft2 = (Tuple2<String, TypeCon>) f;
					dobj.addVarType(ft2._1, ft2._2.name().identifier().qualifiedName().toString());
				});
					
			} else {
				DataType<Type, Type> e = intf.getType().dataType();
				String et = e.toString();
				if(et.startsWith("Enum")) {
					dobj.setEnum(true);
				} else {
					throw new RuntimeException("Unsupported type: " + et);
				}
			}
			
			dobj.setModule(module);
			dobj.setName(name);
			dobj.setArguments(args);
			damlPackage.addDataObject(qn.toString(), dobj);
	    	
		});
		
		//process template, choices
		out._2.getTypeDecls().forEach((qn, intf) -> {
			
			if(intf.getTemplate().isPresent()){
				String module = qn.module().dottedName();
				
	    		DefTemplate<Type> tpl = intf.getTemplate().get();
	    		
	    		DamlTemplate t = new DamlPackage.DamlTemplate();
				damlPackage.addTemplate(qn.toString(), t);
					
	    		tpl.getChoices().forEach((k,c) -> {
	    			 DamlPackage.DamlChoice dc = new DamlPackage.DamlChoice();
	    			 dc.setName(k);
	    			 dc.setModule(module);

	    			 TypeCon tc = (TypeCon) c.param();
	    			 QualifiedName cqn = tc.name().identifier().qualifiedName();
	    			 DamlDataObject cobj = damlPackage.removeDataObject(cqn.toString());
	    			 if(cobj != null) {
	    				 dc.setArguments(cobj.getArguments());
	    				 
	    				 //returns
	    				// processReturns(c.returnType());
	    			 }
	    			 t.addChoice(dc);
	    		});	
	    	}
		});
		
		
		return damlPackage;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<Argument> processReturns(Type rc) {
		List<Argument> arguments = new ArrayList<Argument>();
		
		if(rc instanceof TypePrim) {
			String tp = ((TypePrim)rc).toString();
			if(tp == "PrimTypeUnit") {
				return arguments;
			} else if(tp == "PrimTypeContractId") {
				
			} else {
				throw new RuntimeException("Unsupported return type: " + tp);
			}
		} else if(rc instanceof TypeCon) {
			
		} else {
			throw new RuntimeException("Unsupported return type: " + rc.toString());
		}
		
		return arguments;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<Argument> processArguments(Record rec) {
		List<Argument> arguments = new ArrayList<Argument>();
		
		rec.getFields().forEach(f -> {
			Argument arg = new Argument();
			  
			Tuple2 tuple = (Tuple2)f;
			String pn = (String) tuple._1;
			Type pt = (Type) tuple._2;
			  
			arg.setName(pn);
			setArgType(arg, pt);
			arguments.add(arg);
		});
		return arguments;
	}
	
	private static void setArgType(Argument arg, Type pt) {
		if(pt instanceof TypePrim) {
			TypePrim prim = (TypePrim) pt;
			String dt = prim.typ().toString();
			String primtype = getPrimType(dt);
			if(primtype == "List") {
				arg.setList(true);
				Type tp = prim.typArgs().head();
				setArgType(arg, tp);
			} else if (primtype == "Optional") {
				arg.setOptional(true);
				Type tp = prim.typArgs().head();
				setArgType(arg, tp);
			} else if(primtype == "Map"){
				arg.setMap(true);
				Type tp = prim.typArgs().head();
				setArgType(arg, tp);
			} else {
				arg.setType(primtype);
			}
	
		}else if(pt instanceof TypeNumeric) {
			arg.setType("Decimal");
		}else if (pt instanceof TypeCon) {
			arg.setType("Object");
			TypeCon tc = (TypeCon) pt;
    		QualifiedName cqn = tc.name().identifier().qualifiedName();
    		arg.setParentType(cqn.toString());
		}else {
			throw new RuntimeException("Unsupported type: " + pt.toString());
		}
		
	}
	private static String getPrimType(String dt) {
		switch(dt) {
		case "PrimTypeParty":
			return "Party";
		case "PrimTypeContractId":
		case "PrimTypeText":
			return "String";
		case "PrimTypeBool":
			return "Boolean";
		case "PrimTypeInt64":
			return "Integer";
		case "PrimTypeDate":
			return "Date";
		case "PrimTypeTimestamp":
			return "Datetime";
		case "PrimTypeList":
			return "List";
		case "PrimTypeOptional":
			return "Optional";
		case "PrimTypeMap":
			return "Map";
		default:
			throw new RuntimeException("Unsupported type: " + dt);
			
		}
	}
}
