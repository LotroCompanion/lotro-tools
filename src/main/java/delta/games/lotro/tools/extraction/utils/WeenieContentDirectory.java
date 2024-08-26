package delta.games.lotro.tools.extraction.utils;

import java.util.List;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.DIDMapper;
import delta.games.lotro.dat.loaders.DataIdMapLoader;

/**
 * Utility methods related to Weenie Content.
 * @author DAM
 */
public class WeenieContentDirectory
{
  /**
   * Load some properties using the WeenieContent directory.
   * @param facade Data facade.
   * @param name Content name.
   * @return Some properties or <code>null</code> if not found.
   */
  public static PropertiesSet loadWeenieContentProps(DataFacade facade, String name)
  {
    int id=getWeenieID(facade,name);
    if (id!=0)
    {
      PropertiesSet props=facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
      return props;
    }
    return null;
  }

  /**
   * Load some properties using the WeenieContent directory.
   * @param facade Data facade.
   * @param name Content name.
   * @return Some properties or <code>null</code> if not found.
   */
  public static int getWeenieID(DataFacade facade, String name)
  {
    byte[] data=facade.loadData(0x28000000);
    DIDMapper map=DataIdMapLoader.decodeDataIdMap(data);
    Integer dataId=map.getDataIdForLabel("WEENIECONTENT");
    if (dataId!=null)
    {
      data=facade.loadData(dataId.intValue());
      DIDMapper subMap=DataIdMapLoader.decodeDataIdMap(data);
      List<String> subLabels=subMap.getLabels();
      for(String subLabel : subLabels)
      {
        if (subLabel.startsWith("\u0000\u0000")) continue;
        if (name.equals(subLabel))
        {
          int subDataId=subMap.getDataIdForLabel(subLabel).intValue();
          return subDataId;
        }
      }
    }
    return 0;
  }
}
