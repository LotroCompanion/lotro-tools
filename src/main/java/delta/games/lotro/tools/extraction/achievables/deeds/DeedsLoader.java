package delta.games.lotro.tools.extraction.achievables.deeds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.classes.WellKnownCharacterClassKeys;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.common.ChallengeLevel;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.DeedCategory;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.rewards.Rewards;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.strings.renderer.StringRenderer;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;
import delta.games.lotro.lore.quests.AchievableProxiesResolver;
import delta.games.lotro.lore.webStore.WebStoreItem;
import delta.games.lotro.lore.webStore.WebStoreItemsManager;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.achievables.AchievablesLoadingUtils;
import delta.games.lotro.tools.extraction.achievables.DatObjectivesLoader;
import delta.games.lotro.tools.extraction.achievables.deeds.keys.DeedKeysInjector;
import delta.games.lotro.tools.extraction.utils.StringRenderingUtils;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.tools.extraction.utils.i18n.StringProcessor;

/**
 * Loader for deeds.
 * @author DAM
 */
public class DeedsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(DeedsLoader.class);

  private static final String CLASS_KEY="CLASS";

  private DataFacade _facade;
  private Map<Integer,DeedDescription> _deeds;
  private LotroEnum<DeedCategory> _deedUiTabName;
  private I18nUtils _i18n;
  private DatObjectivesLoader _objectivesLoader;
  private AchievablesLoadingUtils _utils;
  private StringProcessor _processor;
  private LotroEnum<DeedType> _deedTypeEnum;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param utils Utils.
   */
  public DeedsLoader(DataFacade facade, AchievablesLoadingUtils utils)
  {
    _facade=facade;
    _deeds=new HashMap<Integer,DeedDescription>();
    LotroEnumsRegistry registry=LotroEnumsRegistry.getInstance();
    _deedUiTabName=registry.get(DeedCategory.class);
    _i18n=new I18nUtils("deeds",facade.getGlobalStringsManager());
    _objectivesLoader=new DatObjectivesLoader(facade,_i18n);
    _utils=utils;
    _processor=buildProcessor();
    _deedTypeEnum=LotroEnumsRegistry.getInstance().get(DeedType.class);
  }

  /**
   * Get loaded deeds.
   * @return A list of deeds.
   */
  public List<DeedDescription> getDeeds()
  {
    return new ArrayList<DeedDescription>(_deeds.values());
  }

  /**
   * Load a deed.
   * @param deedID Deed identifier.
   * @param properties Properties.
   */
  public void loadDeed(int deedID, PropertiesSet properties)
  {
    DeedDescription deed=new DeedDescription();
    // ID
    deed.setIdentifier(deedID);
    // Name
    String name=_i18n.getNameStringProperty(properties,"Quest_Name",deedID,_processor);
    deed.setName(name);
    String rawName=_i18n.getStringProperty(properties,"Quest_Name");
    deed.setRawName(rawName);

    // Description
    String description=_i18n.getStringProperty(properties,"Quest_Description");
    deed.setDescription(description);
    // UI Tab
    Integer uiTab=((Integer)properties.getProperty("Accomplishment_UITab"));
    DeedCategory uiTabName=_deedUiTabName.getEntry(uiTab.intValue());
    deed.setCategory(uiTabName);
    // Deed type
    handleDeedType(deed,properties);
    // Monster play?
    Integer isMonsterPlayCode=((Integer)properties.getProperty("Quest_IsMonsterPlayQuest"));
    boolean isMonsterPlay=((isMonsterPlayCode!=null) && (isMonsterPlayCode.intValue()!=0));
    deed.setMonsterPlay(isMonsterPlay);

    // Pre-requisites
    _utils.findPrerequisites(deed,properties);

    // Requirements
    _utils.findRequirements(deed,properties);
    // Min level
    Integer minLevel=deed.getMinimumLevel();
    if (minLevel==null)
    {
      minLevel=((Integer)properties.getProperty("Accomplishment_MinLevelToStart"));
      deed.setMinimumLevel(minLevel);
    }

    // Rewards
    Rewards rewards=deed.getRewards();
    ChallengeLevel challengeLevel=_utils.getRewardsLoader().fillRewards(properties,rewards);
    // Challenge level
    deed.setChallengeLevel(challengeLevel);

    // Objectives
    _objectivesLoader.handleObjectives(deed.getObjectives(),deed,properties);

    // Web Store (needed xpack/region): WebStoreAccountItem_DataID
    Integer webStoreItemID=(Integer)properties.getProperty("WebStoreAccountItem_DataID");
    if (webStoreItemID!=null)
    {
      WebStoreItem webStoreItem=WebStoreItemsManager.getInstance().getWebStoreItem(webStoreItemID.intValue());
      deed.setWebStoreItem(webStoreItem);
    }
    // Events
    _utils.getEventIDsLoader().doAchievable(deed);
    // Registration
    _deeds.put(Integer.valueOf(deed.getIdentifier()),deed);
  }

  private StringProcessor buildProcessor()
  {
    StringRenderer customRenderer=StringRenderingUtils.buildAllOptionsRenderer();
    StringProcessor p=new StringProcessor()
    {
      @Override
      public String processString(String input)
      {
        String renderedTitle=customRenderer.render(input);
        renderedTitle=renderedTitle.replace(" ,","");
        renderedTitle=renderedTitle.replace("  "," ");
        renderedTitle=renderedTitle.trim();
        return renderedTitle;
      }
    };
    return p;
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
        type=_deedTypeEnum.getByKey(CLASS_KEY);
      }
      else if (typeCode==2)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.CAPTAIN);
      }
      else if (typeCode==3)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.GUARDIAN);
      }
      else if (typeCode==5)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.MINSTREL);
      }
      else if (typeCode==6)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.BURGLAR);
      }
      else if (typeCode==26)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.HUNTER);
      }
      else if (typeCode==28)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.CHAMPION);
      }
      else if (typeCode==30)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.LORE_MASTER);
      }
      else if (typeCode==35)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.WARDEN);
      }
      else if (typeCode==36)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.RUNE_KEEPER);
      }
      else if (typeCode==38)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.BEORNING);
      }
      else if (typeCode==40)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.BRAWLER);
      }
      else if (typeCode==41)
      {
        type=_deedTypeEnum.getByKey(CLASS_KEY);
        setClassRequirementForDeed(deed,WellKnownCharacterClassKeys.CORSAIR);
      }
      else if (typeCode==34)
      {
        type=_deedTypeEnum.getByKey("EVENT");
      }
      else if (typeCode==1)
      {
        type=_deedTypeEnum.getByKey("EXPLORER");
      }
      else if (typeCode==33)
      {
        type=_deedTypeEnum.getByKey("LORE");
      }
      else if (typeCode==25)
      {
        type=_deedTypeEnum.getByKey("RACE");
      }
      else if (typeCode==11)
      {
        type=_deedTypeEnum.getByKey("REPUTATION");
      }
      else if (typeCode==20)
      {
        type=_deedTypeEnum.getByKey("SLAYER");
      }
      else
      {
        // 0 => Undef
        // 13 => Man
        // 21 => Elf
        // 25 => Race
        // 27 => Dwarf
        // 29 => Hobbit
        // 37 => Beorning
        // 39 => Allegiance
        LOGGER.warn("Unmanaged type: "+typeCode);
      }
    }
    deed.setType(type);
  }

  private void setClassRequirementForDeed(DeedDescription deed, String classKey)
  {
    ClassDescription characterClass=ClassesManager.getInstance().getCharacterClassByKey(classKey);
    deed.setRequiredClass(characterClass);
  }

  /**
   * Load race requirements.
   */
  public void loadRaceRequirements()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900020F);
    Object[] raceIdsArray=(Object[])properties.getProperty("RaceTable_RaceTableList");
    for(Object raceIdObj : raceIdsArray)
    {
      int raceId=((Integer)raceIdObj).intValue();
      PropertiesSet raceProps=_facade.loadProperties(raceId+DATConstants.DBPROPERTIES_OFFSET);
      int raceCode=((Integer)raceProps.getProperty("RaceTable_Race")).intValue();
      RaceDescription race=RacesManager.getInstance().getByCode(raceCode);
      int accomplishmentDirectoryId=((Integer)raceProps.getProperty("RaceTable_AccomplishmentDirectory")).intValue();
      PropertiesSet accomplishmentDirProps=_facade.loadProperties(accomplishmentDirectoryId+DATConstants.DBPROPERTIES_OFFSET);
      Object[] accomplishmentList=(Object[])accomplishmentDirProps.getProperty("Accomplishment_List");
      if (accomplishmentList!=null)
      {
        for(Object accomplishmentListObj : accomplishmentList)
        {
          if (accomplishmentListObj instanceof Integer)
          {
            Integer deedId=(Integer)accomplishmentListObj;
            DeedDescription deed=_deeds.get(deedId);
            if (deed!=null)
            {
              deed.setRequiredRace(race);
            }
          }
          else
          {
            for(Object accomplishmentListObj2 : (Object[])accomplishmentListObj)
            {
              Integer deedId=(Integer)accomplishmentListObj2;
              DeedDescription deed=_deeds.get(deedId);
              if (deed!=null)
              {
                deed.setRequiredRace(race);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Load class requirements.
   */
  public void loadClassRequirements()
  {
    PropertiesSet properties=_facade.loadProperties(0x7900020E);
    Object[] classIdsArray=(Object[])properties.getProperty("AdvTable_LevelTableList");
    for(Object classIdObj : classIdsArray)
    {
      int classId=((Integer)classIdObj).intValue();
      PropertiesSet classProps=_facade.loadProperties(classId+DATConstants.DBPROPERTIES_OFFSET);
      int classCode=((Integer)classProps.getProperty("AdvTable_Class")).intValue();
      ClassDescription characterClass=ClassesManager.getInstance().getCharacterClassByCode(classCode);
      int accomplishmentDirectoryId=((Integer)classProps.getProperty("AdvTable_AccomplishmentDirectory")).intValue();
      PropertiesSet accomplishmentDirProps=_facade.loadProperties(accomplishmentDirectoryId+DATConstants.DBPROPERTIES_OFFSET);
      Object[] accomplishmentList=(Object[])accomplishmentDirProps.getProperty("Accomplishment_List");
      if (accomplishmentList!=null)
      {
        for(Object listItem : accomplishmentList)
        {
          if (listItem instanceof Integer)
          {
            Integer deedId=(Integer)listItem;
            handleClassRequirementForDeed(deedId,characterClass);
          }
          else if (listItem instanceof Object[])
          {
            for(Object listItem2 : (Object[])listItem)
            {
              Integer deedId=(Integer)listItem2;
              handleClassRequirementForDeed(deedId,characterClass);
            }
          }
        }
      }
    }
  }

  private void handleClassRequirementForDeed(Integer deedId, ClassDescription characterClass)
  {
    DeedDescription deed=_deeds.get(deedId);
    if (deed!=null)
    {
      deed.setRequiredClass(characterClass);
    }
  }

  /**
   * Post processing.
   * @return processed deeds list.
   */
  public List<DeedDescription> postProcess()
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    deeds.addAll(_deeds.values());
    Collections.sort(deeds,new IdentifiableComparator<DeedDescription>());
    // - deed keys injection
    DeedKeysInjector injector=new DeedKeysInjector();
    injector.doIt(deeds);
    return deeds;
  }

  /**
   * Resolve proxies.
   * @param resolver Proxies resolver.
   */
  public void resolveProxies(AchievableProxiesResolver resolver)
  {
    for(DeedDescription deed : _deeds.values())
    {
      resolver.resolveDeed(deed);
      _utils.cleanup(deed);
    }
  }

  /**
   * Save.
   */
  public void save()
  {
    saveDeeds();
    _i18n.save();
  }

  private void saveDeeds()
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    deeds.addAll(_deeds.values());
    int nbDeeds=_deeds.size();
    LOGGER.info("Nb deeds: "+nbDeeds);
    // Write deeds file
    Collections.sort(deeds,new IdentifiableComparator<DeedDescription>());
    DeedXMLWriter writer=new DeedXMLWriter();
    boolean ok=writer.writeDeeds(GeneratedFiles.DEEDS,deeds,EncodingNames.UTF_8);
    if (ok)
    {
      LOGGER.info("Wrote deeds file: "+GeneratedFiles.DEEDS);
    }
  }
}
