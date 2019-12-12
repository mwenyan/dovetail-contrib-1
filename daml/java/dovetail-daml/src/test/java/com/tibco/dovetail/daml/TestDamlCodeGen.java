package com.tibco.dovetail.daml;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.Test;
import com.digitalasset.daml.lf.archive.*;
import com.digitalasset.daml.lf.codegen.InterfaceTree;
import com.digitalasset.daml.lf.codegen.InterfaceTrees;
import com.digitalasset.daml.lf.codegen.Module;
import com.digitalasset.daml.lf.data.Ref.Identifier;
import com.digitalasset.daml.lf.iface.DataType;
import com.digitalasset.daml.lf.iface.DefDataType;
import com.digitalasset.daml.lf.iface.DefTemplate;
import com.digitalasset.daml.lf.iface.Interface;
import com.digitalasset.daml.lf.iface.PrimTypeContractId;
import com.digitalasset.daml.lf.iface.PrimTypeParty;
import com.digitalasset.daml.lf.iface.Record;
import com.digitalasset.daml.lf.iface.Type;
import com.digitalasset.daml.lf.iface.TypeCon;
import com.digitalasset.daml.lf.iface.TypeNumeric;
import com.digitalasset.daml.lf.iface.TypePrim;
import com.digitalasset.daml.lf.iface.TemplateChoice;

import com.digitalasset.daml.lf.iface.reader.DamlLfArchiveReader;
import com.digitalasset.daml.lf.iface.reader.Errors;
import com.digitalasset.daml.lf.iface.reader.InterfaceReader;
import com.digitalasset.daml.lf.iface.reader.InterfaceReader.InvalidDataTypeDefinition;
import com.digitalasset.daml.lf.language.Ast;
import com.digitalasset.daml.lf.language.Ast.Package;
import com.digitalasset.daml_lf_dev.DamlLf;
import com.digitalasset.daml_lf_dev.DamlLf.Archive;
import com.digitalasset.daml_lf_dev.DamlLf.ArchivePayload;

import com.google.gson.Gson;

import scala.Function1;
import scala.Immutable;
import scala.Tuple2;

import scala.collection.JavaConversions;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.collection.Traversable;
import scala.util.Try;
import scalaz.$bslash$div;

import com.daml.ledger.javaapi.data.GetPackageResponse;

import com.daml.ledger.rxjava.DamlLedgerClient;
import com.daml.ledger.rxjava.PackageClient;
import com.digitalasset.codegen.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;



public class TestDamlCodeGen {

