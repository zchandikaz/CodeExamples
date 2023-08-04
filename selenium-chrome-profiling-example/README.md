
# Start chrome profile recording from Selenium+Java

You may have used the Performance feature of Chrome DevTools. It’s a very useful tool that can be used to analyze the performance of a page.

![The performance profile of a page](https://cdn-images-1.medium.com/max/3718/1*d9aBzusEPRjI4tZO3oZmzg.png)*The performance profile of a page*

I had to record a profile during the automation in an existing Java + Selenium project. I’ve checked for examples on the web, but that didn’t go well. So that’s why I’m writing this article.

This can be done easily by the [puppeteer](https://pptr.dev/). Because performance profiling(**AKA**: tracing) is an in-built feature in Puppeteer. If you want to do this from the puppeteer you can refer [this page](https://testinproduction.wordpress.com/2017/10/10/puppeteer-using-the-trace-feature/).

## Chrome DevTools Protocol

Puppeteer is doing this using [Chrome DevTools Protocol](https://chromedevtools.github.io/devtools-protocol/). Luckily we can [access the Chrome DevTools Protocol with Selenium 4](https://applitools.com/blog/selenium-4-chrome-devtools/).

“Chrome DevTools Protocol” is an API that provides access to the Chrome Dev Tools, We can start the performance profiling from this.

This is the doc you need for Tracing API.
[**Chrome DevTools Protocol**
*Chrome DevTools Protocol — version tot — Tracing domain*chromedevtools.github.io](https://chromedevtools.github.io/devtools-protocol/tot/Tracing/)

## Implementation

I used Maven as my dependency management system, But you can do this with any other dependency management system as well.

In this example, I’m using the below versions.

* Java: 11

* Maven: 3

* Selenium: 4.11.0

* Chrome: 114.0.5735.90

To access the DevTools in Selenium you need the below dependency.

    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-devtools-v114</artifactId>
        <version>${selenium-java.version-for-profiling}</version>
    </dependency>

But in this example my selenium-java dependency includes the above dependency as well, So I’m not going to add it to my pom.xml .

I created a class “[DevToolUtility.java](https://github.com/zchandikaz/CodeExamples/blob/main/selenium-chrome-profiling-example/src/test/java/org/example/utility/DevToolUtility.java)” to put all this dev tool related stuffs.

So the tracing can be started from “startTracing” method in that class. First, we need to create a session.

    devTools.createSession();

Then need to create a command to send to the dev tools, We need to send the [“Tracing.start”](https://chromedevtools.github.io/devtools-protocol/tot/Tracing/#method-start) command to devtools.

![Tracing API documentation](https://cdn-images-1.medium.com/max/2000/1*sTeGbPLlQEEFFgq891X0bQ.png)*Tracing API documentation*

Here we need to send some parameters, and you can get an idea of them by checking the [documentation](https://chromedevtools.github.io/devtools-protocol/tot/Tracing/). To create this command I’m using [TraceStartCommandFactory](https://github.com/zchandikaz/CodeExamples/blob/main/selenium-chrome-profiling-example/src/test/java/org/example/utility/DevToolUtility.java#L35-L170). Because I have multiple preferences, regarding the content of the profile such as screenshots, animation info, network usage, etc… . The category parameter in this start command decides what kind of data we need to collect from the recording.

In “TraceStartCommandFactory” I added three categories.

* PUPPETEER_CATEGORIES — The categories used in the puppeteer

* EXTRA_CATEGORIES — Categories + more…

* ALL_AVAILABLE_CATEGORIES — All available categories, This is not recommended at all, But you can see all available categories if you want using this.

There are some more parameters but I’m not going to describe them all now, But I’m setting the TransferMode to the “ RETURNASSTREAM”. Because that would be required to open your profile from Chrome.

    return Tracing.start(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(Tracing.StartTransferMode.RETURNASSTREAM),
            Optional.empty(),
            Optional.empty(),
            Optional.of(new TraceConfig(
                    Optional.of(TraceConfig.RecordMode.RECORDASMUCHASPOSSIBLE), 
                    Optional.of(400 * 1024),
                    Optional.of(false),
                    Optional.of(true),
                    Optional.of(true),
                    Optional.of(incTraceCategories),
                    Optional.of(excTraceCategories),
                    Optional.empty(),
                    Optional.empty()
            )),
            Optional.empty(),
            Optional.empty()
    );

Then you need to add a listener to read the recorded profile. I added two listeners.

* Tracing.tracingComplete() — This will be called when the record is completed. The transfer mode should be Tracing.StartTransferMode.RETURNASSTREAM

* Tracing.dataCollected() — This will be called when the record is completed. The transfer mode should be Tracing.StartTransferMode.REPORTEVENTS . You can ignore this one because in this example we are using RETURNASSTREAM mode.

    devTools.addListener(Tracing.tracingComplete(), tracingComplete -> {
        var readable = new ChromeDevToolIOReader(
                devTools,
                tracingComplete.getStream().orElseThrow(),
                50 * 1024 * 1024
        );
        try (
                FileOutputStream fos = new FileOutputStream(
                        Paths.get(
                                "target", 
                                "/Trace - " +profileName + ".json"
                        ).toFile()
                )
        ) {
            while (readable.hasNext()){
                fos.write(readable.next().get());
            }
            traceCompleted = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            traceSemaphore.release();
        }
    });

Here I’ve opened a file to save the profile and that will be saved in my “target” folder as a JSON with the test scenario name.

We need to retrieve the data from the Chrome dev tool chunk by chunk. To make it easier I’ve created “ChromeDevToolIOReader” with the [Iterator](https://www.digitalocean.com/community/tutorials/iterator-design-pattern-java) design pattern.

This is how I’m reading the data from Chrome dev tools.

    private byte[] read(int size) {
        if (status == Status.COMPLETED) {
            return new byte[]{};
        } else if (status == Status.NOT_STARTED) {
            status = Status.IN_PROGRESS;
        }
    
        var response = client.send(
                IO.read(
                        stream,
                        Optional.empty(), Optional.of(size)
                )
        );
        if (response.getEof()) {
            this.status = Status.COMPLETED;
            client.send(IO.close(stream));
        }
        return response.getBase64Encoded().orElse(false)
                ?
                Base64.getDecoder().decode(response.getData())
                :
                response.getData().getBytes();
    }

To retrieve data we need to send [“IO.read”](https://chromedevtools.github.io/devtools-protocol/tot/IO/#method-read) command with the chunk size(optional) and the stream handler and which we received from the “tracingComplete” event parameters.

## Run the test

I’ve used a simple cucumber test scenario to do a simple search and get pictures in Google search.

Also, I’ve added a Maven profile to enable and disable this profiling thing.

![](https://cdn-images-1.medium.com/max/2000/1*lWjYBU1hAzskokZ1aRkvfA.png)

Also if you wish to run in the command line you can use the below command.

    mvn clean verify -P profiling-enabled

## Read the recorded profile

You can simply read the profile by dragging the profile json file into dev tools “Performance” tab or load it from “Load Profile” button.

![Record file](https://cdn-images-1.medium.com/max/2000/1*jBbvbo6LRYspiHvh7609Ng.png)*Record file*

![Loaded Performance Profile](https://cdn-images-1.medium.com/max/2326/1*gFT4TEp6HpgfoXusceqHDg.png)*Loaded Performance Profile*

You can also do the same using “chrome://tracing” page as well.

Happy profiling guys!!
