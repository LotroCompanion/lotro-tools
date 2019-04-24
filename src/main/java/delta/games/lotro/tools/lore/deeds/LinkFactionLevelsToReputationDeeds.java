package delta.games.lotro.tools.lore.deeds;

import java.util.HashMap;
import java.util.List;

import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedProxy;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionLevel;
import delta.games.lotro.lore.reputation.FactionsRegistry;

/**
 * Attempt to link faction levels and reputation deeds (if possible).
 * @author DAM
 */
public class LinkFactionLevelsToReputationDeeds
{
  private HashMap<String,DeedDescription> _deedsByKey;

  private void loadDeeds()
  {
    _deedsByKey=new HashMap<String,DeedDescription>();
    DeedsManager deedsMgr=DeedsManager.getInstance();
    List<DeedDescription> deeds=deedsMgr.getAll();
    for(DeedDescription deed : deeds)
    {
      _deedsByKey.put(deed.getKey(),deed);
    }
  }

  private void findReputationDeeds(FactionsRegistry registry)
  {
    List<Faction> factions=registry.getAll();
    for(Faction faction : factions)
    {
      DeedDescription baseRepDeed=findKnownToFactionDeed(faction);
      if (baseRepDeed!=null)
      {
        enhanceFactionWithDeeds(baseRepDeed,faction);
      }
    }
  }

  private void enhanceFactionWithDeeds(DeedDescription baseRepDeed, Faction faction)
  {
    String factionName=faction.getName();
    System.out.println("Faction: "+factionName);
    FactionLevel[] levels=faction.getLevels();
    int index=0;
    for(FactionLevel level : levels)
    {
      if (level.getValue()==1)
      {
        break;
      }
      index++;
    }
    DeedDescription currentDeed=baseRepDeed;
    while((currentDeed!=null) && (index<levels.length))
    {
      FactionLevel level=levels[index];
      String deedName=currentDeed.getName();
      System.out.println("\t"+level.getName()+" => "+deedName);
      String deedKey=currentDeed.getKey();
      level.setDeedKey(deedKey);
      DeedProxy nextDeedProxy=currentDeed.getNextDeedProxy();
      currentDeed=null;
      if (nextDeedProxy!=null)
      {
        currentDeed=nextDeedProxy.getObject();
        index++;
      }
    }
  }

  private DeedDescription findKnownToFactionDeed(Faction faction)
  {
    String defaultDeedKey="Known_to_the_"+faction.getName();
    defaultDeedKey=defaultDeedKey.replace(' ','_');
    DeedDescription deed=_deedsByKey.get(defaultDeedKey);
    if (deed==null)
    {
      String deedKey=null;
      String key=faction.getKey();
      if ("DWARVES".equals(key)) deedKey="Known_to_Thorin%27s_Hall";
      if ("ESTELDIN".equals(key)) deedKey="Known_to_the_Rangers_of_Esteld%C3%ADn";
      if ("ANNUMINAS".equals(key)) deedKey="Known_to_the_Wardens_of_Ann%C3%BAminas";
      if ("LOSSOTH".equals(key)) deedKey="Known_to_the_Lossoth";
      //if ("ELDGANG".equals(key)) deedKey=""; // No reputation deed
      if ("MORIA_GUARDS".equals(key)) deedKey="Known_to_the_Iron_Garrison_Guards_(Deed)";
      if ("MORIA_MINERS".equals(key)) deedKey="Known_to_the_Iron_Garrison_Miners_(Deed)";
      if ("ALGRAIG".equals(key)) deedKey="Known_to_the_Algraig";
      if ("THEODRED_RIDERS".equals(key)) deedKey="Known_to_Th%C3%A9odred%27s_Riders";
      if ("WOLD".equals(key)) deedKey="Known_to_the_Wold";
      if ("NORCROFTS".equals(key)) deedKey="Known_to_the_Norcrofts";
      if ("ENTWASH_VALE".equals(key)) deedKey="Known_to_the_Entwash_Vale";
      if ("SUTCROFTS".equals(key)) deedKey="Known_to_the_Sutcrofts";
      if ("HELMINGAS".equals(key)) deedKey="Known_to_the_Helmingas";
      if ("FANGORN".equals(key)) deedKey="Known_to_the_Ents_of_Fangorn";
      if ("DOL_AMROTH".equals(key)) deedKey="Known_to_Dol_Amroth";
      // No reputation deed: deed=rep deed+some quests
      //if ("DA_ARMOURY".equals(key)) deedKey="";
      //if ("DA_BANK".equals(key)) deedKey="";
      //if ("DA_DOCKS".equals(key)) deedKey="";
      //if ("DA_GREAT_HALL".equals(key)) deedKey="";
      //if ("DA_LIBRARY".equals(key)) deedKey="";
      //if ("DA_MASON".equals(key)) deedKey="";
      //if ("DA_SWAN_KNIGHTS".equals(key)) deedKey="";
      //if ("DA_WAREHOUSE".equals(key)) deedKey="";
      if ("RINGLO_VALE".equals(key)) deedKey="Known_to_Ringl%C3%B3_Vale";
      if ("DOR_EN_ERNIL".equals(key)) deedKey="Known_to_Dor-en-Ernil";
      if ("LEBENNIN".equals(key)) deedKey="Known_to_Lebennin";
      if ("PELARGIR".equals(key)) deedKey="Known_to_Pelargir";
      if ("RANGERS_ITHILIEN".equals(key)) deedKey="Known_to_the_Rangers_of_Ithilien";
      if ("MINAS_TIRITH".equals(key)) deedKey="Known_to_Minas_Tirith";
      if ("RIDERS_ROHAN".equals(key)) deedKey="Known_to_the_Riders_of_Rohan";
      if ("HOST_OF_THE_WEST".equals(key)) deedKey="Known_to_the_Host_of_the_West_(Faction)(Deed)";
      if ("HOW_ARMOUR".equals(key)) deedKey="Host_of_the_West_Armourer_(Faction)_(Deed)";
      if ("HOW_PROVISIONS".equals(key)) deedKey="Host_of_the_West_Provisioner_(Faction)_(Deed)";
      if ("HOW_WEAPONS".equals(key)) deedKey="Host_of_the_West_Weaponist_(Faction)_(Deed)";
      if ("GORGOROTH".equals(key)) deedKey="Known_in_the_Conquest_of_Gorgoroth";
      if ("ALE_ASSOCIATION".equals(key)) deedKey="Ale_Association_Acquaintance";
      //if ("INN_LEAGUE".equals(key)) deedKey=""; // No reputation deed
      //if ("HOBNANIGANS".equals(key)) deedKey=""; // No reputation deed
      if (deedKey!=null)
      {
        deed=_deedsByKey.get(deedKey);
      }
    }
    if (deed==null)
    {
      System.err.println("No reputation deed for faction: "+faction.getKey());
    }
    return deed;
  }

  /**
   * Update the given registry with deed keys.
   * @param registry Registry to update.
   */
  public void doIt(FactionsRegistry registry)
  {
    loadDeeds();
    findReputationDeeds(registry);
  }
}
