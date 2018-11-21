package delta.games.lotro.tools.characters.dat;

import java.io.File;
import java.util.Collections;
import java.util.List;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.character.traits.io.xml.TraitDescriptionXMLWriter;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.progression.ProgressionsManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.utils.dat.DatIconsUtils;
import delta.games.lotro.tools.utils.dat.DatUtils;
import delta.games.lotro.utils.maths.Progression;
import delta.games.lotro.utils.maths.io.xml.ProgressionsXMLWriter;

/**
 * Get race definitions from DAT files.
 * @author DAM
 */
public class MainRaceDataLoader
{
  //private static final Logger LOGGER=Logger.getLogger(MainRaceDataLoader.class);

  private DataFacade _facade;
  private TraitsManager _traits;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainRaceDataLoader(DataFacade facade)
  {
    _facade=facade;
    _traits=new TraitsManager();
  }

  private void handleRace(int classId)
  {
    PropertiesSet properties=_facade.loadProperties(classId+0x9000000);

    /*
RaceTable_ClassList: 
  #1: 40
  #2: 24
  #3: 172
  #4: 23
  #5: 162
  #6: 185
  #7: 31
  #8: 194
RaceTable_Description: 
  #1: \nNot as long-lived as Elves, sturdy as dwarves, or resilient as hobbits, Men are renowned for their courage and resourcefulness.\n\n
RaceTable_GenderList: 
  #1: 
    RaceTable_GenderType: 4096
    RaceTable_Gender_Desc: 
      #1: The male men. Blah blah blah.
    RaceTable_Gender_Name: 
      #1: Race of Man (Male)
    RaceTable_Gender_Race_Name: 
      #1: Man
    RaceTable_RaceSelect_Background: 1091603817
    RaceTable_RaceSelect_Entity: 1191183880
    RaceTable_RaceSelect_Icon: 1091602757
    RaceTable_RaceSelect_LargeIcon: 1092350624
    RaceTable_RaceSelect_SmallIcon: 1092365270
RaceTable_NationalityList: 
  #1: 17
  #2: 16
  #3: 6
  #4: 5
RaceTable_Race: 23
    */
    System.out.println(properties.dump());

    loadGenders(properties);
    loadCharacteristics(properties);
  }

  private void loadGenders(PropertiesSet properties)
  {
    Object[] traitsProperties=(Object[])properties.getProperty("RaceTable_GenderList");
    for(Object traitPropertiesObj : traitsProperties)
    {
      PropertiesSet traitProperties=(PropertiesSet)traitPropertiesObj;
      //String name=DatUtils.getStringProperty(traitProperties,"RaceTable_Gender_Race_Name");
      String name=DatUtils.getStringProperty(traitProperties,"RaceTable_Gender_Name");
      {
        int selectIconId=((Integer)traitProperties.getProperty("RaceTable_RaceSelect_Icon")).intValue();
        File selectIconFile=new File("races/select-"+name+".png").getAbsoluteFile();
        DatIconsUtils.buildImageFile(_facade,selectIconId,selectIconFile);
      }
      {
        int selectIconId=((Integer)traitProperties.getProperty("RaceTable_RaceSelect_LargeIcon")).intValue();
        File selectIconFile=new File("races/selectLarge-"+name+".png").getAbsoluteFile();
        DatIconsUtils.buildImageFile(_facade,selectIconId,selectIconFile);
      }
      {
        int selectIconId=((Integer)traitProperties.getProperty("RaceTable_RaceSelect_SmallIcon")).intValue();
        File selectIconFile=new File("races/selectSmall-"+name+".png").getAbsoluteFile();
        DatIconsUtils.buildImageFile(_facade,selectIconId,selectIconFile);
      }
    }
  }

  private void loadCharacteristics(PropertiesSet properties)
  {
    Object[] traitsProperties=(Object[])properties.getProperty("AdvTable_RaceCharacteristic_List");
    for(Object traitPropertiesObj : traitsProperties)
    {
      PropertiesSet traitProperties=(PropertiesSet)traitPropertiesObj;
      Integer level=(Integer)traitProperties.getProperty("AdvTable_Trait_Level");
      Integer rank=(Integer)traitProperties.getProperty("AdvTable_Trait_Rank");
      Integer traitId=(Integer)traitProperties.getProperty("AdvTable_Trait_WC");
      System.out.println("Level: "+level+" (rank="+rank+")");
      TraitDescription description=TraitLoader.loadTrait(_facade,traitId.intValue());
      _traits.registerTrait(description);
    }
  }

  private void doIt()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900020F);
    Object[] raceIds=(Object[])properties.getProperty("RaceTable_RaceTableList");
    for(Object raceId : raceIds)
    {
      handleRace(((Integer)raceId).intValue());
    }
    // Save progressions
    List<Progression> progressions=ProgressionsManager.getInstance().getAll();
    File progressionsFile=new File("../lotro-companion/data/lore/progressions_races.xml").getAbsoluteFile();
    ProgressionsXMLWriter.write(progressionsFile,progressions);
    // Save traits
    File traitsFile=new File("../lotro-companion/data/lore/characters/traits_races.xml").getAbsoluteFile();
    List<TraitDescription> traits=_traits.getAll();
    Collections.sort(traits,new IdentifiableComparator<TraitDescription>());
    TraitDescriptionXMLWriter.write(traitsFile,traits);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainRaceDataLoader(facade).doIt();
    facade.dispose();
  }
}
