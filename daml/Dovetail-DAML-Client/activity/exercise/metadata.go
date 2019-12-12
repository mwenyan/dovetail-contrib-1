package exercise

import (
	daml "github.com/TIBCOSoftware/dovetail-contrib/daml/Dovetail-DAML-Client/connector/connector"
	"github.com/project-flogo/core/data/coerce"
	"github.com/project-flogo/core/support/connection"
)

// Settings of the activity
type Settings struct {
}

// Input of the activity
type Input struct {
	PackageID  string             `md:"packageId,required"`
	Template   string             `md:"template,required"`
	Choice     string             `md:"choice,required"`
	ContractID string             `md:"contractId,required"`
	Input      interface{}        `md:"input"`
	Connection connection.Manager `md:"connector,required"`
}

// Output of the activity
type Output struct {
	Output interface{} `md:"output"`
}

// ToMap converts activity input to a map
func (i *Input) ToMap() map[string]interface{} {
	return map[string]interface{}{
		"packageId":  i.PackageID,
		"template":   i.Template,
		"choice":     i.Choice,
		"contractId": i.ContractID,
		"input":      i.Input,
		"connector":  i.Connection,
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
	if i.Choice, err = coerce.ToString(values["choice"]); err != nil {
		return err
	}
	if i.ContractID, err = coerce.ToString(values["contractId"]); err != nil {
		return err
	}
	i.Input = values["input"]

	i.Connection, err = daml.GetSharedConfiguration(values["connector"])
	if err != nil {
		return err
	}
	return nil
}

// ToMap converts activity output to a map
func (o *Output) ToMap() map[string]interface{} {
	return map[string]interface{}{
		"output": o.Output,
	}
}

// FromMap sets activity output values from a map
func (o *Output) FromMap(values map[string]interface{}) error {

	var err error
	if o.Output, err = coerce.ToObject(values["output"]); err != nil {
		return err
	}

	return nil
}
