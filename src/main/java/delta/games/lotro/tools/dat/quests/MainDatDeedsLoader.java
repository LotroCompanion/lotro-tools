package delta.games.lotro.tools.dat.quests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.common.rewards.Rewards;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.lore.deeds.DeedsWriter;

/**
 * Get deeds definitions from DAT files.
 * @author DAM
 */
public class MainDatDeedsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatDeedsLoader.class);

  private DataFacade _facade;
  private List<DeedDescription> _deeds;
  private EnumMapper _category;
  private EnumMapper _uiTab;

  private DatRewardsLoader _rewardsLoader;
  private DatObjectivesLoader _objectivesLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatDeedsLoader(DataFacade facade)
  {
    _facade=facade;
    _deeds=new ArrayList<DeedDescription>();
    _category=_facade.getEnumsManager().getEnumMapper(587202587);
    _uiTab=_facade.getEnumsManager().getEnumMapper(587202588);
    _rewardsLoader=new DatRewardsLoader(facade);
    _objectivesLoader=new DatObjectivesLoader(facade);
  }

  private int nb=0;
  private DeedDescription load(int indexDataId)
  {
    DeedDescription deed=null;
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      System.out.println("************* "+indexDataId+" *****************");
      if (indexDataId==1879000000)
      {
        System.out.println(properties.dump());
      }
      // Check
      boolean useIt=useIt(properties);
      if (!useIt)
      {
        //System.out.println("Ignored ID="+indexDataId+", name="+name);
        return null;
      }
      deed=new DeedDescription();
      // ID
      deed.setIdentifier(indexDataId);
      // Name
      String name=DatUtils.getStringProperty(properties,"Quest_Name");
      deed.setName(name);
      System.out.println("ID: "+indexDataId+", name: "+name);
      // Description
      String description=DatUtils.getStringProperty(properties,"Quest_Description");
      deed.setDescription(description);
      // Category
      Integer categoryId=((Integer)properties.getProperty("Accomplishment_Category"));
      if (categoryId!=null)
      {
        String category=_category.getString(categoryId.intValue());
        deed.setCategory(category);
      }
      // UI Tab
      Integer uiTab=((Integer)properties.getProperty("Accomplishment_UITab"));
      String uiTabName=_uiTab.getString(uiTab.intValue());
      System.out.println("UI tab: "+uiTabName);
      // Deed type
      handleDeedType(deed,properties);
      // Min level
      Integer minLevel=((Integer)properties.getProperty("Accomplishment_MinLevelToStart"));
      deed.setMinLevel(minLevel);
      // Challenge level
      //Integer challengeLevel=(Integer)properties.getProperty("Quest_ChallengeLevel");

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
    if (isAccomplishment==null) return false;
    if (!(isAccomplishment instanceof Integer)) return false;
    if (((Integer)isAccomplishment).intValue()!=1) return false;
    return true;
  }

  private void handleDeedType(DeedDescription deed, PropertiesSet properties)
  {
    DeedType type=null;
    Integer categoryId=((Integer)properties.getProperty("Accomplishment_Category"));
    if (categoryId!=null)
    {
      int typeCode=categoryId.intValue();
      if (typeCode==22)
      {
        type=DeedType.CLASS;
      }
      else if (typeCode==2)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.CAPTAIN);
      }
      else if (typeCode==3)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.GUARDIAN);
      }
      else if (typeCode==5)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.MINSTREL);
      }
      else if (typeCode==6)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.BURGLAR);
      }
      else if (typeCode==26)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.HUNTER);
      }
      else if (typeCode==28)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.CHAMPION);
      }
      else if (typeCode==30)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.LORE_MASTER);
      }
      else if (typeCode==35)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.WARDEN);
      }
      else if (typeCode==36)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.RUNE_KEEPER);
      }
      else if (typeCode==38)
      {
        type=DeedType.CLASS;
        deed.setRequiredClass(CharacterClass.BEORNING);
      }
      else if (typeCode==34)
      {
        type=DeedType.EVENT;
      }
      else if (typeCode==1)
      {
        type=DeedType.EXPLORER;
      }
      else if (typeCode==33)
      {
        type=DeedType.LORE;
      }
      else if (typeCode==25)
      {
        type=DeedType.RACE;
      }
      else if (typeCode==13)
      {
        type=DeedType.RACE;
        deed.setRequiredRace(Race.MAN);
      }
      else if (typeCode==21)
      {
        type=DeedType.RACE;
        deed.setRequiredRace(Race.ELF);
      }
      else if (typeCode==27)
      {
        type=DeedType.RACE;
        deed.setRequiredRace(Race.DWARF);
      }
      else if (typeCode==29)
      {
        type=DeedType.RACE;
        deed.setRequiredRace(Race.HOBBIT);
      }
      else if (typeCode==37)
      {
        type=DeedType.RACE;
        deed.setRequiredRace(Race.BEORNING);
      }
      else if (typeCode==11)
      {
        type=DeedType.REPUTATION;
      }
      else if (typeCode==20)
      {
        type=DeedType.SLAYER;
      }
      else
      {
        LOGGER.warn("Unmanaged type: "+typeCode);
      }
    }
    deed.setType(type);
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      //System.out.println(classDefIndex);
      return (classDefIndex==WStateClass.ACCOMPLISHMENT);
    }
    return false;
  }

  private void doIt()
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();

    for(int id=0x70000000;id<=0x77FFFFFF;id++)
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
    //System.out.println("Places: "+PlaceLoader._names);
  }

  void doIt2()
  {
    PropertiesSet deedsDirectory=_facade.loadProperties(0x79000255);
    //System.out.println(deedsDirectory.dump());
    Object[] list=(Object[])deedsDirectory.getProperty("Accomplishment_List");
    for(Object obj : list)
    {
      if (obj instanceof Integer)
      {
        load(((Integer)obj).intValue());
      }
      else if (obj instanceof Object[])
      {
        Object[] objs=(Object[])obj;
        for(Object obj2 : objs)
        {
          if (obj2 instanceof Integer)
          {
            load(((Integer)obj2).intValue());
          }
          else
          {
            System.out.println(obj.getClass());
          }
        }
      }
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
    new MainDatDeedsLoader(facade).doIt();
    facade.dispose();
  }
}
