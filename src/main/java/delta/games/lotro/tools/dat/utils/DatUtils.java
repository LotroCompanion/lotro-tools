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

  /**
   * Extract a string property.
   * @param properties Properties to use.
   * @param propertyName Property name.
   * @param padding String to use between text items.
   * @return the extracted property.
   */
  public static String getFullStringProperty(PropertiesSet properties, String propertyName, String padding)
  {
    Object value=properties.getProperty(propertyName);
    return getFullString((String[])value,padding);
  }

  /**
   * Get a full string from an array of strings.
   * @param value Input string array.
   * @param padding Padding between string items.
   * @return the result string.
   */
  public static String getFullString(String[] value, String padding)
  {
    String ret=null;
    if (value!=null)
    {
      StringBuilder sb=new StringBuilder();
      for(String line : value)
      {
        line=line.replace("\\n","\n");
        if (sb.length()>0)
        {
          sb.append(padding);
        }
        sb.append(line);
      }
      ret=sb.toString().trim();
    }
    return ret;
  }
}
