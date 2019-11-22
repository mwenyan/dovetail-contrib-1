package createcontract

import (
	"github.com/project-flogo/core/data/coerce"
)

// Settings of the activity
type Settings struct {
}

// Input of the activity
type Input struct {
	PackageID string      `md:"packageId,required"`
	Template  string      `md:"template,required"`
	Input     interface{} `md:"input"`
}

// Output of the activity
type Output struct {
	Code     int                    `md:"code"`
	Message  string                 `md:"message"`
	StateKey string                 `md:"key"`
	Result   map[string]interface{} `md:"result"`
}

// ToMap converts activity input to a map
func (i *Input) ToMap() map[string]interface{} {
	return map[string]interface{}{
		"packageId": i.PackageID,
		"template":  i.Template,
		"input":     i.Input,
	}
}

// FromMap sets activity input values from a map
func (i *Input) FromMap(values map[string]interface{}) error {

	var err error
	if i.PackageID, err = coerce.ToString(values["packageId"]); err != nil {
		return err
	}
	if i.Template, err = coerce.ToString(values["template"]); err != nil {
		return err
	}
	if i.Input, err = coerce.ToObject(values["input"]); err != nil {
		return err
	}

	return nil
}

// ToMap converts activity output to a map
func (o *Output) ToMap() map[string]interface{} {
	return map[string]interface{}{
		"code":    o.Code,
		"message": o.Message,
		"key":     o.StateKey,
		"result":  o.Result,
	}
}

// FromMap sets activity output values from a map
func (o *Output) FromMap(values map[string]interface{}) error {

	var err error
	if o.Code, err = coerce.ToInt(values["code"]); err != nil {
		return err
	}
	if o.Message, err = coerce.ToString(values["message"]); err != nil {
		return err
	}
	if o.StateKey, err = coerce.ToString(values["key"]); err != nil {
		return err
	}
	if o.Result, err = coerce.ToObject(values["result"]); err != nil {
		return err
	}

	return nil
}
