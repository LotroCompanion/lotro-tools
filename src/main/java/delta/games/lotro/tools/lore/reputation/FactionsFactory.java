package delta.games.lotro.tools.lore.reputation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionLevel;
import delta.games.lotro.lore.reputation.ReputationDeed;

/**
 * Factions factory.
 * @author DAM
 */
public class FactionsFactory
{
  private FactionLevelsTemplates _templates;
  private List<String> _categories;
  private List<ReputationDeed> _deeds;
  private HashMap<String,List<Faction>> _factionsByCategory;
  private Faction _guildFaction;

  /**
   * Constructor.
   */
  public FactionsFactory()
  {
    _templates=new FactionLevelsTemplates();
    _categories=new ArrayList<String>();
    _deeds=new ArrayList<ReputationDeed>();
    _factionsByCategory=new HashMap<String,List<Faction>>();
    init();
  }

  /**
   * Get faction categories.
   * @return a list of category names.
   */
  public List<String> getCategories()
  {
    return _categories;
  }

  /**
   * Get the factions for a given category.
   * @param category Category to use.
   * @return A list of factions.
   */
  public List<Faction> getByCategory(String category)
  {
    return _factionsByCategory.get(category);
  }

  /**
   * Get faction deeds.
   * @return a list of faction deeds.
   */
  public List<ReputationDeed> getDeeds()
  {
    return _deeds;
  }

  private Faction initFaction(String category, String key, String name, String template)
  {
    if (!_categories.contains(category))
    {
      _categories.add(category);
    }
    List<Faction> factions=_factionsByCategory.get(category);
    if (factions==null)
    {
      factions=new ArrayList<Faction>();
      _factionsByCategory.put(category,factions);
    }
    Faction faction=buildFaction(category,key,name,template);
    factions.add(faction);
    return faction;
  }

  private Faction buildFaction(String category, String key, String name, String template)
  {
    FactionLevelsTemplate factionTemplate=_templates.getByKey(template);
    List<FactionLevel> levels=factionTemplate.buildLevels();
    Faction faction=new Faction(key,name,category,levels);
    return faction;
  }

  private ReputationDeed getFactionDeedByName(String name)
  {
    ReputationDeed ret=null;
    for(ReputationDeed deed : _deeds)
    {
      if (name.equals(deed.getName()))
      {
        return deed;
      }
    }
    ret=new ReputationDeed(name);
    _deeds.add(ret);
    return ret;
  }

  private ReputationDeed setupDeedForFaction(String deedName,Faction faction)
  {
    ReputationDeed deed=getFactionDeedByName(deedName);
    deed.addFaction(faction);
    return deed;
  }

