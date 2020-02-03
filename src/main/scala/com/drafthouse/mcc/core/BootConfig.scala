package com.drafthouse.mcc.core

import com.twitter.inject.Logging
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}

import scala.collection.JavaConverters._

class BootConfig extends Logging {
  private val application: Config = ConfigFactory.defaultApplication()
  private val overrides: Config = ConfigFactory.defaultOverrides()
  private val reference: Config = ConfigFactory.defaultReference()

  private val _config: Config = overrides.withFallback(application).withFallback(reference)

  def config: Config = _config

  def configDumpEnabled: Boolean = _config.getBoolean("adc.configDump.bootstrap")

  def dump(title: String, config: Config): Unit = {
    val options = ConfigRenderOptions.defaults()
    println(title)
    println(config.root().render(options))
  }

  {
    if (configDumpEnabled) {
      dump("Bootstrap", _config)
    }
  }
}

/**
  * This is the Bootstrap configuration object used to handle any configuration which may need
  * to be performed before the depenency injection lifecycle is completed. In general, access is
  * through the object rather than the associated implementation class.
  */
object BootConfig {
  /**
    * The actual implementation class instance.
    */
  private val singleton: BootConfig = new BootConfig

  /**
    * @return the bootstrap typesafe/lightbend configuration.
    */
  def config: Config = singleton.config

  /**
    * @return Resource prefix used to load configurations.
    */
  def configBase: String = "conf/"

  /**
    * @return name of the default configuration environment.
    */
  def defaultEnv: String = config.getString("adc.defaultEnv")

  /**
    * @return A list of valid names for a configuration environment.
    */
  def validEnvs: List[String] = config.getStringList("adc.validEnvs").asScala.toList

  /**
    *
    * @param config specific configuration object to query for the flag.
    * @return true if we should dump the contents upon startup.
    */
  def isConfigDumpEnabled(config: Config = singleton.config, form: String = "enabled"): Boolean = config.getBoolean(s"adc.configDump.${form}")

  /**
    * Write out a formatted version of the configuration information to the console.
    * @param title headline associated with the dump.
    * @param config the typesafe configuration to be dumped.
    */
  def dump(title: String, config: Config = singleton.config): Unit = singleton.dump(title, config)

  /**
    * @return prefix used to create the URL to talk to mother.
    */
  def servicePrefix: String = config.getString("adc.servicePrefix")

  /**
    * Create a new layered configuration with the existing layers and a new environment dependent layer.
    * @param layer environment dependent layer.
    * @return A composite configuration based upon the three existing layers and the new one.
    */
  def makeConfig(layer: Config): Config =
    singleton.overrides
      .withFallback(layer)
      .withFallback(singleton.application)
      .withFallback(singleton.reference)

  /**
    * The adc.env for the application. It is kept here so that sentry (AdcErrorReporter) which is loaded
    * before it is 'really' set can access it.
    */
  var env: String = defaultEnv

}
