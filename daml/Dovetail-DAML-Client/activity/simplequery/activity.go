package simplequery

import (
	"encoding/json"
	"fmt"
	"strings"

	"github.com/TIBCOSoftware/dovetail-contrib/daml/Dovetail-DAML-Client/common"
	"github.com/pkg/errors"
	"github.com/project-flogo/core/activity"
	logger "github.com/project-flogo/core/support/log"
)

var activityMd = activity.ToMetadata(&Settings{}, &Input{}, &Output{})
var log = logger.ChildLogger(logger.RootLogger(), "daml.activity.simplequery")

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
	log.Debugf("query parameters: %+v\n", input.QueryParams)

	// extract query parameter types from queryParams schema
	var paramTypes map[string]QueryParam
	if input.QueryParams != nil {
		schema, err := common.GetActivityInputSchema(ctx, "queryParams")
		if err != nil {
			log.Error("schema not defined for queryParams\n")
			return false, errors.New("schema not defined for queryParams")
		}
		if paramTypes, err = getQueryParamTypes(schema); err != nil {
			log.Errorf("failed to parse parameter schema %+v\n", err)
			return false, errors.Wrap(err, "failed to parse parameter schema")
		}
	}
	log.Debugf("query parameters metadata:  %+v\n", paramTypes)
	queryStatement, err := prepareQueryStatement(strings.TrimSpace(input.Query), input.QueryParams, paramTypes)
	if err != nil {
		return false, err
	}

	tplt := strings.Split(input.Template, ":")
	query := fmt.Sprintf(`{"%%templates":[{"moduleName":"%s","entityName":"%s"}],%s`, tplt[0], tplt[1], string([]rune(queryStatement)[1:]))
	log.Debugf("query statement: %s\n", query)

	httpresp, err := common.SendQueryRequest([]byte(query), input.Connection)
	if err != nil {
		return false, err
	}

	var output interface{}
	err = json.Unmarshal(httpresp, &output)
	if err != nil {
		return false, err
	}

	ctx.SetOutput("Output", output)

	return true, nil
}

type QueryParam struct {
	FieldType string `json:"type"`
	Repeating bool   `json:"repeating"`
}

func getQueryParamTypes(metadata string) (map[string]QueryParam, error) {
	// extract object field name and type from JSON schema
	var objectProps struct {
		Props map[string]QueryParam `json:"properties"`
	}
	if err := json.Unmarshal([]byte(metadata), &objectProps); err != nil {
		log.Errorf("failed to extract properties from metadata: %+v", err)
		return nil, err
	}
	if objectProps.Props == nil {
		log.Debug("no parameter specified in metadata %s\n", metadata)
		return nil, nil
	}

	return objectProps.Props, nil
}

func prepareQueryStatement(query string, paramValues map[string]interface{}, paramTypes map[string]QueryParam) (string, error) {
	if paramTypes == nil {
		log.Debug("no parameter is defined for query\n")
		return query, nil
	}

	// collect replacer args
	var args []string
	for pname, ptype := range paramTypes {
		value, ok := paramValues[pname]
		if !ok {
			// set default values
			switch ptype.FieldType {
			case "number":
				value = 0
			case "boolean":
				value = false
			default:
				value = ""
			}
		}

		// collect string replacer args
		param := fmt.Sprintf("%v", value)
		if ptype.FieldType == "string" || ptype.FieldType == "date" {
			if jsonBytes, err := json.Marshal(value); err != nil {
				log.Debugf("failed to marshal value %v: %+v\n", value, err)
				param = "null"
			} else {
				param = string(jsonBytes)
			}
		}
		args = append(args, fmt.Sprintf(`"$%s"`, pname), param)
	}
	log.Debugf("query replacer args %v\n", args)

	// replace query parameters with values
	r := strings.NewReplacer(args...)
	return r.Replace(query), nil
}
