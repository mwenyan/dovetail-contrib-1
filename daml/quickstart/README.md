---
title: quick start
weight: 4603
---

We will create an application that integrates with the [IOU example provided by DAML SDK Quickstart guide](https://docs.daml.com/getting-started/quickstart.html). Please refer the quickstart guide on the use case details and install daml SDK as instructed. 

You must have DAML SDK, java 1.8 and above, Tibco Enterprise Flogo 2.8, and docker installed.

## Setup Quickstart daml project

- **download quickstart daml project**
```
daml new quickstart quickstart-java
```
- **build DAR file**
```
cd quickstart

daml build
```
- **start up sandbox**
```
daml sandbox --wall-clock-time --ledgerid MyLedger .daml/dist/quickstart-0.0.1.dar
```
- **start up navigator**
```
daml navigator server
```
- **start up json-api server**
```
json-api --ledger-host  localhost --ledger-port 6865 --http-port 7575
```
## Develop integration application

- **download** all files from the [artifacts](artifacts/) folder

  we assume you are running all commands from the parent folder of artifacts

- **extracts** metadata from dar file
```
java -jar artifacts/daml-parser-0.0.1-SNAPSHOT-shaded.jar -a <path to>/quickstart/.daml/dist/quickstart-0.0.1.dar -o <path to output>
```
- **upload** artifacts/Dovetail-DAML-Client.zip into Tibco Enterprise Flogo Studio or Tibco Cloud Integration from Extensions tab

- **import** DAR metadata
  * from studio Connections tab -> Add Connection -> select "Import DAML Metadata"
  * browse to you metadata file
  * name: iou

- **configure** json-api connection information for Party Alice
  * from studio Connections tab -> Add Connection -> select "DAML Ledger Service Connector"
  * enter connection information
    * name: alice iou server
    * host: use IP address instead of localhost, so you can run testing within the studio
    * port: 7575
    * JWT: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsZWRnZXJJZCI6Ik15TGVkZ2VyIiwiYXBwbGljYXRpb25JZCI6ImZvb2JhciIsInBhcnR5IjoiQWxpY2UifQ.4HYfzjlYr1ApUDot0a6a4zB49zS_jrwRUOCkAiPMqo0
    * timeout: 30

- **configure** json-api connection information for Party Bob
  * from studio Connections tab -> Add Connection -> select "DAML Ledger Service Connector"
  * enter connection information
    * name: bob iou server
    * host: use IP address instead of localhost, so you can run testing within the studio
    * port: 7575
    * JWT: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsZWRnZXJJZCI6Ik15TGVkZ2VyIiwiYXBwbGljYXRpb25JZCI6ImZvb2JhciIsInBhcnR5IjoiQm9iIn0.2LE3fAvUzLx495JWpuSzHye9YaH3Ddt4d2Pj0L1jSjA
    * timeout: 30

- **configure** a kafka connection
  * from studio Connections tab -> Add Connection -> select "Apache Kafka Client Configuration"
    * name: kafka
    * brokers: 192.168.1.3:19092

- **develop** integration application

  We will build two simple flows, 
  * Alice will issue an IOU and transfer it to Bob
  * Bob will receive the IouTranfer.created event from Kafka and accept the transfer

#### Steps to create the application
 1. create an application "iou"
 2. add App properties
   * add a group "bob"
     * add group variable "iou_transfer_topic": ioutransfer.created
     * add group variable "consumer_group_name": bob
 3. create a flow alice_issue_iou_to_bob
   * add a trigger "ReceiveHTTPMessage"
     * port: 9090
     * method: POST
     * path: /alice_issue_iou_to_bob
     * request schema: 
        ```
        {
          "amount": 1000
        }
        ```
   * edit flow ouput schema
      ```
      {
          "type": "object",
          "title": "Inputs",
          "properties": {
              "code": {
                  "type": "integer",
                  "required": false
              },
              "data": {
                  "$schema": "http://json-schema.org/draft-04/schema#",
                  "type": "object",
                  "properties": {
                      "data": {
                          "type": "string"
                      }
                  }
              }
          },
          "required": []
        }
        ```
   * add Dovetail-Client-DAML/CreateContract activity
     * select iou from DAML package dropdown
     * select Iou:Iou from contract template dropdown
     * select "alice iou server" from ledger service dropdown
     * map Input
   * add a branch
     * condition: $activity[CreateContract].output.status == 200
     * add Dovetail-Client-DAML/ExerciseContractChoice activity
        * select iou from DAML package dropdown
        * select Iou:Iou from contract template dropdown
        * select Iou_Transfer from contract choice dropdown
        * select "alice iou server" from ledger service dropdown
        * map Input
     * add Return, and map
   * add a branch
     * condition: $activity[CreateContract].output.status != 200
     * add default/ThrowError activity

  4. create a flow bob_accept_iou_transfer
   * add KafkaConsumer trigger
     * select kafka connection
     * topic: use App property bob.iou_transfer_toic
     * consumer group: use App property bob.consumer_group_name
     * value deserializer: String
     * Output settings
       * add following headers of type "string"
         * APPID
         * FLOWID
         * TXNID
         * CMDID
     * Map to flow inputs
       * just map the stringValue to $trigger.stringValue
   * edit Flow outputs schema
     ```
     {
          "$schema": "http://json-schema.org/draft-04/schema#",
          "type": "object",
          "properties": {
              "status": {
                  "type": "number"
              },
              "errors": {
                  "type": "array",
                  "items": {
                      "type": "string"
                  }
              }
          }
      }
      ```
   * add Kafka/KafkaCommitOffset activity
   * add General/ParseJSONActivity
     * Output settings, we are only interested in the contractId value
       ```
        {
            "contractId":""
        }
        ```
     * add Dovetail-Client-DAML/ExerciseContractChoice activity
        * select iou from DAML package dropdown
        * select Iou:IouTransfer from contract template dropdown
        * select IouTransfer_Accept from contract choice dropdown
        * select "bob iou server" from ledger service dropdown
        * map Input
     * add Return, and map input

#### Test flows

you can test the flows in the Studio

#### Build application

from the Build dropdown, select your platform, the build process will take a couple minutes

#### Run the application

- **start up kafka**
  you can use you own Kafka server, or use the docker image provided in artifacts folder

  ```
  MY_IP=<your ip here> docker-compose -f artifacts/docker-compose.yaml up
  ```

- **create topics**
  for this application, following topics are required, you can use artifiacts/createtopic.sh script.
  * iou.created
  * iou.archived
  * ioutransfer.created
  * ioutransfer.archived
  * command.status

  ```
  artifacts/createtopic.sh iou.created
  ```

  you can use script artifacts/test_subscriber.sh to verify events are published to the topics

  ```
  artifacts/test_subscriber.sh ioutransfer.created
  ```

- **start up the Dovtail event server from DAML**
  
  the event server uses a config.yaml file for configuration settings, an example is provided in artifacts folder, you might need to modify the packageId to match your DAR packageId value.

  ```
  java -jar artifacts/daml-events-0.0.1-SNAPSHOT-shaded.jar -c artifacts/config.yaml -s localhost -p 6865
  ```

- **run the application**

  ```
  ./<your app exec>
  ```

  if you need to override app properties, you can pass a properties.json file to your program, an example is in artifacts folder

  ```
  FLOGO_APP_PROPS_JSON=artifacts/properties.json FLOGO_LOG_LEVEL=INFO ./<your app exec>
  ```

- **invoke the alice_issue_iou_to bob flow**

  ```
  curl -d '{"amount":300}' -H "Content-Type: application/json" -X POST http://localhost:9090/alice_issue_iou_to_bob
  ```

- **view contracts**
  
  go to navigator UI http://localhost:4000 to view contracts created or archived

    

