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

  private Faction initFaction(String category, String key, String name, String[] aliases, String template)
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
    Faction faction=buildFaction(category,key,name,aliases,template);
    factions.add(faction);
    return faction;
  }

  private Faction buildFaction(String category, String key, String name, String[] aliases, String template)
  {
    FactionLevelsTemplate factionTemplate=_templates.getByKey(template);
    List<FactionLevel> levels=factionTemplate.buildLevels();
    Faction faction=new Faction(key,name,category,levels);
    if (aliases!=null)
    {
      for(String alias : aliases)
      {
        faction.addAlias(alias);
      }
    }
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
    Faction shire=initFaction(category,"SHIRE","Mathom Society",new String[]{"The Mathom Society"},FactionLevelsTemplates.CLASSIC);
    ReputationDeed wrDeed=setupDeedForFaction(worldRenowned,shire);
    wrDeed.setLotroPoints(50);
    Faction bree=initFaction(category,"BREE","Men of Bree",null,FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,bree);
    Faction dwarves=initFaction(category,"DWARVES","Thorin's Hall",null,FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,dwarves);
    Faction eglain=initFaction(category,"EGLAIN","Eglain",new String[]{"The Eglain"},FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,eglain);
    Faction esteldin=initFaction(category,"ESTELDIN","Rangers of Esteldín",null,FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,esteldin);
    Faction rivendell=initFaction(category,"RIVENDELL","Elves of Rivendell",null,FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,rivendell);
    ReputationDeed ambassadorDeed=setupDeedForFaction(ambassador,rivendell);
    ambassadorDeed.setLotroPoints(20);
    Faction annuminas=initFaction(category,"ANNUMINAS","Wardens of Annúminas",new String[]{"The Wardens of Annúminas"},FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,annuminas);
    Faction lossoth=initFaction(category,"LOSSOTH","Lossoth of Forochel",new String[]{"Lossoth"},FactionLevelsTemplates.FOROCHEL);
    setupDeedForFaction(worldRenowned,lossoth);
    Faction angmar=initFaction(category,"COUNCIL_OF_THE_NORTH","Council of the North",new String[]{"Council of North"},FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,angmar);
    initFaction(category,"ELDGANG","Eldgang",new String[]{"The Eldgang"},FactionLevelsTemplates.CLASSIC);
    category="Rhovanion";
    initFaction(category,"MORIA_GUARDS","Iron Garrison Guards",null,FactionLevelsTemplates.CLASSIC);
    initFaction(category,"MORIA_MINERS","Iron Garrison Miners",null,FactionLevelsTemplates.CLASSIC);
    Faction galadhrim=initFaction(category,"GALADHRIM","Galadhrim",null,FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(ambassador,galadhrim);
    Faction malledhrim=initFaction(category,"MALLEDHRIM","Malledhrim",null,FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(ambassador,malledhrim);
    initFaction(category,"ELVES_OF_FELEGOTH","Elves of Felegoth",null,FactionLevelsTemplates.CLASSIC);
    initFaction(category,"MEN_OF_DALE","Men of Dale",null,FactionLevelsTemplates.CLASSIC);
    initFaction(category,"DWARVES_OF_EREBOR","Dwarves of Erebor",null,FactionLevelsTemplates.CLASSIC);
    category="Dunland";
    Faction algraig=initFaction(category,"ALGRAIG","Algraig, Men of Enedwaith",new String[]{"Algraig"},FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,algraig);
    Faction greyCompany=initFaction(category,"GREY_COMPANY","Grey Company",new String[]{"The Grey Company"},FactionLevelsTemplates.CLASSIC);
    setupDeedForFaction(worldRenowned,greyCompany);
    initFaction(category,"DUNLAND","Men of Dunland",null,FactionLevelsTemplates.CLASSIC);
    initFaction(category,"THEODRED_RIDERS","Théodred's Riders",new String[]{"Riders of Théodred"},FactionLevelsTemplates.CLASSIC);
    category="Rohan";
    initFaction(category,"STANGARD_RIDERS","Riders of Stangard",new String[]{"The Riders of Stangard"},FactionLevelsTemplates.CLASSIC);
    initFaction(category,"LIMLIGHT_GORGE","Heroes of Limlight Gorge",null,FactionLevelsTemplates.CLASSIC);
    initFaction(category,"WOLD","Men of the Wold",new String[]{"Wold"},FactionLevelsTemplates.CLASSIC);
    initFaction(category,"NORCROFTS","Men of the Norcrofts",new String[]{"Norcrofts"},FactionLevelsTemplates.CLASSIC);
    initFaction(category,"ENTWASH_VALE","Men of the Entwash Vale",new String[]{"Entwash Vale"},FactionLevelsTemplates.CLASSIC);
    initFaction(category,"SUTCROFTS","Men of the Sutcrofts",new String[]{"Sutcrofts"},FactionLevelsTemplates.CLASSIC);
    initFaction(category,"PEOPLE_WILDERMORE","People of Wildermore",null,FactionLevelsTemplates.CLASSIC);
    initFaction(category,"SURVIVORS_WILDERMORE","Survivors of Wildermore",null,FactionLevelsTemplates.CLASSIC);
    initFaction(category,"EORLINGAS","Eorlingas",new String[]{"The Eorlingas"},FactionLevelsTemplates.CLASSIC);
    initFaction(category,"HELMINGAS","Helmingas",new String[]{"The Helmingas"},FactionLevelsTemplates.CLASSIC);
    initFaction(category,"FANGORN","Ents of Fangorn Forest",new String[]{"The Ents of Fangorn Forest"},FactionLevelsTemplates.CLASSIC);
    category="Dol Amroth";
    initFaction(category,"DOL_AMROTH","Dol Amroth",null,FactionLevelsTemplates.CLASSIC);
    initFaction(category,"DA_ARMOURY","Dol Amroth – Armoury",null,FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_BANK","Dol Amroth – Bank",null,FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_DOCKS","Dol Amroth – Docks",null,FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_GREAT_HALL","Dol Amroth – Great Hall",null,FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_LIBRARY","Dol Amroth – Library",null,FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_MASON","Dol Amroth – Mason",null,FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_SWAN_KNIGHTS","Dol Amroth – Swan Knights",null,FactionLevelsTemplates.DOL_AMROTH);
    initFaction(category,"DA_WAREHOUSE","Dol Amroth – Warehouse",null,FactionLevelsTemplates.DOL_AMROTH);
    category="Gondor";
    // For all 3 Central Gondor regions: all levels gives no LP
    initFaction(category,"RINGLO_VALE","Men of Ringló Vale",null,FactionLevelsTemplates.CENTRAL_GONDOR); 
    initFaction(category,"DOR_EN_ERNIL","Men of Dor-en-Ernil",null,FactionLevelsTemplates.CENTRAL_GONDOR);
    initFaction(category,"LEBENNIN","Men of Lebennin",null,FactionLevelsTemplates.CENTRAL_GONDOR);
    initFaction(category,"PELARGIR","Pelargir",null,FactionLevelsTemplates.CLASSIC);
    initFaction(category,"RANGERS_ITHILIEN","Rangers of Ithilien",null,FactionLevelsTemplates.CLASSIC);
    initFaction(category,"MINAS_TIRITH","Defenders of Minas Tirith",null,FactionLevelsTemplates.EXTENDED_CLASSIC);
    initFaction(category,"RIDERS_ROHAN","Riders of Rohan",null,FactionLevelsTemplates.CLASSIC);
    category="Mordor";
    initFaction(category,"HOST_OF_THE_WEST","Host of the West",null,FactionLevelsTemplates.EXTENDED_CLASSIC);
    initFaction(category,"HOW_ARMOUR","Host of the West: Armour",null,FactionLevelsTemplates.HOW);
    initFaction(category,"HOW_PROVISIONS","Host of the West: Provisions",null,FactionLevelsTemplates.HOW);
    initFaction(category,"HOW_WEAPONS","Host of the West: Weapons",null,FactionLevelsTemplates.HOW);
    initFaction(category,"GORGOROTH","Conquest of Gorgoroth",null,FactionLevelsTemplates.GORGOROTH);
    category="Misc";
    Faction aleAssociation=initFaction(category,"ALE_ASSOCIATION","Ale Association",new String[]{"The Ale Association"},FactionLevelsTemplates.ALE_INN);
    aleAssociation.setInitialLevel(aleAssociation.getLevels()[1]);
    Faction innLeague=initFaction(category,"INN_LEAGUE","Inn League",new String[]{"The Inn League"},FactionLevelsTemplates.ALE_INN);
    innLeague.setInitialLevel(innLeague.getLevels()[1]);
    initFaction(category,"HOBNANIGANS","Chicken Chasing League of Eriador",null,FactionLevelsTemplates.HOBNANIGANS);
    // Guild
    _guildFaction=buildFaction(null,"GUILD","Guild",null,FactionLevelsTemplates.GUILD);
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
