package delta.games.lotro.tools.lore.items.tulkas;

import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.lore.items.EquipmentLocation;

/**
 * Constants for Tulkas database.
 * @author DAM
 */
public class TulkasConstants
{
  /**
   * Name of bonuses.
   */
  public static String[] BONUS_NAMES=new String[] {
    "???", // 0
    "Maximum Morale", // 1
    "in-Combat Morale Regen", // 2
    "non-Combat Morale Regen", // 3
    "Maximum Power", // 4
    "in-Combat Power Regen", // 5
    "non-Combat Power Regen", // 6
    "Might", // 7
    "Agility",  // 8
    "Vitality", // 9
    "Will",     // 10
    "Fate",     // 11
    "Critical Rating", // 12
    "Finesse Rating", // 13
    "Physical Mastery Rating", // 14
    "Tactical Mastery Rating", // 15
    "Resistance Rating", // 16
    "Critical Defence", // 17
    "Incoming Healing Rating",
    "Block Rating",
    "Parry Rating",
    "Evade Rating",
    "Melee Defence",
    "Ranged Defence", // 23
    "Tactical Defence",
    "Physical Mitigation",
    "Tactical Mitigation",
    "Audacity", // 27
    "Stealth Level",
    "Tactical Critical Multiplier", // 29
    // Not in version 2:
    "Ranged Offence Rating",
    "All Skill Inductions"
  };

  /**
   * Name of bonuses.
   */
  public static STAT[] STATS=new STAT[] {
    null, //"???", // 0
    STAT.MORALE, // 1
    STAT.ICMR, // 2
    STAT.OCMR, // 3
    STAT.POWER, // 4
    STAT.ICPR, // 5
    STAT.OCPR, // 6
    STAT.MIGHT, // 7
    STAT.AGILITY,  // 8
    STAT.VITALITY, // 9
    STAT.WILL,     // 10
    STAT.FATE,     // 11
    STAT.CRITICAL_RATING, // 12
    STAT.FINESSE, // 13
    STAT.PHYSICAL_MASTERY, // 14
    STAT.TACTICAL_MASTERY, // 15
    STAT.RESISTANCE, // 16
    STAT.CRITICAL_DEFENCE, // 17
    STAT.INCOMING_HEALING,
    STAT.BLOCK,
    STAT.PARRY,
    STAT.EVADE,
    null, //"Melee Defence",
    STAT.RANGED_DEFENCE_PERCENTAGE, // 23
    null, //"Tactical Defence",
    STAT.PHYSICAL_MITIGATION,
    STAT.TACTICAL_MITIGATION,
    STAT.AUDACITY, // 27
    STAT.STEALTH_LEVEL,
    STAT.TACTICAL_CRITICAL_MULTIPLIER, // 29
    // Not in version 2:
    null, //"Ranged Offence Rating",
    STAT.ALL_SKILL_INDUCTION
  };

  /**
   * Indicates if this equipment location is for an armor.
   * @param loc Location to test.
   * @return <code>true</code> if it is, <code>false</code> otherwise.
   */
  public static  boolean isArmor(EquipmentLocation loc)
  {
    if (loc==EquipmentLocation.HEAD) return true;
    if (loc==EquipmentLocation.HAND) return true;
    if (loc==EquipmentLocation.CHEST) return true;
    if (loc==EquipmentLocation.BACK) return true;
    if (loc==EquipmentLocation.LEGS) return true;
    if (loc==EquipmentLocation.FEET) return true;
    if (loc==EquipmentLocation.OFF_HAND) return true;
    if (loc==EquipmentLocation.SHOULDER) return true;
    return false;
  }
}
