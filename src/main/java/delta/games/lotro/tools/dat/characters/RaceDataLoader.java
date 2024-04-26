package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.io.Console;
import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.races.NationalitiesManager;
import delta.games.lotro.character.races.NationalityDescription;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RaceGender;
import delta.games.lotro.character.races.RaceTrait;
import delta.games.lotro.character.races.io.xml.RaceDescriptionXMLWriter;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get race definitions from DAT files.
 * @author DAM
 */
public class RaceDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(RaceDataLoader.class);

  private DataFacade _facade;
  private Map<Integer,RaceDescription> _racesById;
  private EnumMapper _speciesCode;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public RaceDataLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("races",facade.getGlobalStringsManager());
    _racesById=new HashMap<Integer,RaceDescription>();
    _speciesCode=_facade.getEnumsManager().getEnumMapper(587202571);
  }

  private void handleRace(int racePropertiesId)
  {
    PropertiesSet properties=_facade.loadProperties(racePropertiesId+DATConstants.DBPROPERTIES_OFFSET);
    int raceId=((Integer)properties.getProperty("RaceTable_Race")).intValue();
    // Key
    String key=getRaceKey(raceId);
    // Legacy label
    String legacyLabel=getRaceLegacyLabel(raceId);
    RaceDescription raceDescription=new RaceDescription(racePropertiesId,raceId,key,legacyLabel);
    // Name
    String name=_i18n.getEnumValue(_speciesCode,raceId,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
    raceDescription.setName(name);
    // Tag
    String raceName=_speciesCode.getLabel(raceId);
    String tag=DatStringUtils.extractTag(raceName);
    raceDescription.setTag(tag);
    // Description
    String description=_i18n.getStringProperty(properties,"RaceTable_Description");
    if (description==null)
    {
      description="";
    }
    raceDescription.setDescription(description);
    // Short or tall?
    Integer isTall=(Integer)properties.getProperty("RaceTable_IsTallRace");
    boolean tall=((isTall!=null) && (isTall.intValue()==1));
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

  private String getRaceKey(int raceId)
  {
    if (raceId==23) return "man";
    if (raceId==65) return "elf";
    if (raceId==73) return "dwarf";
    if (raceId==81) return "hobbit";
    if (raceId==114) return "beorning";
    if (raceId==117) return "highelf";
    if (raceId==120) return "stoutaxedwarf";
    if (raceId==125) return "riverhobbit";
    return null;
  }

  private String getRaceLegacyLabel(int raceId)
  {
    if (raceId==23) return "Race of Man";
    if (raceId==65) return "Elf";
    if (raceId==73) return "Dwarf";
    if (raceId==81) return "Hobbit";
    if (raceId==114) return "Beorning";
    if (raceId==117) return "High Elf";
    if (raceId==120) return "Stout-axe Dwarf";
    if (raceId==125) return "River Hobbit";
    return null;
  }

  private void loadGenders(RaceDescription race, PropertiesSet properties)
  {
    Object[] gendersProperties=(Object[])properties.getProperty("RaceTable_GenderList");
    PropertiesSet maleProperties=(PropertiesSet)gendersProperties[0];
    RaceGender male=buildGender(maleProperties);
    race.setMaleGender(male);
    if (gendersProperties.length>1)
    {
      PropertiesSet femaleProperties=(PropertiesSet)gendersProperties[1];
      RaceGender female=buildGender(femaleProperties);
      race.setFemaleGender(female);
    }
  }

  private RaceGender buildGender(PropertiesSet genderProperties)
  {
    RaceGender gender=new RaceGender();
    // Name
    String name=_i18n.getStringProperty(genderProperties,"RaceTable_Gender_Name");
    gender.setName(name);
    // Icons
    int iconId=((Integer)genderProperties.getProperty("RaceTable_RaceSelect_Icon")).intValue();
    gender.setIconId(iconId);
    File iconFile=getIconFile(iconId);
    DatIconsUtils.buildImageFile(_facade,iconId,iconFile);
    Integer largeIconId=(Integer)genderProperties.getProperty("RaceTable_RaceSelect_LargeIcon");
    if (largeIconId!=null)
    {
      gender.setLargeIconId(largeIconId.intValue());
      File largeIconFile=getIconFile(largeIconId.intValue());
      DatIconsUtils.buildImageFile(_facade,largeIconId.intValue(),largeIconFile);
    }
    Integer smallIconId=(Integer)genderProperties.getProperty("RaceTable_RaceSelect_SmallIcon");
    if (smallIconId!=null)
    {
      gender.setSmallIconId(smallIconId.intValue());
      File smallIconFile=getIconFile(smallIconId.intValue());
      DatIconsUtils.buildImageFile(_facade,smallIconId.intValue(),smallIconFile);
    }
    // Avatar
    int avatarId=((Integer)genderProperties.getProperty("RaceTable_Gender_PlayerAvatar")).intValue();
    gender.setAvatarId(avatarId);
    return gender;
  }

  void loadAvatar(int avatarId)
  {
    PropertiesSet properties=_facade.loadProperties(avatarId+DATConstants.DBPROPERTIES_OFFSET);
    float icmr=((Float)properties.getProperty("Vital_HealthCombatBaseRegen")).floatValue();
    float ocmr=((Float)properties.getProperty("Vital_HealthPeaceBaseRegen")).floatValue();
    float icpr=((Float)properties.getProperty("Vital_PowerCombatBaseRegen")).floatValue();
    float ocpr=((Float)properties.getProperty("Vital_PowerPeaceBaseRegen")).floatValue();
    Console.println("ICMR="+icmr+", OCMR="+ocmr+", ICPR="+icpr+", OCPR="+ocpr);
  }

  private File getIconFile(int iconID)
  {
    File iconFile=new File(GeneratedFiles.RACE_ICONS_DIR,iconID+".png").getAbsoluteFile();
    return iconFile;
  }

  private void loadCharacteristics(RaceDescription description, PropertiesSet properties)
  {
    Object[] traitsProperties=(Object[])properties.getProperty("AdvTable_RaceCharacteristic_List");
    for(Object traitPropertiesObj : traitsProperties)
    {
      PropertiesSet traitProperties=(PropertiesSet)traitPropertiesObj;
      int level=((Integer)traitProperties.getProperty("AdvTable_Trait_Level")).intValue();
      Integer rank=(Integer)traitProperties.getProperty("AdvTable_Trait_Rank");
      int traitId=((Integer)traitProperties.getProperty("AdvTable_Trait_WC")).intValue();
      TraitDescription trait=TraitUtils.getTrait(traitId);
      LOGGER.debug("Level: "+level+" => trait "+trait+" (rank="+rank+")");
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
      ClassDescription characterClass=ClassesManager.getInstance().getCharacterClassByCode(classCode);
      description.addAllowedClass(characterClass.getKey());
    }
  }

  private void loadRaceEarnableTraits()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900025B);
    Object[] raceArrays=(Object[])properties.getProperty("Trait_Control_RaceArray");
    if (raceArrays==null)
    {
      return;
    }
    for(Object raceArrayObj : raceArrays)
    {
      PropertiesSet raceProps=(PropertiesSet)raceArrayObj;
      int raceId=((Integer)raceProps.getProperty("Trait_Control_Race")).intValue();
      LOGGER.debug("Race: "+raceId);
      RaceDescription description=_racesById.get(Integer.valueOf(raceId));
      if (description!=null)
      {
        Object[] traitsArray=(Object[])raceProps.getProperty("Trait_Control_TraitArray");
        for(Object traitObj : traitsArray)
        {
          int traitId=((Integer)traitObj).intValue();
          TraitDescription trait=TraitUtils.getTrait(traitId);
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
    // Save labels
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new RaceDataLoader(facade).doIt();
    facade.dispose();
  }
}
