package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RaceGender;
import delta.games.lotro.character.races.RaceTrait;
import delta.games.lotro.character.races.io.xml.RaceDescriptionXMLWriter;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.CharacterSex;
import delta.games.lotro.common.Race;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get race definitions from DAT files.
 * @author DAM
 */
public class RaceDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(RaceDataLoader.class);

  private DataFacade _facade;
  private Map<Integer,RaceDescription> _racesById;
  //private EnumMapper _nationalities;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public RaceDataLoader(DataFacade facade)
  {
    _facade=facade;
    _racesById=new HashMap<Integer,RaceDescription>();
    //_nationalities=_facade.getEnumsManager().getEnumMapper(587202577);
    loadNationalities();
  }

  private void handleRace(int racePropertiesId)
  {
    PropertiesSet properties=_facade.loadProperties(racePropertiesId+DATConstants.DBPROPERTIES_OFFSET);
    int raceId=((Integer)properties.getProperty("RaceTable_Race")).intValue();
    //System.out.println(raceId);
    Race race=DatEnumsUtils.getRaceFromRaceId(raceId);
    RaceDescription raceDescription=new RaceDescription(race);
    // Description
    String description=DatUtils.getStringProperty(properties,"RaceTable_Description");
    description=description.trim();
    raceDescription.setDescription(description);
    // Nationalities
    /*
    Object[] nationalityCodes=(Object[])properties.getProperty("RaceTable_NationalityList");
    for(Object nationalityCodeObj : nationalityCodes)
    {
      int nationalityCode=((Integer)nationalityCodeObj).intValue();
      String nationality=_nationalities.getString(nationalityCode);
      System.out.println("Nationality: "+nationality);
    }
    */
    loadGenders(raceDescription,properties);
    loadCharacteristics(raceDescription,properties);
    loadAllowedClasses(raceDescription,properties);
    _racesById.put(Integer.valueOf(raceId),raceDescription);
  }

  private void loadNationalities()
  {
    PropertiesSet properties=_facade.loadProperties(0x79000210);
    Object[] nationalityIdsArray=(Object[])properties.getProperty("NationalityTable_NationalityTableList");
    for(Object nationalityId : nationalityIdsArray)
    {
      handleNationality(((Integer)nationalityId).intValue());
    }
  }

  private void handleNationality(int nationalityId)
  {
    /*
    PropertiesSet properties=_facade.loadProperties(nationalityId+DATConstants.DBPROPERTIES_OFFSET);
    System.out.println(properties.dump());
    */
    /*
    NationalityTable_Desc: 
      #1: <li><rgb=#FFFF00>Lore: </rgb>You grew up in Bree-land, once part of the North Kingdom of Arnor, once ruled by Elendil the Tall as High King of Middle-earth, and later by his elder son Isildur. Now it is but a simple, rustic land, and the North Kingdom is no more.</li>
    NationalityTable_Icon: 1091603145
    NationalityTable_Name: 
      #1: Bree-land
    NationalityTable_Naming_Guideline_Array: 
      #1: 
        NationalityTable_Naming_Guideline: 
          #1: \n\n<li><rgb=#FFFF00>Naming Guidelines: </rgb>Bree-men usually have short, simple English-styled names like Ned, Bill, Mat, Wil, or Tom, but longer or less familiar names -- such as Barliman, Humphrey, or Cuthbert -- are not unknown.\n\nSometimes Bree-landers go by their last names, which are almost always related to plants, such as Appledore, Thistleway, Butterbur, or Ferny.</li>
        NationalityTable_Sex: 4096 (Male)
      #2: 
        NationalityTable_Naming_Guideline: 
          #1: \n\n<li><rgb=#FFFF00>Naming Guidelines: </rgb>Bree-women tend towards simple, familiar English-styled names such as Ellie, Dora, Adela, and Clara, though less familiar names -- such as Amabel, Maribel, or Livina -- are not unknown.\n\nSometimes Bree-landers go by their last names, which are almost always related to plants, such as Appledore, Thistleway, Butterbur, or Ferny.</li>
        NationalityTable_Sex: 8192 (Female)
    NationalityTable_Nationality: 17 (Bree_land)
    NationalityTable_Title: 1879073639
    Reputation_FactionTable: 
      #1: 1879091340
      #2: 1879091408
      #3: 1879091346
      #4: 1879091345
    */
  }

  private void loadGenders(RaceDescription description, PropertiesSet properties)
  {
    Race race=description.getRace();
    Object[] gendersProperties=(Object[])properties.getProperty("RaceTable_GenderList");
    PropertiesSet maleProperties=(PropertiesSet)gendersProperties[0];
    RaceGender male=buildGender(race,CharacterSex.MALE,maleProperties);
    description.setMaleGender(male);
    if (gendersProperties.length>1)
    {
      PropertiesSet femaleProperties=(PropertiesSet)gendersProperties[1];
      RaceGender female=buildGender(race,CharacterSex.FEMALE,femaleProperties);
      description.setFemaleGender(female);
    }
  }

  private RaceGender buildGender(Race race, CharacterSex sex, PropertiesSet genderProperties)
  {
    RaceGender gender=new RaceGender();
    // Name
    String name=DatUtils.getStringProperty(genderProperties,"RaceTable_Gender_Name");
    gender.setName(name);
    // Icons
    int iconId=((Integer)genderProperties.getProperty("RaceTable_RaceSelect_Icon")).intValue();
    gender.setIconId(iconId);
    int largeIconId=((Integer)genderProperties.getProperty("RaceTable_RaceSelect_LargeIcon")).intValue();
    gender.setLargeIconId(largeIconId);
    File largeIconFile=getIconFile(race,sex);
    DatIconsUtils.buildImageFile(_facade,largeIconId,largeIconFile);
    int smallIconId=((Integer)genderProperties.getProperty("RaceTable_RaceSelect_SmallIcon")).intValue();
    gender.setSmallIconId(smallIconId);
    // Avatar
    int avatarId=((Integer)genderProperties.getProperty("RaceTable_Gender_PlayerAvatar")).intValue();
    gender.setAvatarId(avatarId);
    //loadAvatar(avatarId);
    return gender;
  }

  void loadAvatar(int avatarId)
  {
    PropertiesSet properties=_facade.loadProperties(avatarId+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(properties.dump());
    float icmr=((Float)properties.getProperty("Vital_HealthCombatBaseRegen")).floatValue();
    float ocmr=((Float)properties.getProperty("Vital_HealthPeaceBaseRegen")).floatValue();
    float icpr=((Float)properties.getProperty("Vital_PowerCombatBaseRegen")).floatValue();
    float ocpr=((Float)properties.getProperty("Vital_PowerPeaceBaseRegen")).floatValue();
    System.out.println("ICMR="+icmr+", OCMR="+ocmr+", ICPR="+icpr+", OCPR="+ocpr);
  }

  private File getIconFile(Race race, CharacterSex sex)
  {
    String raceIconPath=race.getIconPath();
    File rootDir=new File("../lotro-companion/src/main/java/resources/gui/races");
    File iconFile=new File(rootDir,raceIconPath+"_"+sex.getKey().toLowerCase()+".png").getAbsoluteFile();
    return iconFile;
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
      TraitDescription trait=TraitLoader.getTrait(_facade,traitId);
      RaceTrait raceTrait=new RaceTrait(level,trait);
      description.addTrait(raceTrait);
    }
  }

  private void loadAllowedClasses(RaceDescription description, PropertiesSet properties)
  {
    Object[] classCodesArray=(Object[])properties.getProperty("RaceTable_ClassList");
    for(Object classCodeObj : classCodesArray)
    {
      int classCode=((Integer)classCodeObj).intValue();
      CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(classCode);
      description.addAllowedClass(characterClass);
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
      if (description!=null)
      {
        Object[] traitsArray=(Object[])raceProps.getProperty("Trait_Control_TraitArray");
        for(Object traitObj : traitsArray)
        {
          int traitId=((Integer)traitObj).intValue();
          TraitDescription trait=TraitLoader.getTrait(_facade,traitId);
          description.addEarnableTrait(trait);
        }
      }
      else
      {
        LOGGER.warn("Could not find race ID="+raceId);
      }
    }
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900020F);
    Object[] raceIdsArray=(Object[])properties.getProperty("RaceTable_RaceTableList");
    for(Object raceId : raceIdsArray)
    {
      handleRace(((Integer)raceId).intValue());
    }
    loadRaceEarnableTraits();
    // Save races
    List<RaceDescription> races=new ArrayList<RaceDescription>();
    List<Integer> raceIds=new ArrayList<Integer>(_racesById.keySet());
    Collections.sort(raceIds);
    for(Integer raceId : raceIds)
    {
      races.add(_racesById.get(raceId));
    }
    RaceDescriptionXMLWriter.write(GeneratedFiles.RACES,races);
  }
}