	@Test
	public void test() {
		try {
		/*
			
			DamlLedgerClient client = DamlLedgerClient.forHostWithLedgerIdDiscovery("localhost", 6865, Optional.empty());
			 client.connect();
			PackageClient pclient = client.getPackageClient();
			pclient.listPackages().blockingForEach(s -> System.out.println(s));
			String pid = "0e070f5e735cea63f0ed08bcaff4987a236174c94a2d6fd89950db1bc34ca207";
			GetPackageResponse resp = pclient.getPackage(pid).blockingGet();
			
			ArchivePayload payload = ArchivePayload.parseFrom(resp.getArchivePayload());
			
			
		//	InputStream s = new FileInputStream("/Users/mwenyan@tibco.com/digitalassets/quickstart/.daml/dist/quickstart-0.0.1.dar");
			//Archive archive = Archive.parseFrom(s);
			Tuple2<Object, Interface> pkg = InterfaceReader.readInterface(new Tuple2(pid, payload));
			
			pkg._2.getTypeDecls().forEach((n, i) -> {
				
				System.out.println("name:"+ n.name().toString());
				System.out.println("module:"+ n.module().toString());
				
				
				i.getTemplate().ifPresent(t -> {
					
					t.getChoices().forEach((k, c) -> {
						System.out.println("choice key:"+ k);
					
						c.param().mapTypeVars(new Function1<TypeVar, Type>() {

							@Override
							public Type apply(TypeVar v1) {
								System.out.println("param:" + v1.name());
								
								return null;
							}
							
						});
					});
					
					
				});
				
			});
			
			Seq infs = JavaConverters.asScalaIteratorConverter(Arrays.asList(pkg._2).iterator()).asScala().toSeq();
			Collection<InterfaceTree> col = JavaConversions.asJavaCollection(InterfaceTrees.fromInterfaces(infs).interfaceTrees().iterator().toIterable());
			col.forEach(tr -> {
				
				scala.collection.immutable.Map<String, Module> mods = tr.modules();
				
				JavaConversions.asJavaCollection(mods.keySet().iterator().toIterable()).forEach(s -> {
					System.out.println("m2: " + s);
					
				});	
			
			});
			
			scala.collection.immutable.Map<String, Module> iou = InterfaceTrees.fromInterfaces(infs).interfaceTrees().iterator().next().modules();
			
			*/
			
		/*	scala.collection.mutable.Map<String, DamlLfPackage> pkgs = scala.collection.mutable.Map$.MODULE$.empty();
			scala.collection.mutable.Map<Identifier, Template> tmpls = scala.collection.mutable.Map$.MODULE$.empty();
			scala.collection.mutable.Map<Identifier, DefDataType<Type, Type>> types = scala.collection.mutable.Map$.MODULE$.empty();
			PackageRegistry registry = PackageRegistry.apply(pkgs, tmpls, types);
			
			registry = registry.withPackages(scala.collection.immutable.List$.MODULE$.empty().$colon$colon(pkg._2));
			Collection<com.digitalasset.navigator.model.Template> col = JavaConversions.asJavaCollection(registry.allTemplates().iterator().toIterable());
			col.forEach(t -> {
				t.parameter().getTypArgs().forEach(a -> System.out.println(a.toString()));
			});*/
			
			/*scala.collection.immutable.List<Tuple2<String, Package>> lpkg = pkg.toList();
			
			Collection<Tuple2<String, Package>> col = JavaConversions.asJavaCollection(lpkg.iterator().toIterable());
			col.forEach(t -> {
				System.out.println(t._1);
				System.out.println(t._2);
			});*/
			
			String darf = "/Users/mwenyan@tibco.com/digitalassets/ex-models/airline/.daml/dist/airline.dar";
			//String darf = "/Users/mwenyan@tibco.com/digitalassets/ex-repo-market/.daml/dist/ex-repo-market-2.0.0.dar";
			//String darf = "/Users/mwenyan@tibco.com/digitalassets/ex-collateral/.daml/dist/ex-collateral-1.0.0.dar";
			//String darf = "/Users/mwenyan@tibco.com/digitalassets/ex-bond-trading/target/BondTradingMain.dar";
			//String darf ="/Users/mwenyan@tibco.com/digitalassets/quickstart/.daml/dist/quickstart-0.0.1.dar";
			File lfFile = new java.io.File(darf);
			Try<Dar<Tuple2<String, ArchivePayload>>> dar = DarReader.apply().readArchiveFromFile(lfFile);
		
				  Archive archive = DamlLf.Archive.parser().parseFrom(dar.get().main()._2.toByteArray());
				  Tuple2<Errors<Object, InvalidDataTypeDefinition>, Interface> out = InterfaceReader.readInterface(dar.get().main());
				  
					
				       out._2.getTypeDecls().forEach((qn, intf) -> {
				    	   System.out.println("qn=" + qn);
				    	   
				    	//   Gson gson = new Gson();
					    //    String json = gson.toJson(intf); 
					    //   System.out.println(json);
					     
				    	   
				    	  DataType<Type, Type> dt = intf.getType().dataType(); 
				    	  if(dt instanceof Record) {
				    		  Record rec = (Record)dt;
				    		  rec.getFields().forEach(f -> {
				    			  
				    			  Tuple2 tuple = (Tuple2)f;
				    			  String fn = (String) tuple._1;
				    			  System.out.println("pn=" + fn);
				    			  Type type = (Type) tuple._2;
				    			  if(type instanceof TypePrim) {
				    				  TypePrim prim = (TypePrim) type;
				    				  if(prim.typ().toString() == "PrimTypeParty") {
				    					  System.out.println("PrimTypeParty");
				    				  } else if(prim.typ().toString() == "PrimTypeContractId") {
				    					  System.out.println("PrimTypeContractId");
				    					  prim.getTypArgs().forEach(cid -> {
				    						  TypeCon tcon = (TypeCon)cid;
				    						  System.out.println(tcon.name().identifier().packageId());
				    						  System.out.println(tcon.name().identifier().qualifiedName().module());
				    						  System.out.println(tcon.name().identifier().qualifiedName().name());
				    					  });
				    				  } else if(prim.typ().toString() == "PrimTypeText") {
				    					  System.out.println("PrimTypeText");
				    				  } else if(prim.typ().toString() == "PrimTypeList") {
				    					  System.out.println("+++++"+ prim.getTypArgs());
				    					  Type tp = prim.typArgs().head();
				    					  if(tp instanceof TypePrim) {
				    						  System.out.println("TypePrim+++++"+tp);
				    					  } else if(tp instanceof TypeCon) {
				    						  System.out.println("TypeConn+++++"+tp);
				    					  }
				    					  
				    				  }
				    				  else {
				    					  System.out.println("---"+ prim.typ());
				    					  System.out.println("---"+ prim.getTypArgs());
				    				  }
				    				
				    			  } else if(type instanceof TypeNumeric) {
				    				 
				    			  } else if (type instanceof TypeCon) {
				    				  System.out.println("---"+ type.toString());
				    			  }
				    		  });
				    	  }
				    	  
				    	  if(intf.getTemplate().isPresent()){
				    		 DefTemplate<Type> tpl = intf.getTemplate().get();
				    		
				    		 tpl.getChoices().forEach((k,c) -> {
				    			 System.out.println("k=======" + k);
				    			 System.out.println("r=======" + c.returnType());
				    			 Type rt = c.returnType();
				    			 if(rt instanceof TypePrim) {
				    				 TypePrim rpt = (TypePrim)rt;
				    				 System.out.println("   rpt=======" + rpt.typ());
				    				 System.out.println("   rpt arg=======" + rpt.typ());
				    			 } else if(rt instanceof TypeCon) {
				    				 TypeCon rtc = (TypeCon)rt;
				    				 System.out.println("   rtc=======" + rtc.name().identifier());
				    				 System.out.println("   rtc arg=======" + rtc.getTypArgs());
				    			 }
				    			 
				    			 System.out.println("p=======" + c.param());
				    		 });
				    		
				    	  }
				       });

			  
		}catch(Exception e) {
			e.printStackTrace();
			
		}
	}
	
	
}
