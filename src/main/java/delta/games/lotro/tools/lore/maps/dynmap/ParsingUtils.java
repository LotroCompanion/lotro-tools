package delta.games.lotro.tools.lore.maps.dynmap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import delta.games.lotro.maps.data.Labels;

/**
 * Utility methods used for parsing dynmap data.
 * @author DAM
 */
public class ParsingUtils
{
  private static final Logger _logger=Logger.getLogger(ParsingUtils.class);

  /**
   * Parse a labels structure.
   * @param labelsManager Storage for extracted data.
   * @param labels Input labels definitions in JSON.
   */
  public static void parseLabels(Labels labelsManager, String labels)
  {
    // {en: "Eastern Gondor", de: "Ost-Gondor", fr: "L'Est Du Gondor"}
    try
    {
      JSONObject o=new JSONObject(labels);
      String[] localeKeys=JSONObject.getNames(o);
      for(String localeKey : localeKeys)
      {
        String value=o.getString(localeKey);
        labelsManager.putLabel(localeKey,value);
      }
    }
    catch(Exception e)
    {
      _logger.error("Bad labels: ["+labels+"]",e);
    }
  }
}
