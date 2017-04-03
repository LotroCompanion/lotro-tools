package delta.games.lotro.tools.lore.items.lotroplan;

import delta.common.utils.NumericTools;
import delta.games.lotro.character.stats.Slice;
import delta.games.lotro.common.stats.ScaledArmourComputer;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.ItemQuality;

/**
 * Stats computer.
 * @author DAM
 */
public class StatsComputer
{
  private static final String CALCSLICE_SEED="=CALCSLICE(";
  private static final String CALCSLICE_END=")";

  private static ScaledArmourComputer _armorComputer=new ScaledArmourComputer();

  /**
   * Get a stat value.
   * @param itemLevel Item level.
   * @param formula Formula string.
   * @return A double value or <code>null</code> if formula was not understood.
   */
  public static Double getValue(int itemLevel, String formula)
  {
    Object[] params=splitFormula(formula);
    if (params!=null)
    {
      String statName=(String)params[0];
      Float sliceCount=(Float)params[1];
      double value=getSliceValue(statName,itemLevel,sliceCount.floatValue());
      //return Double.valueOf(value);
      // For the moment, round value to keep computed values the same as old, non computed values
      return Double.valueOf(Math.round(value));
    }
    return null;
  }


  private static Object[] splitFormula(String formula)
  {
    Object[] ret=null;
    if ((formula.startsWith(CALCSLICE_SEED)) && (formula.endsWith(CALCSLICE_END)))
    {
      String paramsStr=formula.substring(CALCSLICE_SEED.length());
      paramsStr=paramsStr.substring(0,paramsStr.length()-CALCSLICE_END.length());
      String[] params=paramsStr.split(";");
      if ((params.length==2) || (params.length==3))
      {
        String statName=params[0];
        //String itemLevelCell=params[1];
        String sliceCountStr=(params.length==3) ? params[2] : "1";

        if ((statName.startsWith("\"")) && (statName.endsWith("\"")))
        {
          statName=statName.substring(1,statName.length()-1);
          sliceCountStr=sliceCountStr.replace(',','.').trim();
          Float sliceCount=NumericTools.parseFloat(sliceCountStr);
          if (sliceCount!=null)
          {
            ret=new Object[2];
            ret[0]=statName;
            ret[1]=sliceCount;
          }
        }
      }
    }
    return ret;
  }

  private static double getSliceValue(String statName, int itemLevel, float sliceCount)
  {
    if ("Might".equals(statName)) {
      return Slice.getBaseStat(itemLevel,sliceCount);
    } else if ("Agility".equals(statName)) {
      return Slice.getBaseStat(itemLevel,sliceCount);
    } else if ("Will".equals(statName)) {
      return Slice.getBaseStat(itemLevel,sliceCount);
    } else if ("Vitality".equals(statName)) {
      return Slice.getBaseStat(itemLevel,sliceCount);
    } else if ("Fate".equals(statName)) {
      return Slice.getBaseStat(itemLevel,sliceCount);
    } else if ("Morale".equals(statName)) {
      return Slice.getMorale(itemLevel,sliceCount);
    } else if ("Power".equals(statName)) {
      return Slice.getPower(itemLevel,sliceCount);
    } else if ("PhyMas".equals(statName)) {
      return Slice.getPhysicalMastery(itemLevel,sliceCount);
    } else if ("TacMas".equals(statName)) {
      return Slice.getTacticalMastery(itemLevel,sliceCount);
    } else if ("CritHit".equals(statName)) {
      return Slice.getCriticalRating(itemLevel,sliceCount);
    } else if ("Finesse".equals(statName)) {
      return Slice.getFinesse(itemLevel,sliceCount);
    } else if ("InHeal".equals(statName)) {
      return Slice.getIncomingHealing(itemLevel,sliceCount);
    } else if ("Resist".equals(statName)) {
      return Slice.getResist(itemLevel,sliceCount);
    } else if ("Block".equals(statName)) {
      return Slice.getBPE(itemLevel,sliceCount);
    } else if ("Parry".equals(statName)) {
      return Slice.getBPE(itemLevel,sliceCount);
    } else if ("Evade".equals(statName)) {
      return Slice.getBPE(itemLevel,sliceCount);
    } else if ("PhyMit".equals(statName)) {
      return Slice.getPhysicalMitigation(itemLevel,sliceCount);
    } else if ("TacMit".equals(statName)) {
      return Slice.getTacticalMitigation(itemLevel,sliceCount);
    } else if ("CritDef".equals(statName)) {
      return Slice.getCriticalDefence(itemLevel,sliceCount);
    } else if (statName.contains("Arm")) {
      return getArmorStat(statName.toUpperCase(),itemLevel,sliceCount);
    } else {
      System.out.println("Unmanaged stat: " + statName);
    }
    return 0;
  }

  private static double getArmorStat(String statName, int itemLevel, float sliceCount)
  {
    String armorClass=statName.substring(3,4);
    String armorType=statName.substring(4,5);
    String armorColor=statName.substring(5,6);
    if (("S".equals(armorType)) && ("H".equals(armorColor)))
    {
      armorType="SH";
      armorColor=statName.substring(6,7);
    }
    if (("C".equals(armorClass)) && ("L".equals(armorType)))
    {
      armorClass="L";
      armorType="CL";
      armorColor=statName.substring(5,6);
    }
    EquipmentLocation slot=null;
    if ("H".equals(armorType)) slot=EquipmentLocation.HEAD;
    if ("S".equals(armorType)) slot=EquipmentLocation.SHOULDER;
    if ("C".equals(armorType)) slot=EquipmentLocation.CHEST;
    if ("G".equals(armorType)) slot=EquipmentLocation.HAND;
    if ("L".equals(armorType)) slot=EquipmentLocation.LEGS;
    if ("B".equals(armorType)) slot=EquipmentLocation.FEET;
    if ("SH".equals(armorType)) slot=EquipmentLocation.OFF_HAND;
    if ("CL".equals(armorType)) slot=EquipmentLocation.BACK;

    ArmourType type=null;
    if ("H".equals(armorClass)) type=ArmourType.HEAVY;
    if ("M".equals(armorClass)) type=ArmourType.MEDIUM;
    if ("L".equals(armorClass)) type=ArmourType.LIGHT;

    ItemQuality quality=null;
    if ("G".equals(armorColor)) quality=ItemQuality.LEGENDARY;
    if ("P".equals(armorColor)) quality=ItemQuality.RARE;
    if ("T".equals(armorColor)) quality=ItemQuality.INCOMPARABLE;
    if ("Y".equals(armorColor)) quality=ItemQuality.UNCOMMON;

    if ((slot!=null) && (type!=null) && (quality!=null))
    {
      return _armorComputer.getArmour(itemLevel,type,slot,quality,sliceCount);
    }
    System.out.println("Unmanaged armor type:" + statName);
    return 0;
  }
}
