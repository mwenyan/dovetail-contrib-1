import { Injectable, Injector } from "@angular/core";
import { WiContrib, WiServiceHandlerContribution, WiContributionUtils,AUTHENTICATION_TYPE } from "wi-studio/app/contrib/wi-contrib";
import { IConnectorContribution } from "wi-studio/common/models/contrib";
import { IValidationResult, ValidationResult, ValidationError } from "wi-studio/common/models/validation";
import { IActionResult, ActionResult } from "wi-studio/common/models/contrib";
import { Observable } from "rxjs/Observable";
import {Http} from "@angular/http";

@Injectable()
@WiContrib({})
export class ContractConnectorService extends WiServiceHandlerContribution {
    constructor( private injector: Injector, private http: Http) {
        super(injector, http);
    }

    value = (fieldName: string, context: IConnectorContribution): Observable<any> | any => {
       
        return null;    
    }

    validate = (fieldName: string, context: IConnectorContribution): Observable<IValidationResult> | IValidationResult => {
       
        return null;
    }

    action = (name: string, context: IConnectorContribution): Observable<IActionResult> | IActionResult => {

       if(name === "Done"){
            var contract = this.loadContract(context)
            if(contract){
                for(let i=0; i<context.settings.length; i++){
                    if(context.settings[i].name==="templates"){
                        context.settings[i].value = contract.templates;
                    } else if(context.settings[i].name==="schemas"){
                        context.settings[i].value = contract.schemas;
                    } else if(context.settings[i].name==="choices"){
                        context.settings[i].value = contract.choices;
                    } else if(context.settings[i].name==="packageId"){
                        context.settings[i].value = contract.packageId
                    }
                }
            }
      
            return Observable.create(observer => {
                let actionResult = {
                    context: context,
                    authType: AUTHENTICATION_TYPE.BASIC,
                    authData: {}
                }
                observer.next(ActionResult.newActionResult().setResult(actionResult));
            })
        } 
        return null
          
    }

    getFieldValue(context, fieldName): any {
        for(let i=0; i<context.settings.length; i++){
            if(context.settings[i].name=== fieldName){
                return context.settings[i].value;
            }
        }
    }

    loadContract(context):any{
        var model = context.getField("contractFile").value
        if(model){
            var json = JSON.parse(atob(model.content.split(",")[1].trim()))
            var daml = {packageId:"", templates:[], choices:new Map(), schemas:new Map()}
            var dataobs = new Map()

            daml.packageId = json.packageId
            for(var obj in json.dataObjects) {
                dataobs.set(obj, json.dataObjects[obj])
            }
           
            for(var t in json.templates){
                daml.templates.push(t)
                daml.schemas.set(t, JSON.stringify(this.createSchema(dataobs, dataobs.get(t))))

                var tch = []
                var choices = json.templates[t].choices
               
                for (var c of choices){
                    tch.push(c.name)
                    var schema = this.createSchema(dataobs, c)
                    if(Boolean(schema))
                        daml.schemas.set(t+"."+c.name, JSON.stringify(schema))
                }
                daml.choices.set(t,tch)
            }

            return daml
        }
        return null
    }
  
    createSchema(dataobjs, choice): any{
        if(Boolean(choice.arguments) && choice.arguments.length > 0){
            var schema = {type: "object", properties:{}, required:[]}
            var args = choice.arguments
            for(var a of args){
                var p = {}
                if(a.isList){
                    p["type"] = "array"
                    p["items"] = this.toFieldJson(dataobjs, a)
                } else if(a.isMap){
                    p["type"] = "object"
                } else {
                    p = this.toFieldJson(dataobjs, a)
                }

                schema.properties[a.name] = p
                if(!a.isOptional)
                    schema.required.push(a.name)
            }
            return schema
        } else {
            return {}
        }
    }


    isPrimitive(type){
        switch(type){
            case "Object":
                return false;
            default:
                return true;
        }
    }

    toSimpleJsonType(prim):any{
        switch (prim){
            case "String":
            case "Boolean":
            case "Integer":
                return {type: prim.toLowerCase()}
            case "Date":
                return {type: "string", "format": "date"}
            case "Datetime":
                return {type: "string", "format": "date-time"}
            case "Party":
            case "Decimal":
                return {type: "string"}
        }
    }

    toFieldJson(dataobjs, arg):any{
        if(this.isPrimitive(arg.type)){
            return this.toSimpleJsonType(arg.type)
        } else {
            if(dataobjs.has(arg.parentType)){
                var obj = dataobjs.get(arg.parentType)
                if(obj.isEnum){
                    return {type: "string"}
                } else if(obj.isVariant){
                    var p = {type: "object", properties: {"oneOf": {type: "object", properties:{}}}}
                    for(var vf in obj.varTypes){
                        p.properties.oneOf.properties[vf] = this.createSchema(dataobjs, dataobjs.get(obj.varTypes[vf]))
                    }
                    return p
                } else {
                    return this.createSchema(dataobjs, obj)
                }
            } else {
                return {type: "any"}
            }
            
        }
    }
}
