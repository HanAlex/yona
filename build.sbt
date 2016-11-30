import com.typesafe.config._
import java.nio.file.Paths

name := """yona"""

version := "1.1.0"

libraryDependencies ++= Seq(
  // Add your project dependencies here,
  javaCore,
  javaJdbc,
  javaEbean,
  javaWs,
  cache,
  // Add your project dependencies here,
  "com.h2database" % "h2" % "1.3.176",
  // JDBC driver for mariadb
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.3.6",
  "net.contentobjects.jnotify" % "jnotify" % "0.94",
  // Core Library
  "org.eclipse.jgit" % "org.eclipse.jgit" % "3.5.3.201412180710-r",
  // Smart HTTP Servlet
  "org.eclipse.jgit" % "org.eclipse.jgit.http.server" % "3.5.3.201412180710-r",
  // Symlink support for Java7
  "org.eclipse.jgit" % "org.eclipse.jgit.java7" % "3.5.3.201412180710-r",
  // svnkit
  "org.tmatesoft.svnkit" % "svnkit" % "1.8.12",
  // svnkit-dav
  "sonia.svnkit" % "svnkit-dav" % "1.8.5-scm2",
  // javahl
  "org.tmatesoft.svnkit" % "svnkit-javahl16" % "1.8.11",
  "net.sourceforge.jexcelapi" % "jxl" % "2.6.10",
// shiro
  "org.apache.shiro" % "shiro-core" % "1.2.1",
  // commons-codec
  "commons-codec" % "commons-codec" % "1.2",
  // apache-mails
  "org.apache.commons" % "commons-email" % "1.2",
  "info.schleichardt" %% "play-2-mailplugin" % "0.9.1",
  "commons-lang" % "commons-lang" % "2.6",
  "org.apache.tika" % "tika-core" % "1.2",
  "commons-io" % "commons-io" % "2.4",
  "org.julienrf" %% "play-jsmessages" % "1.6.2",
  "commons-collections" % "commons-collections" % "3.2.1",
  "org.jsoup" % "jsoup" % "1.8.3",
  "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3",
  "org.mockito" % "mockito-all" % "1.9.0" % "test",
  "com.github.zafarkhaja" % "java-semver" % "0.7.2",
  "com.google.guava" % "guava" % "19.0",
  "com.googlecode.htmlcompressor" % "htmlcompressor" % "1.4",
  "org.springframework" % "spring-jdbc" % "4.1.5.RELEASE",
  "org.mozilla" % "rhino" % "1.7.7.1"
)

val projectSettings = Seq(
  // Add your own project settings here
  resolvers += "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  resolvers += "scm-manager release repository" at "http://maven.scm-manager.org/nexus/content/groups/public",
  resolvers += "tmatesoft release repository" at "http://maven.tmatesoft.com/content/repositories/releases",
  resolvers += "julienrf.github.com" at "http://julienrf.github.com/repo/",
  resolvers += "opencast-public" at "http://nexus.opencast.org/nexus/content/repositories/public",
  resolvers += "jfrog" at "http://repo.jfrog.org/artifactory/libs-releases/", 
  TwirlKeys.templateImports in Compile += "models.enumeration._",
  TwirlKeys.templateImports in Compile += "scala.collection.JavaConversions._",
  TwirlKeys.templateImports in Compile += "play.core.j.PlayMagicForJava._",
  TwirlKeys.templateImports in Compile += "java.lang._",
  TwirlKeys.templateImports in Compile += "java.util._",
  includeFilter in (Assets, LessKeys.less) := "*.less",
  excludeFilter in (Assets, LessKeys.less) := "_*.less",
  javaOptions in test ++= Seq("-Xmx2g", "-Xms1g", "-XX:MaxPermSize=1g", "-Dfile.encoding=UTF-8"),
  javacOptions ++= Seq("-Xlint:all", "-Xlint:-path"),
  scalacOptions ++= Seq("-feature")
)

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](name, version)

buildInfoPackage := "yona"

mappings in Universal :=
    (mappings in Universal).value.filterNot { case (_, file) => file.startsWith("conf/") }

NativePackagerKeys.bashScriptExtraDefines += """# Added by build.sbt
    |[ -n "$YONA_HOME" ] && addJava "-Duser.dir=$YONA_HOME"
    |[ -z "$YONA_HOME" ] && YONA_HOME=$(cd "$(realpath "$(dirname "$(realpath "$0")")")/.."; pwd -P)
    |addJava "-Dyobi.home=$YONA_HOME"
    |
    |yobi_config_file="$YONA_HOME"/conf/application.conf
    |yobi_log_config_file="$YONA_HOME"/conf/application-logger.xml
    |[ -f "$yobi_config_file" ] && addJava "-Dconfig.file=$yobi_config_file"
    |[ -f "$yobi_log_config_file" ] && addJava "-Dlogger.file=$yobi_log_config_file"
    |
    |addJava "-DapplyEvolutions.default=true"
    |""".stripMargin

NativePackagerKeys.batScriptExtraDefines += """
    | if "%JAVA_OPTS%"=="" SET JAVA_OPTS=-Duser.dir=%YONA_HOME% -Dyona.home=%YONA_HOME% -Dconfig.file=%YONA_HOME%\conf\application.conf -Dlogger.file=%YONA_HOME%\conf\application-logger.xml -DapplyEvolutions.default=true
    |""".stripMargin

lazy val yobi = (project in file("."))
      .enablePlugins(PlayScala)
      .enablePlugins(SbtWeb)
      .enablePlugins(SbtTwirl)
      .settings(projectSettings: _*)
      .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
      .settings(de.johoop.findbugs4sbt.FindBugs.findbugsSettings: _*)
      .settings(findbugsExcludeFilters :=  Some(
          <FindBugsFilter>
            <!-- Exclude classes generated by PlayFramework. See docs/examples
                 at http://findbugs.sourceforge.net/manual/filter.html for the
                 filtering rules. -->
            <Match>
              <Class name="~views\.html\..*"/>
            </Match>
            <Match>
              <Class name="~Routes.*"/>
            </Match>
            <Match>
              <Class name="~controllers\.routes.*"/>
            </Match>
          </FindBugsFilter>
        )
      )
