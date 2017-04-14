package delta.games.lotro.tools.lore.items.lotroplan;

import delta.common.utils.NumericTools;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.stats.ItemStatSliceData;

/**
 * Parser for slice formulas.
 * @author DAM
 */
public class SliceFormulaParser
{
  private static final String CALCSLICE_SEED="=CALCSLICE(";
  private static final String CALCSLICE_END=")";

  /**
   * Parse formula.
   * @param formula Formula string.
   * @return Slice parameters or <code>null</code>.
   */
  public static ItemStatSliceData parse(String formula)
  {
    ItemStatSliceData ret=null;
    if ((formula.startsWith(CALCSLICE_SEED)) && (formula.endsWith(CALCSLICE_END)))
    {
      String paramsStr=formula.substring(CALCSLICE_SEED.length());
      paramsStr=paramsStr.substring(0,paramsStr.length()-CALCSLICE_END.length());
      String[] params=paramsStr.split(";");
      if ((params.length==2) || (params.length==3))
      {
        String statName=params[0];
        //String itemLevelCell=params[1];
        String sliceCountStr=(params.length==3) ? params[2] : null;

        if ((statName.startsWith("\"")) && (statName.endsWith("\"")))
        {
          statName=statName.substring(1,statName.length()-1);
          Float sliceCount=null;
          if (sliceCountStr!=null)
          {
            sliceCountStr=sliceCountStr.replace(',','.').trim();
            sliceCount=NumericTools.parseFloat(sliceCountStr);
          }
          STAT stat=getStatFromStatName(statName);
          String additionalParameter=null;
          if (stat==STAT.ARMOUR)
          {
            additionalParameter=statName;
          }
          if (stat!=null)
          {
            ret=new ItemStatSliceData(stat,sliceCount,additionalParameter);
          }
        }
      }
    }
    return ret;
  }

  private static STAT getStatFromStatName(String statName)
  {
    if ("Might".equals(statName)) return STAT.MIGHT;
    if ("Agility".equals(statName)) return STAT.AGILITY;
    if ("Will".equals(statName)) return STAT.WILL;
    if ("Vitality".equals(statName)) return STAT.VITALITY;
    if ("Fate".equals(statName)) return STAT.FATE;
    if ("Morale".equals(statName)) return STAT.MORALE;
    if ("Power".equals(statName)) return STAT.POWER;
    if ("PhyMas".equals(statName)) return STAT.PHYSICAL_MASTERY;
    if ("TacMas".equals(statName)) return STAT.TACTICAL_MASTERY;
    if ("CritHit".equals(statName)) return STAT.CRITICAL_RATING;
    if ("Finesse".equals(statName)) return STAT.FINESSE;
    if ("InHeal".equals(statName)) return STAT.INCOMING_HEALING;
    if ("Resist".equals(statName)) return STAT.RESISTANCE;
    if ("Block".equals(statName)) return STAT.BLOCK;
    if ("Parry".equals(statName)) return STAT.PARRY;
    if ("Evade".equals(statName)) return STAT.EVADE;
    if ("PhyMit".equals(statName)) return STAT.PHYSICAL_MITIGATION;
    if ("TacMit".equals(statName)) return STAT.TACTICAL_MITIGATION;
    if ("CritDef".equals(statName)) return STAT.CRITICAL_DEFENCE;
    if (statName.contains("Arm")) return STAT.ARMOUR;
    return null;
  }
}
