package com.drafthouse.mcc.module

import com.drafthouse.mcc.core.BootConfig
import javax.inject.Singleton
import com.google.inject.Provides
import com.twitter.inject.{Logging, TwitterModule}
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Ignore for assessment.
  *
  * Provides the global application configuration object based upon the
  * application environment defined by a command-line argument (-adc.env)
  * by reading the configuration files stored as resources.
  */
object TypesafeConfigModule extends TwitterModule with Logging {

  /**
    * List of available environment names. Each name provides a unique application configuration.
    */
  val validEnvs: List[String] = BootConfig.validEnvs
  //List("dev-remote", "dev-docker", "dev", "test", "test-remote", "beta", "stage", "prod")
  /**
    * A single string which lists all of the available environment names. Used in error messages.
    */
  val validEnvsString: String = validEnvs.mkString(", ")

  /**
    * Flag used to define the environment configuration.
    */
  val env = flag[String]("adc.env", BootConfig.defaultEnv, s"application run mode [${validEnvsString}]")

  def createConfig(environment: String): Config = {
    if (!validEnvs.contains(environment)) {
      throw new RuntimeException(s"Bad flag value for adc.env: ${env()}. Expected one of: ${validEnvsString}")
    }

    val configPath = BootConfig.configBase + environment + ".conf"
    val layer = ConfigFactory.parseResourcesAnySyntax(configPath)
    val config = BootConfig.makeConfig(layer)
    if (BootConfig.isConfigDumpEnabled(config, "layer")) {
      BootConfig.dump("layer", layer)
    }
    if (BootConfig.isConfigDumpEnabled(config)) {
      BootConfig.dump(s"${environment} configuration", config)
    }
    BootConfig.env = environment
    config
  }

  private lazy val config: Config = createConfig(env())

  /**
    * @return the application configuration for DI.
    */
  @Provides @Singleton
  def provideConfig(): Config = config
}
