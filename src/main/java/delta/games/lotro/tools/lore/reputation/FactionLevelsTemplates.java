package delta.games.lotro.tools.lore.reputation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.lore.reputation.FactionLevel;

/**
 * Templates for faction levels.
 * @author DAM
 */
public class FactionLevelsTemplates
{
  /**
   * Enemy.
   */
  public static final FactionLevel ENEMY=new FactionLevel("ENEMY","Enemy",-2,0,10000);
  /**
   * Outsider.
   */
  public static final FactionLevel OUTSIDER=new FactionLevel("OUTSIDER","Outsider",-1,0,10000);
  /**
   * Neutral.
   */
  public static final FactionLevel NEUTRAL=new FactionLevel("NEUTRAL","Neutral",0,0,0);
  /**
   * Acquaintance.
   */
  public static final FactionLevel ACQUAINTANCE=new FactionLevel("ACQUAINTANCE","Acquaintance",1,5,10000);
  /**
   * Friend.
   */
  public static final FactionLevel FRIEND=new FactionLevel("FRIEND","Friend",2,10,20000);
  /**
   * Ally.
   */
  public static final FactionLevel ALLY=new FactionLevel("ALLY","Ally",3,15,25000);
  /**
   * Kindred.
   */
  public static final FactionLevel KINDRED=new FactionLevel("KINDRED","Kindred",4,20,30000);
  /**
   * Respected.
   */
  public static final FactionLevel RESPECTED=new FactionLevel("RESPECTED","Respected",5,20,45000);
  /**
   * Honoured.
   */
  public static final FactionLevel HONOURED=new FactionLevel("HONOURED","Honoured",6,20,60000);
  /**
   * Celebrated.
   */
  public static final FactionLevel CELEBRATED=new FactionLevel("CELEBRATED","Celebrated",7,50,90000);

  /**
   * Classic faction.
   */
  public static final String CLASSIC="CLASSIC";
  /**
   * Host of the West.
   */
  public static final String HOW="HOW";
  /**
   * Central Gondor.
   */
  public static final String CENTRAL_GONDOR="CENTRAL_GONDOR";
  /**
   * Guild faction.
   */
  public static final String GUILD="GUILD";
  /**
   * Forochel faction.
   */
  public static final String FOROCHEL="FOROCHEL";
  /**
   * Dol Amroth Districts faction.
   */
  public static final String DOL_AMROTH="DOL_AMROTH";
  /**
   * Hobnanigans.
   */
  public static final String HOBNANIGANS="HOBNANIGANS";
  /**
   * Inn League/Ale Association.
   */
  public static final String ALE_INN="ALE_INN";
  /**
   * Gorgoroth faction.
   */
  public static final String GORGOROTH="GORGOROTH";
  /**
   * Extended-respected faction.
   */
  public static final String EXTENDED_RESPECTED="EXTENDED_RESPECTED";
  /**
   * Extended-classic faction.
   */
  public static final String EXTENDED_CLASSIC="EXTENDED_CLASSIC";

  private HashMap<String,FactionLevelsTemplate> _templates;

  /**
   * Constructor.
   */
  public FactionLevelsTemplates()
  {
    _templates=new HashMap<String,FactionLevelsTemplate>();
    init();
  }

  /**
   * Get a faction template by key.
   * @param key Key to use.
   * @return A template or <code>null</code> if not found.
   */
  public FactionLevelsTemplate getByKey(String key)
  {
    return _templates.get(key);
  }

  private void init()
  {
    register(buildClassic());
    register(buildHostOfTheWest());
    register(buildCentralGondor());
    register(buildForochel());
    register(buildRespected());
    register(buildExtendedClassic());
    register(buildGorgoroth());
    register(buildGuild());
    register(buildDolAmroth());
    register(buildHobnanigans());
    register(buildAleInn());
  }

  private void register(FactionLevelsTemplate template)
  {
    _templates.put(template.getKey(),template);
  }

