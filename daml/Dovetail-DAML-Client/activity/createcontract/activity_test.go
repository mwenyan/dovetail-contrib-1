package createcontract

import (
	"encoding/json"
	"fmt"
	"testing"

	"github.com/TIBCOSoftware/flogo-lib/core/data"
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
	inputmp := make(map[string]interface{})
	inputmp["Value"] = `{"issuer":"Alice","amount":"10000","currency":"USD","owner":"Alice"}`
	v, err := json.Marshal(inputmp)
	complexObject := &data.ComplexObject{}
	err = json.Unmarshal([]byte(v), complexObject)

	tc.SetInputObject(&Input{PackageID: "testKey", Template: "Iou:Iou", Input: complexObject})

	_, err = act.Eval(tc)
	if err != nil {
		fmt.Errorf("has error: %v\n", err)
	}
	//check result attr
}
