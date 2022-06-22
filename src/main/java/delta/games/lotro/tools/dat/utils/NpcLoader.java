package delta.games.lotro.tools.dat.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.utils.StringUtils;

/**
 * NPC loader.
 * @author DAM
 */
public class NpcLoader
{
  private static final Logger LOGGER=Logger.getLogger(NpcLoader.class);

  private static Map<Integer,String> _names=new HashMap<Integer,String>();

  /**
   * Load a NPC.
   * @param facade Data facade.
   * @param npcId NPC identifier.
   * @return the loaded NPC name.
   */
  public static String loadNPC(DataFacade facade, int npcId)
  {
    String ret=_names.get(Integer.valueOf(npcId));
    if (ret==null)
    {
      PropertiesSet npcProperties=facade.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
      if (npcProperties!=null)
      {
        // Name
        String npcName=DatUtils.getStringProperty(npcProperties,"Name");
        npcName=StringUtils.removeMarks(npcName);
        _names.put(Integer.valueOf(npcId),npcName);
        ret=npcName;
      }
      else
      {
        LOGGER.warn("Could not load NPC "+npcId+" from properties!");
      }
    }
    return ret;
  }
}
