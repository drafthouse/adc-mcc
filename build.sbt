name := "adc-mcc"
organization := "com.drafthouse"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.8"

javacOptions ++= Seq("-Xlint:unchecked")
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xmax-classfile-name", "100")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.jcenterRepo,
  "Twitter Maven" at "https://maven.twttr.com",
  "Finatra Repo" at "https://twitter.github.com/finatra"
)

// assembly for packaging as single jar
assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case other => MergeStrategy.defaultMergeStrategy(other)
}
assemblyJarName in assembly := s"${name.value}-exec.jar"

Revolver.settings
javaOptions in reStart ++= Seq(
  "-Xss10m",
  "-Xmx2g",
  "-Djava.net.preferIPv4Stack=true",
  "-Djava.awt.headless=true"
)
// Uncomment to pass traffic through a SOCKS proxy (like Charles proxy or Fiddler)
//reStartArgs ++= Seq(
//  "-com.twitter.finagle.socks.socksProxyHost=localhost",
//  "-com.twitter.finagle.socks.socksProxyPort=50002"
//)
reLogTag in reStart := "adc-mcc"
Revolver.enableDebugging(port = 5050, suspend = false)

lazy val versions = new {
  val finatra = "18.6.0"
  val bijection = "0.9.5"
  val logback = "1.1.6"
  val guice = "4.2.0"
  val typesafeConfig = "1.3.0"
  val ficus = "1.4.3" // for scala friendly typesafe config
  val cats = "0.8.1"
  val circe = "0.6.0"

  val commonsIo = "2.5"
  val commonsCodec = "1.10"

  // ---- Documentation ----
  val swagger = "18.6.0"
  val swaggerUi = "2.2.10"
  val jackson = "2.9.6"
}

libraryDependencies ++= Seq(
  // typesafe config
  "com.typesafe" % "config" % versions.typesafeConfig,
  "com.iheart" %% "ficus" % versions.ficus, // for scala friendly typesafe config

  // finatra
  "com.twitter" %% "finatra-http" % versions.finatra,
  "com.twitter" %% "finatra-slf4j" % "2.13.0",
  "com.twitter" %% "finatra-httpclient" % versions.finatra,
  "com.twitter" %% "bijection-core" % versions.bijection,
  "com.twitter" %% "bijection-util" % versions.bijection,

  // Other infrastructure
  "ch.qos.logback" % "logback-classic" % versions.logback,

  // functional
  "org.typelevel" %% "cats" % versions.cats,

  // json
  "io.circe" %% "circe-core" % versions.circe,
  "io.circe" %% "circe-generic" % versions.circe,
  "io.circe" %% "circe-parser" % versions.circe,
  "io.circe" %% "circe-generic-extras" % versions.circe,

  // misc
  "commons-io" % "commons-io" % versions.commonsIo,
  "commons-codec" % "commons-codec" % versions.commonsCodec,

  // Documentation
  "com.jakehschwartz" %% "finatra-swagger" % versions.swagger,
  "org.webjars" % "swagger-ui" % versions.swaggerUi
)

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % versions.jackson
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-paranamer" % versions.jackson
dependencyOverrides += "com.fasterxml.jackson.module" %% "jackson-module-scala" % versions.jackson
dependencyOverrides += "org.webjars" % "swagger-ui" % versions.swaggerUi

excludeDependencies ++= Seq(
 // ExclusionRule("io.netty", "netty")
 ExclusionRule("commons-logging", "commons-logging") // commons-logging conflicts with jcl-over-slf4j which we use
)

// ---- Test options
fork in Test := true
