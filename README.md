# Alamo Drafthouse Middleware Coding Challenge (adc-mcc)

Alamo Drafthouse takes great care in how we select members of our engineering team. This technical challenge will help 
us get an idea of how you would use software to solve problems for our end users -- some of the most avid movie lovers 
in the known universe. 

We are using this assessment to evaluate a number of skills which are critical for our back-end development team. Most 
notably, this challenge will help us assess your ability to simultaneously absorb a (partially) new or unfamiliar 
technology and some loose requirements in order to build a service that is useful, handles the edge cases, and performs 
well in spite of the messy, not entirely consistent, and inconvenient data available behind the scenes. 

We're looking for some insight into your ability to implement a feature in a clean, production-quality, and maintainable
manner and we plan to discuss your solution with you in our follow-on technical interviews (so make some notes).

We know that your time is important, so we’ve designed the challenge to take less than 5 hours depending on how 
familiar you are with Scala development but spend as much time as you need to produce something you are proud
of and that reflects your real-world work product. This can be a simple project but it can go pretty deep if
you want it to (advanced caching, error handling/reporting, metrics, tracing, etc).
We hope you have some fun while you build it!

## Mission: Report on Fill Rate

For this coding assessment we are asking you to put together an API (one or more endpoints) to help us understand the
percentage of seats that we've sold. We call this the "fill rate" and it's an important metric as it helps
us schedule the correct amount of labor at the venues and helps us determine if our film programming/scheduling is
optimal or needs to be tweaked. 

This example is a bit contrived as we wouldn't really use our transactional middleware to report on a metric 
like this but it's a good example of how we often have to combine data in interesting ways. Our middleware platform
regularly has to bend over backward to work around oddities with the systems with which we integrate and this challenge 
is a little window into that world.

## Submission Mechanics