  private void init()
  {
    String worldRenowned="World Renowned";
    String ambassador="Ambassador of the Elves";

    // ERIADOR
    String category="Eriador";
    Faction shire=initFaction(category,"SHIRE","The Mathom Society",FactionLevelsTemplates.CLASSIC);
    ReputationDeed wrDeed=setupDeedForFaction(worldRenowned,shire);
    wrDeed.setLotroPoints(50);
    Faction bree=initFaction(category,"BREE","Men of Bree",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,bree);
    Faction dwarves=initFaction(category,"DWARVES","Thorin's Hall",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,dwarves);
    Faction eglain=initFaction(category,"EGLAIN","The Eglain",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,eglain);
    Faction esteldin=initFaction(category,"ESTELDIN","Rangers of Esteldín",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,esteldin);
    Faction rivendell=initFaction(category,"RIVENDELL","Elves of Rivendell",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,rivendell);
    ReputationDeed ambassadorDeed=setupDeedForFaction(ambassador,rivendell);
    ambassadorDeed.setLotroPoints(20);
    Faction annuminas=initFaction(category,"ANNUMINAS","The Wardens of Annúminas",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,annuminas);
    Faction lossoth=initFaction(category,"LOSSOTH","Lossoth of Forochel",FactionLevelsTemplates.FOROCHEL);
    setupDeedForFaction(worldRenowned,lossoth);
    Faction angmar=initFaction(category,"COUNCIL_OF_THE_NORTH","Council of the North",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,angmar);
    initFaction(category,"ELDGANG","The Eldgang",FactionLevelsTemplates.CLASSIC);
    category="Rhovanion";
    initFaction(category,"MORIA_GUARDS","Iron Garrison Guards",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"MORIA_MINERS","Iron Garrison Miners",FactionLevelsTemplates.CLASSIC);
    Faction galadhrim=initFaction(category,"GALADHRIM","Galadhrim",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(ambassador,galadhrim);
    Faction malledhrim=initFaction(category,"MALLEDHRIM","Malledhrim",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(ambassador,malledhrim);
    initFaction(category,"ELVES_OF_FELEGOTH","Elves of Felegoth",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"MEN_OF_DALE","Men of Dale",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"DWARVES_OF_EREBOR","Dwarves of Erebor",FactionLevelsTemplates.EXTENDED_RESPECTED);
    initFaction(category,"GREY_MOUNTAINS_EXPEDITION","Grey Mountains Expedition",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"WILDERFOLK","Wilderfolk",FactionLevelsTemplates.CLASSIC);
    category="Dunland";
    Faction algraig=initFaction(category,"ALGRAIG","Algraig, Men of Enedwaith",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,algraig);
    Faction greyCompany=initFaction(category,"GREY_COMPANY","The Grey Company",FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,greyCompany);
    initFaction(category,"DUNLAND","Men of Dunland",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"THEODRED_RIDERS","Théodred's Riders",FactionLevelsTemplates.CLASSIC);
    category="Rohan";
    initFaction(category,"STANGARD_RIDERS","The Riders of Stangard",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"LIMLIGHT_GORGE","Heroes of Limlight Gorge",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"WOLD","Men of the Wold",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"NORCROFTS","Men of the Norcrofts",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"ENTWASH_VALE","Men of the Entwash Vale",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"SUTCROFTS","Men of the Sutcrofts",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"PEOPLE_WILDERMORE","People of Wildermore",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"SURVIVORS_WILDERMORE","Survivors of Wildermore",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"EORLINGAS","The Eorlingas",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"HELMINGAS","The Helmingas",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"FANGORN","The Ents of Fangorn Forest",FactionLevelsTemplates.CLASSIC);
    category="Dol Amroth";
    initFaction(category,"DOL_AMROTH","Dol Amroth",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"DA_ARMOURY","Dol Amroth - Armoury",FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_BANK","Dol Amroth - Bank",FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_DOCKS","Dol Amroth - Docks",FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_GREAT_HALL","Dol Amroth - Great Hall",FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_LIBRARY","Dol Amroth - Library",FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_MASON","Dol Amroth - Mason",FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_SWAN_KNIGHTS","Dol Amroth - Swan-knights",FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_WAREHOUSE","Dol Amroth - Warehouse",FactionLevelsTemplates.DOL_AMROTH);
    category="Gondor";
    // For all 3 Central Gondor regions: all levels gives no LP
    initFaction(category,"RINGLO_VALE","Men of Ringló Vale",FactionLevelsTemplates.CENTRAL_GONDOR); 
    initFaction(category,"DOR_EN_ERNIL","Men of Dor-en-Ernil",FactionLevelsTemplates.CENTRAL_GONDOR);
    initFaction(category,"LEBENNIN","Men of Lebennin",FactionLevelsTemplates.CENTRAL_GONDOR);
    initFaction(category,"PELARGIR","Pelargir",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"RANGERS_ITHILIEN","Rangers of Ithilien",FactionLevelsTemplates.CLASSIC);
    initFaction(category,"MINAS_TIRITH","Defenders of Minas Tirith",FactionLevelsTemplates.EXTENDED_CLASSIC);
    initFaction(category,"RIDERS_ROHAN","Riders of Rohan",FactionLevelsTemplates.CLASSIC);
    category="Mordor";
    initFaction(category,"HOST_OF_THE_WEST","Host of the West",FactionLevelsTemplates.EXTENDED_CLASSIC);
    initFaction(category,"HOW_ARMOUR","Host of the West: Armour",FactionLevelsTemplates.HOW);
    initFaction(category,"HOW_PROVISIONS","Host of the West: Provisions",FactionLevelsTemplates.HOW);
    initFaction(category,"HOW_WEAPONS","Host of the West: Weapons",FactionLevelsTemplates.HOW);
    initFaction(category,"GORGOROTH","Conquest of Gorgoroth",FactionLevelsTemplates.GORGOROTH);
    category="Misc";
    Faction aleAssociation=initFaction(category,"ALE_ASSOCIATION","The Ale Association",FactionLevelsTemplates.ALE_INN);
    aleAssociation.setInitialLevel(aleAssociation.getLevels()[2]);
    Faction innLeague=initFaction(category,"INN_LEAGUE","The Inn League",FactionLevelsTemplates.ALE_INN);
    innLeague.setInitialLevel(innLeague.getLevels()[2]);
    initFaction(category,"HOBNANIGANS","Chicken Chasing League of Eriador",FactionLevelsTemplates.HOBNANIGANS);
    // Guild
    _guildFaction=buildFaction(null,"GUILD","Guild",FactionLevelsTemplates.GUILD);
  }

  /**
   * Get the guild faction.
   * @return the guild faction.
   */
  public Faction getGuildFaction()
  {
    return _guildFaction;
  }
}
