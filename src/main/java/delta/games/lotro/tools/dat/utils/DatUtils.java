package delta.games.lotro.tools.dat.utils;

import delta.games.lotro.dat.data.PropertiesSet;

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
    String ret=null;
    Object value=properties.getProperty(propertyName);
    if (value!=null)
    {
      if (value instanceof String[])
      {
        ret=((String[])value)[0];
        ret=ret.replace("\\n","\n");
      }
    }
    return ret;
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
    String ret=null;
    Object value=properties.getProperty(propertyName);
    if (value!=null)
    {
      if (value instanceof String[])
      {
        StringBuilder sb=new StringBuilder();
        for(String line : (String[])value)
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
    }
    return ret;
  }

  /**
   * Fix a name (remove any trailing [...]).
   * @param name Name to fix.
   * @return Fixed name.
   */
  public static String fixName(String name)
  {
    if (name==null)
    {
      return name;
    }
    int index=name.lastIndexOf('[');
    if (index!=-1)
    {
      name=name.substring(0,index);
    }
    return name;
  }
}
