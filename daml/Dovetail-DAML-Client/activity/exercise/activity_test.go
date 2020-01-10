package exercise

import (
	"encoding/json"
	"fmt"
	"testing"

	damlcommon "github.com/TIBCOSoftware/dovetail-contrib/daml/Dovetail-DAML-Client/common"
	"github.com/project-flogo/core/activity"
	"github.com/project-flogo/core/data/mapper"
	"github.com/project-flogo/core/data/resolve"
	"github.com/project-flogo/core/support/test"
	"github.com/stretchr/testify/assert"
)

func TestRegister(t *testing.T) {

	ref := activity.GetRef(&Activity{})
	act := activity.Get(ref)

	assert.NotNil(t, act)
}

func TestCreate(t *testing.T) {

	mf := mapper.NewFactory(resolve.GetBasicResolver())
	iCtx := test.NewActivityInitContext(Settings{}, mf)
	act, err := New(iCtx)
	assert.Nil(t, err)
	assert.NotNil(t, act, "activity should not be nil")
}

func TestEmptyArgs(t *testing.T) {
	fmt.Println("testing empty args...")
	var empty struct{}
	req := damlcommon.ExerciseChoice{}
	req.TemplateID.ModuleName = "Iou"
	req.TemplateID.EntityName = "IouTransfer"
	req.ContractID = "#100:0"
	req.Choice = "IouTransfer_Accept"
	req.Argument = empty

	sreq, err := json.Marshal(req)
	if err != nil {
		fmt.Printf("err=%v\n", err)
	}
	fmt.Printf("empty=%s\n", string(sreq))
}

/*
func TestEval(t *testing.T) {

	defer func() {
		if r := recover(); r != nil {
			t.Failed()
			t.Errorf("panic during execution: %v", r)
		}
	}()

	mf := mapper.NewFactory(resolve.GetBasicResolver())
	iCtx := test.NewActivityInitContext(Settings{}, mf)
	act, err := New(iCtx)
	assert.Nil(t, err)

	tc := test.NewActivityContext(act.Metadata())
	v := `{"newOwner":"Bob"}`
	var input interface{}
	err = json.Unmarshal([]byte(v), &input)
	if err != nil {
		fmt.Errorf("json parse error: %v", err)
	}

	//conn := `{"name":"iou","host":"localhost","port":7575,"JWT":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsZWRnZXJJZCI6Ik15TGVkZ2VyIiwiYXBwbGljYXRpb25JZCI6ImZvb2JhciIsInBhcnR5IjoiQWxpY2UifQ.4HYfzjlYr1ApUDot0a6a4zB49zS_jrwRUOCkAiPMqo0","timeout":30}`
	conn := make(map[string]interface{})
	conn["name"] = "iou"
	conn["host"] = "localhost"
	conn["port"] = 7575
	conn["JWT"] = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsZWRnZXJJZCI6Ik15TGVkZ2VyIiwiYXBwbGljYXRpb25JZCI6ImZvb2JhciIsInBhcnR5IjoiQWxpY2UifQ.4HYfzjlYr1ApUDot0a6a4zB49zS_jrwRUOCkAiPMqo0"
	conn["timeout"] = 30
	conn["connectorType"] = "json-api"
	config := connection.Config{Ref: "github.com/TIBCOSoftware/dovetail-contrib/daml/Dovetail-DAML-Client/connector/connector", Settings: conn}
	err = connection.ResolveConfig(&config)
	if err != nil {
		fmt.Errorf("to config error: %v", err)
	}
	fmt.Printf("config=%v\n", config)
	mgr, err := connection.NewManager(&config)
	if err != nil {
		fmt.Errorf("to manager error: %v", err)
	}

	tc.SetInputObject(&Input{PackageID: "testKey", Template: "Iou:Iou", Choice: "Iou_Transfer", ContractID: "#29260:0", Input: input, Connection: mgr})
	fmt.Println("2")
	_, err = act.Eval(tc)
	if err != nil {
		fmt.Errorf("has error: %v", err)
	}

	//check output
	output := tc.GetOutput("output")
	sout, err := json.Marshal(output)
	fmt.Printf("output=%v\n", string(sout))
}
*/
