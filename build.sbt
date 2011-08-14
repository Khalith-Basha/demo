name := "GitoChtoTo"

version := "0.1-SNAPHOT"

seq(webSettings :_*)

scalaVersion := "2.8.1"


resolvers ++= Seq (
  "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  "twitter.com"     at "http://maven.twttr.com/",
  "java.net"        at "http://download.java.net/maven/2"
  )


libraryDependencies ++= Seq (
  "org.apache.sshd"     % "sshd-core" 	      % "0.5.0",
  "org.eclipse.jgit"    % "org.eclipse.jgit"  % "1.0.0.201106090707-r",
  "org.slf4j"           % "slf4j-jdk14"       % "1.5.11",
  "com.twitter"         % "util-core"         % "1.11.1",
  "com.twitter"         % "util-logging"      % "1.11.1",
  "com.twitter"         % "util-eval"         % "1.11.1",
  "commons-codec"       % "commons-codec"     % "1.5",
  "commons-dbcp"        % "commons-dbcp"      % "1.4",
  "commons-pool"        % "commons-pool"      % "1.5.4",
  "com.jolbox"          % "bonecp"            % "0.7.1.RELEASE",
  "com.h2database"      % "h2"                % "1.3.158",
  "junit"               % "junit"             % "4.8"   % "test",
  "net.liftweb"         %% "lift-webkit"      % "2.4-M3" % "compile",
  "org.eclipse.jetty"   % "jetty-webapp"      % "7.3.0.v20110203" % "jetty",
  "ch.qos.logback"      % "logback-classic"   % "0.9.26"
  )