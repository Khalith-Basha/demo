name := "luna-tool"

version := "0.8-SNAPSHOT"

seq(webSettings :_*)

scalaVersion := "2.9.1"

scalacOptions += "-deprecation"

checksums := Nil

resolvers ++= Seq (
  "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  "scala-tools snapshots" at "http://scala-tools.org/repo-snapshots",
  "my github repo" at "http://btd.github.com/maven2"
  )


libraryDependencies ++= Seq (
  "net.databinder"      %% "dispatch-nio"                 % "0.8.7",
  "org.mindrot"         % "jbcrypt"                       % "0.3m",
  "org.eclipse.jgit"    % "org.eclipse.jgit"              % "1.3.0.201202151440-r",
  "org.apache.sshd"     % "sshd-core"                     % "0.6.0",
  "org.apache.mina"     % "mina-core"                     % "2.0.4",
  "commons-codec"       % "commons-codec"                 % "1.5",
  "org.lunatool"        %% "scala-linguist"               % "1.3",
  "junit"               % "junit"                         % "4.8"                    % "test",
  "net.liftweb"         %% "lift-webkit"                  % "2.5-SNAPSHOT"           % "compile",
  "net.liftweb"         %% "lift-db"                      % "2.5-SNAPSHOT"           % "compile",
  "net.liftweb"         %% "lift-mongodb"                 % "2.5-SNAPSHOT"           % "compile",
  "net.liftweb"         %% "lift-mongodb-record"          % "2.5-SNAPSHOT"           % "compile",
  "com.foursquare"      %% "rogue"                        % "1.1.2"                  intransitive(),
  "org.eclipse.jetty"   % "jetty-webapp"                  % "8.0.4.v20111024"        % "container",
  "ch.qos.logback"      % "logback-classic"               % "1.0.0"                  % "container"
  )