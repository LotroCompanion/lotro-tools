package delta.games.lotro.tools.extraction.achievables;

import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Utility methods related to quests/deeds loader.
 * @author DAM
 */
public class DatQuestDeedsUtils
{
  /**
   * Indicates if the given ID is the one of a quest or deed.
   * @param facade Data facade.
   * @param id Identifier to test.
   * @return <code>true</code> if it is, <code>false</code> otherwise.
   */
  public static boolean isQuestOrDeedId(DataFacade facade, int id)
  {
    byte[] data=facade.loadData(id);
    if (data!=null)
    {
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return (classDefIndex==WStateClass.ACCOMPLISHMENT);
    }
    return false;
  }

  /**
   * Indicates if the given properties are for a quest.
   * @param properties Properties to use.
   * @return <code>true</code> for a quest, <code>false</code> for a deed.
   */
  public static boolean isQuest(PropertiesSet properties)
  {
    Object isAccomplishment=properties.getProperty("Quest_IsAccomplishment");
    if (isAccomplishment instanceof Integer)
    {
      if (((Integer)isAccomplishment).intValue()==1)
      {
        return false;
      }
    }
    return true;
  }
}
