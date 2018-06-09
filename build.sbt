scalaVersion in ThisBuild := "2.12.6"

lazy val root = (project in file(".")).settings(name := """money-transfer-example""").enablePlugins(Common, PlayScala)

libraryDependencies += guice
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.3"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3"
libraryDependencies += "com.h2database" % "h2" % "1.4.197"
libraryDependencies += specs2 % Test
