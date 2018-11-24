package delta.games.lotro.tools.characters.dat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RaceGender;
import delta.games.lotro.character.races.RaceTrait;
import delta.games.lotro.character.races.io.xml.RaceDescriptionXMLWriter;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.character.traits.io.xml.TraitDescriptionXMLWriter;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.Race;
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
  private Map<Integer,RaceDescription> _racesById;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainRaceDataLoader(DataFacade facade)
  {
    _facade=facade;
    _traits=new TraitsManager();
    _racesById=new HashMap<Integer,RaceDescription>();
  }

  private void handleRace(int racePropertiesId)
  {
    PropertiesSet properties=_facade.loadProperties(racePropertiesId+0x9000000);
    int raceId=((Integer)properties.getProperty("RaceTable_Race")).intValue();
    System.out.println(raceId);
    Race race=getRaceFromRaceId(raceId);
    RaceDescription raceDescription=new RaceDescription(race);
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
RaceTable_NationalityList: 
  #1: 17
  #2: 16
  #3: 6
  #4: 5
    */
    //System.out.println(properties.dump());

    loadGenders(raceDescription,properties);
    loadCharacteristics(raceDescription,properties);
    _racesById.put(Integer.valueOf(raceId),raceDescription);
  }

  private Race getRaceFromRaceId(int raceId)
  {
    if (raceId==23) return Race.MAN;
    if (raceId==65) return Race.ELF;
    if (raceId==73) return Race.DWARF;
    if (raceId==81) return Race.HOBBIT;
    if (raceId==114) return Race.BEORNING;
    if (raceId==117) return Race.HIGH_ELF;
    return null;
  }

  private void loadGenders(RaceDescription description, PropertiesSet properties)
  {
    Object[] gendersProperties=(Object[])properties.getProperty("RaceTable_GenderList");
    PropertiesSet maleProperties=(PropertiesSet)gendersProperties[0];
    RaceGender male=buildGender(maleProperties);
    description.setMaleGender(male);
    if (gendersProperties.length>1)
    {
      PropertiesSet femaleProperties=(PropertiesSet)gendersProperties[1];
      RaceGender female=buildGender(femaleProperties);
      description.setFemaleGender(female);
    }
  }

  private RaceGender buildGender(PropertiesSet genderProperties)
  {
    RaceGender gender=new RaceGender();
    String name=DatUtils.getStringProperty(genderProperties,"RaceTable_Gender_Name");
    gender.setName(name);
    System.out.println(name);
    int iconId=((Integer)genderProperties.getProperty("RaceTable_RaceSelect_Icon")).intValue();
    gender.setIconId(iconId);
    {
      File selectIconFile=new File("races/select-"+name+".png").getAbsoluteFile();
      DatIconsUtils.buildImageFile(_facade,iconId,selectIconFile);
    }
    int largeIconId=((Integer)genderProperties.getProperty("RaceTable_RaceSelect_LargeIcon")).intValue();
    gender.setLargeIconId(largeIconId);
    {
      File selectIconFile=new File("races/selectLarge-"+name+".png").getAbsoluteFile();
      DatIconsUtils.buildImageFile(_facade,largeIconId,selectIconFile);
    }
    int smallIconId=((Integer)genderProperties.getProperty("RaceTable_RaceSelect_SmallIcon")).intValue();
    gender.setSmallIconId(smallIconId);
    {
      File selectIconFile=new File("races/selectSmall-"+name+".png").getAbsoluteFile();
      DatIconsUtils.buildImageFile(_facade,smallIconId,selectIconFile);
    }
    return gender;
  }

  private void loadCharacteristics(RaceDescription description, PropertiesSet properties)
  {
    Object[] traitsProperties=(Object[])properties.getProperty("AdvTable_RaceCharacteristic_List");
    for(Object traitPropertiesObj : traitsProperties)
    {
      PropertiesSet traitProperties=(PropertiesSet)traitPropertiesObj;
      int level=((Integer)traitProperties.getProperty("AdvTable_Trait_Level")).intValue();
      //Integer rank=(Integer)traitProperties.getProperty("AdvTable_Trait_Rank");
      int traitId=((Integer)traitProperties.getProperty("AdvTable_Trait_WC")).intValue();
      //System.out.println("Level: "+level+" (rank="+rank+")");
      TraitDescription trait=TraitLoader.loadTrait(_facade,traitId);
      _traits.registerTrait(trait);
      RaceTrait raceTrait=new RaceTrait(level,trait);
      description.addTrait(raceTrait);
    }
  }

  private void loadRaceEarnableTraits()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900025B);
    //System.out.println(properties.dump());
    Object[] raceArrays=(Object[])properties.getProperty("Trait_Control_RaceArray");
    for(Object raceArrayObj : raceArrays)
    {
      PropertiesSet raceProps=(PropertiesSet)raceArrayObj;
      int raceId=((Integer)raceProps.getProperty("Trait_Control_Race")).intValue();
      //System.out.println("Race: "+raceId);
      RaceDescription description=_racesById.get(Integer.valueOf(raceId));
      Object[] traitsArray=(Object[])raceProps.getProperty("Trait_Control_TraitArray");
      for(Object traitObj : traitsArray)
      {
        int traitId=((Integer)traitObj).intValue();
        TraitDescription trait=TraitLoader.loadTrait(_facade,traitId);
        _traits.registerTrait(trait);
        description.addEarnableTrait(trait);
      }
    }
  }

  private void doIt()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900020F);
    Object[] raceIdsArray=(Object[])properties.getProperty("RaceTable_RaceTableList");
    for(Object raceId : raceIdsArray)
    {
      handleRace(((Integer)raceId).intValue());
    }
    loadRaceEarnableTraits();
    // Save progressions
    List<Progression> progressions=ProgressionsManager.getInstance().getAll();
    File progressionsFile=new File("../lotro-companion/data/lore/progressions_races.xml").getAbsoluteFile();
    ProgressionsXMLWriter.write(progressionsFile,progressions);
    // Save traits
    File traitsFile=new File("../lotro-companion/data/lore/characters/traits_races.xml").getAbsoluteFile();
    List<TraitDescription> traits=_traits.getAll();
    Collections.sort(traits,new IdentifiableComparator<TraitDescription>());
    TraitDescriptionXMLWriter.write(traitsFile,traits);
    // Save races
    File racesFile=new File("../lotro-companion/data/lore/characters/races.xml").getAbsoluteFile();
    List<RaceDescription> races=new ArrayList<RaceDescription>();
    List<Integer> raceIds=new ArrayList<Integer>(_racesById.keySet());
    Collections.sort(raceIds);
    for(Integer raceId : raceIds)
    {
      races.add(_racesById.get(raceId));
    }
    RaceDescriptionXMLWriter.write(racesFile,races);
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
