# Flume RESTful Driver
This project is an improvement driver for Apache Flume 1.8.
It provides a very simple way by using RESTful style to control
and monitor collection jobs.    
* 2019.04.07 update to 0.0.2-SNAPSHOT 
    1. update to flume-ng 1.9.
    2. provide a monitor to look after the status of agents.
    3. add the http security module from spring boot
## 
As you know, Apache Flume is a distributed, reliable, and available service for 
efficiently collecting, aggregating, and moving large amounts of log data.      
for more information, see
https://github.com/apache/flume
## Documentation for Apache Flume
First of all, you should know how to use the apache flume.      
Documentation is included in the binary distribution under the docs directory.
In source form, it can be found in the flume-ng-doc directory.

The Flume 1.x guide and FAQ are available here:

* https://cwiki.apache.org/FLUME
* https://cwiki.apache.org/confluence/display/FLUME/Getting+Started

## How to use
Checkout spring boot properties in application.properties   

These are RESTful interface in TaskController.java:        
* /job/register     
 Method:Post        
 Data:
```
{
    "agentName": "agent1",
    "interceptor": {}，
    "channels": [
        {
            "channel": {
                "checkpointDir": "flumeConf\agent1\checkpoint",
                "dataDirs": "flumeConf\agent1\tmp",
                "nameSpace": "ch1",
                "type": "file"
            },
            "sinks": [
                {
                    "nameSpace": "sink1",
                    "sink.directory": "D:\pagefile\opt\output",
                    "type": "org.apache.flume.sink.RollingFileSink"
                }
            ]
        }
    ],
    "source": {
        "nameSpace": "sr1",
        "spoolDir": "D:\pagefile\opt\insert",
        "type": "spooldir"
    }
}
```
We provide a bean named JobPorperties to collect the json properties from user.  
`agentName` must specified, as it use as the id of a job. We will register a spring bean
in Spring's ApplicationContext (StaticApplicationContext)   
`nameSpace` is the id for every `LifecycleAware`(Source,Interceptor,Channel & Sink).
We will allocate a real name for LifecycleAware by using the agent name and this id.
Rule: ${agentName}_${nameSpace} 
These json will be translated to the real properties of the Flume.As followings:
```
    "agent1.channels": "agent1_ch1",
    "agent1.channels.agent1_ch1.checkpointDir": "flumeConf\agent1\checkpoint",
    "agent1.channels.agent1_ch1.dataDirs": "flumeConf\agent1\tmp",
    "agent1.channels.agent1_ch1.nameSpace": "ch1",
    "agent1.channels.agent1_ch1.type": "file",
    "agent1.sinks": "agent1_sink1",
    "agent1.sinks.agent1_sink1.channel": "agent1_ch1",
    "agent1.sinks.agent1_sink1.nameSpace": "sink1",
    "agent1.sinks.agent1_sink1.sink.directory": "D:\opt\output",
    "agent1.sinks.agent1_sink1.type": "org.apache.flume.sink.RollingFileSink",
    "agent1.sources": "agent1_sr1",
    "agent1.sources.agent1_sr1.channels": "agent1_ch1",
    "agent1.sources.agent1_sr1.nameSpace": "sr1",
    "agent1.sources.agent1_sr1.spoolDir": "D:\opt\storm-topology",
    "agent1.sources.agent1_sr1.type": "spooldir"
```
We only support one source and one interceptor in an agent at one moment.
Trees are like this:
```
  source 
    |---interceptor
          |---channel1
          .  |---sink1.1
          .  |---sink1.2
          .     ...
          |---channel2
           .  |---sink1.1
           .  |---sink1.2
           .     ...    
  
```
The reason is simple: Multiple source for one channel seems not effective for
most of the time. And if you wanna two copies, two channels are needed by them.
Couple of sinks is useful for some high-efficiency consuming systems.   
* /jobs/list    
 Method:Get
 ```
 [
     {  
         "agentName": "agent1", 
         "counterInfos": {  
             "CHANNEL.ch1": {   
                 "ChannelCapacity": "1000000",  
                 "ChannelFillPercentage": "0.0",    
                 "ChannelSize": "0",    
                 "CheckpointBackupWriteErrorCount": "0",    
                 "CheckpointWriteErrorCount": "0",  
                 "Closed": "0", 
                 "EventPutAttemptCount": "0",   
                 "EventPutErrorCount": "0", 
                 "EventPutSuccessCount": "0",   
                 "EventTakeAttemptCount": "3",  
                 "EventTakeErrorCount": "0",    
                 "EventTakeSuccessCount": "0",  
                 "Open": "true",    
                 "StartTime": "1554636025722",  
                 "StopTime": "0",   
                 "Type": "CHANNEL", 
                 "Unhealthy": "0"   
             }, 
             "SINK.sink1": {    
                 "BatchCompleteCount": "0", 
                 "BatchEmptyCount": "0",    
                 "BatchUnderflowCount": "0",    
                 "ChannelReadFail": "0",    
                 "ConnectionClosedCount": "0",  
                 "ConnectionCreatedCount": "1", 
                 "ConnectionFailedCount": "0",  
                 "EventDrainAttemptCount": "0", 
                 "EventDrainSuccessCount": "0", 
                 "EventWriteFail": "0", 
                 "StartTime": "1554636026223",  
                 "StopTime": "0",   
                 "Type": "SINK" 
             }, 
             "SOURCE.sr1": {    
                 "AppendAcceptedCount": "0",    
                 "AppendBatchAcceptedCount": "0",   
                 "AppendBatchReceivedCount": "0",   
                 "AppendReceivedCount": "0",    
                 "ChannelWriteFail": "0",   
                 "EventAcceptedCount": "0", 
                 "EventReadFail": "0",  
                 "EventReceivedCount": "0", 
                 "GenericProcessingFail": "0",  
                 "OpenConnectionCount": "0",    
                 "StartTime": "1554636026243",  
                 "StopTime": "0",   
                 "Type": "SOURCE"   
             }  
         }, 
         "execState": "RUNNING" 
     }  
 ]       
 ```
   This request will return all the status of agents.
 
