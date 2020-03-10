package delta.games.lotro.tools.dat.factions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionLevel;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.lore.reputation.io.xml.FactionsXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.quests.ReputationDeedsFinder;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get faction definitions from DAT files.
 * @author DAM
 */
public class MainDatFactionsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatFactionsLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatFactionsLoader(DataFacade facade)
  {
    _facade=facade;
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

  //private Set<String> propNames=new HashSet<String>();
  private Faction load(int factionId)
  {
    if (!useFaction(factionId))
    {
      return null;
    }
    PropertiesSet properties=_facade.loadProperties(factionId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      LOGGER.warn("Could not load properties for faction ID="+factionId);
      return null;
    }
    // Name
    String name=DatUtils.getStringProperty(properties,"Reputation_Faction_Name");
    name=name.trim();
    if (name.startsWith("TBD"))
    {
      return null;
    }

    Faction faction=new Faction(factionId);
    faction.setName(name);
    LOGGER.info("ID: "+factionId+" => "+name);

    // Category
    String category=getCategory(factionId);
    faction.setCategory(category);

    // Description
    String description=DatUtils.getStringProperty(properties,"Reputation_Faction_Description");
    faction.setDescription(description);

    // Tier names:
    int tierNamesId=((Integer)properties.getProperty("Reputation_Faction_TierNameProgression")).intValue();
    LOGGER.info("Tier names table: "+tierNamesId);
    Map<Integer,String> tierNames=getTierNames(tierNamesId);
    LOGGER.info(tierNames);

    // Lowest/initial/highest tiers
    int lowestTier=((Integer)properties.getProperty("Reputation_LowestTier")).intValue();
    faction.setLowestTier(lowestTier);
    int highestTier=((Integer)properties.getProperty("Reputation_HighestTier")).intValue();
    faction.setHighestTier(highestTier);
    int defaultTier=((Integer)properties.getProperty("Reputation_Faction_DefaultTier")).intValue();
    faction.setInitialTier(defaultTier);
    LOGGER.info("Tiers (lowest/default/highest): "+lowestTier+" / "+defaultTier+" / "+highestTier);

    // Guild?
    boolean isGuild=isGuildFaction(factionId);
    faction.setIsGuildFaction(isGuild);
    // Property names
    PropertiesRegistry propsRegistry=_facade.getPropertiesRegistry();
    int currentTierPropId=((Integer)properties.getProperty("Reputation_Faction_CurrentTier_PropertyName")).intValue();
    String currentTierPropertyName=propsRegistry.getPropertyDef(currentTierPropId).getName();
    faction.setCurrentTierPropertyName(currentTierPropertyName);
    int earnedRepPropId=((Integer)properties.getProperty("Reputation_Faction_EarnedReputation_PropertyName")).intValue();
    String earnedRepPropertyName=propsRegistry.getPropertyDef(earnedRepPropId).getName();
    faction.setCurrentReputationPropertyName(earnedRepPropertyName);
    /*
    int globalCapPropId=((Integer)properties.getProperty("Reputation_Faction_GlobalCap_PropertyName")).intValue();
    System.out.println("Global cap property: "+propsRegistry.getPropertyDef(globalCapPropId));

    Object disableAcc=properties.getProperty("Reputation_Faction_DisableAcceleration");
    if (disableAcc!=null)
    {
      // Only 1 for Dol Amroth library (null otherwise)
      System.out.println("Disable acc: "+disableAcc);
    }
    Integer lowestMonetizedTier=(Integer)properties.getProperty("Reputation_Faction_LowestMonetizedTier");
    if (lowestMonetizedTier!=null)
    {
      // 4 for guilds, nothing for other factions
      System.out.println("Lowest monetized tier: "+lowestMonetizedTier);
    }
    Integer webStoreDataId=(Integer)properties.getProperty("WebStoreAccountItem_DataID");
    if (webStoreDataId!=null)
    {
      // Only for guilds
      System.out.println("Web store ID: "+webStoreDataId);
    }
    */

    // Faction advancement table
    // Here we find the total amount of reputation points for each tier
    int reputationTableId=((Integer)properties.getProperty("Reputation_Faction_AdvancementTable")).intValue();
    LOGGER.info("Reputation table: "+reputationTableId);
    WStateDataSet table=_facade.loadWState(reputationTableId);
    long[] reputationTable=(long[])table.getValue(1);
    LOGGER.info(Arrays.toString(reputationTable));

    List<FactionLevel> levels=buildFactionLevels(tierNames,reputationTable,lowestTier,highestTier);
    for(FactionLevel level : levels)
    {
      faction.addFactionLevel(level);
    }
    return faction;
  }

  private Map<Integer,String> getTierNames(int tableId)
  {
    PropertiesSet tierNamesProps=_facade.loadProperties(tableId+DATConstants.DBPROPERTIES_OFFSET);
    Map<Integer,String> ret=new HashMap<Integer,String>();
    int minIndex=((Integer)tierNamesProps.getProperty("Progression_MinimumIndexValue")).intValue();
    Object[] tierPropsArray=(Object[])tierNamesProps.getProperty("StringInfoProgression_Array");
    int index=minIndex;
    for(Object tierPropsObj : tierPropsArray)
    {
      String[] tierNames=(String[])tierPropsObj;
      String tierName=tierNames[0];
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
   */
  public void doIt()
  {
    List<Faction> factions=buildFactions();
    factions=sortFactions(factions);
    FactionsRegistry registry=new FactionsRegistry();
    for(Faction faction : factions)
    {
      registry.registerFaction(faction);
    }
    associateDeeds(registry);
    save(registry);
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
    if (factions.size()>0) {
      LOGGER.warn("Sort order not specified for some factions: "+factions);
    }
    return ret;
  }

  private void associateDeeds(FactionsRegistry registry)
  {
    ReputationDeedsFinder finder=new ReputationDeedsFinder();
    DeedsManager deedsManager=new DeedsManager();
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
  }

  private void save(FactionsRegistry factions)
  {
    File toFile=GeneratedFiles.FACTIONS;
    boolean ok=FactionsXMLWriter.writeFactionsFile(toFile,factions);
    if (ok)
    {
      LOGGER.info("Wrote file: "+toFile);
    }
    else
    {
      LOGGER.error("Failed to build factions registry file: "+toFile);
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
    if (factionId==1879124448) return true; // Cook's Guild
    if (factionId==1879124449) return true; // Jeweller's Guild
    if (factionId==1879124450) return true; // Metalsmith's Guild
    if (factionId==1879124451) return true; // Scholar's Guild
    if (factionId==1879124452) return true; // Tailor's Guild
    if (factionId==1879124453) return true; // Weaponsmith's Guild
    if (factionId==1879124454) return true; // Woodworker's Guild
    return false;
  }

  private String getCategory(int factionId)
  {
    String category="Eriador";
    if (factionId==1879091345) return category; // Shire
    if (factionId==1879091340) return category; // Bree
    if (factionId==1879091408) return category; // Thorin's Hall
    if (factionId==1879161272) return category; // The Eglain
    if (factionId==1879091344) return category; // The Rangers of Esteldín
    if (factionId==1879091346) return category; // Elves of Rivendell
    if (factionId==1879091343) return category; // The Wardens of Annúminas
    if (factionId==1879103954) return category; // Lossoth of Forochel
    if (factionId==1879091341) return category; // Council of the North
    if (factionId==1879097420) return category; // The Eldgang
    category="Rhovanion";
    if (factionId==1879143761) return category; // Iron Garrison Guards
    if (factionId==1879143766) return category; // Iron Garrison Miners
    if (factionId==1879150133) return category; // Galadhrim
    if (factionId==1879154438) return category; // Malledhrim
    if (factionId==1879362403) return category; // Elves of Felegoth
    if (factionId==1879362405) return category; // Men of Dale
    if (factionId==1879363082) return category; // Dwarves of Erebor
    if (factionId==1879368441) return category; // Grey Mountains Expedition
    if (factionId==1879386002) return category; // Wilderfolk
    category="Dunland";
    if (factionId==1879181920) return category; // Algraig, Men of Enedwaith
    if (factionId==1879181919) return category; // The Grey Company
    if (factionId==1879202077) return category; // Men of Dunland
    if (factionId==1879202078) return category; // Théodred's Riders
    category="Rohan";
    if (factionId==1879227796) return category; // The Riders of Stangard
    if (factionId==1879230121) return category; // Heroes of Limlight Gorge
    if (factionId==1879237312) return category; // Men of the Wold
    if (factionId==1879237304) return category; // Men of the Norcrofts
    if (factionId==1879237267) return category; // Men of the Entwash Vale
    if (factionId==1879237243) return category; // Men of the Sutcrofts
    if (factionId==1879259430) return category; // People of Wildermore
    if (factionId==1879259431) return category; // Survivors of Wildermore
    if (factionId==1879271130) return category; // The Eorlingas
    if (factionId==1879271131) return category; // The Helmingas
    if (factionId==1879303012) return category; // The Ents of Fangorn Forest
    category="Dol Amroth";
    if (factionId==1879306071) return category; // Dol Amroth
    if (factionId==1879308442) return category; // Dol Amroth - Armoury
    if (factionId==1879308438) return category; // Dol Amroth - Bank
    if (factionId==1879308441) return category; // Dol Amroth - Docks
    if (factionId==1879308436) return category; // Dol Amroth - Great Hall
    if (factionId==1879308443) return category; // Dol Amroth - Library
    if (factionId==1879308440) return category; // Dol Amroth - Mason
    if (factionId==1879308439) return category; // Dol Amroth - Swan-knights
    if (factionId==1879308437) return category; // Dol Amroth - Warehouse
    category="Gondor";
    if (factionId==1879315479) return category; // Men of Ringló Vale
    if (factionId==1879315480) return category; // Men of Dor-en-Ernil
    if (factionId==1879315481) return category; // Men of Lebennin
    if (factionId==1879314940) return category; // Pelargir
    if (factionId==1879322612) return category; // Rangers of Ithilien
    if (factionId==1879326961) return category; // Defenders of Minas Tirith
    if (factionId==1879330539) return category; // Riders of Rohan
    category="Mordor";
    if (factionId==1879334719) return category; // Host of the West
    if (factionId==1879341949) return category; // Host of the West: Armour
    if (factionId==1879341953) return category; // Host of the West: Provisions
    if (factionId==1879341952) return category; // Host of the West: Weapons
    if (factionId==1879345132) return category; // Red Sky Clan
    if (factionId==1879345136) return category; // Conquest of Gorgoroth
    if (factionId==1879345134) return category; // Enmity of Fushaum Bal south
    if (factionId==1879345135) return category; // Enmity of Fushaum Bal north
    if (factionId==1879389868) return category; // The White Company
    if (factionId==1879389872) return category; // Reclamation of Minas Ithil
    if (factionId==1879389871) return category; // The Great Alliance
    category="Misc";
    if (factionId==1879182957) return category; // The Ale Association
    if (factionId==1879103953) return category; // The Inn League
    if (factionId==1879305436) return category; // Chicken Chasing League of Eriador
    category="";
    if (factionId==1879400830) return category; // Townsfolk of the Kingstead
    if (factionId==1879400827) return category; // Townsfolk of the Eastfold

    if (isGuildFaction(factionId)) return "Guild";
    return "???";
  }

  private int[] getLegacyFactionOrder()
  {
    int[] ret=new int[] {
      // Eriador
      1879091345,1879091340,1879091408,1879161272,1879091344,1879091346,1879091343,1879103954,1879091341,1879097420,
      // Rhovanion
      1879143761,1879143766,1879150133,1879154438,1879362403,1879362405,1879363082,1879368441,1879386002,
      // Dunland
      1879181920,1879181919,1879202077,1879202078,1879227796,1879230121,
      // Rohan
      1879237312,1879237304,1879237267,1879237243,1879259430,1879259431,1879271130,1879271131,1879303012,
      // Dol Amroth
      1879306071,1879308442,1879308438,1879308441,1879308436,1879308443,1879308440,1879308439,1879308437,
      // Gondor
      1879315479,1879315480,1879315481,1879314940,1879322612,1879326961,1879330539,
      // Mordor
      1879334719,1879341949,1879341953,1879341952,1879345136,
      // Misc
      1879182957,1879103953,1879305436,
      // Mordor again
      1879345132,1879345134,1879345135,1879389868,1879389872,1879389871,
      // Guilds
      1879124448,1879124449,1879124450,1879124451,1879124452,1879124453,1879124454,
      // Empty
      1879400830,1879400827
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
    new MainDatFactionsLoader(facade).doIt();
    facade.dispose();
  }
}
