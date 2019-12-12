package simplequery

import (
	"encoding/json"
	"testing"

	"github.com/project-flogo/core/activity"
	"github.com/project-flogo/core/data/mapper"
	"github.com/project-flogo/core/data/resolve"
	"github.com/project-flogo/core/support/test"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
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

func TestQueryParams(t *testing.T) {

	defer func() {
		if r := recover(); r != nil {
			t.Failed()
			t.Errorf("panic during execution: %v", r)
		}
	}()

	//	mf := mapper.NewFactory(resolve.GetBasicResolver())
	//	iCtx := test.NewActivityInitContext(Settings{}, mf)
	//	act, err := New(iCtx)
	//	assert.Nil(t, err)

	query := `{
            "sParam": "$sparam",
            "iParam": {
                "$gt": "$iparam"
            },
            "bParam": "$bparam"
        }`
	params := `{
        "sparam": "hello",
        "iparam": 100,
        "bparam": true}`
	types := `{
        "sparam": {"type":"string", "repeating":false},
        "iparam": {"type": "number", "repeating":false},
		"bparam": {"type":"boolean", "repeating":false}
		}`
	result := `{
            "sParam": "hello",
            "iParam": {
                "$gt": 100
            },
            "bParam": true
        }`
	var queryParams map[string]interface{}
	err := json.Unmarshal([]byte(params), &queryParams)
	require.NoError(t, err, "failed to parse queryParams")
	var paramTypes map[string]QueryParam
	err = json.Unmarshal([]byte(types), &paramTypes)
	require.NoError(t, err, "failed to parse paramTypes")
	stmt, err := prepareQueryStatement(query, queryParams, paramTypes)
	require.NoError(t, err, "failed to prepare query statement")
	assert.Equal(t, result, stmt, "unexpected resulting query statement")

}
