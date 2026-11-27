## Welcome to Blazegraph 3 ##

Blazegraph 3 is a new beginning for the Blazegraph project, distinguished from
the original, archived Blazegraph version. The code retains the logic and
capabilities of its predecessor and is now maintained by a new group of
maintainers. Associated libraries have been updated for this release.

The project owes a continuing debt to the original developers whose work made
this release possible.

Blazegraph is a high-performance graph database supporting Blueprints and
RDF/SPARQL APIs. It supports up to 50 billion edges on a single machine and
powers the Wikimedia Foundation's [Wikidata Query Service](https://query.wikidata.org/).

This software is provided on an "AS IS" basis, without warranties of any kind.

![image](http://blog.blazegraph.com/wp-content/uploads/2015/07/blazegraph_by_systap_favicon.png)

Please see the release notes in [releases](bigdata/src/releases) for version changes.

[Sign up](http://eepurl.com/VLpUj) to get the latest news on Blazegraph.

Please also visit us at our: [website](https://www.blazegraph.com), [wiki](https://wiki.blazegraph.com), and [blog](https://blog.blazegraph.com/).

Find an issue?   Need help?  See [JIRA](https://jira.blazegraph.com).

Reporting a security issue: [Security Reporting](Security.md).

### Quick Start with the Executable Jar ###
Up and running with Blazegraph in under 30 seconds:  [Quick Start](https://wiki.blazegraph.com/wiki/index.php/Quick_Start).

### Deploying in Production ###
Blazegraph is designed to be easy to use and get started. It ships without SSL or authentication by default for this reason. For production deployments, we _strongly_ recommend you enable SSL, authentication, and appropriate network configurations. There are some helpful links below to enable you to do this. 

#### Enabling SSL support ####
To enable SSL support, uncomment the example [jetty.xml](blazegraph-jar/src/main/resources/jetty.xml#L141) and configure it for your local keystore.

#### Configuration Authentication ####
By default, Blazegraph ships without authentication enabled. This is great for developing, getting started, and doing research with Blazegraph. However, it's not recommended for any production deployment. To configuration authentication, you must configure it either within the web app container or via a reverse-proxy configuration.

Note that the Blazegraph namespace feature for [multi-tenancy](https://wiki.blazegraph.com/wiki/index.php/REST_API#Multi-Tenancy_API) does not provide security isolation. Users that can access the base URI of the server can access any of the available namespaces. You can further restrict this through a combination of authentication configuration and restricting access to specific namespace URIs, i.e. `/blazegraph/namespace/NAMESPACE/sparql`.

There are three basic options:

1. **Configuring Jetty Authentication for a standalone Jetty deployment**:  Follow the [jetty](http://www.eclipse.org/jetty/documentation/9.2.22.v20170531/configuring-security-authentication.html) guide to configure authentication for the [jetty.xml](blazegraph-jar/src/main/resources/jetty.xml) you use to deploy the server by uncommenting the `<Get name="securityHandler">` section. You'll need to create a [realm.properties](blazegraph-jar/src/main/resources/realm.properties) and update the jetty.xml to point to its location on the filesystem.  Then configure the [web.xml](bigdata-war-html/src/main/webapp/WEB-INF/web.xml) to uncomment the security-constraint.
1. **Configuring Tomcat Authentication for a standalone Tomcat deployment**:  First configure a Tomcat [Realm](https://tomcat.apache.org/tomcat-7.0-doc/realm-howto.html) with your choice of authentication method (JDBC, JNDI, etc.). Then configure the [web.xml](bigdata-war-html/src/main/webapp/WEB-INF/web.xml) to uncomment the security-constraint.
1. **Setup a reverse-proxy configuration with authentication**:  You can setup an http or https reverse proxy configuration that has authentication and forward requests to the local Blazegraph instance (typically running on localhost:9999). This is a good option with [Nginx](https://community.openhab.org/t/using-nginx-reverse-proxy-authentication-and-https/14542) and [Apache](https://stackoverflow.com/questions/5011102/apache-reverse-proxy-with-basic-authentication). 

##### Mitigating Cross-Site Request Forgery (CSRF) #####
If you enable authentication and expose the Blazegraph workbench, you should also take steps to protect against CSRF. Tomcat8 provides a [CSRF filter](https://tomcat.apache.org/tomcat-8.0-doc/config/filter.html#CSRF_Prevention_Filter_for_REST_APIs) that can be configured. For Jetty, if you configure authentication the default value for `SecurityHandler.setSessionRenewedOnAuthentication(true)` can also be used. CSRF protection may require REST clients to implement HTTP headers to be used to interact with the service.

### Building the code ###
As a quick start, run `mvn install -DskipTests` or the utility script `./scripts/mavenInstall.sh `.

Blazegraph 3 requires JDK 27 or later to build and run the server. Java
libraries are compiled for Java 21 so that Java 21 clients can use them.
To build the standalone executable JAR, run
`mvn -PDeployment -pl blazegraph-jar -am package -DskipTests`. Start it with
`java -jar blazegraph-jar/target/blazegraph-jar-3.0.0-SNAPSHOT.jar` and open
`http://localhost:9999/blazegraph/`. The build skips tests and uses the current
snapshot version; it is not a published release.

#### Experimental virtual-thread request dispatch ####

The standalone executable JAR can opt into Jetty virtual-thread request dispatch:

```
java -Dblazegraph.virtualThreads.enabled=true -jar blazegraph-jar/target/blazegraph-jar-3.0.0-SNAPSHOT.jar
```

This is disabled by default; omit the property or set it to `false` to roll back.
It applies to the standalone JAR, not external WAR containers. The Jetty
platform-thread pool and the separate query executor remain in place. Custom
Jetty configurations must use a `QueuedThreadPool` without an existing virtual
threads executor when the flag is enabled. To bound
HTTP work in this mode, the following JVM properties control Jetty's request
admission handler:

| Property | Default | Meaning |
| --- | --- | --- |
| `blazegraph.virtualThreads.maxRequests` | `64` | Maximum concurrent requests; must be positive. |
| `blazegraph.virtualThreads.maxQueuedRequests` | `64` | Maximum suspended requests; zero rejects excess requests immediately. |
| `blazegraph.virtualThreads.queueTimeoutMillis` | `30000` | Maximum queue wait in milliseconds; must be positive. |

Requests exceeding the queue limit or timeout receive HTTP 503. These bounds
apply only when the flag is enabled; configure them for the workload and
available resources. The feature is experimental: benchmark it against the
platform-thread mode, including a tuned Jetty pool, before enabling it in
production. It does not increase the query executor's concurrency limit.
Compare both modes on the same JDK, data and hardware, with the same query
concurrency and comparable request admission limits. Include short queries,
expensive joins, disk-heavy reads and slow clients at increasing concurrency;
measure successful throughput, p95/p99 latency, rejections, memory, CPU and
thread counts. Use an isolated test database and repeat runs after warmup.

##### Virtual-thread benchmark results #####

On 26 November 2026, the standalone server was benchmarked on Windows 11 with
Oracle JDK 27.0.1.1 and an Intel Core i7-7700HQ (4 cores, 8 logical processors).
Both modes used a 16-thread query executor and a synthetic, warm dataset of
10,000 entities and 30,000 triples. The load generator and both server
processes ran on the same machine; only one server was under load at a time.

The table reports median successful requests per second and the median of the
per-trial p95 latencies, from three trials per case. Each trial ran for at
least eight seconds after a five-second warmup, including time to finish
in-flight requests. Slow-streaming clients read responses in 2 KiB chunks
with a 2 ms delay between reads. No request errors were observed.

| Workload | Concurrent clients | Platform req/s | Platform p95 | Virtual req/s | Virtual p95 |
| --- | ---: | ---: | ---: | ---: | ---: |
| Lookup | 8 | 304.6 | 31 ms | 306.8 | 33 ms |
| Lookup | 48 | 311.0 | 170 ms | 302.1 | 171 ms |
| Join | 8 | 313.7 | 30 ms | 314.9 | 30 ms |
| Join | 48 | 307.4 | 183 ms | 311.1 | 167 ms |
| Slow streaming | 8 | 7.7 | 1,089 ms | 8.0 | 1,022 ms |
| Slow streaming | 48 | 31.0 | 1,708 ms | 30.0 | 1,825 ms |
| Lookup, tuned pool | 96 | 302.7 | 341 ms | 303.4 | 346 ms |
| Slow streaming, tuned pool | 96 | 66.4 | 1,530 ms | 64.6 | 1,534 ms |

The 96-client cases used a 128-thread Jetty pool; the other cases used the
default 64-thread pool. Virtual-thread admission limits were 128 and 64 active
requests respectively. These results show no consistent throughput benefit, so
virtual-thread dispatch remains disabled by default.

This is not a production-representative benchmark: the dataset was small and
cached, the trials were short, and the load generator shared the server's
machine. Results do not cover cold-disk reads, updates, or remote services and
should not be generalized to other workloads.

The Maven build also compiles the Angular 22 and Angular Material 3 Workbench
included in the WAR. The previous jQuery Workbench remains available at
`/legacy.html`.

For more detailed maven information see the [wiki](https://wiki.blazegraph.com/wiki/index.php/MavenNotes). 

### Samples and Examples ###
There are code samples and examples to get started with the Blazegraph Database [here] (https://github.com/blazegraph/blazegraph-samples).  Tinkerpop3 examples are included directly within the Tinkerpop3 repository per below.

### Javadocs ###
Click here to view the lastest [API Javadocs](https://blazegraph.github.io/database/apidocs/index.html).

### Maven Central ###
Starting with the 2.0.0 release, the Blazegraph Database is available on Maven Central.  To include the core platform and dependencies, include the artifact below in your dependencies.   [Developing with Maven](https://wiki.blazegraph.com/wiki/index.php/MavenNotes) has notes on developing with Blazegraph Database source code and Maven.

```
    <dependency>
        <groupId>com.blazegraph</groupId>
        <artifactId>bigdata-core</artifactId>
        <version>2.0.0</version>
    </dependency>
    <!-- Use if Tinkerpop 2.5 support is needed ; See also Tinkerpop3 below. -->
    <dependency>
        <groupId>com.blazegraph</groupId>
        <artifactId>bigdata-blueprints</artifactId>
        <version>2.0.0</version>
    </dependency>
```

If you'd just link the Blazegraph Database dependencies without any of the external libraries, use the bigdata-runtime artifact.

```
    <dependency>
        <groupId>com.blazegraph</groupId>
        <artifactId>bigdata-runtime</artifactId>
        <version>2.0.0</version>
    </dependency>
```

### Deployers ###

Starting with 2.0.0, the default context path for deployment is `http://localhost:9999/blazegraph/`.  There are also Maven artifacts for WAR deployers (`blazegraph-war`), executable Jar files (`blazegraph-jar`), [Debian Package](blazegraph-deb/) (`blazegraph-deb`), [RPM](blazegraph-rpm/) (`blazegraph-rpm`), and a [Tarball](blazegraph-tgz/) (`blazegraph-tgz`).

The `bigdata-war` and `bigdata-jar` artifacts are included for legacy purposes and use the `/bigdata/` context path.

### Tinkerpop3 ###
Tinkerpop3 supports requires Java 1.8 and is now in a separate repository.  See [Tinkerpop3](https://github.com/blazegraph/tinkerpop3).  It is also available as Maven Central artifact.

```
    <dependency>
        <groupId>com.blazegraph</groupId>
        <artifactId>blazegraph-gremlin</artifactId>
        <version>1.0.0</version>
    </dependency>
    
```

### Triple Pattern Fragment (TPF) Server ###
There is a [Blazegraph Triple Pattern Fragment TPF](https://github.com/hartig/BlazegraphBasedTPFServer) server that supports [Linked Data Fragments](http://linkeddatafragments.org/).

### Blazegraph Python Client ###
There is a Blazegraph Python Client [here](https://github.com/blazegraph/blazegraph-python)

### Blazegraph Dot Net RDF Client ###
There is a Blazegraph Dot Net RDF Client [here](https://github.com/blazegraph/blazegraph-dotnetrdf)
