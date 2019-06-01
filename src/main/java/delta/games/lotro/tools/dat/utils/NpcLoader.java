package delta.games.lotro.tools.dat.utils;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * NPC loader.
 * @author DAM
 */
public class NpcLoader
{
  //private static final Logger LOGGER=Logger.getLogger(NpcLoader.class);

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
      PropertiesSet npcProperties=facade.loadProperties(0x9000000+npcId);
      if (npcProperties!=null)
      {
        // Name
        String npcName=DatUtils.getStringProperty(npcProperties,"Name");
        if (npcName!=null)
        {
          npcName=DatUtils.fixName(npcName);
          _names.put(Integer.valueOf(npcId),npcName);
        }
        ret=npcName;
      }
    }
    return ret;
  }
}
