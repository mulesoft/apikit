mule-module-apikit
==================

A Mule module for implementing REsT and SOAP APIs.

Installation
------------

Maven dependency:

    <dependency>
        <groupId>org.mule.modules</groupId>
        <artifactId>mule-module-apikit</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

At the moment the module is hosted in mulesoft's internal private repository

    <repository>
        <id>mule-ee-snapshots</id>
        <name>Mulesoft.com Repository</name>
        <url>http://dev.ee.mulesource.com/repository/content/repositories/snapshots/</url>
    </repository>

These are the jars that either need to be present in the application lib directory or added to $MULE_HOME/lib/user:

    mule-module-apikit-1.0-SNAPSHOT.jar
    guava-13.0.1.jar
    jackson-core-2.1.1.jar
    jackson-annotations-2.1.1.jar
    jackson-databind-2.1.1.jar