Once you have completed the project, please send us a link to your source repository (or email a zipped copy of your
source). We recommend writing a README (you can append to the end of this README if that's easiest) with a
summary of any questions, design trade-offs, comments about any notable code, and any extra instructions required
to build or run your example and its tests. We're going to discuss your submission in your interview so some
notes might help jog your memory as we're talking through your solution.

Please do **NOT** fork this to a public repository or submit pull requests to this repository as
that will make your code submission easily visible to other candidates. Simply pushing to a new (public) remote is
fine. The best approach is to create a branch for your work and to push those changes (and the develop and master
branches) up to a new repository you create. Some example steps to do this:

```
$ git clone git@github.com:drafthouse/adc-mcc.git
$ cd adc-mcc
$ git remote rename origin drafthouse
$ # Create your (public) hosted repository in Github (or wherever)
$ git remote add myremote git@github.com:YOUR_ACCOUNT_HERE/YOUR_REPO_HERE.git
$ git push --all myremote -u
$ git branch YOUR_BRANCH_NAME develop
$ git checkout YOUR_BRANCH_NAME
$ git push --set-upstream myremote YOUR_BRANCH_NAME
```

## Starting Infrastructure

We're providing a stripped down version of the server implementation we use in our middleware platform. It's 
implemented in Scala (2.12) and is based on 
[Twitter's Finatra](https://twitter.github.io/finatra) framework which sits on top of their excellent
[Finagle infrastructure](https://twitter.github.io/finagle). The platform uses [Guice](https://github.com/google/guice)
for dependency injection,
[Typesafe config](https://github.com/lightbend/config) for configuration, and [Circe](https://circe.github.io/circe)
for JSON serialization. We use [Swagger](https://swagger.io/) for in-line API documentation.

This repostory includes a fully working HTTP server with some example endpoints so you can see the basic structure
for how our services are implemented. The build is [SBT-based](https://www.scala-sbt.org/1.x/docs/) and building
and running the server should only take a few minutes once you have the prerequisites installed on your system.

### Prerequisites

This server environment requires JDK 8, and SBT 1.X (we're using 1.2.8). Scala 2.12 should come along for the
ride but you may need to download that and install it depending on how your IDE works.
The simplest one-stop guide for getting the prerequisites installed can be found on the
[Scala language site](https://www.scala-lang.org/download/2.12.8.html).
Oracle JDK 8 or Open JDK 8 both work fine so use whatever your preference is.

* **JDK 8** - If you are on a Mac, AdoptOpenJDK is a good
option via Homebrew. [Instructions here](https://installvirtual.com/install-openjdk-8-on-mac-using-brew-adoptopenjdk/).
And [Oracle JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) works great
too.
* **SBT** - On a Mac, `brew install sbt` will install the latest 1.X SBT (1.3.8) which will work fine.

### Building/Running the Server

Building the server should be as simple as `sbt assembly` once you have the prerequisites installed. That will produce
an executable uberjar in `target/scala-2.12/adc-mcc-exec.jar`. You can run that with a simple
`java -jar target/scala-2.12/adc-mcc-exec.jar`. The server runs HTTP on [port 9999](http://localhost:9999).

For development it's easiest to run the server in SBT using [sbt-revolver](https://github.com/spray/sbt-revolver) which 
is already configured in the project. A simple `sbt ~reStart` will run the server in a watch loop. Whenever code is 
updated, the code will be incrementally compiled and the server automatically restarted. The code/build/run loop is 
pretty fast and yields a decent development experience that works with any editor.

You should see something like this:

```
$ sbt
[info] Loading global plugins from /Users/ajwhitney/.sbt/1.0/plugins
[info] Loading settings for project adc-mcc-build from plugins.sbt ...
[info] Loading project definition from /Users/ajwhitney/code/drafthouse/adc-mcc/project
[info] Loading settings for project adc-mcc from build.sbt ...
[info] Set current project to adc-mcc (in build file:/Users/ajwhitney/code/drafthouse/adc-mcc/)
[info] sbt server started at local:///Users/ajwhitney/.sbt/1.0/server/d8fc7f07f18ebcd57325/sock
sbt:adc-mcc> ~reStart
[info] Updating ...
[info] Done updating.
[warn] There may be incompatibilities among your library dependencies; run 'evicted' to see detailed eviction warnings.
[info] Compiling 22 Scala sources and 3 Java sources to /Users/ajwhitney/code/drafthouse/adc-mcc/target/scala-2.12/classes ...
[info] Done compiling.
[info] Application adc-mcc not yet started
[info] Starting application adc-mcc in the background ...
adc-mcc Starting com.drafthouse.mcc.AdcMccServerMain.main()
adc-mcc Listening for transport dt_socket at address: 5050
[success] Total time: 13 s, completed Feb 7, 2020 9:18:20 PM
1. Waiting for source changes in project adc-mcc... (press enter to interrupt)
adc-mcc 2020-02-07 21:18:21,031 INF [    main]                  Slf4jBridgeUtility$       org.slf4j.bridge.SLF4JBridgeHandler installed.
adc-mcc 2020-02-07 21:18:21,528 INF [    main]                  HttpMuxer$                HttpMuxer[/admin/metrics.json] = com.twitter.finagle.stats.MetricsExporter(com.twitter.finagle.stats.MetricsExporter)
adc-mcc 2020-02-07 21:18:21,529 INF [    main]                  HttpMuxer$                HttpMuxer[/admin/per_host_metrics.json] = com.twitter.finagle.stats.HostMetricsExporter(com.twitter.finagle.stats.HostMetricsExporter)
adc-mcc 2020-02-07 21:18:21,548 INF [    main]                  AdcMccServerMain$         Process started
adc-mcc 2020-02-07 21:18:21,779 INF [    main]                  AdcMccServerMain$         Serving admin http on 0.0.0.0/0.0.0.0:9990
adc-mcc 2020-02-07 21:18:22,080 INF [    main]                  finagle                   Finagle version 18.6.0 (rev=5b9961ef6ce11aa58376a8dc6d9d1f45175c7404) built at 20180614-165659
adc-mcc 2020-02-07 21:18:22,862 INF [    main]                  AdcMccServerMain$         Resolving Finagle clients before warmup
adc-mcc 2020-02-07 21:18:22,865 INF [    main]                  finagle                   feeds.drafthouse.com:443 resolved to Addr.Bound, current size=2
adc-mcc 2020-02-07 21:18:22,865 INF [    main]                  finagle                   drafthouse.com:443 resolved to Addr.Bound, current size=2
adc-mcc 2020-02-07 21:18:22,867 INF [    main]                  AdcMccServerMain$         Done resolving clients: [feeds.drafthouse.com:443, drafthouse.com:443].
adc-mcc 2020-02-07 21:18:22,904 INF [    main]                  MDCInitializer$           Initialized MDC.
adc-mcc 2020-02-07 21:18:23,887 INF [    main]                  AdcMccServerMain$         Warming up.
adc-mcc 2020-02-07 21:18:23,982 INF [    main]                  HttpRouter                Adding routes
adc-mcc GET     /webjars/:*
adc-mcc GET     /api-docs/model
adc-mcc GET     /api-docs/ui
adc-mcc GET     /
adc-mcc GET     /doc
adc-mcc GET     /docs
adc-mcc GET     /sample/ping
adc-mcc GET     /sample/market/:marketIdOrSlug/film
adc-mcc GET     /v1/session-fill-rate
adc-mcc OPTIONS /:*
adc-mcc 2020-02-07 21:18:24,021 INF [    main]                  AdcMccServerMain$         http server started on port: 9999
adc-mcc 2020-02-07 21:18:24,022 INF [    main]                  AdcMccServerMain$         admin http server started on port 9990
adc-mcc 2020-02-07 21:18:24,025 INF [    main]                  AdcMccServerMain$         com.drafthouse.mcc.AdcMccServerMain started.
adc-mcc 2020-02-07 21:18:24,026 INF [    main]                  AdcMccServerMain$         Startup complete, server awaiting.
adc-mcc 2020-02-07 21:18:24,028 INF [    main]                  Awaiter$                  Awaiting 2 awaitables:
adc-mcc com.twitter.finagle.server.ListeningStackServer$$anon$1
adc-mcc com.twitter.finagle.server.ListeningStackServer$$anon$1
```

Once the server is running, hit [http://localhost:9999](http://localhost:9999) and you should be redirected
to the Swagger documentation where you can execute the available APIs.

#### Debugging

If you want to use a stepwise debugger, you can attach a remote debugger to port 5050. 

#### SOCKS proxy

If you want to monitor the traffic the server is exchanging with remote resources, you can configure it to run through a
SOCKS proxy. [Charles Proxy](https://www.charlesproxy.com/) on the Mac works great 
and [Fiddler](https://www.telerik.com/download/fiddler) will likely work on Windows or Linux (and Mac too nowadays, I 
guess). To enable this when using sbt-revolver (sbt ~reStart), uncomment the following lines in the build.sbt file. 

```
// Uncomment to pass traffic through a SOCKS proxy (like Charles proxy or Fiddler)
//reStartArgs ++= Seq(
//  "-com.twitter.finagle.socks.socksProxyHost=localhost",
//  "-com.twitter.finagle.socks.socksProxyPort=50002"
//)
```

If you are running with the executable jar, pass the application parameters
`-com.twitter.finagle.socks.socksProxyHost=localhost` and
`-com.twitter.finagle.socks.socksProxyPort=50002` 

Although this is how we work with the real servers during development it's not really necessary for this
project as the endpoints you'll be hitting are slow-changing and you can just load them up with curl
or in your browser to see what the server is seeing. _Note_ that a running (and property configured) SOCKS proxy is
required if these lines are uncommented. If you tell it to use a proxy and your proxy isn't working, your remote
calls will fail.

## The Assignment: Fill Rate API

It's time to start calculating the fill rate, sliced in various different ways. We want to be able to look at various
different time periods (today, this week, arbitrary range), filter by location (market, cinema, or national), 
films, or series (extra credit), and see the fill rate broken down by business day of week and day part.

The API you build to do this is up to you. We've provided a stubbed out starting point in
`com.drafthouse.mcc.controller.FillRateController` but how you structure the endpoint(s) is up to you. Feel free to
modify any of the included classes.

Step 1: Make it work correctly. Step 2: Make it readable/maintainable. Step 3: Make it perform. Step 4: Extra credit.
Step 5: Profit.

Functional requirements:

* We want to be able to filter the sessions considered by
    * Location - cinema, market, or national
    * Film - Multiple would be useful (maybe comma-delimited). In this data set there will be multiple "Films" for the
    same logical title (2d-star-wars and 3d-star-wars, for example). We might want to know fill rate for the logical
    title (which would require multiple Film identifiers. For example, the fll rate for Star Wars on opening weekend
    would need to consider a number of variants of Star Wars).
    * Time frame - Most commonly we'll want to look at today and this week but we might want to look at next
    week or some arbitrary period. Keep in mind that we will be thinking in terms of cinema local time (CLT) so
    you'll want to use that concept. This week in Brooklyn means a different universal time range than this week in
    San Francisco.
    * Series - **Extra credit**. Slice the data by series so we can see what the fill rate for Weird Wednesday
    shows is like. Which film is in which series and therefore which sessions are in play is going to be
    expensive to calculate (given the endpoints we're providing) so it will be some extra work to make that
    perform well.
* We want to know what we're looking at in the results. Include the identifying information for the entities
included in the calculation. We know what we asked for (filtered by) but we want to know what we found. So,
for example, if we ask for fill rate tomorrow in Austin, include the cinema ID/slugs of the cinemas included in
the results (if a cinema isn't showing anything during that period, don't include it's ID/slug). Include the
film slugs that are showing (included in the fill rates).
* We want to see the fill rates broken down by
    * Overall - Overall and overall by day part.
    * Business Day of Week - Use the enum values from `java.time.DayOfWeek`. Only include the
    business days of week included in the result. Include overall and and by day part.
    * Day Part - overall and as part of business day of week.
* Should your API report IDs, slugs, or names? IDs are hard to grok. Names aren't precise if you want to follow up
with a subsequent query. Slugs tend to be a nice middle ground, identifying but human readable. We usually
try to make our APIs accept both IDs and slugs.
    
Non-functional Requirements/Considerations:

* Maintainability and performance are our primary concerns beyond the functional.
* Try not to win the obfuscated Scala code competition. Be concise but not too concise. If you are doing something
non-obvious (unexpected use of implicits comes to mind) or some other kind of Scala/Cats/shapeless magic, make
a note of it in your code so someone can unwind what's going on six months from now. We're simpletons.
* Initial loads are going to be slow. The market feeds in particular are large and you need to ingest at least
one of them. National, where you need all of the market feeds, will be a pig. Think about how you might improve
performance for subsequent queries. Consider data freshness for the various feeds (some change infrequently, some
more frequently). How might you improve cold-start performance? How much data are we talking about? Can we cache
everything? What are the memory requirements for your solution? How would your solution differ (or would it) for
a single server versus a cluster or pool of servers. There's a balance between speedy and stale and slow and correct
and we want to see how you solve for that balance. Can you give control of that balance point to your users?
* How should you handle "minor" errors? You'll be aggregating lots of data. What if one call fails (can't load the seating
data for one session, for example). When is it OK to eat the data vs. erroring vs. caveating the result?

Put on your user hat and design what is useable and flexible.

### Domain Concepts

* **Cinema/Theater** - A cinema is a multi-screen complex where we show movies. Examples are South Lamar and Mueller in
Austin. These are sometimes called theaters, although that is an overloaded term so I'll refer to them as cinemas
in this document. Cinemas are identified with a 4-digit ID ("0004") and with an alpha slug
("south-lamar"). A cinema ID can be converted to a market ID by replacing the last two digits with "00"
("0004" South Lamer -> "0000" Austin)
* **Screen/Auditorium/Theater** - A screen or auditorium (also known as theater) is the room where we show a movie.
Usually identified with a String number (e.g. "5" but can be alpha, e.g. "A")  
* **Market** - A market is a metro-area where we have one or more cinemas. A market is identified by a 4-digit market ID
("2100") and is also identified with an alpha slug ("nyc"). Times (unless noted with UTC) are in cinema local time
(CLT) which is local to the time zone of the cinema. Cinemas (in the market feed) have a property, `timeZoneATE`, that indicates the
time zone of the cinema. We often support the concept of a "national" market in our APIs that rolls up all of the
markets into a single super market. We use slug "national" and ID "9900" to identify this logical market using
the identifying structure of market slug and market ID. There are constants in `com.drafthouse.mcc.domain.Hardcoded.Market`
to capture these values.
* **Show/Film/Movie** - A film is a title we show (Birds of Prey) and is identified by an alpha slug ("birds-of-prey")
or an ID/HO code ("A000023456")
* **Series** - A curated collection of films (Weird Wednesday, Terror Tuesday). Identifiable by an alpha slug
("weird-wednesday").
* **Session** - A session is the presentation/showing of a particular film at a particular screen (and cinema)
at a particular time. A session is something you can buy tickets to see.
Birds of Prey at 6:40pm on 14 Feb at screen 5 of Slaughter Lane is a session. A session has
an associated seating layout, tickets sold, seats sold, etc. A session is identified by a multi-digit session ID
("149260"). A session ID is only unique to a cinema. The composite key of cinema ID ("0006") and session ID ("149260"")
is unique across the system. Almost all sessions are reserved seating. This is noted in the market feed with
`reservedSeating: "1"`. Non-reserved seating or General Admission (GA) sessions won't have seating data available
so you can ignore any sessions that aren't reserved seating.
* **Seating Data** - The seating data associated with a session. The seating data is composed of seating areas
which are composed of a rectangular, two-dimensional grid of rows and columns.
* **Seating Area** - The top-level subdivision of the seating data. A session may support multiple seating areas
Most sessions have a single seating area. Screen #1 at Ritz (0002), Austin (0000) is a (rare) case
that has a seating area for the floor and a second seating area for the balcony. 
* **Row** - A row in the seating data. All rows in a seating area have the same number of columns (it's a grid).
A (row, column) coordinate contains a `Space`.
* **Space** - A coordinate in the seating grid identified by (row, column). A space may be blank/empty
(aisle or dead space) or it may contain a seat and/or table. Spaces have a `seatStatus` that describes 
whether the seat is sold or not. `EMPTY` should be counted as an unsold seat. `SOLD` and `RESERVED` should be counted as
a "sold" seat. Other values (`NONE`, `BROKEN`, `PLACEHOLDER`, and `UNKNOWN`) should not factor into the fill rate calculation.
* **Business Day** - A business day at the Alamo runs from 6:00am to 6:00am so a show at 12:30am the early 
morning of 3 March is part of the business day identified by previous calendar day 2 March. We usually think about
time in terms of the time at the cinema (cinema local time or CLT). Our middleware APIs identify times as UTC or CLT
but most of the APIs we consume do not and are generally in CLT. The `timeZoneATE` field on cinema in the market
feed will let you translate back and forth. The constant
`com.drafthouse.mcc.domain.Hardcoded.DateTime.BUSINESS_DAY_START_TIME_CLT` captures the boundary in LocalTime.
* **Day Part** - For analysis we often break up the day into a few chunks as the behavior of guests (and film
programming) is different during different parts of the day. We call these chunks "day parts". The class
`com.drafthouse.mcc.domain.DayPart` identifies the way we want to slice up the day for this excercise.

### Available APIs

* **Market Slugs** - `GET https://drafthouse.com/api/v1/slugs/markets` - Provides the market ID (vistaId), slug, 
and status (we only care about "open" markets) for all of the markets. This updates no more frequently 
than every 30 minutes but it really only changes when we add or close a market (several times a year).
You'll need to know all of the markets in order to implement a national roll-up. 
[See it in action](https://drafthouse.com/api/v1/slugs/markets).
* **Market Feed** - `GET https://feeds.drafthouse.com/adcService/showtimes.svc/marketFilms/{marketIdOrSlug}` -
A monster of an endpoint that contains all of the sessions in a market that are currently for sale
(identifiable by ID or slug) along with summary information about the films and cinemas. The structure is
inconvenient. This data refreshes every 10 minutes and sessions (and films) will come and go as they are
passed by or scheduled.
[See it in action](https://feeds.drafthouse.com/adcService/showtimes.svc/marketFilms/austin) (for Austin).
* **Seating Data** - `GET /s/mother/v1/page/seats/preview/{cinemaId}/{sessionId}` - The seating data for the
specified session. Space `seatStatus` of `SOLD` or `RESERVED` are considered "sold". Space `seatStatus` of `EMPTY` is "not sold".
Other seatStatus values should be ignored/not counted. This data is cached for a minute or two but changes
frequently, particularly as it gets close to show time.
[See it in action](http://drafthouse.com/s/mother/v1/app/seats/0301/130090) (for a 24 Jun 2020 session
(130090) at Winchester, VA (0301)).
* **Seating Preview** - `GET /s/mother/v1/app/seats/{cinemaId}/{sessionId}/render` - The seating data for the
specified session rendered as ASCII art. This might be useful to help understand the seating data. "X" is a sold seat.
This data is live/no cache.
[See it in action](http://drafthouse.com/s/mother/v1/app/seats/0301/130090/render) (for a 24 Jun 2020 session
(130090) at Winchester, VA (0301)).
* **Film Content** - `GET https://drafthouse.com/api/v1/shows/{filmSlug}` or
`GET https://drafthouse.com/api/v1/shows/id/{filmId}`. This endpoint contains the content for the film such as
images, trailers, and the title displayed on the website (often slightly different than the title available
from the ticketing system via the market feed). The show feed also contains an (optional) `series` property that
identifies which series the film is part of (if any). You'll need this for the extra credit.
[See it in action](http://drafthouse.com/api/v1/shows/weird-wednesday-nowhere) (for Weird Wednesday Nowhere)

## Code Tour and Tips

### Code Layout

These are the parts of the provided codebase that you are likely to have to modify or understand a bit more deeply.

* `README.md` - This file. Add a section for your notes or add a separate notes file.
* `build.sbt` - Add any new dependencies or test configuration here.
* `service.log` and `access.log` - The `src/main/resources/logback.xml` configuration outputs two log files
in the current working directory
* `src/main/resources/application.conf` - The root configuration file for the app. Add any configuration points
you need here.
* `src/main/scala` - The code
    * `com.drafhouse.mcc.AdcMccServer` - The server. Main. Bootstrap city. If you write any new (Guice) modules
    (unlikely), you will need to add them to the `modules` property. If you add a new (Finatra) controller (or filter),
    you will need to add that to the `router` in `configureHttp`.
    * `com.drafthouse.mcc.core.CommonCirceSerializers` - If you need to write a new custom Circe serializer
    (encoder/decoder), this is a good place to put it. There are a few examples for enums and time stamps.
    Generally wherever we're serializing, we'll import all the implicits (CommonCirceSerializers._)
    so they are available as needed.
    * `com.drafthouse.mcc.core.AdcBaseController` - You should extend this for any controllers you build as it
    handles Swagger and logging.
    * `com.drafthouse.mcc.swagger.BaseSwaggerModel` - Any top-level API documentation should go in here.
    * `com.drafthouse.mcc.sample` - Sample controllers and services so you can see the basic mechanics.
    * `com.drafthouse.mcc.domain.Hardcoded` - A place to put global constants. They always exist.
    * `com.drafthouse.mcc.domain.*Client` - Annotations used to differentiate which `HttpClient` should be
    injected. We use type-based auto injection but there are typically multiple HttpClients and the
    dependency injector needs a way of identifying which to use. These are configured in `HttpClientModules`
    but you should only need the two: `@FeedsClient` for the market feed (feeds.drafthouse.com) and
    `@DrafthouseClient` for the other endpoints (drafthouse.com). See `SampleFeedService` for an example of
    using `@FeedsClient`
    * `com.drafthouse.util.TwitterConverters` - The implicits in this object will help you translate between
    Twitter and Scala implementations of Futures if you find yourself using a library that uses Scala Futures.
    * `com.drafthouse.mcc.domain.AdcError` - Exceptions that understand how we report errors with categories
    and codes. Use one of these Exceptions and create custom `ErrorCode`s as necessary.
    * `com.drafthouse.mcc.domain.AdcExceptionMapper` - You likely don't need to worry about this but if you
    introduce a custom Exception type that requires a custom error serialization, this is where you do that.
    
### Notes/Tips

* **Futures** - This framework uses Twitter Futures (extensively). They are analogous to (but predate) Scala Futures.
You can convert between the two with the implicits in `com.drafthouse.util.TwitterConverters` if you use a library
that uses Scala Futures but barring that, you'll just be working with Twitter Futures.
[Read about Twitter Futures](https://twitter.github.io/finagle/guide/Futures.html).
Note that any blocking calls (likely from external libraries) should be wrapped in a `FuturePool` to keep the
code base non-blocking. `for` comprehensions yielding Futures is the common code pattern.
If you aren't consuming and producing Futures in your services and controllers, you're doing something wrong.
Everything (except simple helper methods) should be asynchronous.
* **Options** - The Option type class is probably the second most widely used type class after Future so
Option[Future[A]] and Future[Option[A]] maybe need combining in a number of places.
Enjoy a good Future[Option[Future[A]]] and remember that we want to be able to understand the code.
* **Errors** - Use `Future.exception` and Exceptions from `AdcError`. Define your own `ErrorCode`s as needed.
* **Payloads** - Success payloads should contain a `data` property with whatever structure is relevant under `data`.
Error payloads (mostly already handled by `AdcExceptionMapper`) should contain an `error` property with
a nested `errorCode` property identifying an error category and code.
* **API documentation** - Document your APIs. Controllers should provide their endpoints via ***method***WithDoc methods
which should include parameters (`PathParameter` and `QueryParameter`) and descriptions. Follow the sample controllers.
Annotate any serialized class properties with `ApiParam` as these show up in the "model" in Swagger.
* **Circe** - Use Circe auto serialization if you can to save yourself some effort.

# Questions/Blocked?

Shoot us an email or give us a call. We don't want you stuck and frustrated on something stupid.

Have some feedback on the assignment, let us know.

 
Good Luck,

Your Friends at Alamo Drafthouse