package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.races.NationalitiesManager;
import delta.games.lotro.character.races.NationalityDescription;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RaceGender;
import delta.games.lotro.character.races.RaceTrait;
import delta.games.lotro.character.races.io.xml.NationalityDescriptionXMLWriter;
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
import delta.games.lotro.utils.StringUtils;

/**
 * Get race definitions from DAT files.
 * @author DAM
 */
public class RaceDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(RaceDataLoader.class);

  private DataFacade _facade;
  private Map<Integer,RaceDescription> _racesById;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public RaceDataLoader(DataFacade facade)
  {
    _facade=facade;
    _racesById=new HashMap<Integer,RaceDescription>();
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
    if (description==null)
    {
      description="";
    }
    description=description.trim();
    raceDescription.setDescription(description);
    // Short or tall?
    Integer isTall=(Integer)properties.getProperty("RaceTable_IsTallRace");
    boolean tall=(isTall!=null)?(isTall.intValue()==1):true;
    raceDescription.setTall(tall);
    // Nationalities
    Object[] nationalityCodes=(Object[])properties.getProperty("RaceTable_NationalityList");
    for(Object nationalityCodeObj : nationalityCodes)
    {
      int nationalityCode=((Integer)nationalityCodeObj).intValue();
      NationalityDescription nationality=NationalitiesManager.getInstance().getNationalityDescription(nationalityCode);
      raceDescription.addNationality(nationality);
    }
    loadGenders(raceDescription,properties);
    loadCharacteristics(raceDescription,properties);
    loadAllowedClasses(raceDescription,properties);
    _racesById.put(Integer.valueOf(raceId),raceDescription);
  }

  private void loadNationalities()
  {
    List<NationalityDescription> nationalities=new ArrayList<NationalityDescription>();
    PropertiesSet properties=_facade.loadProperties(0x79000210);
    Object[] nationalityIdsArray=(Object[])properties.getProperty("NationalityTable_NationalityTableList");
    for(Object nationalityIdObj : nationalityIdsArray)
    {
      int nationalityId=((Integer)nationalityIdObj).intValue();
      NationalityDescription nationality=handleNationality(nationalityId);
      nationalities.add(nationality);
      int nationalityCode=nationality.getIdentifier();
      // Aliases
      if (nationalityCode==16) nationality.addAlias("Dale");
      if (nationalityCode==12) nationality.addAlias("Lonely Mountain");
      if (nationalityCode==18) nationality.addAlias("Fallohides");
      if (nationalityCode==13) nationality.addAlias("Stoors");
      if (nationalityCode==27) nationality.addAlias("Mordor Mountains");
    }
    NationalityDescriptionXMLWriter.write(GeneratedFiles.NATIONALITIES,nationalities);
  }

  private NationalityDescription handleNationality(int nationalityId)
  {
    PropertiesSet properties=_facade.loadProperties(nationalityId+DATConstants.DBPROPERTIES_OFFSET);
    // Code
    int code=((Integer)properties.getProperty("NationalityTable_Nationality")).intValue();
    NationalityDescription ret=new NationalityDescription(code);
    // Name
    String name=DatUtils.getStringProperty(properties,"NationalityTable_Name");
    name=StringUtils.fixName(name);
    ret.setName(name);
    // Description
    String description=DatUtils.getStringProperty(properties,"NationalityTable_Desc");
    ret.setDescription(description);
    // Icon ID
    int iconID=((Integer)properties.getProperty("NationalityTable_Icon")).intValue();
    ret.setIconID(iconID);
    // Guidelines
    Object[] guidelinesArray=(Object[])properties.getProperty("NationalityTable_Naming_Guideline_Array");
    for(Object guidelineEntryObj : guidelinesArray)
    {
      PropertiesSet guidelineProps=(PropertiesSet)guidelineEntryObj;
      String guideline=DatUtils.getStringProperty(guidelineProps,"NationalityTable_Naming_Guideline");
      int sexCode=((Integer)guidelineProps.getProperty("NationalityTable_Sex")).intValue();
      if (sexCode==4096)
      {
        ret.setNamingGuidelineMale(guideline);
      }
      else if (sexCode==8192)
      {
        ret.setNamingGuidelineFemale(guideline);
      }
      else
      {
        LOGGER.warn("Unmanaged gender code: "+sexCode);
      }
    }
    // Title
    Integer titleID=(Integer)properties.getProperty("NationalityTable_Title");
    if (titleID!=null)
    {
      ret.setTitleID(titleID);
    }
    return ret;
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
    Integer largeIconId=(Integer)genderProperties.getProperty("RaceTable_RaceSelect_LargeIcon");
    if (largeIconId!=null)
    {
      gender.setLargeIconId(largeIconId.intValue());
      File largeIconFile=getIconFile(race,sex);
      DatIconsUtils.buildImageFile(_facade,largeIconId.intValue(),largeIconFile);
    }
    Integer smallIconId=(Integer)genderProperties.getProperty("RaceTable_RaceSelect_SmallIcon");
    if (smallIconId!=null)
    {
      gender.setSmallIconId(smallIconId.intValue());
    }
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
    File rootDir=new File("../lotro-companion/src/main/resources/resources/gui/races");
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
    if (raceArrays==null)
    {
      return;
    }
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
