package app.vizion.exampleProject.auth.utils

object TypesafeUtils {

  import com.typesafe.config.Config
  import java.util.Properties

  def toProperties(config: Config): Properties = {
    val properties = new Properties()
    config
      .entrySet()
      .forEach(e => {
        properties.setProperty(e.getKey, config.getString(e.getKey))
        () //this is ugly ...
      })
    properties
  }
}
