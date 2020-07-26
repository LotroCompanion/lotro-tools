package delta.games.lotro.tools.lore.maps.dynmap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Utility methods used for parsing dynmap data.
 * @author DAM
 */
public class ParsingUtils
{
  private static final Logger LOGGER=Logger.getLogger(ParsingUtils.class);

  /**
   * Parse a labels structure.
   * @param labels Input labels definitions in JSON.
   * @return the label for the default locale ("en").
   */
  public static String parseLabel(String labels)
  {
    // {en: "Eastern Gondor", de: "Ost-Gondor", fr: "L'Est Du Gondor"}
    try
    {
      JSONObject json=new JSONObject(labels);
      return parseLabel(json);
    }
    catch(Exception e)
    {
      LOGGER.error("Bad labels: ["+labels+"]",e);
    }
    return "";
  }

  /**
   * Parse a labels structure.
   * @param json Input labels definitions in JSON.
   * @return the label for the default locale ("en").
   */
  public static String parseLabel(JSONObject json)
  {
    // {en: "Eastern Gondor", de: "Ost-Gondor", fr: "L'Est Du Gondor"}
    try
    {
      String[] localeKeys=JSONObject.getNames(json);
      for(String localeKey : localeKeys)
      {
        if ("en".equals(localeKey))
        {
          String value=json.getString(localeKey);
          return value;
        }
      }
    }
    catch(Exception e)
    {
      LOGGER.error("Bad labels: ["+json+"]",e);
    }
    return "";
  }
}
