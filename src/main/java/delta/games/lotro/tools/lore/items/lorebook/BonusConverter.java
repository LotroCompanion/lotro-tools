package delta.games.lotro.tools.lore.items.lorebook;

import java.util.HashMap;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.tools.lore.items.lorebook.bonus.Bonus;
import delta.games.lotro.tools.lore.items.lorebook.bonus.Bonus.BONUS_OCCURRENCE;
import delta.games.lotro.tools.lore.items.lorebook.bonus.BonusManager;
import delta.games.lotro.tools.lore.items.lorebook.bonus.BonusType;
import delta.games.lotro.tools.lore.items.tulkas.TulkasValuesUtils;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Bonus converter: converts bonus items to stats.
 * @author DAM
 */
public class BonusConverter
{
  private HashMap<BonusType,STAT> _map;

  /**
   * Constructor.
   */
  public BonusConverter()
  {
    initMap();
  }

  private void initMap()
  {
    _map=new HashMap<BonusType,STAT>();
    _map.put(BonusType.MAX_MORALE,STAT.MORALE);
    _map.put(BonusType.ICMR,STAT.ICPR);
    _map.put(BonusType.NCMR,STAT.OCMR);
    _map.put(BonusType.MAX_POWER,STAT.POWER);
    _map.put(BonusType.ICPR,STAT.ICPR);
    _map.put(BonusType.NCPR,STAT.OCPR);
    _map.put(BonusType.MIGHT,STAT.MIGHT);
    _map.put(BonusType.AGILITY,STAT.AGILITY);
    _map.put(BonusType.VITALITY,STAT.VITALITY);
    _map.put(BonusType.WILL,STAT.WILL);
    _map.put(BonusType.FATE,STAT.FATE);
    _map.put(BonusType.CRIT_RATING,STAT.CRITICAL_RATING);
    _map.put(BonusType.FINESSE,STAT.FINESSE);
    _map.put(BonusType.PHYSICAL_MASTERY,STAT.PHYSICAL_MASTERY);
    _map.put(BonusType.TACTICAL_MASTERY,STAT.TACTICAL_MASTERY);
    _map.put(BonusType.RESISTANCE,STAT.RESISTANCE);
    _map.put(BonusType.CRITICAL_DEFENCE,STAT.CRITICAL_DEFENCE);
    _map.put(BonusType.INCOMING_HEALING,STAT.INCOMING_HEALING);
    _map.put(BonusType.BLOCK,STAT.BLOCK);
    
    _map.put(BonusType.PARRY,STAT.PARRY);
    _map.put(BonusType.EVADE,STAT.EVADE);
    _map.put(BonusType.PHYSICAL_MIT,STAT.PHYSICAL_MITIGATION);
    _map.put(BonusType.TACTICAL_MIT,STAT.TACTICAL_MITIGATION);
    _map.put(BonusType.AUDACITY,STAT.AUDACITY);
    _map.put(BonusType.STEALTH_LEVEL,STAT.STEALTH_LEVEL);
    _map.put(BonusType.TACTICAL_CRIT_MULTIPLIER,STAT.TACTICAL_CRITICAL_MULTIPLIER);
    _map.put(BonusType.ALL_SKILL_INDUCTIONS,STAT.ALL_SKILL_INDUCTION);

    // Not mapped:
    /*
    public static BonusType FELLOWSHIP_MANOEUVRE_MIGHT=new BonusType("FELLOWSHIP_MANOEUVRE_MIGHT","Fellowship Manoeuvre Damage from Might",VALUE_CLASS.PERCENTAGE);
    public static BonusType FELLOWSHIP_MANOEUVRE_GUILE=new BonusType("FELLOWSHIP_MANOEUVRE_GUILE","Fellowship Manoeuvre Damage from Guile",VALUE_CLASS.PERCENTAGE);
    public static BonusType MELEE_CRIT_RATING=new BonusType("MELEE_CRIT_RATING","Melee Critical Rating",VALUE_CLASS.INTEGER);
    public static BonusType RANGED_CRIT_RATING=new BonusType("RANGED_CRIT_RATING","Ranged Critical Rating",VALUE_CLASS.INTEGER);
    public static BonusType MELEE_DEFENCE=new BonusType("MELEE_DEFENCE","Melee Defence Rating",VALUE_CLASS.INTEGER);
    public static BonusType RANGED_DEFENCE=new BonusType("RANGED_DEFENCE","Ranged Defence",VALUE_CLASS.INTEGER);
    public static BonusType TACTICAL_DEFENCE=new BonusType("TACTICAL_DEFENCE","Tactical Defence",VALUE_CLASS.INTEGER);
    public static BonusType MELEE_OFFENCE=new BonusType("MELEE_OFFENCE","Melee Offence Rating",VALUE_CLASS.INTEGER);
    public static BonusType RANGED_OFFENCE=new BonusType("RANGED_OFFENCE","Ranged Offence Rating",VALUE_CLASS.INTEGER);
    public static BonusType OTHER=new BonusType("OTHER","Other",VALUE_CLASS.STRING);
    */
  }

  /**
   * Get stats from a bonus manager.
   * @param bonusMgr Bonus manager.
   * @return A set of stats.
   */
  public BasicStatsSet getStats(BonusManager bonusMgr)
  {
    BasicStatsSet stats=new BasicStatsSet();
    int nbBonus=bonusMgr.getNumberOfBonus();
    for(int i=0;i<nbBonus;i++)
    {
      Bonus bonus=bonusMgr.getBonusAt(i);
      parseBonus(bonus,stats);
    }
    return stats;
  }

  private void parseBonus(Bonus bonus, BasicStatsSet stats)
  {
    BONUS_OCCURRENCE occurrence=bonus.getBonusOccurrence();
    if (occurrence==BONUS_OCCURRENCE.ALWAYS)
    {
      BonusType type=bonus.getBonusType();
      if (type!=BonusType.OTHER)
      {
        STAT stat=getStatFromBonusType(type);
        if (stat!=null)
        {
          Object value=bonus.getValue();
          FixedDecimalsInteger statValue=TulkasValuesUtils.fromObjectValue(value);
          stats.addStat(stat,statValue);
          // TODO remove bonus from manager
        }
        else
        {
          System.out.println("Ignored: "+bonus);
        }
      }
    }
    else
    {
      //System.out.println("Ignored: "+bonus);
    }
  }

  private STAT getStatFromBonusType(BonusType type)
  {
    STAT ret=_map.get(type);
    return ret;
  }
}
