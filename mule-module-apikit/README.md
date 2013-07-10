mule-module-apikit
==================

A Mule module for implementing REsT and SOAP APIs.

Configuration using Maven Mule Plugin
-------------------------------------

**Mule plugin configuration:**

    <build>
        <plugins>
            <plugin>
                <groupId>org.mule.tools</groupId>
                <artifactId>maven-mule-plugin</artifactId>
                <version>1.7</version>
                <extensions>true</extensions>
                <configuration>
                    <copyToAppsDirectory>true</copyToAppsDirectory>
                    <excludeMuleDependencies>true</excludeMuleDependencies>
                    <inclusions>
                        <inclusion>
                            <groupId>org.mule.modules</groupId>
                            <artifactId>mule-module-apikit</artifactId>
                        </inclusion>
                    </inclusions>
                </configuration>
            </plugin>
        </plugins>
    </build>

**APIKit dependency:**

    <dependency>
        <groupId>org.mule.modules</groupId>
        <artifactId>mule-module-apikit</artifactId>
        <version>${project.version}</version>
        <exclusions>
            <exclusion>
                <groupId>org.mule</groupId>
                <artifactId>mule-core</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.mule.transports</groupId>
                <artifactId>mule-transport-http</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.mule.modules</groupId>
                <artifactId>mule-module-json</artifactId>
            </exclusion>
            <exclusion>
                <groupId>com.sun.xml.bind</groupId>
                <artifactId>jaxb-impl</artifactId>
            </exclusion>
            <exclusion>
                <groupId>wsdl4j</groupId>
                <artifactId>wsdl4j</artifactId>
            </exclusion>
            <exclusion>
                <groupId>javax.mail</groupId>
                <artifactId>mailapi</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

**Repository configuration:**

    <repositories>
        <repository>
            <id>mule-ee-releases</id>
            <name>Mule EE Releases Repository</name>
            <url>http://dev.ee.mulesource.com/repository/content/repositories/releases/</url>
        </repository>
        <repository>
            <id>mule-ee-snapshots</id>
            <name>Mule EE Snapshots Repository</name>
            <url>http://dev.ee.mulesource.com/repository/content/repositories/snapshots/</url>
        </repository>
    </repositories>

*These are the jars that either need to be present in the application lib directory or added to $MULE_HOME/lib/user:*

    mule-module-apikit-${project.version}.jar
    guava-13.0.1.jar
    jackson-core-2.1.1.jar
    jackson-annotations-2.1.1.jar
    jackson-databind-2.1.1.
    json-schema-validator-1.2.2.jar
    joda-time-2.1.jar
    rhino-1.7R4.jar

