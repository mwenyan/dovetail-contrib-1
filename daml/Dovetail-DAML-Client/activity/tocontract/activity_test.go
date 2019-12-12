package parsecontract

import (
	"encoding/json"
	"fmt"
	"testing"

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

	v := `{"newOwner":"Bob"}`
	var input interface{}
	err = json.Unmarshal([]byte(v), &input)
	if err != nil {
		fmt.Errorf("json parse error: %v", err)
	}

	conn := `{"name":"iou","host":"localhost","port":7575,"JWT":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsZWRnZXJJZCI6Ik15TGVkZ2VyIiwiYXBwbGljYXRpb25JZCI6ImZvb2JhciIsInBhcnR5IjoiQWxpY2UifQ.4HYfzjlYr1ApUDot0a6a4zB49zS_jrwRUOCkAiPMqo0","timeout":30}`
	err = json.Unmarshal([]byte(conn), &input)
	tc.SetInputObject(&Input{Input: input})
	_, err = act.Eval(tc)
	if err != nil {
		fmt.Errorf("has error: %v", err)
	}

	//check output
	output := tc.GetOutput("output")
	sout, err := json.Marshal(output)
	fmt.Printf("output=%v\n", string(sout))
}
