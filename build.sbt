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

// Temporarily commented out until https://github.com/wellcometrust/platform/issues/3806
// In order to access our libraries in S3 we need to set the following:

s3CredentialsProvider := { _ =>
  val builder = new STSAssumeRoleSessionCredentialsProvider.Builder(
    "arn:aws:iam::760097843905:role/platform-read_only",
    UUID.randomUUID().toString
  )

  builder.build()
}

lazy val requests_api = setupProject(
  project,
  "requests_api",
  externalDependencies = RequestsDependencies.commonDependencies
)

lazy val status_api = setupProject(
  project,
  "status_api",
  externalDependencies = RequestsDependencies.commonDependencies
)
