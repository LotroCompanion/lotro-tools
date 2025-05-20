package delta.games.lotro.tools.voicesExtractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.voicesExtractor.npc.NpcVoices;

/**
 * Analyzes quest data to find NPC/sound pairs.
 * @author DAM
 */
public class QuestAnalyzer
{
  private Map<Integer,NpcVoices> _npcVoices;
  private ScriptsInspectorForVoices _inspector;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public QuestAnalyzer(DataFacade facade)
  {
    _inspector=new ScriptsInspectorForVoices(facade);
    _npcVoices=new HashMap<Integer,NpcVoices>();
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
      Integer npcId=(Integer)roleProps.getProperty("QuestDispenser_NPC");
      if ((npcId!=null) && (npcId.intValue()!=0))
      {
        Integer soundID=(Integer)roleProps.getProperty("QuestDispenser_VOSoundID");
        if ((soundID!=null) && (soundID.intValue()!=0))
        {
          ResultElement element=new ResultElement();
          element.questID=questID;
          element.npcID=npcId.intValue();
          element.soundID=soundID.intValue();
          results.add(element);
        }
        Integer scriptID=(Integer)roleProps.getProperty("QuestDispenser_VOScriptID");
        if ((scriptID!=null) && (scriptID.intValue()!=0))
        {
          handleNpcScript(questID,npcId.intValue(),scriptID.intValue(),results);
        }
      }
    }
  }

  private void handleNpcScript(int questID, int npcID, int scriptID, List<ResultElement> results)
  {
    Integer key=Integer.valueOf(npcID);
    NpcVoices voices=_npcVoices.get(key);
    if (voices==null)
    {
      voices=_inspector.handleNPC(npcID);
      _npcVoices.put(key,voices);
    }
    List<Integer> soundIDs=voices.getSounds(questID,scriptID);
    if (!soundIDs.isEmpty())
    {
      for(Integer soundID : soundIDs)
      {
        ResultElement element=new ResultElement();
        element.questID=questID;
        element.npcID=npcID;
        element.soundID=soundID.intValue();
        results.add(element);
      }
    }
  }
}
