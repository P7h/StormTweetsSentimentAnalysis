# StormTweetsSentimentAnalysis
----------

## Introduction
This repository contains an application which is built to demonstrate as an example of Storm distributed framework by performing sentiment analysis of tweets originating from U.S. in real-time.

[Storm](http://storm-project.net) is a free and open source distributed realtime computation system, developed at BackType by Nathan Marz and team. It has been open sourced by Twitter [post BackType acquisition] in August, 2011.<br>
This application has been developed and tested with Storm v0.8.2 on CentOS. Application may or may not work with earlier or later versions than Storm v0.8.2.<br>

This application has been tested in:<br>

+ Local mode on a CentOS virtual machine and even on Microsoft Windows 7 machine.
+ Cluster mode on a private cluster and also on Amazon EC2 environment of 4 machines and 5 machines respectively; with all the machines in private cluster running Ubuntu while EC2 environment machines were powered by CentOS.

## Features
* Application retrieves tweets from Twitter stream (using [Twitter4J](http://twitter4j.org)).<br>
* It calculates sentiment analysis of the tweets every 30 seconds.
* There are three different objects within a tweet that we can use to determine it’s origin. This application utilizes all the three of them and prioritizes in the following order [high to low]:
	* The coordinates object
	* The place object
	* The user object
* For reverse geocoding, this application uses Bing Maps API. 
	* For more information and sign up, please check [Getting Started with Bing Maps](http://msdn.microsoft.com/en-us/library/ff428643.aspx).
	* Please note that you would need Windows Live account for signing up for Bing Maps API key.
	* Also, please consider opting for Basic Plan for Bing Maps API, as that is better for our usage. As of 18th June, 2013, limit is 50k requests for 24 hours.
* This application uses [AFINN](http://www2.imm.dtu.dk/pubdb/views/publication_details.php?id=6010) which contains a list of pre-computed sentiment scores.
	* These words are used to determine sentiment of the each tweet which is retrieved using Streaming API.
* After processing, the application logs the sentiment values of the states to the console and also to a log file.<br>
* This gives us the most happiest state of US and most unhappiest state as well.
* As of current day, this codebase has been updated with decent comments, wherever required.
* Also this project has been made compatible with both Eclipse IDE and IntelliJ IDEA. Import the project in your favorite IDE [which has Maven plugin installed] and you can quickly follow the code.


## Configuration
Please check the [`config.properties`](src/main/resources/config.properties) and add your own values and complete the integration of Twitter API to your application by looking at your values from [Twitter Developer Page](https://dev.twitter.com/apps).<br>
If you did not create a Twitter App before, then please create a new Twitter App where you will get all the required values of `config.properties` afresh and then populate them here without any mistake.<br>
Also please add the value of Bing Maps API Key to [`config.properties`](src/main/resources/config.properties#L10), as that will be used for getting the reverse geocode location using Latitude and Longitude.<br>
And finally please check [but don't modify] the [`AFINN-111.txt`](src/main/resources/AFINN-111.txt) file to see the pre-computed sentiment scores of ~2500 words / phrases.

## Dependencies
* Storm v0.8.2
* Twitter4J v3.0.3
* Google Guava v14.0.1
* Jackson v1.9.12
* SLF4J v1.7.5
* Logback v1.0.13

Also, please check [`pom.xml`](pom.xml) for more information on the various other dependencies of the project.<br>

## Requirements
This project uses Maven to build and run the topology.<br>
You need the following on your machine:

* Oracle JDK >= 1.7.x
* Apache Maven >= 3.0.5
* Clone this repo and import as an existing Maven project to either Eclipse IDE or IntelliJ IDEA.
* This application uses [Google Guava](https://code.google.com/p/guava-libraries) for making life simple while using Collections.
* Requires ZooKeeper, JZMQ, ZeroMQ installed and configured in case of executing this project in distributed mode i.e. Storm Cluster.<br>
	- Follow the steps mentioned [here](https://github.com/nathanmarz/storm/wiki/Setting-up-a-Storm-cluster) for more details on setting up a Storm Cluster.<br>

Rest of the required frameworks and libraries are downloaded by Maven as required in the build process, the first time the Maven build is invoked.

## Usage
To build and run this topology, you must use Java 1.7.

### Local Mode:
Local mode can also be run on Windows environment without installing any specific software or framework as such. *Note*: Please be sure to clear your temp folder as it adds lot of temporary files in every run.<br>
In local mode, this application can be run from command line by invoking:<br>

    mvn clean compile exec:java -Dexec.classpathScope=compile -Dexec.mainClass=org.p7h.storm.sentimentanalysis.topology.SentimentAnalysisTopology
or

    mvn clean compile package && java -jar target/storm-sentiment-analysis-0.1-jar-with-dependencies.jar
	
### Distributed [or Cluster / Production] Mode:
Distributed mode requires a complete and proper Storm Cluster setup. Please refer this [wiki](https://github.com/nathanmarz/storm/wiki/Setting-up-a-Storm-cluster) for setting up a Storm Cluster.<br>
In distributed mode, after starting Nimbus and Supervisors on individual machines, this application can be executed on the master [or Nimbus] machine by invoking the following on the command line:

    storm jar target/storm-sentiment-analysis-0.1-jar-with-dependencies.jar org.p7h.storm.sentimentanalysis.topology.SentimentAnalysisTopology SentimentAnalysis

## Problems
If you find any issues, please report them either raising an [issue](https://github.com/P7h/StormTweetsSentimentAnalysis/issues) here on GitHub or alert me on my Twitter handle [@P7h](http://twitter.com/P7h). Or even better, please send a [pull request](https://github.com/P7h/StormTweetsSentimentAnalysis/pulls).
Appreciate your help. Thanks!

## License
Copyright &copy; 2013 Prashanth Babu.<br>
Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).