package exercise

import (
	"encoding/json"
	"fmt"
	"strings"

	damlcommon "github.com/TIBCOSoftware/dovetail-contrib/daml/Dovetail-DAML-Client/common"
	"github.com/project-flogo/core/activity"
	logger "github.com/project-flogo/core/support/log"
)

var activityMd = activity.ToMetadata(&Settings{}, &Input{}, &Output{})
var log = logger.ChildLogger(logger.RootLogger(), "daml.activity.exercise")

func init() {
	_ = activity.Register(&Activity{}, New)
}

// Activity is a stub for creating a contract
type Activity struct {
}

// New creates a new Activity
func New(ctx activity.InitContext) (activity.Activity, error) {
	return &Activity{}, nil
}

// Metadata implements activity.Activity.Metadata
func (a *Activity) Metadata() *activity.Metadata {
	return activityMd
}

// Eval implements activity.Activity.Eval
func (a *Activity) Eval(ctx activity.Context) (done bool, err error) {
	input := &Input{}
	var empty struct{}
	err = ctx.GetInputObject(input)
	if err != nil {
		return false, err
	}

	log.Debugf("contract ID = %s\n", input.ContractID)
	log.Debugf("input body = %v\n", input.Input)

	tplt := strings.Split(input.Template, ":")
	req := damlcommon.ExerciseChoice{}
	req.TemplateID.ModuleName = tplt[0]
	req.TemplateID.EntityName = tplt[1]
	req.ContractID = input.ContractID
	req.Choice = input.Choice
	if input.Input == nil {
		req.Argument = empty
	} else {
		req.Argument = input.Input
	}

	sreq, err := json.Marshal(req)
	if err != nil {
		return false, err
	}

	httpresp, err := damlcommon.SendExerciseRequest(sreq, input.Connection)
	if err != nil {
		return false, err
	}

	var output interface{}
	err = json.Unmarshal(httpresp, &output)
	if err != nil {
		return false, err
	}

	finalout := damlcommon.ExerciseChoiceResult{}
	txns := damlcommon.ExerciseChoiceTxnOut{Archived: make([]damlcommon.ArchivedContract, 0), Created: make([]damlcommon.CreatedContract, 0)}
	mapoutput := output.(map[string]interface{})
	log.Debugf("output from json api server = %v\n", mapoutput)

	for k, v := range mapoutput {
		if k == "status" {
			finalout.Status = int(v.(float64))
		} else if k == "result" {
			result, ok := v.(map[string]interface{})
			if ok {
				for _, rslt := range result["contracts"].([]interface{}) {
					r := rslt.(map[string]interface{})
					txns = processResults(txns, r)
				}
			} else {
				return false, fmt.Errorf("Error processing exercise choice output")
			}

			finalout.Result = txns
		} else if k == "errors" {
			finalout.Errors = make([]string, 0)
			errors := v.([]interface{})
			for _, e := range errors {
				finalout.Errors = append(finalout.Errors, e.(string))
			}
		}
	}

	ctx.SetOutput("output", finalout)
	log.Debugf("output body = %s\n", output)
	return true, nil
}

func processResults(txns damlcommon.ExerciseChoiceTxnOut, result map[string]interface{}) damlcommon.ExerciseChoiceTxnOut {
	if rv, ok := result["archived"]; ok {
		maprv := rv.(map[string]interface{})
		archived := damlcommon.ArchivedContract{}
		archived.WitnessParties = maprv["witnessParties"]
		archived.ContractID = maprv["contractId"].(string)
		template := maprv["templateId"].(map[string]interface{})
		archived.TemplateID.PackageID = template["packageId"].(string)
		archived.TemplateID.ModuleName = template["moduleName"].(string)
		archived.TemplateID.EntityName = template["entityName"].(string)
		txns.Archived = append(txns.Archived, archived)
	} else if rv, ok := result["created"]; ok {
		maprv := rv.(map[string]interface{})
		created := damlcommon.CreatedContract{}
		created.WitnessParties = maprv["witnessParties"]
		created.ContractID = maprv["contractId"].(string)
		template := maprv["templateId"].(map[string]interface{})
		created.TemplateID.PackageID = template["packageId"].(string)
		created.TemplateID.ModuleName = template["moduleName"].(string)
		created.TemplateID.EntityName = template["entityName"].(string)
		if agree, ok := maprv["agreementText"]; ok {
			created.AgreementText = agree.(string)
		} else {
			created.AgreementText = ""
		}
		created.Signatories = maprv["signatories"]
		created.Observers = maprv["obervers"]
		created.Argument = maprv["argument"]
		txns.Created = append(txns.Created, created)
	}
	return txns
}
