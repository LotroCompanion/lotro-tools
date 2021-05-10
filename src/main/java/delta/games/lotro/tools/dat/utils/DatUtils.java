package delta.games.lotro.tools.dat.utils;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatStringUtils;

/**
 * Utility methods related to DAT parsing.
 * @author DAM
 */
public class DatUtils
{
  /**
   * Extract a string property.
   * @param properties Properties to use.
   * @param propertyName Property name.
   * @return the extracted property.
   */
  public static String getStringProperty(PropertiesSet properties, String propertyName)
  {
    return DatStringUtils.getStringProperty(properties,propertyName);
  }
}
