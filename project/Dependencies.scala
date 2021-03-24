import sbt._

object WellcomeDependencies {
  val defaultVersion = "26.4.3"

  lazy val versions = new {
    val typesafe = defaultVersion
    val fixtures = defaultVersion
    val json = defaultVersion
    val monitoring = defaultVersion
  }

  val jsonLibrary: Seq[ModuleID] = library(
    name = "json",
    version = versions.json
  )

  val fixturesLibrary: Seq[ModuleID] = library(
    name = "fixtures",
    version = versions.fixtures
  )

  val monitoringLibrary: Seq[ModuleID] = library(
    name = "monitoring",
    version = versions.monitoring
  )

  val monitoringTypesafeLibrary: Seq[ModuleID] = library(
    name = "monitoring_typesafe",
    version = versions.monitoring
  )

  val typesafeLibrary: Seq[ModuleID] = library(
    name = "typesafe_app",
    version = versions.typesafe
  ) ++ fixturesLibrary

  private def library(name: String, version: String): Seq[ModuleID] = Seq(
    "uk.ac.wellcome" %% name % version,
    "uk.ac.wellcome" %% name % version % "test" classifier "tests"
  )
}

object ExternalDependencies {
  lazy val versions = new {
    val akkaHttpCirce = "1.25.2"

    val scalatest = "3.0.1"
    val wiremock = "2.18.0"
    val logback = "1.2.3"
    val logstashLogback = "6.1"
  }

  val logbackDependencies = Seq(
    "ch.qos.logback" % "logback-classic" % versions.logback,
    "ch.qos.logback" % "logback-core" % versions.logback,
    "ch.qos.logback" % "logback-access" % versions.logback,
    "net.logstash.logback" % "logstash-logback-encoder" % versions.logstashLogback
  )

  val scalatestDependencies = Seq[ModuleID](
    "org.scalatest" %% "scalatest" % versions.scalatest % "test"
  )

  val akkaDependencies = Seq[ModuleID](
    "de.heikoseeberger" %% "akka-http-circe" % versions.akkaHttpCirce
  )

  val wiremockDependencies = Seq[ModuleID](
    "com.github.tomakehurst" % "wiremock" % versions.wiremock % "test"
  )
}

object RequestsDependencies {
  val commonDependencies =
    ExternalDependencies.akkaDependencies ++
      ExternalDependencies.scalatestDependencies ++
      ExternalDependencies.logbackDependencies ++
      ExternalDependencies.wiremockDependencies ++
      WellcomeDependencies.jsonLibrary ++
      WellcomeDependencies.monitoringLibrary ++
      WellcomeDependencies.typesafeLibrary ++
      WellcomeDependencies.monitoringTypesafeLibrary
}
