# Corda5 Cordapp Template 

## Environment Requirements: 
1. Download and install Java 11
2. Download and install `cordapp-builder` 
3. Download and install `corda-cli` 

You can find detailed instructions for steps 2 - 3 at [here](https://docs.r3.com/en/platform/corda/5.0-dev-preview-1/getting-started/overview.html)

## App Functionalities 
This app is a skeleton Corda 5 Cordapp. The app has a TemplateState, a TemplateStateContract, and a TemplateFlow. The flow will send a p2p transaction that carries the TemplateState to the target party. The TemplateState always carries a Hello-World String. 

## How to run the template

Corda 5 re-engineers the test development experience, utilizing Docker for test deployment. We need to follow a couple of steps to test deploy the app. 

1. Build the projects.
```shell
./gradlew clean build
```
2. Create the cpb file from the compiled cpk files in both contracts and workflows.

```shell
cordapp-builder create --cpk contracts/build/libs/tododist-contracts-1.0-SNAPSHOT-cordapp.cpk --cpk workflows/build/libs/tododist-workflows-1.0-SNAPSHOT-cordapp.cpk -o tododist.cpb
```

3. Configure the network.
```shell
corda-cli network config docker-compose tododist-network
```

4. Create a docker compose yaml file and start the docker containers.
```shell
corda-cli network deploy -n tododist-network -f c5cordapp-tododist.yaml | docker-compose -f - up -d
```

This step will take a few mintues to complete. If you are wondering what is running behind the scene, open a new terminal and run: 
```shell
docker-compose -f docker-compose.yaml logs -f 
```

5. Install the cpb file into the network.
```shell
corda-cli package install -n tododist-network tododist.cpb
```
All the steps are combined into a shell script called run.sh - you can simply call `sh ./run.sh` in your terminal and that will sequentially run steps 1 to 5. 

You can always look at the status of the network with the command: 
```shell
corda-cli network status -n tododist-network
```
You can shut down the test network with the command: 
```shell
corda-cli network terminate -n tododist-network -ry
```
So far, your app is successfully running on a Corda 5 test deployment network. 

## Interact with the app 
Open a browser and go to `https://localhost:<port>/api/v1/swagger`

For this app, the ports are: 
* PartyA's node: 12112
* PartyB's node: 12116

NOTE: This information is in the status printout of the network. Use the status network command documented above if you want to check the ports. 

The url will bring you to the Swagger API interface. It's a set of HTTP APIs which you can use out of the box. In order to continue interacting with your app, you need to log in. 

Depending on the node that you chose to go to, you need to log into the node use the correct credentials. 

For this app, the logins are: 
* PartyA - Login: angelenos, password: password, Basic YW5nZWxlbm9zOnBhc3N3b3Jk
* PartyB - Login: londoner, password: password, Basic bG9uZG9uZXI6cGFzc3dvcmQ=

NOTE: This information is in the c5cordapp-tododist.yaml file. 

Let's test if you have successfully logged in by going to the RegisteredFlows:

![img.png](registeredflows.png)

You should see a 200 success callback code, and a response body that looks like: 
```json
[
  "net.corda.c5template.flows.TemplateFlow"
]
```

Now, let's look at the `startflow` API. We will test our templateFlow with it.

In the request body for `startflow` in Swagger, enter: 
```json
{
  "rpcStartFlowRequest": {
    "clientId": "launchpad-2", 
    "flowName": "com.learncorda.tododist.flows.CreateToDoFlow", 
    "parameters": { 
      "parametersInJson": "{\"task\": \"Buy Milk\"}" 
    } 
  } 
}
```
This request carries three pieces of information: 
1. The clientID of this call 
2. The flow we are triggering 
3. The flow parameters that we are providing. 

After the call, you should see a 200 success call code, and a response body that looks like: 
```json
{
  "flowId": {
    "uuid": "81e1415e-be7c-4038-8d06-8e76bdfd8bc7"
  },
  "clientId": "launchpad-2"
}
```
NOTE: This does not mean the transaction is passed through, it means the flow is successfully executed, but the success of the transaction is not guaranteed. 

You would need either go to `flowoutcomeforclientid` or `flowoutcome` to see the result of the flow. In this case, we will use the clientID to query the flow result: 

Enter the clientID of our previous flow call: `launchpad-2`

You should see the following response: 
```json
{
  "status": "COMPLETED",
  "resultJson": "{ \n \"txId\" : \"SHA-256:D72F5CDAD49709C9B2CB3762B1DA4F92053484A1781D926AC29F9B90EE95A627\",\n \"outputStates\" : [\"{\\\"assignedBy\\\":\\\"OU\=INC, O\=PartyB, L\=London, C\=GB\\\",\\\"assignedTo\\\":\\\"OU\=INC, O\=PartyB, L\=London, C\=GB\\\",\\\"taskDescription\\\":\\\"Buy Milk\\\"}\"], \n \"signatures\": [\"uQgPNpHO1w53u6JjLhY/K6TduggyLAxTtFnQRKm4LCcsaS/ebpnzN76hm8BYqtsKo2Dtq6eigfeegcQo6UEgAw==\"]\n}",
  "exceptionDigest": null
}
```

`resultJson` formatted
```json
{
  "txId": "SHA-256:D72F5CDAD49709C9B2CB3762B1DA4F92053484A1781D926AC29F9B90EE95A627",
  "outputStates": [
    {
      "assignedBy": "OU=INC,O=PartyB,L=London,C=GB",
      "assignedTo": "OU=INC,O=PartyB,L=London,C=GB",
      "taskDescription": "Buy Milk"
    }
  ],
  "signatures": [
    "uQgPNpHO1w53u6JjLhY/K6TduggyLAxTtFnQRKm4LCcsaS/ebpnzN76hm8BYqtsKo2Dtq6eigfeegcQo6UEgAw=="
  ]
}
```
The completed status of the flow that both the flow and its carried transaction were successful. 

In the request body for `startflow` in Swagger, enter:
```json
{
  "rpcStartFlowRequest": {
    "clientId": "launchpad-2", 
    "flowName": "com.learncorda.tododist.flows.AssignToDoInitiator", 
    "parameters": { 
      "parametersInJson": "{\"linearId\": \"b6e8a52a-8916-4a9e-93c8-49963a62ab01\",\"assignedTo\": \"C=GB, L=London, O=PartyB, OU=INC\"}" 
    } 
  } 
}
```
