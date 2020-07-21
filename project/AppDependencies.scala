import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val testScope = "test, it, component"

  val compile = Seq(
    "uk.gov.hmrc"                    %%  "simple-reactivemongo"  % "7.30.0-play-27",
    ws,
    "uk.gov.hmrc"                    %%  "bootstrap-play-26"     % "1.14.0",
    "uk.gov.hmrc"                    %%  "wco-dec"               % "0.34.0",
    "uk.gov.hmrc"                    %%  "logback-json-logger"   % "4.8.0",
    "com.typesafe.play"              %%  "play-json-joda"        % "2.6.10",
    "com.github.cloudyrock.mongock"  %  "mongock-core"           % "2.0.2",
    "org.mongodb.scala"              %% "mongo-scala-driver"     % "2.9.0",
    "com.github.tototoshi"           %% "scala-csv"              % "1.3.6"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"            % "3.0.8"             % testScope,
    "org.scalatestplus.play"  %% "scalatestplus-play"   % "3.1.2"             % testScope,
    "com.github.tomakehurst"  % "wiremock-jre8"         % "2.25.1"            % testScope,
    "org.pegdown"             % "pegdown"               % "1.6.0"             % testScope,
    "com.typesafe.play"       %% "play-test"            % PlayVersion.current % testScope,
    "org.mockito"             % "mockito-core"          % "3.0.0"             % "test"
  )
}
