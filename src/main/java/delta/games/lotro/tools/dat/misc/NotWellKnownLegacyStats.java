package delta.games.lotro.tools.dat.misc;

/**
 * Enum that contains all legacy stats that are not referenced in WellKnownStat
 * (not used in stat computations).
 * @author DAM
 */
public enum NotWellKnownLegacyStats
{
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
   * Fire mitigation.
   */
  FIRE_MITIGATION("Fire Mitigation"),
  /**
   * Frost mitigation rating.
   */
  FROST_MITIGATION("Frost Mitigation"),
  /**
   * Acid mitigation rating.
   */
  ACID_MITIGATION("Acid Mitigation"),
  /**
   * Shadow mitigation percentage.
   */
  SHADOW_MITIGATION("Shadow Mitigation"),
  /**
   * Item Wear Chance on Hit.
   */
  ITEM_WEAR_CHANCE_ON_HIT("Item Wear Chance on Hit",true),
  /**
   * Audacity.
   */
  AUDACITY("Audacity"),
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
   * Blade line AOE skills power cost (percentage).
   */
  BLADE_AOE_SKILLS_POWER_COST_PERCENTAGE("Blade Line AOE Skills Power Cost %",true),
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
  ALL_SKILL_INDUCTION("All Skill Induction (%)",true);

  private String _name;

  private NotWellKnownLegacyStats(String name, String... aliases)
  {
    _name=name;
  }

  private NotWellKnownLegacyStats(String name, boolean isPercentage, String... aliases)
  {
    _name=name;
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
