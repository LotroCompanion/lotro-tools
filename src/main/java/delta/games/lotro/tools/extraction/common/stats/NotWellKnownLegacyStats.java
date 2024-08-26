package delta.games.lotro.tools.extraction.common.stats;

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
  MELEE_DEFENCE(),
  /**
   * Ranged Defence.
   */
  RANGED_DEFENCE(),
  /**
   * Tactical Defence.
   */
  TACTICAL_DEFENCE(),
  /**
   * Fire mitigation.
   */
  FIRE_MITIGATION(),
  /**
   * Frost mitigation rating.
   */
  FROST_MITIGATION(),
  /**
   * Acid mitigation rating.
   */
  ACID_MITIGATION(),
  /**
   * Shadow mitigation percentage.
   */
  SHADOW_MITIGATION(),
  /**
   * Item Wear Chance on Hit.
   */
  ITEM_WEAR_CHANCE_ON_HIT(),
  /**
   * Audacity.
   */
  AUDACITY(),
  /**
   * Stealth level.
   */
  STEALTH_LEVEL(),
  /**
   * Melee Defence (%).
   */
  MELEE_DEFENCE_PERCENTAGE(), // Incoming Melee Damage
  /**
   * Tactical Defence (%).
   */
  TACTICAL_DEFENCE_PERCENTAGE(), // Incoming Tactical Damage
  /**
   * Critical chance of ranged auto-attack (percentage).
   */
  RANGED_AUTO_ATTACKS_CRIT_CHANCE_PERCENTAGE(),
  /**
   * Devastate magnitude (percentage).
   */
  DEVASTATE_MAGNITUDE_PERCENTAGE(),
  /**
   * Tactical critical multiplier (percentage).
   */
  TACTICAL_CRITICAL_MULTIPLIER(),
  /**
   * Blade line AOE power cost (percentage).
   */
  BLADE_LINE_AOE_POWER_COST_PERCENTAGE(),
  /**
   * Strike skills power cost (percentage).
   */
  STRIKE_SKILLS_POWER_COST_PERCENTAGE(),
  /**
   * Blade line AOE skills power cost (percentage).
   */
  BLADE_AOE_SKILLS_POWER_COST_PERCENTAGE(),
  /**
   * Tricks power cost (percentage).
   */
  TRICKS_POWER_COST_PERCENTAGE(),
  /**
   * Sign of the wild skills power cost (percentage).
   */
  SIGN_OF_THE_WILD_POWER_COST_PERCENTAGE(),
  /**
   * Ballad and coda damage (percentage).
   */
  BALLAD_AND_CODA_DAMAGE_PERCENTAGE(),
  /**
   * Attack duration (percentage).
   */
  ATTACK_DURATION_PERCENTAGE(),
  /**
   * Jeweller critical chance (percentage).
   */
  JEWELLER_CRIT_CHANCE_PERCENTAGE(),
  /**
   * Cook critical chance (percentage).
   */
  COOK_CRIT_CHANCE_PERCENTAGE(),
  /**
   * Scholar critical chance (percentage).
   */
  SCHOLAR_CRIT_CHANCE_PERCENTAGE(),
  /**
   * Tailor critical chance (percentage).
   */
  TAILOR_CRIT_CHANCE_PERCENTAGE(),
  /**
   * Metalsmith critical chance (percentage).
   */
  METALSMITH_CRIT_CHANCE_PERCENTAGE(),
  /**
   * Weaponsmith critical chance (percentage).
   */
  WEAPONSMITH_CRIT_CHANCE_PERCENTAGE(),
  /**
   * Woodworker critical chance (percentage).
   */
  WOODWORKER_CRIT_CHANCE_PERCENTAGE(),
  /**
   * Prospector mining duration (seconds).
   */
  PROSPECTOR_MINING_DURATION(),
  /**
   * Farmer harvesting duration (seconds).
   */
  FARMER_MINING_DURATION(),
  /**
   * Forester chopping duration (seconds).
   */
  FORESTER_CHOPPING_DURATION(),
  /**
   * Scholar researching duration (seconds).
   */
  SCHOLAR_RESEARCHING_DURATION(),

  /**
   * Perceived threat (percentage).
   */
  PERCEIVED_THREAT(),
  /**
   * All skill induction (%).
   */
  ALL_SKILL_INDUCTION();

  /**
   * Get the name of this stat.
   * @return a stat name.
   */
  public String getKey()
  {
    return name();
  }
}
