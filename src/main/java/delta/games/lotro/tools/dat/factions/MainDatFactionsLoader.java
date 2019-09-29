package delta.games.lotro.tools.dat.factions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.reputation.FactionLevels;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionLevel;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.lore.reputation.ReputationDeed;
import delta.games.lotro.lore.reputation.io.xml.FactionsXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.quests.ReputationDeedsFinder;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.lore.reputation.FactionsFactory;

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

    Faction faction=new Faction();
    // Identifier
    faction.setIdentifier(factionId);
    faction.setName(name);
    LOGGER.info("ID: "+factionId+" => "+name);

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
    lowestTier=fixLowestTier(factionId);
    faction.setLowestTier(lowestTier);
    int highestTier=((Integer)properties.getProperty("Reputation_HighestTier")).intValue();
    faction.setHighestTier(highestTier);
    int defaultTier=((Integer)properties.getProperty("Reputation_Faction_DefaultTier")).intValue();
    faction.setInitialTier(defaultTier);
    LOGGER.info("Tiers (lowest/default/highest): "+lowestTier+" / "+defaultTier+" / "+highestTier);

    /*
    // Property names
    PropertiesRegistry propsRegistry=_facade.getPropertiesRegistry();
    int currentTierPropId=((Integer)properties.getProperty("Reputation_Faction_CurrentTier_PropertyName")).intValue();
    System.out.println("Current tier property: "+propsRegistry.getPropertyDef(currentTierPropId));
    int earnedRepPropId=((Integer)properties.getProperty("Reputation_Faction_EarnedReputation_PropertyName")).intValue();
    System.out.println("Earned rep property: "+propsRegistry.getPropertyDef(earnedRepPropId));
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
    int xpTableId=((Integer)properties.getProperty("Reputation_Faction_AdvancementTable")).intValue();
    LOGGER.info("XP table: "+xpTableId);
    List<Object> table=_facade.loadWState(xpTableId);
    long[] xpTable=(long[])table.get(1);
    LOGGER.info(Arrays.toString(xpTable));

    if (factionId==1879305436)
    {
      FactionLevel level=new FactionLevel("NONE","-",0,0,0);
      level.setTier(2);
      faction.addFactionLevel(level);
      defaultTier=2;
      faction.setLowestTier(2);
    }
    List<FactionLevel> levels=buildFactionLevels(tierNames,xpTable,lowestTier,highestTier);
    for(FactionLevel level : levels)
    {
      faction.addFactionLevel(level);
      if (level.getTier()==defaultTier)
      {
        faction.setInitialLevel(level);
      }
    }
    return faction;
  }

  private boolean isNeutral(FactionLevel level)
  {
    if (FactionLevels.NEUTRAL.equals(level.getKey())) return true;
    if ("NONE".equals(level.getKey())) return true;
    if ("Neutral".equals(level.getName())) return true;
    return false;
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

  private List<FactionLevel> buildFactionLevels(Map<Integer,String> tierNames, long[] xpTable, int lowestTier, int highestTier)
  {
    List<FactionLevel> factionLevels=new ArrayList<FactionLevel>();
    for(int tier=lowestTier;tier<=highestTier;tier++)
    {
      String tierName=tierNames.get(Integer.valueOf(tier));
      long xp=xpTable[tier];
      long previousXp=xpTable[tier-1];
      int xpDiff=(int)(xp-previousXp);
      String key=String.valueOf(tier);
      FactionLevel factionLevel=new FactionLevel(key,tierName,0,0,xpDiff);
      factionLevel.setTier(tier);
      factionLevels.add(factionLevel);
    }
    return factionLevels;
  }

  private void doIt()
  {
    List<Faction> factions=buildFactions();
    FactionsRegistry registry=mergeFactionsData(factions);
    cleanupFactionsData(registry);
    associateDeeds(registry);
    manualFixes(registry);
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

  private FactionsRegistry mergeFactionsData(List<Faction> datFactions)
  {
    FactionsRegistry registry=new FactionsRegistry();
    FactionsFactory factory=new FactionsFactory();
    // Categories
    List<String> categories=factory.getCategories();
    for(String category : categories)
    {
      List<Faction> factions=factory.getByCategory(category);
      for(Faction faction : factions)
      {
        String factionName=faction.getName();
        Faction datFaction=getFactionByName(datFactions,factionName);
        if (datFaction!=null)
        {
          Faction newFaction=mergeFactionData(faction,datFaction);
          registry.registerFaction(newFaction);
          datFactions.remove(datFaction);
        }
      }
    }
    // Register missing factions
    for(Faction datFaction : datFactions)
    {
      if (!isGuildFaction(datFaction.getIdentifier()))
      {
        registry.registerFaction(datFaction);
      }
    }
    // Guild faction
    Faction guildFaction=factory.getGuildFaction();
    registry.registerFaction(guildFaction);
    // Deeds
    for(ReputationDeed deed : factory.getDeeds())
    {
      registry.addDeed(deed);
    }
    return registry;
  }

  private Faction mergeFactionData(Faction oldFaction, Faction datFaction)
  {
    LOGGER.debug("Merge faction data for: "+datFaction.getName());
    datFaction.setKey(oldFaction.getKey());
    datFaction.setCategory(oldFaction.getCategory());
    // Merge levels
    FactionLevel[] oldLevels=oldFaction.getLevels();
    FactionLevel[] datLevels=datFaction.getLevels();
    if (oldLevels.length==datLevels.length)
    {
      for(int i=0;i<oldLevels.length;i++)
      {
        datLevels[i].setKey(oldLevels[i].getKey());
      }
    }
    else
    {
      LOGGER.warn("Levels count mismatch for faction "+oldFaction.getName()+": "+oldLevels.length+"!="+datLevels.length);
    }
    return datFaction;
  }

  private void cleanupFactionsData(FactionsRegistry registry)
  {
    for(Faction faction : registry.getAll())
    {
      cleanupFactionLevels(faction);
    }
  }

  private void cleanupFactionLevels(Faction faction)
  {
    LOGGER.debug("Cleanup faction data for: "+faction.getName());
    int neutralTier=0;
    for(FactionLevel level : faction.getLevels())
    {
      if (isNeutral(level))
      {
        neutralTier=level.getTier();
        level.setRequiredXp(0);
        LOGGER.debug("Neutral tier is: "+level);
      }
    }
    // Setup code/value
    for(FactionLevel level : faction.getLevels())
    {
      int code=level.getTier()-neutralTier;
      level.setValue(code);
    }
  }

  private void associateDeeds(FactionsRegistry registry)
  {
    ReputationDeedsFinder finder=new ReputationDeedsFinder();
    finder.init(DeedsManager.getInstance().getAll());
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

  private Faction getFactionByName(List<Faction> factions, String factionName)
  {
    Faction ret=null;
    for(Faction faction : factions)
    {
      String currentFactionName=faction.getName();
      if (currentFactionName.equals(factionName))
      {
        ret=faction;
      }
    }
    if (ret==null)
    {
      LOGGER.warn("No faction with name '"+factionName+"'!");
    }
    return ret;
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

  private int fixLowestTier(int factionId)
  {
    if (hasOutsider(factionId)) return 2;
    if (!hasEnemyAndOutsider(factionId)) return 3;
    return 1;
  }

  private boolean hasOutsider(int factionId)
  {
    if (factionId==1879103954) return true; // Lossoth
    if (factionId==1879345132) return true; // Red Sky Clan
    return false;
  }

  private boolean hasEnemyAndOutsider(int factionId)
  {
    if (factionId==1879345134) return true; // Enmity of Fushaum Bal south
    if (factionId==1879345135) return true; // Enmity of Fushaum Bal north
    if (factionId==1879182957) return true; // The Ale Association
    if (factionId==1879103953) return true; // The Inn League
    return false;
  }

  private boolean isGuildFaction(int factionId)
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

  private void manualFixes(FactionsRegistry registry)
  {
    // Hobnanigans
    {
      Faction hobnanigans=registry.getByKey("HOBNANIGANS");
      hobnanigans.getLevelByKey("ROOKIE").setRequiredXp(0);
      FactionLevel none=hobnanigans.getLevelByKey("NONE");
      hobnanigans.setInitialLevel(none);
    }
    // Lossoth
    {
      Faction lossoth=registry.getByKey("LOSSOTH");
      lossoth.getLevelByKey("OUTSIDER").setRequiredXp(0);
      lossoth.getLevelByKey("NEUTRAL").setRequiredXp(10000);
    }
    // Grey company
    {
      Faction greyCompany=registry.getByKey("GREY_COMPANY");
      FactionLevel neutral=greyCompany.getLevelByKey("NEUTRAL");
      greyCompany.setInitialLevel(neutral);
    }
    // Ale Association
    {
      Faction aleAssociation=registry.getByKey("ALE_ASSOCIATION");
      aleAssociation.getLevelByKey("ENEMY").setRequiredXp(10000);
    }
    // Inn League
    {
      Faction aleAssociation=registry.getByKey("INN_LEAGUE");
      aleAssociation.getLevelByKey("ENEMY").setRequiredXp(10000);
    }
    // Mordor reputations
    {
      String category="Mordor";
      // Red Sky Clan
      Faction redSkyClan=registry.getById(1879345132);
      redSkyClan.setCategory(category);
      redSkyClan.getLevelByKey("2").setRequiredXp(0);
      redSkyClan.getLevelByKey("3").setRequiredXp(10000);
      // Enmity of Fushaum Bal south
      Faction fushaumBalSouth=registry.getById(1879345134);
      fushaumBalSouth.setCategory(category);
      fushaumBalSouth.getLevelByKey("1").setRequiredXp(10000);
      fushaumBalSouth.getLevelByKey("2").setRequiredXp(0);
      fushaumBalSouth.getLevelByKey("3").setRequiredXp(10000);
      // Enmity of Fushaum Bal north
      Faction fushaumBalNorth=registry.getById(1879345135);
      fushaumBalNorth.setCategory(category);
      fushaumBalNorth.getLevelByKey("1").setRequiredXp(10000);
      fushaumBalNorth.getLevelByKey("2").setRequiredXp(0);
      fushaumBalNorth.getLevelByKey("3").setRequiredXp(10000);
    }
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
