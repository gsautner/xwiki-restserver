# XWiki Rest Server
A minimal REST server expandable with [XWiki components](http://extensions.xwiki.org/xwiki/bin/view/Extension/Component+Module).

## Usage

### Create REST resources
Add the following dependency to your project:

```xml
<dependency>
  <groupId>org.xwiki.contrib</groupId>
  <artifactId>xwiki-restserver-api</artifactId>
  <version>1.0</version>
</dependency>
```

Then, implement some REST resources by creating XWiki components (must be singleton):

```java
@Path(HelloWorldResource.PATH)  // URL of the resource
@Component                      // Indicate it's an XWiki component
@Singleton                      // Only singletons are supportted
@Named(HelloWorldResource.PATH) // Don't forget to give a name to your component,
                                // otherwise it will overwrite the "default" component.
public class HelloWorldResource implements org.xwiki.contrib.rest.RestResource 
{
    public static final String PATH = "/hello";
    
    @GET
    @Produces("application/json")
    @Formatted
    public HelloWorld getHelloWorld()
    {
        return new HelloWorld("Hello World!", 1);
    }
}
```
(don't forget to fill the `components.txt` file!)

### Standalone server

Add the following dependency to your project:

```xml
<dependency>
  <groupId>org.xwiki.contrib</groupId>
  <artifactId>xwiki-restserver-standalone</artifactId>
  <version>1.0</version>
</dependency>
```

Then run a standalone server:

```java
public static void main(String[] args)
{
    try {
        XWikiRestServer server = new XWikiRestServer(PORT_NUMBER, new XWikiJaxRsApplication());
        server.run();
    } catch (ComponentLookupException e) {
        e.printStackTrace();
    }
}
```

### Create a WAR to put in a Servlet Container

In plan, but not supported yet.

### Test your REST resources
Add the following dependencies to your project:

```xml
<!-- Test dependencies -->
<dependency>
  <groupId>javax.servlet</groupId>
  <artifactId>javax.servlet-api</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.xwiki.commons</groupId>
  <artifactId>xwiki-commons-tool-test-component</artifactId>
  <version>7.4.2</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.xwiki.contrib</groupId>
  <artifactId>xwiki-restserver-test</artifactId>
  <version>1.0</version>
  <scope>test</scope>
</dependency>
```

Then, write the functional test:
```java
@AllComponents
public class FunctionalTests
{
    /**
     * Utility class to run and perform requests on a test server.
     */
    private static TestServer testServer;

    @BeforeClass
    public static void setUp() throws Exception
    {
        testServer = new TestServer(new XWikiJaxRsApplication());
        testServer.start();
    }

    @AfterClass
    public static void tearDown()
    {
        testServer.stop();
    }

    @Test
    public void testHelloWorldResource() throws Exception
    {
        // Test
        String result = testServer.doGet("/hello");

        // Verify
        String expectedResult = "{\n"
                + "  \"message\" : \"Hello World!\",\n"
                + "  \"version\" : 1,\n"
                + "  \"otherMessages\" : [ \"Message 1\", \"Message 2\", \"Message 3\" ]\n"
                + "}";

        assertEquals(expectedResult, result);
    }
}
```
And that's all!

You could find a more complete example in `xwiki-restserver-example`, with unit and functional tests.
