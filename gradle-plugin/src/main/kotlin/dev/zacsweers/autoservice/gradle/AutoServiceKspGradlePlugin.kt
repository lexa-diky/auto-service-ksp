package dev.zacsweers.autoservice.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.jvm.tasks.ProcessResources
import javax.inject.Inject

public abstract class AutoServiceKspGradlePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("autoServiceKsp", AutoServiceKspExtension::class.java)
    project.run {
      pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        // Necessary to ensure the generated service file is included in the jar
        extensions.getByType(SourceSetContainer::class.java).configureEach { sourceSet ->
          sourceSet.resources {
            it.srcDir("build/generated/ksp/src/${sourceSet.name}/resources")
          }
        }

        val compileKotlin = tasks.named("compileKotlin")
        tasks.named("processResources", ProcessResources::class.java).configure {
          it.dependsOn(compileKotlin)
        }

        dependencies.add("implementation", extension.autoServiceVersion.map { "com.google.auto.service:auto-service-annotations:$it" })
      }

      pluginManager.withPlugin("symbol-processing") {
        dependencies.add("ksp", extension.autoServiceKspVersion.map { "dev.zacsweers.autoservice:auto-service-ksp:$it" })
      }
    }
  }
}

/** Configuration for the AutoServiceKsp gradle plugin. */
public abstract class AutoServiceKspExtension @Inject constructor(objects: ObjectFactory) {
  /**
   * Specifies the version of auto-service-ksp to use. Defaults to the version of the gradle plugin.
   */
  public val autoServiceKspVersion: Property<String> = objects.property(String::class.java)
    .convention(Versions.AUTO_SERVICE_KSP_VERSION)

  /** Specifies the version of auto-service-annotations to use. */
  public val autoServiceVersion: Property<String> = objects.property(String::class.java)
    .convention(Versions.AUTO_SERVICE_VERSION)
}