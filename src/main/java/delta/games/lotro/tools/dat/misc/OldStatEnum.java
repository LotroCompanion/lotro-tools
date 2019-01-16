package delta.games.lotro.tools.dat.misc;

import java.util.HashMap;

/**
 * A LOTRO character stat.
 * @author DAM
 */
public enum OldStatEnum
{
  /**
   * Morale.
   */
  MORALE("Morale","Maximum Morale"),
  /**
   * Power.
   */
  POWER("Power","Maximum Power"),
  /**
   * Armour.
   */
  ARMOUR("Armour"),
  /**
   * Might.
   */
  MIGHT("Might"),
  /**
   * Agility.
   */
  AGILITY("Agility"),
  /**
   * Vitality.
   */
  VITALITY("Vitality"),
  /**
   * Will.
   */
  WILL("Will"),
  /**
   * Fate.
   */
  FATE("Fate"),
  // Offence
  /**
   * Critical rating.
   */
  CRITICAL_RATING("Critical Rating", "CRITICAL_HIT", "Critical hit"),
  /**
   * Critical % (melee).
   */
  CRITICAL_MELEE_PERCENTAGE("Critical % (melee)",true),
  /**
   * Critical % (ranged).
   */
  CRITICAL_RANGED_PERCENTAGE("Critical % (ranged)",true),
  /**
   * Critical % (tactical).
   */
  CRITICAL_TACTICAL_PERCENTAGE("Critical % (tactical)",true),
  /**
   * Devastate % (melee).
   */
  DEVASTATE_MELEE_PERCENTAGE("Devastate % (melee)",true),
  /**
   * Critical % (ranged).
   */
  DEVASTATE_RANGED_PERCENTAGE("Devastate % (ranged)",true),
  /**
   * Critical % (tactical).
   */
  DEVASTATE_TACTICAL_PERCENTAGE("Devastate % (tactical)",true),
  /**
   * Cri&Devastate Magnitude % (melee).
   */
  CRIT_DEVASTATE_MAGNITUDE_MELEE_PERCENTAGE("Critical & Devastate Magnitude % (melee)",true),
  /**
   * Cri&Devastate Magnitude % (ranged).
   */
  CRIT_DEVASTATE_MAGNITUDE_RANGED_PERCENTAGE("Critical & Devastate Magnitude % (ranged)",true),
  /**
   * Cri&Devastate Magnitude % (tactical).
   */
  CRIT_DEVASTATE_MAGNITUDE_TACTICAL_PERCENTAGE("Critical & Devastate Magnitude % (tactical)",true),
  /**
   * Finesse.
   */
  FINESSE("Finesse"),
  /**
   * Finesse %.
   */
  FINESSE_PERCENTAGE("Finesse %",true),
  /**
   * Physical Mastery.
   */
  PHYSICAL_MASTERY("Physical Mastery","Physical Mastery Rating"),
  /**
   * Melee Damage %.
   */
  MELEE_DAMAGE_PERCENTAGE("Melee Damage %",true),
  /**
   * Ranged Damage %.
   */
  RANGED_DAMAGE_PERCENTAGE("Ranged Damage %",true),
  /**
   * Tactical Mastery.
   */
  TACTICAL_MASTERY("Tactical Mastery","Tactical Mastery Rating"),
  /**
   * Tactical Damage %.
   */
  TACTICAL_DAMAGE_PERCENTAGE("Tactical Damage %",true),
  /**
   * Outgoing Healing Rating.
   */
  OUTGOING_HEALING("Outgoing Healing",false),
  /**
   * Outgoing Healing %.
   */
  OUTGOING_HEALING_PERCENTAGE("Outgoing Healing %",true),
  // Defence
  /**
   * Resistance.
   */
  RESISTANCE("Resistance", "Resist"),
  /**
   * Resistance %.
   */
  RESISTANCE_PERCENTAGE("Resistance %",true),
  /**
   * Melee Defence.
   */
  MELEE_DEFENCE("Melee Defence", "Melee Defence Rating"),
  /**
   * Ranged Defence.
   */
  RANGED_DEFENCE("Ranged Defence", "Ranged Defence Rating"),
  /**
   * Tactical Defence.
   */
  TACTICAL_DEFENCE("Tactical Defence", "Tactical Defence Rating"),
  /**
   * Critical Defence.
   */
  CRITICAL_DEFENCE("Critical Defence", "CRITICAL_AVOID", "Critical avoidance", "Critical Defense Rating"),
  /**
   * Critical Defence % (melee/ranged/tactical).
   */
  CRITICAL_DEFENCE_PERCENTAGE("Critical Defence %",true),
  /**
   * Critical Defence % (melee).
   */
  MELEE_CRITICAL_DEFENCE("Melee Critical Defence %",true),
  /**
   * Critical Defence % (ranged).
   */
  RANGED_CRITICAL_DEFENCE("Ranged Critical Defence %",true),
  /**
   * Critical Defence % (tactical).
   */
  TACTICAL_CRITICAL_DEFENCE("Tactical Critical Defence %",true),
  /**
   * Incoming Healing.
   */
  INCOMING_HEALING("Incoming Healing","Incoming Healing Rating"),
  /**
   * Incoming Healing percentage.
   */
  INCOMING_HEALING_PERCENTAGE("Incoming Healing %",true),
  // Avoidance
  /**
   * Block.
   */
  BLOCK("Block", "Block Rating"),
  /**
   * Block (percentage).
   */
  BLOCK_PERCENTAGE("Block %",true),
  /**
   * Partial Block (percentage).
   */
  PARTIAL_BLOCK_PERCENTAGE("Partial Block %",true),
  /**
   * Partial Block Mitigation (percentage).
   */
  PARTIAL_BLOCK_MITIGATION_PERCENTAGE("Partial Block Mitigation %",true,"Partial Block Mitigation"),
  /**
   * Parry.
   */
  PARRY("Parry", "Parry Rating"),
  /**
   * Parry (percentage).
   */
  PARRY_PERCENTAGE("Parry %",true),
  /**
   * Partial Parry (percentage).
   */
  PARTIAL_PARRY_PERCENTAGE("Partial Parry %",true),
  /**
   * Partial Parry Mitigation (percentage).
   */
  PARTIAL_PARRY_MITIGATION_PERCENTAGE("Partial Parry Mitigation %",true,"Partial Parry Mitigation"),
  /**
   * Evade.
   */
  EVADE("Evade", "Evade Rating"),
  /**
   * Evade (percentage).
   */
  EVADE_PERCENTAGE("Evade %",true),
  /**
   * Partial Evade (percentage).
   */
  PARTIAL_EVADE_PERCENTAGE("Partial Evade %",true),
  /**
   * Partial Evade Mitigation (percentage).
   */
  PARTIAL_EVADE_MITIGATION_PERCENTAGE("Partial Evade Mitigation %",true,"Partial Evade Mitigation"),
  // Mitigations
  // Damage Source: Melee, Ranged, Tactical
  // Damage Type: Physical Mitigation, Tactical Mitigation
  /**
   * Physical mitigation.
   */
  PHYSICAL_MITIGATION("Physical Mitigation", "Physical mitigation", "PhyMit"),
  /**
   * Physical mitigation percentage.
   */
  PHYSICAL_MITIGATION_PERCENTAGE("Physical Mitigation %",true),
  /**
   * Orc-craft and Fell-wrought mitigation.
   */
  OCFW_MITIGATION("Orc-craft/Fell-wrought Mitigation"),
  /**
   * Orc-craft and Fell-wrought mitigation percentage.
   */
  OCFW_MITIGATION_PERCENTAGE("Orc-craft/Fell-wrought Mitigation %",true),
  /**
   * Tactical mitigation.
   */
  TACTICAL_MITIGATION("Tactical Mitigation", "Tactical mitigation", "TacMit"),
  /**
   * Tactical mitigation percentage.
   */
  TACTICAL_MITIGATION_PERCENTAGE("Tactical Mitigation %",true),
  /**
   * Fire mitigation.
   */
  FIRE_MITIGATION("Fire Mitigation"),
  /**
   * Fire mitigation percentage.
   */
  FIRE_MITIGATION_PERCENTAGE("Fire Mitigation %",true),
  /**
   * Lightning mitigation percentage.
   */
  LIGHTNING_MITIGATION_PERCENTAGE("Lightning Mitigation %",true),
  /**
   * Frost mitigation rating.
   */
  FROST_MITIGATION("Frost Mitigation"),
  /**
   * Frost mitigation percentage.
   */
  FROST_MITIGATION_PERCENTAGE("Frost Mitigation %",true),
  /**
   * Acid mitigation rating.
   */
  ACID_MITIGATION("Acid Mitigation"),
  /**
   * Acid mitigation percentage.
   */
  ACID_MITIGATION_PERCENTAGE("Acid Mitigation %",true),
  /**
   * Shadow mitigation percentage.
   */
  SHADOW_MITIGATION("Shadow Mitigation"),
  /**
   * Shadow mitigation percentage.
   */
  SHADOW_MITIGATION_PERCENTAGE("Shadow Mitigation %",true),
  /**
   * non-Combat Morale Regeneration.
   */
  OCMR("Non-Combat Morale Regeneration", "NCMR"),
  /**
   * In-Combat Morale Regeneration.
   */
  ICMR("In-Combat Morale Regeneration", "in-Combat Morale Regen"),
  /**
   * non-Combat Power Regeneration.
   */
  OCPR("Non-Combat Power Regeneration", "NCPR"),
  /**
   * In-Combat Power Regeneration.
   */
  ICPR("In-Combat Power Regeneration", "in-Combat Power Regen"),
  /**
   * Item Wear Chance on Hit.
   */
  ITEM_WEAR_CHANCE_ON_HIT("Item Wear Chance on Hit",true),
  /**
   * Audacity.
   */
  AUDACITY("Audacity"),
  /**
   * Hope.
   */
  HOPE("Hope"),
  /**
   * Light of Eärendil.
   */
  LIGHT_OF_EARENDIL("Light of Eärendil"),
  /**
   * Stealth level.
   */
  STEALTH_LEVEL("Stealth Level"),
  /**
   * Melee Defence (%).
   */
  MELEE_DEFENCE_PERCENTAGE("Melee Defence %", true), // Incoming Melee Damage
  /**
   * Tactical Defence (%).
   */
  TACTICAL_DEFENCE_PERCENTAGE("Tactical Defence %", true), // Incoming Tactical Damage
  /**
   * Ranged Defence (%).
   */
  RANGED_DEFENCE_PERCENTAGE("Ranged Defence %", true, "Ranged Defence"), // Incoming Ranged Damage
  /**
   * Critical chance of ranged auto-attack (percentage).
   */
  RANGED_AUTO_ATTACKS_CRIT_CHANCE_PERCENTAGE("Ranged Auto-attacks Critical Chance %"),
  /**
   * Devastate magnitude (percentage).
   */
  DEVASTATE_MAGNITUDE_PERCENTAGE("Devastate Magnitude %",true,"Devastate Magnitude"),
  /**
   * Tactical critical multiplier (percentage).
   */
  TACTICAL_CRITICAL_MULTIPLIER("Tactical Critical Multiplier %",true),
  /**
   * Blade line AOE power cost (percentage).
   */
  BLADE_LINE_AOE_POWER_COST_PERCENTAGE("Blade Line AOE Power Cost %",true),
  /**
   * Strike skills power cost (percentage).
   */
  STRIKE_SKILLS_POWER_COST_PERCENTAGE("Strike Skills Power Cost %",true),
  /**
   * Tricks power cost (percentage).
   */
  TRICKS_POWER_COST_PERCENTAGE("Tricks Power Cost %",true),
  /**
   * Sign of the wild skills power cost (percentage).
   */
  SIGN_OF_THE_WILD_POWER_COST_PERCENTAGE("Sign of the Wild Skills Power Cost %",true),
  /**
   * Ballad and coda damage (percentage).
   */
  BALLAD_AND_CODA_DAMAGE_PERCENTAGE("Ballad and Coda Damage %",true),
  /**
   * Attack duration (percentage).
   */
  ATTACK_DURATION_PERCENTAGE("Attack Duration %",true,"Attack Duration"),
  /**
   * Jeweller critical chance (percentage).
   */
  JEWELLER_CRIT_CHANCE_PERCENTAGE("Jeweller Critical Chance %",true),
  /**
   * Cook critical chance (percentage).
   */
  COOK_CRIT_CHANCE_PERCENTAGE("Cook Critical Chance %",true),
  /**
   * Scholar critical chance (percentage).
   */
  SCHOLAR_CRIT_CHANCE_PERCENTAGE("Scholar Critical Chance %",true),
  /**
   * Tailor critical chance (percentage).
   */
  TAILOR_CRIT_CHANCE_PERCENTAGE("Tailor Critical Chance %",true),
  /**
   * Metalsmith critical chance (percentage).
   */
  METALSMITH_CRIT_CHANCE_PERCENTAGE("Metalsmith Critical Chance %",true),
  /**
   * Weaponsmith critical chance (percentage).
   */
  WEAPONSMITH_CRIT_CHANCE_PERCENTAGE("Weaponsmith Critical Chance %",true),
  /**
   * Woodworker critical chance (percentage).
   */
  WOODWORKER_CRIT_CHANCE_PERCENTAGE("Woodworker Critical Chance %",true),
  /**
   * Prospector mining duration (seconds).
   */
  PROSPECTOR_MINING_DURATION("Prospector Mining Duration (s)"),
  /**
   * Farmer harvesting duration (seconds).
   */
  FARMER_MINING_DURATION("Farmer Harvesting Duration (s)"),
  /**
   * Forester chopping duration (seconds).
   */
  FORESTER_CHOPPING_DURATION("Forester Chopping Duration (s)"),
  /**
   * Scholar researching duration (seconds).
   */
  SCHOLAR_RESEARCHING_DURATION("Scholar Researching Duration (s)"),

