package delta.games.lotro.tools.dat.factions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Templates for faction levels.
 * @author DAM
 */
public class FactionLevelTemplates
{
  /**
   * Enemy.
   */
  private static final String ENEMY="ENEMY";
  /**
   * Outsider.
   */
  private static final String OUTSIDER="OUTSIDER";
  /**
   * Neutral.
   */
  private static final String NEUTRAL="NEUTRAL";
  /**
   * Acquaintance.
   */
  private static final String ACQUAINTANCE="ACQUAINTANCE";
  /**
   * Friend.
   */
  public static final String FRIEND="FRIEND";
  /**
   * Ally.
   */
  private static final String ALLY="ALLY";
  /**
   * Kindred.
   */
  private static final String KINDRED="KINDRED";
  /**
   * Respected.
   */
  private static final String RESPECTED="RESPECTED";
  /**
   * Honoured.
   */
  private static final String HONOURED="HONOURED";
  /**
   * Celebrated.
   */
  private static final String CELEBRATED="CELEBRATED";

  /**
   * Classic faction.
   */
  public static final String CLASSIC="CLASSIC";
  /**
   * Host of the West.
   */
  public static final String HOW="HOW";
  /**
   * Guild faction.
   */
  public static final String GUILD="GUILD";
  /**
   * Dol Amroth Districts faction.
   */
  public static final String DOL_AMROTH="DOL_AMROTH";
  /**
   * Hobnanigans.
   */
  public static final String HOBNANIGANS="HOBNANIGANS";
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

  private HashMap<String,List<String>> _templates;

  /**
   * Constructor.
   */
  public FactionLevelTemplates()
  {
    _templates=new HashMap<String,List<String>>();
    init();
  }

  /**
   * Get a faction template by key.
   * @param key Key to use.
   * @return A template or <code>null</code> if not found.
   */
  public List<String> getByKey(String key)
  {
    return _templates.get(key);
  }

  private void init()
  {
    register(CLASSIC,buildClassic());
    register(HOW,buildHostOfTheWest());
    register(EXTENDED_RESPECTED,buildRespected());
    register(EXTENDED_CLASSIC,buildExtendedClassic());
    register(GORGOROTH,buildGorgoroth());
    register(GUILD,buildGuild());
    register(DOL_AMROTH,buildDolAmroth());
    register(HOBNANIGANS,buildHobnanigans());
  }

  private void register(String key, List<String> levelKeys)
  {
    _templates.put(key,levelKeys);
  }

  private List<String> buildClassic()
  {
    List<String> levels=new ArrayList<String>();
    levels.add(ENEMY);
    levels.add(OUTSIDER);
    levels.add(NEUTRAL);
    levels.add(ACQUAINTANCE);
    levels.add(FRIEND);
    levels.add(ALLY);
    levels.add(KINDRED);
    return levels;
  }

  private List<String> buildHostOfTheWest()
  {
    List<String> levels=new ArrayList<String>();
    levels.add(ENEMY);
    levels.add(OUTSIDER);
    levels.add("NONE");
    levels.add("INITIAL");
    levels.add("INTERMEDIATE");
    levels.add("ADVANCED");
    levels.add("FINAL");
    return levels;
  }

  private List<String> buildRespected()
  {
    List<String> levels=new ArrayList<String>();
    levels.add(ENEMY);
    levels.add(OUTSIDER);
    levels.add(NEUTRAL);
    levels.add(ACQUAINTANCE);
    levels.add(FRIEND);
    levels.add(ALLY);
    levels.add(KINDRED);
    levels.add(RESPECTED);
    return levels;
  }

  private List<String> buildExtendedClassic()
  {
    List<String> levels=new ArrayList<String>();
    levels.add(ENEMY);
    levels.add(OUTSIDER);
    levels.add(NEUTRAL);
    levels.add(ACQUAINTANCE);
    levels.add(FRIEND);
    levels.add(ALLY);
    levels.add(KINDRED);
    levels.add(RESPECTED);
    levels.add(HONOURED);
    levels.add(CELEBRATED);
    return levels;
  }

  private List<String> buildGorgoroth()
  {
    List<String> levels=new ArrayList<String>();
    levels.add(ENEMY);
    levels.add(OUTSIDER);
    levels.add(NEUTRAL);
    levels.add(ACQUAINTANCE);
    levels.add(FRIEND);
    levels.add(ALLY);
    levels.add(KINDRED);
    levels.add(RESPECTED);
    levels.add(HONOURED);
    levels.add("CELEBRATED_GORGOROTH");
    return levels;
  }

  private List<String> buildGuild()
  {
    List<String> levels=new ArrayList<String>();
    levels.add(null);
    levels.add(null);
    levels.add("INITIATE");
    levels.add("APPRENTICE");
    levels.add("JOURNEYMAN");
    levels.add("EXPERT");
    levels.add("ARTISAN");
    levels.add("MASTER");
    levels.add("EASTEMNET MASTER");
    levels.add("WESTEMNET MASTER");
    levels.add("MINAS ITHIL MASTER");
    return levels;
  }

  private List<String> buildDolAmroth()
  {
    List<String> levels=new ArrayList<String>();
    levels.add(ENEMY);
    levels.add(OUTSIDER);
    levels.add(NEUTRAL);
    levels.add(ACQUAINTANCE);
    return levels;
  }

  private List<String> buildHobnanigans()
  {
    List<String> levels=new ArrayList<String>();
    levels.add(null);
    levels.add(null);
    levels.add("ROOKIE");
    levels.add("LEAGUER");
    levels.add("MAJOR_LEAGUER");
    levels.add("ALL_STAR");
    levels.add("HALL_OF_FAMER");
    return levels;
  }
}
