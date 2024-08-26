package delta.games.lotro.tools.extraction.items;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Utility methods related to item level offsets.
 * @author DAM
 */
public class ItemLevelOffsetsUtils
{
  /**
   * Build a map distribution type to item level offset.
   * @param facade Data facade.
   * @return a map.
   */
  public static Map<Integer,Integer> buildOffsetsMap(DataFacade facade)
  {
    Map<Integer,Integer> ret=new HashMap<Integer,Integer>();
    // InventoryControl
    PropertiesSet props=facade.loadProperties(0x79000230);
    Object[] offsetsList=(Object[])props.getProperty("InventoryControl_DistributionToLevelOffsetList");
    for(Object offsetEntry : offsetsList)
    {
      PropertiesSet entryProps=(PropertiesSet)offsetEntry;
      int type=((Integer)entryProps.getProperty("InventoryControl_DistributionType")).intValue();
      int offset=((Integer)entryProps.getProperty("InventoryControl_LevelOffset")).intValue();
      if (offset!=0)
      {
        ret.put(Integer.valueOf(type),Integer.valueOf(offset));
      }
    }
    /*
  InventoryControl_DistributionToLevelOffsetList:
    #1:
      InventoryControl_DistributionType: 3 (Quest)
      InventoryControl_LevelOffset: 0
     */
    return ret;
  }
}
