package createcontract

import (
	"encoding/json"
	"fmt"
	"testing"

	daml "github.com/TIBCOSoftware/dovetail-contrib/daml/Dovetail-DAML-Client/connector/connector"
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
	//	inputmp := make(map[string]interface{})
	//	inputmp["Value"] = `{"observers":[],"issuer":"Alice","amount":"10000","currency":"USD","owner":"Alice"}`
	//	v, err := json.Marshal(inputmp)
	//	complexObject := &data.ComplexObject{}
	//	err = json.Unmarshal([]byte(v), complexObject)

	v := `{"observers":[],"issuer":"Alice","amount":"10000","currency":"USD","owner":"Alice"}`
	var input interface{}
	err = json.Unmarshal([]byte(v), &input)
	if err != nil {
		fmt.Errorf("json parse error: %v", err)
	}
	conn := make(map[string]interface{})
	conn["display"] = "iou"
	conn["host"] = "localhost"
	conn["port"] = 7575
	conn["JWT"] = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsZWRnZXJJZCI6Ik15TGVkZ2VyIiwiYXBwbGljYXRpb25JZCI6ImZvb2JhciIsInBhcnR5IjoiQWxpY2UifQ.4HYfzjlYr1ApUDot0a6a4zB49zS_jrwRUOCkAiPMqo0"
	conn["connectorType"] = "json-api"
	conn["timeout"] = 30

	factory := &daml.DamlLedgerServiceConnectorFactory{}
	mgr, err := factory.NewManager(conn)
	if err != nil {
		fmt.Errorf("can't create connection manager: %v", err)
	}

	err = tc.SetInputObject(&Input{PackageID: "testKey", Template: "Iou:Iou", Input: input, Connection: mgr})
	if err != nil {
		fmt.Errorf("set input error: %v", err)
	}

	_, err = act.Eval(tc)
	if err != nil {
		fmt.Errorf("has error: %v", err)
	}

	//check output
	output := tc.GetOutput("output")
	fmt.Printf("output=%v\n", output)
}
