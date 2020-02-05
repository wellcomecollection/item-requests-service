import java.io.File
import java.util.UUID

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider

def setupProject(
    project: Project,
    folder: String,
    localDependencies: Seq[Project] = Seq(),
    externalDependencies: Seq[ModuleID] = Seq()
): Project = {

  Metadata.write(project, folder, localDependencies)

  val dependsOn = localDependencies
    .map { project: Project =>
      ClasspathDependency(
        project = project,
        configuration = Some("compile->compile;test->test")
      )
    }

  project
    .in(new File(folder))
    .settings(Common.settings: _*)
    .settings(DockerCompose.settings: _*)
    .enablePlugins(DockerComposePlugin)
    .enablePlugins(JavaAppPackaging)
    .dependsOn(dependsOn: _*)
    .settings(libraryDependencies ++= externalDependencies)
}

s3CredentialsProvider := { _ =>
  val builder = new STSAssumeRoleSessionCredentialsProvider.Builder(
    "arn:aws:iam::760097843905:role/platform-read_only",
    UUID.randomUUID().toString
  )

  builder.build()
}

lazy val common = setupProject(
  project = project,
  folder = "common",
  externalDependencies = RequestsDependencies.commonDependencies
)

lazy val requests_api = setupProject(
  project,
  "requests_api",
  localDependencies = Seq(common)
)

lazy val items_api = setupProject(
  project,
  "items_api",
  localDependencies = Seq(common)
)
