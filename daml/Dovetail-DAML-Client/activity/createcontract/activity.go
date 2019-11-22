package createcontract

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
	"time"

	damlcommon "github.com/TIBCOSoftware/dovetail-contrib/daml/Dovetail-DAML-Client/activity"
	"github.com/TIBCOSoftware/flogo-lib/core/data"
	"github.com/project-flogo/core/activity"
)

var activityMd = activity.ToMetadata(&Settings{}, &Input{}, &Output{})

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

	tplt := strings.Split(input.Template, ":")
	req := damlcommon.CreateContract{}
	req.TemplateID.ModuleName = tplt[0]
	req.TemplateID.EntityName = tplt[1]
	cobj, err := data.CoerceToComplexObject(input.Input)
	if err != nil {
		return false, err
	}
	req.Argument, err = data.CoerceToObject(cobj.Value)
	if err != nil {
		return false, err
	}

	sreq, err := json.Marshal(req)
	if err != nil {
		return false, err
	}
	fmt.Println(string(sreq))
	client := &http.Client{Timeout: time.Second * 30}
	httpreq, err := http.NewRequest("POST", "http://localhost:7575/command/create", bytes.NewBuffer(sreq))
	if err != nil {
		return false, err
	}
	fmt.Println("2")
	httpreq.Header.Add("Authorization", "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsZWRnZXJJZCI6Ik15TGVkZ2VyIiwiYXBwbGljYXRpb25JZCI6ImZvb2JhciIsInBhcnR5IjoiQWxpY2UifQ.4HYfzjlYr1ApUDot0a6a4zB49zS_jrwRUOCkAiPMqo0")
	httpreq.Header.Add("Content-Type", "application/json")
	fmt.Println("3")
	httpresp, err := client.Do(httpreq)
	if err != nil {
		fmt.Printf("4=%v\n", err)
		return false, err
	}
	defer httpresp.Body.Close()

	fmt.Printf("resp=%v\n", httpresp)
	return true, nil
}
