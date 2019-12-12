package connector

import (
	"errors"
	dlog "log"
	"os"

	"git.tibco.com/git/product/ipaas/wi-contrib.git/connection/generic"
	"github.com/Shopify/sarama"
	"github.com/project-flogo/core/data/coerce"
	"github.com/project-flogo/core/data/metadata"
	"github.com/project-flogo/core/support/connection"
	"github.com/project-flogo/core/support/log"
)

var logCache = log.ChildLogger(log.RootLogger(), "kafka-connection")
var factory = &DamlLedgerServiceConnectorFactory{}

type Settings struct {
	ConnectionTimeout int    `md:"timeout"`
	Name              string `md:"display,required"`
	ConnectorType     string `md:"connectorType,required"`
	SSL               bool   `md:"enableSSL"`
	Host              string `md:"host"`
	Port              int    `md:"port"`
	JWT               string `md:"JWT"`
}

type APIServerClientConfig struct {
	Host              string
	Port              int
	JWT               string
	ConnectionTimeout int
	ConnectorType     string
	Name              string
}

func init() {
	if os.Getenv(log.EnvKeyLogLevel) == "DEBUG" {
		// Enable debug logs for sarama lib
		sarama.Logger = dlog.New(os.Stderr, "[dovetail-daml-connector]", dlog.LstdFlags)
	}

	err := connection.RegisterManagerFactory(factory)
	if err != nil {
		panic(err)
	}
}

type DamlLedgerServiceConnectorFactory struct {
}

func (*DamlLedgerServiceConnectorFactory) Type() string {
	return "DamlLedgerServiceConnector"
}

func (*DamlLedgerServiceConnectorFactory) NewManager(settings map[string]interface{}) (connection.Manager, error) {

	sharedConn := &APIServerSharedConfigManager{}
	var err error
	sharedConn.config, err = getClientConfig(settings)
	if err != nil {
		return nil, err
	}
	return sharedConn, nil
}

type APIServerSharedConfigManager struct {
	config *APIServerClientConfig
}

func (k *APIServerSharedConfigManager) Type() string {
	return "DamlLedgerServiceConnector"
}

func (k *APIServerSharedConfigManager) GetConnection() interface{} {
	return k
}

func (k *APIServerSharedConfigManager) GetClientConfiguration() *APIServerClientConfig {
	return k.config
}

func (k *APIServerSharedConfigManager) ReleaseConnection(connection interface{}) {

}

func (k *APIServerSharedConfigManager) Start() error {
	return nil
}

func (k *APIServerSharedConfigManager) Stop() error {

	return nil
}

func GetSharedConfiguration(conn interface{}) (connection.Manager, error) {

	var cManager connection.Manager
	var err error
	_, ok := conn.(map[string]interface{})
	if ok {
		cManager, err = handleLegacyConnection(conn)
	} else {
		cManager, err = coerce.ToConnection(conn)
	}

	if err != nil {
		return nil, err
	}

	return cManager, nil
}

func handleLegacyConnection(conn interface{}) (connection.Manager, error) {

	connectionObject, _ := coerce.ToObject(conn)
	if connectionObject == nil {
		return nil, errors.New("Connection object is nil")
	}

	id := connectionObject["id"].(string)

	cManager := connection.GetManager(id)
	if cManager == nil {

		connObject, err := generic.NewConnection(connectionObject)
		if err != nil {
			return nil, err
		}

		cManager, err = factory.NewManager(connObject.Settings())
		if err != nil {
			return nil, err
		}

		err = connection.RegisterManager(id, cManager)
		if err != nil {
			return nil, err
		}
	}

	return cManager, nil

}

func getClientConfig(settings map[string]interface{}) (*APIServerClientConfig, error) {
	connectionConfig := &APIServerClientConfig{}

	s := &Settings{}

	err := metadata.MapToStruct(settings, s, false)

	if err != nil {
		return nil, err
	}
	connectionConfig.ConnectionTimeout = s.ConnectionTimeout

	connectionConfig.Host = s.Host
	connectionConfig.Port = s.Port
	connectionConfig.JWT = s.JWT
	connectionConfig.ConnectionTimeout = s.ConnectionTimeout
	connectionConfig.ConnectorType = s.ConnectorType
	connectionConfig.Name = s.Name
	return connectionConfig, nil
}
