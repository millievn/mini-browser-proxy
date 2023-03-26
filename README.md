# MiniBrowser Proxy

> Fork of [BrowserMob Proxy ](https://github.com/LittleProxy/LittleProxy) as all forks and itself are deprecated.

## Changes since fork
### v2.2.0-release

- extract core package and remove others like [REST API](https://github.com/lightbody/browsermob-proxy#rest-api) (for I
  never use it) and [MITM](https://github.com/lightbody/browsermob-proxy#ssl-support) (It's great and no need to update)
- replace deprecated deps `net.lightbody.bmp:littleproxy-1.1.0-beta-bmp-17` with `xyz.rogfam:littleproxy-2.0.16`
- merge [Fixing credentials leak #756](https://github.com/lightbody/browsermob-proxy/pull/756)
  and [Recognize & handle brotli compression of the response body #742](https://github.com/lightbody/browsermob-proxy/pull/742)
- update dependencies especially `slf4j`  `netty` `log4j`

-----------------------------

MiniBrowser Proxy allows you to manipulate HTTP requests and responses, capture HTTP content, and export performance
data
as a [HAR file](http://www.softwareishard.com/blog/har-12-spec/) powered
by [LittleProxy](https://github.com/LittleProxy/LittleProxy).

If you're running MiniBrowser Proxy within a Java application or Selenium test, get started
with [Embedded Mode](#getting-started-embedded-mode).

### Getting started: Embedded Mode

To use MiniBrowser Proxy in your tests or application, add the `browsermob-core` dependency to your pom:

```xml

<dependency>
    <groupId>io.github.qiwang97</groupId>
    <artifactId>browsermob-core</artifactId>
    <version>new-release-version</version>
    <scope>test</scope>
</dependency>
```

Start the proxy:

```java
    BrowserMobProxy proxy=new BrowserMobProxyServer();
		proxy.start(0);
		int port=proxy.getPort(); // get the JVM-assigned port
// Selenium or HTTP client configuration goes here
```

Then configure your HTTP client to use a proxy running at the specified port.

**Using with Selenium?** See the [Using with Selenium](#using-with-selenium) section.

## Features and Usage

> This repo is forked from [BrowserMob Proxy](https://github.com/lightbody/browsermob-proxy) and support all features
> in `browsermob-core`.
>
> See [BrowserMob Proxy](https://github.com/lightbody/browsermob-proxy) to find more details.

The proxy is programmatically controlled via a REST interface or by being embedded directly inside Java-based programs
and unit tests. It captures performance data in
the [HAR format](http://groups.google.com/group/http-archive-specification). In addition it can actually control HTTP
traffic, such as:

- blacklisting and whitelisting certain URL patterns
- simulating various bandwidth and latency
- remapping DNS lookups
- flushing DNS caching
- controlling DNS and request timeouts
- automatic BASIC authorization

### Embedded Mode

If you're using Java and Selenium, the easiest way to get started is to embed the project directly in your test. First,
you'll need to make sure that all the dependencies are imported in to the project. You can find them in the *lib*
directory. Or, if you're using Maven, you can add this to your pom:

```xml

<dependency>
    <groupId>io.github.qiwang97</groupId>
    <artifactId>browsermob-core</artifactId>
    <version>new-release-version</version>
    <scope>test</scope>
</dependency>
```

Once done, you can start a proxy using `net.lightbody.bmp.BrowserMobProxy`:

```java
    BrowserMobProxy proxy=new BrowserMobProxyServer();
		proxy.start(0);
		// get the JVM-assigned port and get to work!
		int port=proxy.getPort();
//...
```

Consult the Javadocs on the `net.lightbody.bmp.BrowserMobProxy` class for the full API.

### Using With Selenium

**Selenium 3 users**: Due to a [geckodriver issue](https://github.com/mozilla/geckodriver/issues/97), Firefox 51 and
lower do not properly support proxies with WebDriver's DesiredCapabilities.
See [this answer](http://stackoverflow.com/a/41373808/4256475) for a suitable work-around.

BrowserMob Proxy makes it easy to use a proxy in Selenium tests:

```java
    // start the proxy
    BrowserMobProxy proxy=new BrowserMobProxyServer();
				proxy.start(0);

				// get the Selenium proxy object
				Proxy seleniumProxy=ClientUtil.createSeleniumProxy(proxy);

				// configure it as a desired capability
				DesiredCapabilities capabilities=new DesiredCapabilities();
				capabilities.setCapability(CapabilityType.PROXY,seleniumProxy);

				// start the browser up
				WebDriver driver=new FirefoxDriver(capabilities);

				// enable more detailed HAR capture, if desired (see CaptureType for the complete list)
				proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT,CaptureType.RESPONSE_CONTENT);

				// create a new HAR with the label "yahoo.com"
				proxy.newHar("yahoo.com");

				// open yahoo.com
				driver.get("http://yahoo.com");

				// get the HAR data
				Har har=proxy.getHar();
```

**Note**: If you're running running tests on a Selenium grid, you will need to customize the Selenium Proxy object
created by `createSeleniumProxy()` to point to the hostname of the machine that your test is running on. You can also
run a standalone
BrowserMob Proxy instance on a separate machine and configure the Selenium Proxy object to use that proxy.

### HTTP Request Manipulation

**HTTP request manipulation has changed in 2.1.0+ with LittleProxy.** The LittleProxy-based interceptors are easier to
use and more reliable. The legacy ProxyServer implementation **will not** support the new interceptor methods.

#### 2.1.0+ (LittleProxy) interceptors

There are four new methods to support request and response interception in LittleProxy:

- `addRequestFilter`
- `addResponseFilter`
- `addFirstHttpFilterFactory`
- `addLastHttpFilterFactory`

For most use cases, including inspecting and modifying requests/responses, `addRequestFilter` and `addResponseFilter`
will be sufficient. The request and response filters are easy to use:

```java
    proxy.addRequestFilter(new RequestFilter(){
@Override
public HttpResponse filterRequest(HttpRequest request,HttpMessageContents contents,HttpMessageInfo messageInfo){
		if(messageInfo.getOriginalUri().endsWith("/some-endpoint-to-intercept")){
		// retrieve the existing message contents as a String or, for binary contents, as a byte[]
		String messageContents=contents.getTextContents();

		// do some manipulation of the contents
		String newContents=messageContents.replaceAll("original-string","my-modified-string");
		//[...]

		// replace the existing content by calling setTextContents() or setBinaryContents()
		contents.setTextContents(newContents);
		}

		// in the request filter, you can return an HttpResponse object to "short-circuit" the request
		return null;
		}
		});

		// responses are equally as simple:
		proxy.addResponseFilter(new ResponseFilter(){
@Override
public void filterResponse(HttpResponse response,HttpMessageContents contents,HttpMessageInfo messageInfo){
		if(/*...some filtering criteria...*/){
		contents.setTextContents("This message body will appear in all responses!");
		}
		}
		});
```

With Java 8, the syntax is even more concise:

```java
        proxy.addResponseFilter((response,contents,messageInfo)->{
		if(/*...some filtering criteria...*/){
		contents.setTextContents("This message body will appear in all responses!");
		}
		});
```

See the javadoc for the `RequestFilter` and `ResponseFilter` classes for more information.

For fine-grained control over the request and response lifecycle, you can add "filter factories" directly
using `addFirstHttpFilterFactory` and `addLastHttpFilterFactory` (see the examples in the InterceptorTest unit tests).

## Building the latest from source

You'll need maven (`brew install maven` if you're on OS X):

    [~]$ mvn -DskipTests

When you build the latest code from source, you'll have access to the latest snapshot release. To use the SNAPSHOT
version in your code, modify the version in your pom:

```xml

<dependency>
    <groupId>io.github.qiwang97</groupId>
    <artifactId>browsermob-core</artifactId>
    <version>new-release-version</version>
    <scope>test</scope>
</dependency>
```
