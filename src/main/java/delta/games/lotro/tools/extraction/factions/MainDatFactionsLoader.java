package delta.games.lotro.tools.extraction.factions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionLevel;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.lore.reputation.ReputationDeed;
import delta.games.lotro.lore.reputation.io.xml.FactionsXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Get faction definitions from DAT files.
 * @author DAM
 */
public class MainDatFactionsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatFactionsLoader.class);

  private DataFacade _facade;
  private FactionLevelTemplates _templates;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatFactionsLoader(DataFacade facade)
  {
    _facade=facade;
    _templates=new FactionLevelTemplates();
    _i18n=new I18nUtils("factions",facade.getGlobalStringsManager());
  }

  /*
Sample properties:
************* 1879091341 *****************
Reputation_Faction_AdvancementTable: 1879090256
Reputation_Faction_CurrentTier_PropertyName: 268441507
Reputation_Faction_DefaultTier: 3
Reputation_Faction_Description:
  #1: A Council of several races who struggle to take the war into the heart of Angmar itself. They strike from the hidden refuge of Gath Forthnír in the far northern wastes of Angmar.
Reputation_Faction_EarnedReputation_PropertyName: 268441501
Reputation_Faction_GlobalCap_PropertyName: 268441759
Reputation_Faction_Name:
  #1: Council of the North
Reputation_Faction_TierNameProgression: 1879209346
Reputation_HighestTier: 7
Reputation_LowestTier: 1
  */

  private Faction load(int factionId)
  {
    if (!useFaction(factionId))
    {
      return null;
    }
    PropertiesSet properties=_facade.loadProperties(factionId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      LOGGER.warn("Could not load properties for faction ID={}",Integer.valueOf(factionId));
      return null;
    }
    // Name
    String name=_i18n.getNameStringProperty(properties,"Reputation_Faction_Name",factionId,0);
    if (name.startsWith("TBD"))
    {
      return null;
    }
    return loadOneFaction(factionId,properties,name);
  }

  private Faction loadOneFaction(int factionId, PropertiesSet properties, String name)
  {
    Faction faction=new Faction(factionId);
    faction.setName(name);
    LOGGER.info("Loading faction: {}",faction);

    // Category
    String[] factionDescription=getFactionDescription(factionId);
    String category=(factionDescription.length>0)?factionDescription[0]:"?";
    faction.setCategory(category);
    String key=(factionDescription.length>1)?factionDescription[1]:null;
    faction.setLegacyKey(key);

    // Description
    String description=_i18n.getStringProperty(properties,"Reputation_Faction_Description");
    faction.setDescription(description);

    // Tier names
    Map<Integer,String> tierNames=null;
    Integer tierNamesId=(Integer)properties.getProperty("Reputation_Faction_TierNameProgression");
    if (tierNamesId!=null)
    {
      LOGGER.debug("Tier names table ID: {}",tierNamesId);
      tierNames=getTierNames(tierNamesId.intValue());
      LOGGER.debug("{}",tierNames);
    }
    else
    {
      tierNames=new HashMap<Integer,String>();
    }

    // Faction advancement table
    // Here we find the total amount of reputation points for each tier
    Integer reputationTableId=(Integer)properties.getProperty("Reputation_Faction_AdvancementTable");
    LOGGER.debug("Reputation table: {}",reputationTableId);
    WStateDataSet table=_facade.loadWState(reputationTableId.intValue());
    long[] reputationTable=extractReputationTable(table);
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("{}",Arrays.toString(reputationTable));
    }

    // Guild?
    boolean isGuild=isGuildFaction(factionId);
    faction.setIsGuildFaction(isGuild);

    // Lowest/initial/highest tiers
    Integer lowestTierInt=(Integer)properties.getProperty("Reputation_LowestTier");
    int lowestTier=(lowestTierInt!=null)?lowestTierInt.intValue():0;
    Integer highestTierInt=(Integer)properties.getProperty("Reputation_HighestTier");
    int highestTier=(highestTierInt!=null)?highestTierInt.intValue():0;
    int defaultTier=((Integer)properties.getProperty("Reputation_Faction_DefaultTier")).intValue();
    if ((lowestTier==0) && (highestTier==0))
    {
      lowestTier=1;
      highestTier=reputationTable.length-1;
    }
    LOGGER.debug("Tiers (lowest/default/highest): {} / {} / {}",lowestTierInt,Integer.valueOf(defaultTier),highestTierInt);
    faction.setLowestTier(lowestTier);
    faction.setHighestTier(highestTier);
    faction.setInitialTier(defaultTier);

    handleFactionPropertyNames(properties,faction);
    handleFactionUnusedAttributes(properties);

    List<FactionLevel> levels=buildFactionLevels(tierNames,reputationTable,lowestTier,highestTier);
    for(FactionLevel level : levels)
    {
      faction.addFactionLevel(level);
    }
    String template=(factionDescription.length>2)?factionDescription[2]:null;
    if (template!=null)
    {
      List<String> levelKeys=_templates.getByKey(template);
      if (levelKeys!=null)
      {
        if (levelKeys.size()!=levels.size())
        {
          LOGGER.warn("Size mismatch for faction: {}",faction);
        }
        else
        {
          int index=0;
          for(FactionLevel level : levels)
          {
            String levelKey=levelKeys.get(index);
            level.setLegacyKey(levelKey);
            index++;
          }
        }
      }
    }
    return faction;
  }

  private void handleFactionPropertyNames(PropertiesSet properties, Faction faction)
  {
    PropertiesRegistry propsRegistry=_facade.getPropertiesRegistry();
    int currentTierPropId=((Integer)properties.getProperty("Reputation_Faction_CurrentTier_PropertyName")).intValue();
    String currentTierPropertyName=propsRegistry.getPropertyDef(currentTierPropId).getName();
    faction.setCurrentTierPropertyName(currentTierPropertyName);
    int earnedRepPropId=((Integer)properties.getProperty("Reputation_Faction_EarnedReputation_PropertyName")).intValue();
    String earnedRepPropertyName=propsRegistry.getPropertyDef(earnedRepPropId).getName();
    faction.setCurrentReputationPropertyName(earnedRepPropertyName);
  }

  private void handleFactionUnusedAttributes(PropertiesSet properties)
  {
    int globalCapPropId=((Integer)properties.getProperty("Reputation_Faction_GlobalCap_PropertyName")).intValue();
    if (LOGGER.isDebugEnabled())
    {
      PropertiesRegistry propsRegistry=_facade.getPropertiesRegistry();
      PropertyDefinition def=propsRegistry.getPropertyDef(globalCapPropId);
      LOGGER.debug("Global cap property: {}",def);
    }

    Integer disableAcc=(Integer)properties.getProperty("Reputation_Faction_DisableAcceleration");
    if ((disableAcc!=null) && (disableAcc.intValue()==1))
    {
      // Only 1 for Dol Amroth library (null otherwise)
      LOGGER.debug("Disable acc: {}",disableAcc);
    }
    Integer lowestMonetizedTier=(Integer)properties.getProperty("Reputation_Faction_LowestMonetizedTier");
    if (lowestMonetizedTier!=null)
    {
      // 4 for guilds, nothing for other factions
      LOGGER.debug("Lowest monetized tier: {}",lowestMonetizedTier);
    }
    Integer webStoreDataId=(Integer)properties.getProperty("WebStoreAccountItem_DataID");
    if (webStoreDataId!=null)
    {
      // Only for guilds
      LOGGER.debug("Web store ID: {}",webStoreDataId);
    }
  }

  private long[] extractReputationTable(WStateDataSet table)
  {
    long[] reputationTable=null;
    if (Context.isLive())
    {
      reputationTable=(long[])table.getValue(1);
    }
    else
    {
      @SuppressWarnings("unchecked")
      Map<Integer,Long> map=(Map<Integer,Long>)table.getValue(1);
      int size=map.size();
      reputationTable=new long[size+1];
      for(Map.Entry<Integer,Long> entry : map.entrySet())
      {
        reputationTable[entry.getKey().intValue()]=entry.getValue().longValue();
      }
    }
    return reputationTable;
  }

  private Map<Integer,String> getTierNames(int tableId)
  {
    PropertiesSet tierNamesProps=_facade.loadProperties(tableId+DATConstants.DBPROPERTIES_OFFSET);
    Map<Integer,String> ret=new HashMap<Integer,String>();
    int minIndex=((Integer)tierNamesProps.getProperty("Progression_MinimumIndexValue")).intValue();
    ArrayPropertyValue tierPropsArray=(ArrayPropertyValue)tierNamesProps.getPropertyValueByName("StringInfoProgression_Array");
    int index=minIndex;
    for(PropertyValue tierPropsObj : tierPropsArray.getValues())
    {
      String tierName=_i18n.getStringProperty(tierPropsObj,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
      ret.put(Integer.valueOf(index),tierName);
      index++;
    }
    return ret;
  }

  private List<FactionLevel> buildFactionLevels(Map<Integer,String> tierNames, long[] reputationTable, int lowestTier, int highestTier)
  {
    List<FactionLevel> factionLevels=new ArrayList<FactionLevel>();
    for(int tier=lowestTier;tier<=highestTier;tier++)
    {
      String tierName=tierNames.get(Integer.valueOf(tier));
      long reputation=reputationTable[tier];
      FactionLevel factionLevel=new FactionLevel(tier,tierName,0,(int)reputation);
      factionLevels.add(factionLevel);
    }
    return factionLevels;
  }

  /**
   * Load factions.
   * @return the loaded data.
   */
  public FactionsRegistry doIt()
  {
    List<Faction> factions=buildFactions();
    factions=sortFactions(factions);
    FactionsRegistry registry=new FactionsRegistry();
    for(Faction faction : factions)
    {
      registry.registerFaction(faction);
    }
    buildFactionDeeds(registry);
    save(registry);
    // Labels
    _i18n.save();
    return registry;
  }

  private List<Faction> buildFactions()
  {
    List<Faction> ret=new ArrayList<Faction>();
    PropertiesSet indexProperties=_facade.loadProperties(0x7900A452);
    Object[] idsArray=(Object[])indexProperties.getProperty("Reputation_FactionTable");
    for(Object idObj : idsArray)
    {
      int id=((Integer)idObj).intValue();
      Faction faction=load(id);
      if (faction!=null)
      {
        ret.add(faction);
      }
    }
    return ret;
  }

  private List<Faction> sortFactions(List<Faction> factions)
  {
    List<Faction> ret=new ArrayList<Faction>();
    int[] factionIds=getLegacyFactionOrder();
    for(int factionId : factionIds)
    {
      Faction foundFaction=null;
      for(Faction faction : factions)
      {
        if (faction.getIdentifier()==factionId)
        {
          foundFaction=faction;
          break;
        }
      }
      if (foundFaction!=null)
      {
        factions.remove(foundFaction);
        ret.add(foundFaction);
      }
    }
    if (!factions.isEmpty())
    {
      LOGGER.warn("Sort order not specified for some factions: {}",factions);
    }
    ret.addAll(factions);
    return ret;
  }

  /**
   * Find deeds associated to faction levels.
   * @param registry Factions registry to use.
   */
  public static void associateDeeds(FactionsRegistry registry)
  {
    ReputationDeedsFinder finder=new ReputationDeedsFinder();
    DeedsManager deedsManager=DeedsManager.getInstance();
    finder.init(deedsManager.getAll());
    for(Faction faction : registry.getAll())
    {
      FactionLevel[] levels=faction.getLevels();
      for(FactionLevel level : levels)
      {
        DeedDescription deed=finder.findDeed(faction.getIdentifier(),level.getTier());
        if (deed!=null)
        {
          level.setDeedKey(deed.getIdentifyingKey());
          int lotroPoints=deed.getRewards().getLotroPoints();
          level.setLotroPoints(lotroPoints);
        }
      }
    }
    save(registry);
  }

  /**
   * Save a factions registry to a file.
   * @param factions
   */
  public static void save(FactionsRegistry factions)
  {
    File toFile=GeneratedFiles.FACTIONS;
    boolean ok=FactionsXMLWriter.writeFactionsFile(toFile,factions);
    if (ok)
    {
      LOGGER.info("Wrote file: {}",toFile);
    }
    else
    {
      LOGGER.error("Failed to build factions registry file: {}",toFile);
    }
  }

  private boolean useFaction(int id)
  {
    if (id==1879090244) return false; // Zombie Pirates of Evendim
    if (id==1879090243) return false; // Hobbit Ninjas of The Shire
    if (id==1879345133) return false; // Enmity of Ungol
    return true;
  }

  boolean isGuildFaction(int factionId)
  {
    // May be loaded from CraftDirectory/CraftDirectory_CraftGuildArray...
    if (factionId==1879124448) return true; // Cook's Guild
    if (factionId==1879124449) return true; // Jeweller's Guild
    if (factionId==1879124450) return true; // Metalsmith's Guild
    if (factionId==1879124451) return true; // Scholar's Guild
    if (factionId==1879124452) return true; // Tailor's Guild
    if (factionId==1879124453) return true; // Weaponsmith's Guild
    if (factionId==1879124454) return true; // Woodworker's Guild
    return false;
  }

  private String[] getFactionDescription(int factionId)
  {
    String[] ret=getEriadorFactionsDescription(factionId);
    if (ret.length>0)
    {
      return ret;
    }
    ret=getRhovanionFactionsDescription(factionId);
    if (ret.length>0)
    {
      return ret;
    }
    ret=getDunlandFactionsDescription(factionId);
    if (ret.length>0)
    {
      return ret;
    }
    ret=getRohanFactionsDescription(factionId);
    if (ret.length>0)
    {
      return ret;
    }
    ret=getDolAmrothFactionsDescription(factionId);
    if (ret.length>0)
    {
      return ret;
    }
    ret=getGondorFactionsDescription(factionId);
    if (ret.length>0)
    {
      return ret;
    }
    ret=getMordorFactionsDescription(factionId);
    if (ret.length>0)
    {
      return ret;
    }
    String category="Misc";
    if (factionId==1879182957) return new String[]{category,"ALE_ASSOCIATION",FactionLevelTemplates.CLASSIC}; // The Ale Association
    if (factionId==1879103953) return new String[]{category,"INN_LEAGUE",FactionLevelTemplates.CLASSIC}; // The Inn League
    if (factionId==1879305436) return new String[]{category,"HOBNANIGANS",FactionLevelTemplates.HOBNANIGANS}; // Chicken Chasing League of Eriador
    if (factionId==1879464285) return new String[]{category,null,null}; // The Path of Valour
    if (factionId==1879490383) return new String[]{category,null,null}; // Amelia's Studies
    if (factionId==1879496954) return new String[]{category,null,null}; // Moria Bounty Hunters
    category="Guild";
    if (factionId==1879124448) return new String[]{category,"GUILD_COOK",FactionLevelTemplates.GUILD}; // Cook's Guild
    if (factionId==1879124449) return new String[]{category,"GUILD_JEWELLER",FactionLevelTemplates.GUILD}; // Jeweller's Guild
    if (factionId==1879124450) return new String[]{category,"GUILD_METALSMITH",FactionLevelTemplates.GUILD}; // Metalsmith's Guild
    if (factionId==1879124451) return new String[]{category,"GUILD_SCHOLAR",FactionLevelTemplates.GUILD}; // Scholar's Guild
    if (factionId==1879124452) return new String[]{category,"GUILD_TAILOR",FactionLevelTemplates.GUILD}; // Tailor's Guild
    if (factionId==1879124453) return new String[]{category,"GUILD_WEAPONSMITH",FactionLevelTemplates.GUILD}; // Weaponsmith's Guild
    if (factionId==1879124454) return new String[]{category,"GUILD_WOODWORKER",FactionLevelTemplates.GUILD}; // Woodworker's Guild
    category="Umbar";
    // - guilds
    if (factionId==1879479489) return new String[]{category,null,null};
    if (factionId==1879479494) return new String[]{category,null,null};
    if (factionId==1879479492) return new String[]{category,null,null};
    if (factionId==1879479491) return new String[]{category,null,null};
    if (factionId==1879479493) return new String[]{category,null,null};
    if (factionId==1879479490) return new String[]{category,null,null};
    if (factionId==1879479487) return new String[]{category,null,null};
    // - others
    if (factionId==1879465745) return new String[]{category,null,null}; // Citizens of Umbar Baharbêl
    if (factionId==1879489735) return new String[]{category,null,null}; // Phetekâri of Umbar
    if (factionId==1879489736) return new String[]{category,null,null}; // The Ikorbâni
    if (factionId==1879489734) return new String[]{category,null,null}; // The Adúrhid

    LOGGER.warn("Unmanaged faction ID: {}",Integer.valueOf(factionId));
    return new String[]{};
  }

  private String[] getEriadorFactionsDescription(int factionId)
  {
    String category="Eriador";
    if (factionId==1879091345) return new String[]{category,"SHIRE",FactionLevelTemplates.CLASSIC}; // Shire
    if (factionId==1879091340) return new String[]{category,"BREE",FactionLevelTemplates.CLASSIC}; // Bree
    if (factionId==1879091408) return new String[]{category,"DWARVES",FactionLevelTemplates.CLASSIC}; // Thorin's Hall
    if (factionId==1879161272) return new String[]{category,"EGLAIN",FactionLevelTemplates.CLASSIC}; // The Eglain
    if (factionId==1879091344) return new String[]{category,"ESTELDIN",FactionLevelTemplates.CLASSIC}; // The Rangers of Esteldín
    if (factionId==1879091346) return new String[]{category,"RIVENDELL",FactionLevelTemplates.CLASSIC}; // Elves of Rivendell
    if (factionId==1879091343) return new String[]{category,"ANNUMINAS",FactionLevelTemplates.CLASSIC}; // The Wardens of Annúminas
    if (factionId==1879103954) return new String[]{category,"LOSSOTH",FactionLevelTemplates.CLASSIC}; // Lossoth of Forochel
    if (factionId==1879091341) return new String[]{category,"COUNCIL_OF_THE_NORTH",FactionLevelTemplates.CLASSIC}; // Council of the North
    if (factionId==1879097420) return new String[]{category,"ELDGANG",FactionLevelTemplates.CLASSIC}; // The Eldgang
    if (factionId==1879413167) return new String[]{category,null,null}; // The League of the Axe
    if (factionId==1879413168) return new String[]{category,null,null}; // Woodcutter's Brotherhood
    if (factionId==1879442863) return new String[]{category,null,null}; // Defenders of The Angle
    if (factionId==1879443125) return new String[]{category,null,null}; // The Yonder-watch
    if (factionId==1879448435) return new String[]{category,null,null}; // Dúnedain of Cardolan
    if (factionId==1879457726) return new String[]{category,null,null}; // Stewards of the Iron-home
    return new String[]{};
  }

  private String[] getRhovanionFactionsDescription(int factionId)
  {
    String category="Rhovanion";
    if (factionId==1879143761) return new String[]{category,"MORIA_GUARDS",FactionLevelTemplates.CLASSIC}; // Iron Garrison Guards
    if (factionId==1879143766) return new String[]{category,"MORIA_MINERS",FactionLevelTemplates.CLASSIC}; // Iron Garrison Miners
    if (factionId==1879150133) return new String[]{category,"GALADHRIM",FactionLevelTemplates.CLASSIC}; // Galadhrim
    if (factionId==1879154438) return new String[]{category,"MALLEDHRIM",FactionLevelTemplates.CLASSIC}; // Malledhrim
    if (factionId==1879362403) return new String[]{category,"ELVES_OF_FELEGOTH",FactionLevelTemplates.CLASSIC}; // Elves of Felegoth
    if (factionId==1879362405) return new String[]{category,"MEN_OF_DALE",FactionLevelTemplates.CLASSIC}; // Men of Dale
    if (factionId==1879363082) return new String[]{category,"DWARVES_OF_EREBOR",FactionLevelTemplates.EXTENDED_RESPECTED}; // Dwarves of Erebor
    if (factionId==1879368441) return new String[]{category,"GREY_MOUNTAINS_EXPEDITION",FactionLevelTemplates.CLASSIC}; // Grey Mountains Expedition
    if (factionId==1879386002) return new String[]{category,"WILDERFOLK",FactionLevelTemplates.CLASSIC}; // Wilderfolk
    if (factionId==1879403792) return new String[]{category,null,null}; // Protectors of Wilderland
    if (factionId==1879407816) return new String[]{category,null,null}; // March on Gundabad
    if (factionId==1879408300) return new String[]{category,null,null}; // The Gabil'akkâ
    if (factionId==1879413559) return new String[]{category,null,null}; // The Haban’akkâ of Thráin
    if (factionId==1879416174) return new String[]{category,null,null}; // Kharum-ubnâr
    if (factionId==1879416935) return new String[]{category,null,null}; // Reclaimers of the Mountain-hold
    return new String[]{};
  }

  private String[] getDunlandFactionsDescription(int factionId)
  {
    String category="Dunland";
    if (factionId==1879181920) return new String[]{category,"ALGRAIG",FactionLevelTemplates.CLASSIC}; // Algraig, Men of Enedwaith
    if (factionId==1879181919) return new String[]{category,"GREY_COMPANY",FactionLevelTemplates.CLASSIC}; // The Grey Company
    if (factionId==1879202077) return new String[]{category,"DUNLAND",FactionLevelTemplates.CLASSIC}; // Men of Dunland
    if (factionId==1879202078) return new String[]{category,"THEODRED_RIDERS",FactionLevelTemplates.CLASSIC}; // Théodred's Riders
    return new String[]{};
  }

  private String[] getRohanFactionsDescription(int factionId)
  {
    String category="Rohan";
    if (factionId==1879227796) return new String[]{category,"STANGARD_RIDERS",FactionLevelTemplates.CLASSIC}; // The Riders of Stangard
    if (factionId==1879230121) return new String[]{category,"LIMLIGHT_GORGE",FactionLevelTemplates.CLASSIC}; // Heroes of Limlight Gorge
    if (factionId==1879237312) return new String[]{category,"WOLD",FactionLevelTemplates.CLASSIC}; // Men of the Wold
    if (factionId==1879237304) return new String[]{category,"NORCROFTS",FactionLevelTemplates.CLASSIC}; // Men of the Norcrofts
    if (factionId==1879237267) return new String[]{category,"ENTWASH_VALE",FactionLevelTemplates.CLASSIC}; // Men of the Entwash Vale
    if (factionId==1879237243) return new String[]{category,"SUTCROFTS",FactionLevelTemplates.CLASSIC}; // Men of the Sutcrofts
    if (factionId==1879259430) return new String[]{category,"PEOPLE_WILDERMORE",FactionLevelTemplates.CLASSIC}; // People of Wildermore
    if (factionId==1879259431) return new String[]{category,"SURVIVORS_WILDERMORE",FactionLevelTemplates.CLASSIC}; // Survivors of Wildermore
    if (factionId==1879271130) return new String[]{category,"EORLINGAS",FactionLevelTemplates.CLASSIC}; // The Eorlingas
    if (factionId==1879271131) return new String[]{category,"HELMINGAS",FactionLevelTemplates.CLASSIC}; // The Helmingas
    if (factionId==1879303012) return new String[]{category,"FANGORN",FactionLevelTemplates.CLASSIC}; // The Ents of Fangorn Forest
    if (factionId==1879400830) return new String[]{category,null,null}; // Townsfolk of the Kingstead
    if (factionId==1879400827) return new String[]{category,null,null}; // Townsfolk of the Eastfold
    return new String[]{};
  }

  private String[] getDolAmrothFactionsDescription(int factionId)
  {
    String category="Dol Amroth";
    if (factionId==1879306071) return new String[]{category,FactionLevelTemplates.DOL_AMROTH,FactionLevelTemplates.CLASSIC}; // Dol Amroth
    if (factionId==1879308442) return new String[]{category,"DA_ARMOURY",FactionLevelTemplates.DOL_AMROTH}; // Dol Amroth - Armoury
    if (factionId==1879308438) return new String[]{category,"DA_BANK",FactionLevelTemplates.DOL_AMROTH}; // Dol Amroth - Bank
    if (factionId==1879308441) return new String[]{category,"DA_DOCKS",FactionLevelTemplates.DOL_AMROTH}; // Dol Amroth - Docks
    if (factionId==1879308436) return new String[]{category,"DA_GREAT_HALL",FactionLevelTemplates.DOL_AMROTH}; // Dol Amroth - Great Hall
    if (factionId==1879308443) return new String[]{category,"DA_LIBRARY",FactionLevelTemplates.DOL_AMROTH}; // Dol Amroth - Library
    if (factionId==1879308440) return new String[]{category,"DA_MASON",FactionLevelTemplates.DOL_AMROTH}; // Dol Amroth - Mason
    if (factionId==1879308439) return new String[]{category,"DA_SWAN_KNIGHTS",FactionLevelTemplates.DOL_AMROTH}; // Dol Amroth - Swan-knights
    if (factionId==1879308437) return new String[]{category,"DA_WAREHOUSE",FactionLevelTemplates.DOL_AMROTH}; // Dol Amroth - Warehouse
    return new String[]{};
  }

  private String[] getGondorFactionsDescription(int factionId)
  {
    String category="Gondor";
    if (factionId==1879315479) return new String[]{category,"RINGLO_VALE",FactionLevelTemplates.CLASSIC}; // Men of Ringló Vale
    if (factionId==1879315480) return new String[]{category,"DOR_EN_ERNIL",FactionLevelTemplates.CLASSIC}; // Men of Dor-en-Ernil
    if (factionId==1879315481) return new String[]{category,"LEBENNIN",FactionLevelTemplates.CLASSIC}; // Men of Lebennin
    if (factionId==1879314940) return new String[]{category,"PELARGIR",FactionLevelTemplates.CLASSIC}; // Pelargir
    if (factionId==1879322612) return new String[]{category,"RANGERS_ITHILIEN",FactionLevelTemplates.CLASSIC}; // Rangers of Ithilien
    if (factionId==1879326961) return new String[]{category,"MINAS_TIRITH",FactionLevelTemplates.EXTENDED_CLASSIC}; // Defenders of Minas Tirith
    if (factionId==1879330539) return new String[]{category,"RIDERS_ROHAN",FactionLevelTemplates.CLASSIC}; // Riders of Rohan
    if (factionId==1879463136) return new String[]{category,null,null}; // The Renewal of Gondor
    return new String[]{};
  }

  private String[] getMordorFactionsDescription(int factionId)
  {
    String category="Mordor";
    if (factionId==1879334719) return new String[]{category,"HOST_OF_THE_WEST",FactionLevelTemplates.EXTENDED_CLASSIC}; // Host of the West
    if (factionId==1879341949) return new String[]{category,"HOW_ARMOUR",FactionLevelTemplates.HOW}; // Host of the West: Armour
    if (factionId==1879341953) return new String[]{category,"HOW_PROVISIONS",FactionLevelTemplates.HOW}; // Host of the West: Provisions
    if (factionId==1879341952) return new String[]{category,"HOW_WEAPONS",FactionLevelTemplates.HOW}; // Host of the West: Weapons
    if (factionId==1879345132) return new String[]{category,null,null}; // Red Sky Clan
    if (factionId==1879345136) return new String[]{category,"GORGOROTH",FactionLevelTemplates.GORGOROTH}; // Conquest of Gorgoroth
    if (factionId==1879345134) return new String[]{category,null,null}; // Enmity of Fushaum Bal south
    if (factionId==1879345135) return new String[]{category,null,null}; // Enmity of Fushaum Bal north
    if (factionId==1879389868) return new String[]{category,null,null}; // The White Company
    if (factionId==1879389872) return new String[]{category,null,null}; // Reclamation of Minas Ithil
    if (factionId==1879389871) return new String[]{category,null,null}; // The Great Alliance
    return new String[]{};
  }

  private void buildFactionDeeds(FactionsRegistry registry)
  {
    ReputationDeed wrDeed=new ReputationDeed("World Renowned");
    wrDeed.setLotroPoints(50);
    wrDeed.addFaction(registry.getById(1879091345)); // Shire
    wrDeed.addFaction(registry.getById(1879091340)); // Bree
    wrDeed.addFaction(registry.getById(1879091408)); // Thorin's Hall
    wrDeed.addFaction(registry.getById(1879161272)); // Eglain
    wrDeed.addFaction(registry.getById(1879091344)); // Esteldin
    wrDeed.addFaction(registry.getById(1879091346)); // Rivendell
    wrDeed.addFaction(registry.getById(1879091343)); // Annuminas
    wrDeed.addFaction(registry.getById(1879103954)); // Lossoth
    wrDeed.addFaction(registry.getById(1879091341)); // Angmar
    wrDeed.addFaction(registry.getById(1879181920)); // Algraig
    wrDeed.addFaction(registry.getById(1879181919)); // Grey company
    registry.addDeed(wrDeed);

    ReputationDeed ambassadorDeed=new ReputationDeed("Ambassador of the Elves");
    ambassadorDeed.setLotroPoints(20);
    ambassadorDeed.addFaction(registry.getById(1879091346)); // Rivendell
    ambassadorDeed.addFaction(registry.getById(1879150133)); // Galadhrim
    ambassadorDeed.addFaction(registry.getById(1879154438)); // Malledhrim
    registry.addDeed(ambassadorDeed);
  }

  private int[] getLegacyFactionOrder()
  {
    int[] ret=new int[] {
      // Eriador
      1879091345,1879091340,1879091408,1879161272,1879091344,1879091346,1879091343,1879103954,1879091341,1879097420,
      1879413167,1879413168,1879442863,1879443125,1879448435,1879457726,
      // Rhovanion
      1879143761,1879143766,1879150133,1879154438,1879362403,1879362405,1879363082,1879368441,1879386002,1879403792,1879407816,1879408300,
      1879413559,1879416174,1879416935,
      // Dunland
      1879181920,1879181919,1879202077,1879202078,1879227796,1879230121,
      // Rohan
      1879237312,1879237304,1879237267,1879237243,1879259430,1879259431,1879271130,1879271131,1879303012,1879400830,1879400827,
      // Dol Amroth
      1879306071,1879308442,1879308438,1879308441,1879308436,1879308443,1879308440,1879308439,1879308437,
      // Gondor
      1879315479,1879315480,1879315481,1879314940,1879322612,1879326961,1879330539,1879463136,
      // Mordor
      1879334719,1879341949,1879341953,1879341952,1879345136,
      // Mordor again
      1879345134,1879345135,1879345132,1879389871,1879389868,1879389872,
      // Guilds
      1879124448,1879124449,1879124450,1879124451,1879124452,1879124453,1879124454,
      // Umbar guilds
      1879479489,1879479494,1879479492,1879479491,1879479493,1879479490,1879479487,
      // Umbar
      1879465745,1879489735,1879489736,1879489734,
      // Misc
      1879182957,1879103953,1879305436,1879464285,1879490383,1879496954
    };
    return ret;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    FactionsRegistry registry=new MainDatFactionsLoader(facade).doIt();
    MainDatFactionsLoader.associateDeeds(registry);
    facade.dispose();
  }
}
