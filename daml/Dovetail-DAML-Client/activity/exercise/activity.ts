import {Observable} from "rxjs/Observable";
import {Injectable, Injector, Inject} from "@angular/core";
import {Http} from "@angular/http";
import {
    WiContrib,
    WiServiceHandlerContribution,
    IValidationResult,
    ValidationResult,
    IActivityContribution,
    WiContributionUtils,
    IConnectorContribution
} from "wi-studio/app/contrib/wi-contrib";

import * as lodash from "lodash";

@WiContrib({})
@Injectable()
export class ExerciseActivityContributionHandler extends WiServiceHandlerContribution {
    constructor(private injector: Injector, private http: Http,) {
        super(injector, http);
    }
   
    value = (fieldName: string, context: IActivityContribution): any | Observable<any> => {
        let conId = context.getField("contract").value;
        let template = context.getField("template").value;
        let choice = context.getField("choice").value;
        switch(fieldName) {
            case "contract":
                let connectionRefs = [];
                return Observable.create(observer => {
                    WiContributionUtils.getConnections(this.http, "Dovetail-DAML-Client").subscribe((data: IConnectorContribution[]) => {
                        data.forEach(connection => {
                            if ((<any>connection).isValid) {
                                for(let i=0; i < connection.settings.length; i++) {
                                    if(connection.settings[i].name === "display"){
                                        connectionRefs.push({
                                            "unique_id": WiContributionUtils.getUniqueId(connection),
                                            "name": connection.settings[i].value
                                        });
                                        break;
                                    }
                                }
                            }
                        });
                        observer.next(connectionRefs);
                    });
                });
            case "template":
                if(Boolean(conId) == false)
                    return null;

                return Observable.create(observer => {
                    WiContributionUtils.getConnection(this.http, conId)
                                        .map(data => data)
                                        .subscribe(data => {
                                            for (let setting of data.settings) {
                                                if (setting.name === "templates") {
                                                    observer.next(setting.value);
                                                    break;
                                                }
                                            }
                                        });
                });
                case "choice":
                        if(Boolean(conId) == false || Boolean(template) == false)
                            return null;
        
                        return Observable.create(observer => {
                            WiContributionUtils.getConnection(this.http, conId)
                                                .map(data => data)
                                                .subscribe(data => {
                                                    let choices = new Map();
                                                    for (let setting of data.settings) {
                                                        if (setting.name === "choices") {
                                                            setting.value.map(item => {
                                                                var s = item[1]
                                                                choices.set(item[0],s)
                                                            });
                                                            var cc = []
                                                            var cc = cc.concat(choices.get(template))
                                                            if(Boolean(cc) && cc.length > 0)
                                                                observer.next(cc);
                                                            else
                                                                observer.next([{
                                                                    "unique_id":"none",
                                                                    "name": "-- not defined --"
                                                                }])
                                                            break;
                                                        }
                                                    }
                                                });
                        });
            case "input":
                if(Boolean(conId) == false || Boolean(template) == false || Boolean(choice) == false)
                    return null;
                
                if(choice == "none")
                    return null

                return Observable.create(observer => {
                    this.getSchemas(conId).subscribe( schemas => {
                        var templschema = schemas.get(template + "." + choice)
                        observer.next(templschema);
                        
                    });
                });  
        }
        return null;
    }
 
    validate = (fieldName: string, context: IActivityContribution): Observable<IValidationResult> | IValidationResult => {
        return null;
    }

    getSchemas(conId):  Observable<any> {
        let schemas = new Map();
        return Observable.create(observer => {
            WiContributionUtils.getConnection(this.http, conId)
                            .map(data => data)
                            .subscribe(data => {
                                let schemas = new Map();
                                for (let setting of data.settings) {
                                    if(setting.name === "schemas") {
                                        setting.value.map(item => {
                                            var s = item[1]
                                            schemas.set(item[0],s)
                                        });
                                        observer.next(schemas);
                                        break;
                                    }
                                }
                            });
                        });
    }
}