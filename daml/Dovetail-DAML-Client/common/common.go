package common

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"time"

	daml "github.com/TIBCOSoftware/dovetail-contrib/daml/Dovetail-DAML-Client/connector/connector"
	"github.com/labstack/gommon/log"
	"github.com/pkg/errors"
	"github.com/project-flogo/core/activity"
	"github.com/project-flogo/core/data/schema"
	"github.com/project-flogo/core/support/connection"
)

type CreateContract struct {
	TemplateID Template    `json:"templateId"`
	Argument   interface{} `json:"argument"`
}

type ExerciseChoice struct {
	TemplateID Template    `json:"templateId,required"`
	ContractID string      `json:"contractId,required"`
	Choice     string      `json:"choice,required"`
	Argument   interface{} `json:"argument"`
}

type ExerciseChoiceResult struct {
	Status int                  `json:"status"`
	Result ExerciseChoiceTxnOut `json:"result,omitempty"`
	Errors []string             `json:"errors,omitempty"`
}

type QueryResult struct {
	Status int               `json:"status"`
	Result []CreatedContract `json:"result,omitempty"`
	Errors []string          `json:"errors,omitempty"`
}

type CreateContractResult struct {
	Status int             `json:"status"`
	Result CreatedContract `json:"result,omitempty"`
	Errors []string        `json:"errors,omitempty"`
}

type ExerciseChoiceTxnOut struct {
	Archived []ArchivedContract `json:"archived,omitempty"`
	Created  []CreatedContract  `json:"created,omitempty"`
}
type CreatedContract struct {
	TemplateID     Template    `json:"templateId,required"`
	ContractID     string      `json:"contractId,required"`
	WitnessParties interface{} `json:"witnessParties,omitempty"`
	AgreementText  string      `json:"agreementText,omitempty"`
	Signatories    interface{} `json:"signatories,omitempty"`
	Observers      interface{} `json:"observers,omitempty"`
	Argument       interface{} `json:"argument,omitempty"`
}

type ArchivedContract struct {
	TemplateID     Template    `json:"templateId,required"`
	ContractID     string      `json:"contractId,required"`
	WitnessParties interface{} `json:"witnessParties,omitempty"`
}

type Template struct {
	PackageID  string `json:"packageId,omitempty"`
	ModuleName string `json:"moduleName"`
	EntityName string `json:"entityName"`
}

type DAJsonAPIConn struct {
	Name    string `json:"name"`
	Host    string `json:"host"`
	Port    int    `json:"port"`
	JWT     string `json:"JWT"`
	Timeout int    `json:"timeout"`
}

//SendExerciseRequest will send exercise choice command to ledger
func SendExerciseRequest(req []byte, cnn connection.Manager) ([]byte, error) {
	config := cnn.(*daml.APIServerSharedConfigManager).GetClientConfiguration()

	if config.ConnectorType == "json-api" {
		url := fmt.Sprintf("http://%s:%d/command/exercise", config.Host, config.Port)

		return sendReqToDAJsonApi(req, url, config)
	}
	return nil, fmt.Errorf("Ledger connection type is not supported: %s", config.ConnectorType)
}

//SendQueryRequest will send query command to ledger
func SendQueryRequest(req []byte, cnn connection.Manager) ([]byte, error) {
	config := cnn.(*daml.APIServerSharedConfigManager).GetClientConfiguration()
	if config.ConnectorType == "json-api" {
		url := fmt.Sprintf("http://%s:%d/contracts/search", config.Host, config.Port)
		fmt.Printf("url=%s\n", url)
		fmt.Printf("req=%s\n", string(req))
		return sendReqToDAJsonApi(req, url, config)
	}
	return nil, fmt.Errorf("Ledger connection type is not supported: %s", config.ConnectorType)
}

//SendCreateRequest will send contract creation command to ledger
func SendCreateRequest(req []byte, cnn connection.Manager) ([]byte, error) {
	config := cnn.(*daml.APIServerSharedConfigManager).GetClientConfiguration()

	if config.ConnectorType == "json-api" {
		url := fmt.Sprintf("http://%s:%d/command/create", config.Host, config.Port)
		return sendReqToDAJsonApi(req, url, config)
	}
	return nil, fmt.Errorf("Ledger connection type is not supported: %s", config.ConnectorType)
}

func parseDAJsonApiConnInfo(cnninfo string) (*DAJsonAPIConn, error) {
	cnnconfig := &DAJsonAPIConn{}
	err := json.Unmarshal([]byte(cnninfo), cnnconfig)
	if err != nil {
		return nil, err
	}

	return cnnconfig, nil
}
func sendReqToDAJsonApi(req []byte, url string, config *daml.APIServerClientConfig) ([]byte, error) {
	client := &http.Client{Timeout: time.Second * time.Duration(config.ConnectionTimeout)}

	httpreq, err := http.NewRequest("POST", url, bytes.NewBuffer(req))
	if err != nil {
		return nil, err
	}

	auth := fmt.Sprintf("Bearer %s", config.JWT)
	httpreq.Header.Add("Authorization", auth)
	httpreq.Header.Add("Content-Type", "application/json")

	httpresp, err := client.Do(httpreq)
	if err != nil {
		return nil, err
	}
	defer httpresp.Body.Close()

	//if httpresp.StatusCode == http.StatusOK {
	bodyBytes, err := ioutil.ReadAll(httpresp.Body)
	if err != nil {
		return nil, err
	}

	return bodyBytes, nil
	//}
	//return nil, fmt.Errorf("Http request failed, status code: %d, message: %v", httpresp.StatusCode, httpresp.Body)
}

// GetActivityInputSchema returns schema of an activity input attribute
func GetActivityInputSchema(ctx activity.Context, name string) (string, error) {
	if sIO, ok := ctx.(schema.HasSchemaIO); ok {
		log.Debugf("schema IO: %v", sIO)
		s := sIO.GetInputSchema(name)
		if s != nil {
			log.Debugf("schema for attribute '%s': %T, %s\n", name, s, s.Value())
			return s.Value(), nil
		}
	}
	return "", errors.Errorf("schema not found for attribute %s", name)
}