* /job/infos  
 Method:Get  
 Param:agentName  
 This will return an information of job which are specified by agentName.
 ```
{
    "agentName": "agent1",
    "channels": [
        {
            "channel": {
                "checkpointDir": "flumeConf\agent1\checkpoint",
                "dataDirs": "flumeConf\agent1\tmp",
                "nameSpace": "ch1",
                "type": "file"
            },
            "sinks": [
                {
                    "nameSpace": "sink1",
                    "sink.directory": "D:\pagefile\opt\output",
                    "type": "org.apache.flume.sink.RollingFileSink"
                }
            ]
        }
    ],
    "lifeCycleCounters": {
        "CHANNEL.ch1": {
            "ChannelCapacity": "1000000",
            "ChannelFillPercentage": "0.0",
            "ChannelSize": "0",
            "CheckpointBackupWriteErrorCount": "0",
            "CheckpointWriteErrorCount": "0",
            "Closed": "0",
            "EventPutAttemptCount": "0",
            "EventPutErrorCount": "0",
            "EventPutSuccessCount": "0",
            "EventTakeAttemptCount": "58",
            "EventTakeErrorCount": "0",
            "EventTakeSuccessCount": "0",
            "Open": "true",
            "StartTime": "1554636025722",
            "StopTime": "0",
            "Type": "CHANNEL",
            "Unhealthy": "0"
        },
        "SINK.sink1": {
            "BatchCompleteCount": "0",
            "BatchEmptyCount": "0",
            "BatchUnderflowCount": "0",
            "ChannelReadFail": "0",
            "ConnectionClosedCount": "9",
            "ConnectionCreatedCount": "10",
            "ConnectionFailedCount": "0",
            "EventDrainAttemptCount": "0",
            "EventDrainSuccessCount": "0",
            "EventWriteFail": "0",
            "StartTime": "1554636026223",
            "StopTime": "0",
            "Type": "SINK"
        },
        "SOURCE.sr1": {
            "AppendAcceptedCount": "0",
            "AppendBatchAcceptedCount": "0",
            "AppendBatchReceivedCount": "0",
            "AppendReceivedCount": "0",
            "ChannelWriteFail": "0",
            "EventAcceptedCount": "0",
            "EventReadFail": "0",
            "EventReceivedCount": "0",
            "GenericProcessingFail": "0",
            "OpenConnectionCount": "0",
            "StartTime": "1554636026243",
            "StopTime": "0",
            "Type": "SOURCE"
        }
    },
    "source": {
        "nameSpace": "sr1",
        "spoolDir": "D:\pagefile\opt\insert",
        "type": "spooldir"
    }
}
```
 * /job/agents  
 Method:Get  
 This will return an array of agent names.  
`["agent1","agent2"]`
 
 * /job/stop  
 Method:Delete  
 Param:agentName  
 This will stop the job which are named ${agentName}
 * /jobs/clean  
 Method:Delete  
 This method will stop and clean the information of jobs(agents) which were failed.(To release the memories of H2 Database embedded)    
## Compiling Environment

Compiling our RESTful Driver requires the following tools:

* Oracle Java JDK 1.8
* Apache Maven 3.x