  /**
   * Perceived threat (percentage).
   */
  PERCEIVED_THREAT("Perceived Threat (%)",true),
  /**
   * All skill induction (%).
   */
  ALL_SKILL_INDUCTION("All Skill Induction (%)",true),
  /**
   * War-steed Endurance.
   */
  WARSTEED_ENDURANCE("War-steed Endurance"),
  /**
   * War-steed Power.
   */
  WARSTEED_POWER("War-steed Power"),
  /**
   * War-steed Agility.
   */
  WARSTEED_AGILITY("War-steed Agility"),
  /**
   * War-steed Strength.
   */
  WARSTEED_STRENGTH("War-steed Strength");

  private String _name;
  private String[] _aliases;

  private static HashMap<String,OldStatEnum> _map=new HashMap<String,OldStatEnum>();

  static
  {
    for (OldStatEnum stat : OldStatEnum.values())
    {
      stat.register();
    }
  }

  private OldStatEnum(String name, String... aliases)
  {
    _name=name;
    _aliases=aliases;
  }

  private OldStatEnum(String name, boolean isPercentage, String... aliases)
  {
    _name=name;
    _aliases=aliases;
  }

  private void register() {
    _map.put(_name,this);
    _map.put(name(),this);
    if (_aliases!=null)
    {
      for(String alias : _aliases)
      {
        _map.put(alias,this);
      }
    }
  }

  /**
   * Get a stat by name.
   * @param name Name to use.
   * @return A stat instance or <code>null</code> if not found.
   */
  public static OldStatEnum getByName(String name)
  {
    return _map.get(name);
  }

  /**
   * Get the name of this stat.
   * @return a stat name.
   */
  public String getKey()
  {
    return name();
  }

  /**
   * Get the name of this stat.
   * @return a stat name.
   */
  public String getName()
  {
    return _name;
  }
}
