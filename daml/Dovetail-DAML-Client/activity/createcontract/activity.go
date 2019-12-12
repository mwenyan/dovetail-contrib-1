package createcontract

import (
	"encoding/json"
	"strings"

	damlcommon "github.com/TIBCOSoftware/dovetail-contrib/daml/Dovetail-DAML-Client/common"
	"github.com/project-flogo/core/activity"
	logger "github.com/project-flogo/core/support/log"
)

var activityMd = activity.ToMetadata(&Settings{}, &Input{}, &Output{})
var log = logger.ChildLogger(logger.RootLogger(), "daml.activity.createcontract")

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
	err = ctx.GetInputObject(input)
	if err != nil {
		return false, err
	}
	log.Debugf("input body = %v\n", input.Input)

	tplt := strings.Split(input.Template, ":")
	req := damlcommon.CreateContract{}
	req.TemplateID.ModuleName = tplt[0]
	req.TemplateID.EntityName = tplt[1]
	req.Argument = input.Input

	sreq, err := json.Marshal(req)
	if err != nil {
		return false, err
	}

	httpresp, err := damlcommon.SendCreateRequest(sreq, input.Connection)
	if err != nil {
		return false, err
	}

	var output interface{}
	err = json.Unmarshal(httpresp, &output)
	if err != nil {
		return false, err
	}

	ctx.SetOutput("output", output)
	log.Debugf("output body = %s\n", output)
	return true, nil
}
