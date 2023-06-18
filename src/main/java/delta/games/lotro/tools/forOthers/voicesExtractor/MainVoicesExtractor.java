package delta.games.lotro.tools.forOthers.voicesExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DatConfiguration;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.agents.npcs.NPCsManager;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;
import delta.games.lotro.tools.forOthers.voicesExtractor.QuestAnalyzer.ResultElement;
import delta.games.lotro.tools.lore.sounds.SoundExtractor;

/**
 * Tool to extract quest NPC voices.
 * @author DAM
 */
public class MainVoicesExtractor
{
  private DataFacade _facade;
  private File _rootDir;
  private SoundExtractor _soundsExtractor;
  private QuestAnalyzer _questAnalyzer;
  private List<ResultElement> _elements;

  private MainVoicesExtractor()
  {
    DatConfiguration cfg=new DatConfiguration();
    cfg.setLocale("fr");
    _facade=new DataFacade(cfg);
    _rootDir=new File("d:\\tmp\\sounds");
    _soundsExtractor=new SoundExtractor(_facade);
    _questAnalyzer=new QuestAnalyzer();
    _elements=new ArrayList<ResultElement>();
  }

  private void doIt()
  {
    List<Integer> questIDs=findQuests();
    System.out.println("Found "+questIDs);
    for(Integer questID : questIDs)
    {
      handleQuest(questID.intValue());
    }
    for(ResultElement element : _elements)
    {
      handleElement(element);
    }
  }

  private void handleQuest(int questID)
  {
    PropertiesSet props=_facade.loadProperties(questID+DATConstants.DBPROPERTIES_OFFSET);
    _questAnalyzer.handleQuest(props,_elements);
  }

  private void handleElement(ResultElement element)
  {
    NpcDescription npc=findNPC(element.npcID);
    if (npc==null)
    {
      return;
    }
    String name=npc.getName();
    File rootDir=new File(_rootDir,name);
    rootDir.mkdirs();
    _soundsExtractor.saveSound(rootDir,element.soundID);
  }

  private NpcDescription findNPC(int id)
  {
    return NPCsManager.getInstance().getNPCById(id);
  }

  private List<Integer> findQuests()
  {
    List<Integer> ret=new ArrayList<Integer>();
    for(QuestDescription quest : QuestsManager.getInstance().getAll())
    {
      // 14=Ered Luin, 150=Epic - Vol. III, Book 6: Mists of Anduin
      //if (quest.getCategory().getCode()==150)
      {
        ret.add(Integer.valueOf(quest.getIdentifier()));
      }
    }
    return ret;
  }

  /**
   * Main method for this tool
   * @param args Not used
   */
  public static void main(String[] args)
  {
    new MainVoicesExtractor().doIt();
  }
}
