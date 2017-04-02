package delta.games.lotro.tools.lore.items.lotroplan;

import java.util.HashMap;
import java.util.Map;

import delta.common.utils.NumericTools;
import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;

/**
 * Lotro plan stats table.
 * @author DAM
 */
public class LotroPlanTable
{
  /**
   * Index of the 'name' column.
   */
  public static final int NAME_INDEX=0;
  /**
   * Index of the 'item level' column.
   */
  public static final int ITEM_LEVEL_INDEX=1;
  /**
   * Index of the 'armour' column.
   */
  public static final int ARMOUR_INDEX=2;
  private static final int FIRST_STAT_INDEX=3; // Might
  /**
   * Index of the 'notes' column.
   */
  public static final int NOTES=28;
  private static final int AUDACITY_INDEX=26;
  private static final int HOPE_INDEX=27;
  private static final int PARRY_PERCENTAGE_INDEX=53;
  private static final int RANGED_DEFENCE_PERCENTAGE_INDEX=62;
  // All other stats are unused
  //Name  iLvl  Armour  Might Agility Vitality  Will  Fate  Morale  Power ICMR  NCMR  ICPR  NCPR  CritHit Finesse PhyMas  TacMas  Resist  CritDef InHeal  Block Parry Evade PhyMit  TacMit  Audacity  Hope  Notes ArmourP MoraleP PowerP  MelCritP  RngCritP  TacCritP  HealCritP MelMagnP  RngMagnP  TacMagnP  HealMagnP MelDmgP RngDmgP TacDmgP OutHealP  MelIndP RngIndP TacIndP HealIndP  AttDurP RunSpdP CritDefP  InHealP BlockP  ParryP  EvadeP  PblkP PparP PevaP PblkMitP  PparMitP  PevaMitP  MelRedP RngRedP TacRedP PhyMitP TacMitP

  private static final STAT[] STATS={ STAT.MIGHT, STAT.AGILITY, STAT.VITALITY, STAT.WILL, STAT.FATE, STAT.MORALE, STAT.POWER,
    STAT.ICMR, STAT.OCMR, STAT.ICPR, STAT.OCPR, STAT.CRITICAL_RATING, STAT.FINESSE, STAT.PHYSICAL_MASTERY, STAT.TACTICAL_MASTERY,
    STAT.RESISTANCE, STAT.CRITICAL_DEFENCE, STAT.INCOMING_HEALING, STAT.BLOCK, STAT.PARRY, STAT.EVADE,
    STAT.PHYSICAL_MITIGATION, STAT.TACTICAL_MITIGATION
  };

  private HashMap<Integer,STAT> _mapIndexToStat;

  /**
   * Constructor.
   */
  public LotroPlanTable()
  {
    _mapIndexToStat=new HashMap<Integer,STAT>();
    //_map=new HashMap<String,IntegerHolder>();
    for(int i=0;i<STATS.length;i++)
    {
      _mapIndexToStat.put(Integer.valueOf(i+FIRST_STAT_INDEX),STATS[i]);
    }
    _mapIndexToStat.put(Integer.valueOf(AUDACITY_INDEX),STAT.AUDACITY);
    _mapIndexToStat.put(Integer.valueOf(HOPE_INDEX),STAT.HOPE);
    _mapIndexToStat.put(Integer.valueOf(PARRY_PERCENTAGE_INDEX),STAT.PARRY_PERCENTAGE);
    _mapIndexToStat.put(Integer.valueOf(RANGED_DEFENCE_PERCENTAGE_INDEX),STAT.RANGED_DEFENCE_PERCENTAGE);
  }

  /**
   * Load stats from fields.
   * @param fields Fields to read.
   * @return A set of stats.
   */
  public BasicStatsSet loadStats(String[] fields)
  {
    BasicStatsSet stats=new BasicStatsSet();
    for(Map.Entry<Integer,STAT> entry : _mapIndexToStat.entrySet())
    {
      int index=entry.getKey().intValue();
      if (index>=fields.length) continue;
      String valueStr=fields[index];
      if (valueStr.endsWith("%"))
      {
        valueStr=valueStr.substring(0,valueStr.length()-1);
        valueStr=valueStr.replace(',','.');
        Float statValue=NumericTools.parseFloat(valueStr);
        if (statValue!=null)
        {
          stats.setStat(entry.getValue(),statValue.floatValue());
        }
      }
      else if (valueStr.contains("CALCSLICE"))
      {
        int itemLevel=NumericTools.parseInt(fields[ITEM_LEVEL_INDEX],-1);
        Double statValue=StatsComputer.getValue(itemLevel,valueStr);
        if (statValue!=null)
        {
          stats.setStat(entry.getValue(),statValue.floatValue());
        }
        else
        {
          // TODO warning
        }
      }
      else
      {
        boolean isPercent=false;
        if (valueStr.startsWith("="))
        {
          valueStr=valueStr.substring(1);
          isPercent=true;
        }
        valueStr=valueStr.replace(',','.').trim();
        if (valueStr.contains("."))
        {
          Float statValue=NumericTools.parseFloat(valueStr);
          if (statValue!=null)
          {
            float value=statValue.floatValue();
            if (isPercent)
            {
              value*=100;
            }
            stats.setStat(entry.getValue(),value);
          }
        }
        else
        {
          Integer statValue=NumericTools.parseInteger(valueStr);
          if (statValue!=null)
          {
            int value=statValue.intValue();
            if (isPercent)
            {
              value*=100;
            }
            stats.setStat(entry.getValue(),value);
          }
        }
      }
    }
    return stats;
  }
}
