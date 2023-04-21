ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "procedural-images-generator"
  )

val circeVersion = "0.14.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "org.scalafx" % "scalafx_3" % "19.0.0-R30"
/*libraryDependencies ++= {
  // Determine OS version of JavaFX binaries
  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux") => "linux"
    case n if n.startsWith("Mac") => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ => throw new Exception("Unknown platform!")
  }
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
    .map(m => "org.openjfx" % s"javafx-$m" % "19" classifier osName)
}*/

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest-flatspec" % "3.2.15",
  "org.scalatest" %% "scalatest-matchers-core" % "3.2.15",
  "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.15"
)

val buildSettings = Defaults.defaultCompileSettings ++ Seq(
   javaOptions += "-Xmx4G",
)

javaOptions ++= Seq("-Xmx4G")