  private FactionLevelsTemplate buildClassic()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(NEUTRAL);
    levels.add(ACQUAINTANCE);
    levels.add(FRIEND);
    levels.add(ALLY);
    levels.add(KINDRED);
    return new FactionLevelsTemplate(CLASSIC,levels);
  }

  private FactionLevelsTemplate buildHostOfTheWest()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(new FactionLevel("NONE","-",0,0,0));
    levels.add(new FactionLevel("INITIAL","Initial",1,0,10000));
    levels.add(new FactionLevel("INTERMEDIATE","Intermediate",2,5,20000));
    levels.add(new FactionLevel("ADVANCED","Advanced",3,10,25000));
    levels.add(new FactionLevel("FINAL","Final",4,15,30000));
    return new FactionLevelsTemplate(HOW,levels);
  }

  private FactionLevelsTemplate buildAleInn()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(ENEMY);
    levels.add(OUTSIDER);
    levels.add(NEUTRAL);
    levels.add(new FactionLevel("ACQUAINTANCE","Acquaintance",1,0,10000));
    levels.add(new FactionLevel("FRIEND","Friend",2,0,20000));
    levels.add(new FactionLevel("ALLY","Ally",3,0,25000));
    levels.add(new FactionLevel("KINDRED","Kindred",4,0,30000));
    return new FactionLevelsTemplate(ALE_INN,levels);
  }

  private FactionLevelsTemplate buildCentralGondor()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(NEUTRAL);
    levels.add(new FactionLevel("ACQUAINTANCE","Acquaintance",1,0,10000));
    levels.add(new FactionLevel("FRIEND","Friend",2,0,20000));
    levels.add(new FactionLevel("ALLY","Ally",3,0,25000));
    levels.add(new FactionLevel("KINDRED","Kindred",4,0,30000));
    return new FactionLevelsTemplate(CENTRAL_GONDOR,levels);
  }

  private FactionLevelsTemplate buildForochel()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(OUTSIDER);
    levels.add(new FactionLevel("NEUTRAL","Neutral",0,0,10000));
    levels.add(ACQUAINTANCE);
    levels.add(FRIEND);
    levels.add(ALLY);
    levels.add(KINDRED);
    return new FactionLevelsTemplate(FOROCHEL,levels);
  }

  private FactionLevelsTemplate buildRespected()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(NEUTRAL);
    levels.add(ACQUAINTANCE);
    levels.add(FRIEND);
    levels.add(ALLY);
    levels.add(KINDRED);
    levels.add(RESPECTED);
    return new FactionLevelsTemplate(EXTENDED_RESPECTED,levels);
  }

  private FactionLevelsTemplate buildExtendedClassic()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(NEUTRAL);
    levels.add(ACQUAINTANCE);
    levels.add(FRIEND);
    levels.add(ALLY);
    levels.add(KINDRED);
    levels.add(RESPECTED);
    levels.add(HONOURED);
    levels.add(CELEBRATED);
    return new FactionLevelsTemplate(EXTENDED_CLASSIC,levels);
  }

  private FactionLevelsTemplate buildGorgoroth()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(NEUTRAL);
    levels.add(ACQUAINTANCE);
    levels.add(FRIEND);
    levels.add(ALLY);
    levels.add(KINDRED);
    levels.add(RESPECTED);
    levels.add(HONOURED);
    levels.add(new FactionLevel("CELEBRATED_GORGOROTH","Celebrated",7,20,90000));
    return new FactionLevelsTemplate(GORGOROTH,levels);
  }

  private FactionLevelsTemplate buildGuild()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(new FactionLevel("INITIATE","Guild Initiate",0,0,0));
    levels.add(new FactionLevel("APPRENTICE","Apprentice of the Guild",1,0,10000));
    levels.add(new FactionLevel("JOURNEYMAN","Journeyman of the Guild",2,0,20000));
    levels.add(new FactionLevel("EXPERT","Expert of the Guild",3,0,25000));
    levels.add(new FactionLevel("ARTISAN","Artisan of the Guild",4,0,30000));
    levels.add(new FactionLevel("MASTER","Master of the Guild",5,0,45000));
    levels.add(new FactionLevel("EASTEMNET MASTER","Eastemnet Master of the Guild",6,0,60000));
    levels.add(new FactionLevel("WESTEMNET MASTER","Westemnet Master of the Guild",7,0,90000));
    return new FactionLevelsTemplate(GUILD,levels);
  }

  private FactionLevelsTemplate buildDolAmroth()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(NEUTRAL);
    levels.add(new FactionLevel("ACQUAINTANCE","Acquaintance",1,0,10000));
    return new FactionLevelsTemplate(DOL_AMROTH,levels);
  }

  private FactionLevelsTemplate buildHobnanigans()
  {
    List<FactionLevel> levels=new ArrayList<FactionLevel>();
    levels.add(new FactionLevel("ROOKIE","Rookie",0,0,0));
    levels.add(new FactionLevel("LEAGUER","Minor Leaguer",1,0,10000));
    levels.add(new FactionLevel("MAJOR_LEAGUER","Major Leaguer",2,0,20000));
    levels.add(new FactionLevel("ALL_STAR","All-star",3,0,25000));
    levels.add(new FactionLevel("HALL_OF_FAMER","Hall of Famer",4,0,30000));
    return new FactionLevelsTemplate(HOBNANIGANS,levels);
  }
}
