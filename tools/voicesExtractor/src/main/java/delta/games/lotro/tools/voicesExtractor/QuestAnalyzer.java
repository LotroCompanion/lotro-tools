package delta.games.lotro.tools.voicesExtractor;

import java.util.List;

import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Analyzes quest data to find NPC/sound pairs.
 * @author DAM
 */
public class QuestAnalyzer
{
  /**
   * Result element: NPC/sound pair.
   * @author DAM
   */
  public class ResultElement
  {
    /**
     * Quest ID.
     */
    public int questID;
    /**
     * NPC identifier.
     */
    public int npcID;
    /**
     * Sound identifier.
     */
    public int soundID;
  }

  /**
   * Handle a single quest.
   * @param questID Quest ID.
   * @param properties Quest properties.
   * @param results Storage for results.
   */
  public void handleQuest(int questID, PropertiesSet properties, List<ResultElement> results)
  {
    // Quest_RoleArray
    Object[] roles=(Object[])properties.getProperty("Quest_RoleArray");
    if (roles==null)
    {
      return;
    }
    for(Object roleObj : roles)
    {
/*
  QuestDispenser_Action: 6 (Bestow)
  QuestDispenser_NPC: 1879228301
  QuestDispenser_RoleName: Bestower1
  QuestDispenser_RoleSuccessText: 'I do not believe in long farewells. You have little time to delay. You must ride swiftly, for you have a long journey ahead of you.'\n\n<rgb=#FF0000>You will automatically travel to Talan Haldir in Lothlórien when you accept this quest.</rgb>\n
  QuestDispenser_VOScriptID: 268444047 (VO@NPC::File)
  QuestDispenser_VOSoundID: 704671567
  Quest_ObjectiveIndex: 0
*/
      PropertiesSet roleProps=(PropertiesSet)roleObj;
      //int dispenserAction=((Integer)roleProps.getProperty("QuestDispenser_Action")).intValue();
      Integer npcId=(Integer)roleProps.getProperty("QuestDispenser_NPC");
      Integer soundID=(Integer)roleProps.getProperty("QuestDispenser_VOSoundID");
      if ((npcId!=null) && (npcId.intValue()!=0) && (soundID!=null) && (soundID.intValue()!=0))
      {
        ResultElement element=new ResultElement();
        element.questID=questID;
        element.npcID=npcId.intValue();
        element.soundID=soundID.intValue();
        results.add(element);
      }
    }
  }
}
