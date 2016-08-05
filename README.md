[![Build Status](https://travis-ci.org/flipkart-incubator/simpleJobScheduler.svg?branch=master)](https://travis-ci.org/flipkart-incubator/simpleJobScheduler)
## Documentation and Examples
### Ridiculously short Quick start guide
#### Building & Booting
```
Ensure that you have a mysql & zookeeper instance running on your box
```
For simplicity, we assume the local mysql instance has no password for the root user (i.e mysql -uroot should log you in as root user)
Also, the configuration assumes zookeeper listening on 2181 (zk default)

<br>
Start by building the package. This will compile the code, setup a default local database and run tests
```
./build.sh
```

Next, Run the server
```
./run.sh
```
You should see something on the lines of `[           main] com.flipkart.jobscheduler.Application    : Started Application in xx.xxx seconds (JVM running for xx.xxx)` 

#### Running Jobs
*Jobs* are capable of hitting given HTTP APIs. Currently we support http GET & POST actions.An optional request body and Http headers can also be specified while creating jobs.

Job scheduler provides a convenience library to schedule Jobs. This is basically a wrapper over the HTTP API

Maven dependency:
```
<dependency>
        <groupId>com.flipkart.jobscheduler</groupId>
        <artifactId>client</artifactId>
        <version>1.0.0</version>
</dependency> 
```

In your java code, create the client using
```
HttpJobSchedulerClient client = new HttpJobSchedulerClient("http://localhost:11000");
```

***Creating Scheduled Jobs***

**Note**: Job names must be unique

Example: Create a job that executes every second
```
client.createJob(new ScheduledJob("new-job",
      new HttpApi(HttpApi.Method.POST,"<api-url>","<body-if-applicable>","Headers-to-be-used-while-hitting-api"),
      new Schedule(System.currentTimeMillis(),null,1000l)
      ));
```

***Creating One-Time Jobs***

Example: Create a job that will be triggered 5 minutes from now.
```
client.createJob(new OneTimeJob("new-job",
        System.currentTimeMillis() + (5*60*1000),
        new HttpApi(HttpApi.Method.POST,"<api-url>","<body-if-applicable>","Headers-to-be-used-while-hitting-api")
        ));
```
A one time job gets automatically deleted after being triggered.

However, since job names must be unique, the operation of creating a job with the same name as an existing job before the existing job is triggered will result in an error.

## Releases
| Release | Date | Description |
|:------------|:----------------|:------------|
| Version 1.0.0            | Jul 2016      |    First release

## Changelog
Changelog can be viewed in [CHANGELOG.md](https://github.com/flipkart-incubator/simpleJobScheduler/blob/master/CHANGELOG.md) file

## License
SimpleJobScheduler is licensed under : The Apache Software License, Version 2.0. Here is a copy of the license (http://www.apache.org/licenses/LICENSE-2.0.txt)

## Contributors
Gautam BT

Sahil Aggarwal (@SahilAggarwal)

Smit Shah (@Who828)

Yogesh Nachnani (@yogeshnachnani)
