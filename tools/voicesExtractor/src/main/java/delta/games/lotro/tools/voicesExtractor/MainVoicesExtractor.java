package delta.games.lotro.tools.voicesExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DatConfiguration;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.tools.voicesExtractor.QuestAnalyzer.ResultElement;

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
    _rootDir=new File("d:\\tmp\\sounds2");
    _soundsExtractor=new SoundExtractor(_facade);
    _questAnalyzer=new QuestAnalyzer();
    _elements=new ArrayList<ResultElement>();
  }

  private void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    //for(int id=DEBUG_ID;id<=DEBUG_ID;id++)
    {
      byte[] data=_facade.loadData(id);
      if (data!=null)
      {
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==WStateClass.ACCOMPLISHMENT)
        {
          handleQuest(id);
        }
      }
    }
    for(ResultElement element : _elements)
    {
      handleElement(element);
    }
  }

  private void handleQuest(int questID)
  {
    PropertiesSet props=_facade.loadProperties(questID+DATConstants.DBPROPERTIES_OFFSET);
    _questAnalyzer.handleQuest(questID,props,_elements);
  }

  private void handleElement(ResultElement element)
  {
    String npcName=findNPCName(element.npcID);
    if (npcName==null)
    {
      return;
    }
    File rootDir=new File(_rootDir,npcName);
    rootDir.mkdirs();
    _soundsExtractor.saveSound(rootDir,element.questID,element.soundID);
  }

  private String findNPCName(int npcId)
  {
    PropertiesSet properties=_facade.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      // Name
      String npcName=DatStringUtils.getStringProperty(properties,"Name");
      npcName=DatStringUtils.fixName(npcName);
      return npcName;
    }
    return null;
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
