package delta.games.lotro.tools.dat.quests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.Rewards;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.lore.deeds.DeedsWriter;

/**
 * Get quests definitions from DAT files.
 * @author DAM
 */
public class MainDatQuestsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatQuestsLoader.class);

  private DataFacade _facade;
  private List<DeedDescription> _deeds;
  private EnumMapper _category;

  private DatRewardsLoader _rewardsLoader;
  private DatObjectivesLoader _objectivesLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatQuestsLoader(DataFacade facade)
  {
    _facade=facade;
    _deeds=new ArrayList<DeedDescription>();
    _category=_facade.getEnumsManager().getEnumMapper(587202585);
    _rewardsLoader=new DatRewardsLoader(facade);
    _objectivesLoader=new DatObjectivesLoader(facade);
  }

  private void handleArc(int arcId)
  {
    PropertiesSet arcProps=_facade.loadProperties(arcId+0x9000000);
    //System.out.println(arcProps.dump());
    String arcName=DatUtils.getStringProperty(arcProps,"QuestArc_Name");
    System.out.println("Arc name: "+arcName);
    Object[] list=(Object[])arcProps.getProperty("QuestArc_Quest_Array");
    for(Object obj : list)
    {
      Integer questId=(Integer)obj;
      load(questId.intValue());
    }
  }

  private int nb=0;
  private DeedDescription load(int indexDataId)
  {
    DeedDescription deed=null;
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      //System.out.println("************* "+indexDataId+" *****************");
      if (indexDataId==1879000000)
      {
        System.out.println(properties.dump());
      }
      deed=new DeedDescription();
      // ID
      //deed.setIdentifier(indexDataId);
      // Name
      String name=DatUtils.getStringProperty(properties,"Quest_Name");
      System.out.println("\t"+indexDataId+": "+name);
      deed.setName(name);
      // Description
      String description=DatUtils.getStringProperty(properties,"Quest_Description");
      deed.setDescription(description);
      // Check
      boolean useIt=useIt(properties);
      if (!useIt)
      {
        //System.out.println("Ignored ID="+indexDataId+", name="+name);
        return null;
      }
      System.out.println("ID: "+indexDataId+", name: "+name);
      // Category
      Integer categoryId=((Integer)properties.getProperty("Quest_Category"));
      if (categoryId!=null)
      {
        String category=_category.getString(categoryId.intValue());
        deed.setCategory(category);
      }
      // Min level
      Integer minLevel=((Integer)properties.getProperty("Quest_ChallengeLevel"));
      deed.setMinLevel(minLevel);

      // Chain
      Integer nextQuestId=((Integer)properties.getProperty("Quest_NextQuest"));
      if (nextQuestId!=null)
      {
        System.out.println("Next quest: "+nextQuestId);
      }

      // Rewards
      Rewards rewards=deed.getRewards();
      _rewardsLoader.fillRewards(properties,rewards);

      // Objectives
      _objectivesLoader.handleObjectives(properties);
      // Web Store (needed xpack/region): WebStoreAccountItem_DataID
      nb++;
      _deeds.add(deed);
    }
    else
    {
      LOGGER.warn("Could not handle deed ID="+indexDataId);
    }
    return deed;
  }

  private boolean useIt(PropertiesSet properties)
  {
    Object isAccomplishment=properties.getProperty("Quest_IsAccomplishment");
    if (isAccomplishment==null) return true;
    if (!(isAccomplishment instanceof Integer)) return true;
    if (((Integer)isAccomplishment).intValue()==1) return false;
    return true;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      //System.out.println(classDefIndex);
      return (classDefIndex==1398); // TODO: use WStateClass constant
    }
    return false;
  }

  private void doIt()
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();

    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    //for(int id=1879363113;id<=1879363113;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        DeedDescription deed=load(id);
        if (deed!=null)
        {
          System.out.println("Deed: "+deed);
          deeds.add(deed);
        }
      }
    }
    System.out.println("Nb deeds: "+nb);
    DeedsWriter.writeSortedDeeds(_deeds,new File("deeds_dat.xml").getAbsoluteFile());
    System.out.println(_objectivesLoader.eventIds);
    System.out.println(_objectivesLoader.propNames);
    //System.out.println("Places: "+PlaceLoader._names);
  }

  void doIt2()
  {
    PropertiesSet deedsDirectory=_facade.loadProperties(0x7900E36F);
    //System.out.println(deedsDirectory.dump());
    Object[] list=(Object[])deedsDirectory.getProperty("QuestArcDirectory_QuestArc_Array");
    for(Object obj : list)
    {
      Integer arcId=(Integer)obj;
      handleArc(arcId.intValue());
    }
    System.out.println("Nb deeds: "+nb);
    DeedsWriter.writeSortedDeeds(_deeds,new File("deeds_dat.xml").getAbsoluteFile());
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatQuestsLoader(facade).doIt();
    facade.dispose();
  }
}
