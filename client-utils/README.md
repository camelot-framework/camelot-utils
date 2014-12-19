# Сamelot Client Utils

Contains a ```CamelotClient``` class that aims to help sending messages
to a corresponding camelot-powered service somewhere via HTTP.

One of the possible models for building a [Camelot]-based system is 
to develop the nedded plugins (i.e aggregators, processors) and resources, 
launch a camelot instance with them somewhere on a server and then
supply it with data in form of xml/json-messages sent to one of the resources via HTTP.

Once messages arrive, the resource class can reject some of them 
based on their parameters etc. and redirect the rest to the main camelot input queue,
where from they will be consumed and handled by plugins.

The ```CamelotClient``` provides functionality to send these messages,
you will only have to specify which.

## Contents
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Steps](#steps)
  - [1. Write a Camelot-resource class](#1-write-a-camelot-resource-class)
  - [2. Generate a simple client based on your plugins wadl](#2-generate-a-simple-client-based-on-your-plugins-wadl)
  - [3. Wrap generated client for more convenient usage](#3-wrap-generated-client-for-more-convenient-usage)
  - [4. Specify the server uri](#4-specify-the-server-uri)
- [Result](#result)
- [Configuration](#configuration)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Steps

### 1. Write a Camelot-resource class

When you're creating any Resource class in your application specify
a PUT-path where the message will be sent. Redirect the arriving messages
to the camelot main input queue.

```java
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlSeeAlso;

import ru.yandex.qatools.camelot.api.annotations.MainInput;
import ru.yandex.qatools.camelot.api.EventProducer;

import my.camelot.application.beans.*;

@Path("/my-app")
@XmlSeeAlso(MyInputBean.class)
public class MyResource {

    @MainInput
    EventProducer input;

    @PUT
    @Path("/events")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response sendMessage(MyInputBean bean) {
        input.produce(bean);
        return Response.ok("ok").build();
    }
}
```

Don't forget to add your resource to ```camelot.xml```.

### 2. Generate a simple client based on your plugins wadl

We'll assume that you;re using maven. Create a module/project and
add the following to the pom:

```xml
<properties>
    <jersey.version>2.10.1</jersey.version>
    <wadl.version>1.1.6</wadl.version>

    <jetty.port>18084</jetty.port>
    <activemq.port>52619</activemq.port>

    <camelot-test.runForked>true</camelot-test.runForked>
    <camelot.url>http://localhost:${jetty.port}/camelot/</camelot.url>
    <camelot-test.waitUntilFinished>false</camelot-test.waitUntilFinished>

    <generated.client.package.name>my.camelot.application.client</generated.client.package.name>
    <generated.client.class.name>MyCamelotApplicationClientRaw</generated.client.class.name>
</properties>

<dependencies>
    <dependency>
        <groupId>org.glassfish.jersey.core</groupId>
        <artifactId>jersey-client</artifactId>
        <version>${jersey.version}</version>
    </dependency>
    <dependency>
        <groupId>ru.yandex.qatools.camelot.utils</groupId>
        <artifactId>client-utils</artifactId>
        <version>${camelot-utils.version}</version>
    </dependency>
    <dependency>
        <groupId>${my.camelot.application.groupId}</groupId>
        <artifactId>${my.camelot.application.artifactId}</artifactId>
        <version>${my.camelot.application.version}</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>ru.yandex.qatools.camelot</groupId>
            <artifactId>camelot-test-maven-plugin</artifactId>
            <version>${camelot.version}</version>
            <configuration>
                <waitUntilFinished>${camelot-test.waitUntilFinished}</waitUntilFinished>
                <runForked>${camelot-test.runForked}</runForked>
                <jettyPort>${jetty.port}</jettyPort>
                <activemqPort>${activemq.port}</activemqPort>
                <activemqBrokers>tcp://localhost:${activemq.port}</activemqBrokers>
            </configuration>
            <executions>
                <execution>
                    <id>run</id>
                    <goals>
                        <goal>stop</goal>
                        <goal>run</goal>
                    </goals>
                    <phase>validate</phase>
                </execution>
                <execution>
                    <id>stop</id>
                    <goals>
                        <goal>stop</goal>
                    </goals>
                    <phase>process-sources</phase>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>org.jvnet.ws.wadl</groupId>
            <artifactId>wadl-client-plugin</artifactId>
            <version>${wadl.version}</version>
            <executions>
                <execution>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <generationStyle>jaxrs20</generationStyle>
                <targets>
                    <url>${camelot.url}application.wadl</url>
                </targets>
                <autoPackaging>false</autoPackaging>
                <customClassNames>
                    <property>
                        <name>${camelot.url}</name>
                        <!--suppress MavenModelInspection -->
                        <value>${generated.client.class.name}</value>
                    </property>
                </customClassNames>
                <!--suppress MavenModelInspection -->
                <packageName>${generated.client.package.name}</packageName>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Run ```mvn clean compile```, the embedded camelot instance with start,
load your plugins, make a wadl of your resources, a client class with the 
specified name will be generated and the camelot will shut down. After that you will
be able to use a simple client like that:

```java
import my.camelot.application.client.MyCamelotApplicationClientRaw;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

Client jerseyClient = ClientBuilder.newClient();
URI myApplicationServerUri = new URI("...");
MyInputBean bean = new MyInputBean();
bean.setData("data");

new MyCamelotApplicationClientRaw.MyApp(jerseyClient, myApplicationServerUri)
        .events()
        .putXmlAs(bean, String.class);
```

### 3. Wrap generated client for more convenient usage

```java
public class MyCamelotApplicationClient extends CamelotClient {
    public void send(MyInputBean event) throws CamelotClientException {
        findEndpointAndRun(new EndpointOperation<String>() {
            @Override
            public String runOn(Client client, URI uri) {
                return new MyCamelotApplicationClientRaw.MyApp(client, uri)
                        .events()
                        .putXmlAs(bean, String.class);
            }
        });
    }
}
```

### 4. Specify the server uri

```CamelotClient``` automatically loads the information about the uri(s)
where your camelot application is running. They are specified via an xml
with the following format:

```xml
<camelot xmlns="urn:beans.client.camelot.qatools.yandex.ru">
    <endpoint url="http://my-camelot-application-01.my-domain.org/camelot"/>
    <endpoint url="http://my-camelot-application-02.my-domain.org/camelot"/>
</camelot>
```

You can either save in under ```/etc/camelot/endpoints.xml``` (no option for Windows, sorry :)
or add to classpath as a resource named ```camelot-endpoints.xml```.

## Result

So, finally, when you've done all the steps above, to send the messages 
from a given java-running system to your camelot application, all you'll
have to do is:

```java
MyInputBean bean = new MyInputBean();
bean.setData("data");
new MyCamelotApplicationClient().send(bean);
```

And that's it! It will automatically iterate over your endpoints and 
try to send a message until it succeeds or the endpoints list is over.
In this case it will throw a ```CamelotClientException``` with a corresponding message.

## Configuration
Also you can specify [connect] and [read] timeouts for the jersey client. You can do that two ways:
 1. set JVM systems properties named ```camelot.client.connect.timeout``` and ```camelot.client.read.timeout``` specifing values in milliseconds;
 2. specify the same properties in a resource named ```camelot.properties```.

Defaults are both set to 30 sec to ensure message delivery.

[connect]:https://jersey.java.net/apidocs/2.13/jersey/org/glassfish/jersey/client/ClientProperties.html#CONNECT_TIMEOUT "CONNECT_TIMEOUT"
[read]:https://jersey.java.net/apidocs/2.13/jersey/org/glassfish/jersey/client/ClientProperties.html#READ_TIMEOUT "READ_TIMEOUT"
