package delta.games.lotro.tools.dat.misc;

import org.apache.log4j.Logger;

import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatsRegistry;
import delta.games.lotro.dat.misc.Context;

/**
 * Adds legacy stats names to stat descriptions.
 * @author DAM
 */
public class StatMappings
{
  private static final Logger LOGGER=Logger.getLogger(StatMappings.class);

  private static StatsRegistry _stats;

  /**
   * Setup mappings between DAT stats and legacy stats.
   * @param stats Stats registry to update.
   */
  public static void setupMappings(StatsRegistry stats)
  {
    _stats=stats;
    boolean live=Context.isLive();
    //registerMapping("Combat_Agent_Armor_Value_Float","ARMOUR");
    registerMapping("Health_MaxLevel","MORALE");
    registerMapping("Power_MaxLevel","POWER");
    registerMapping("Stat_Might","MIGHT");
    registerMapping("Stat_Agility","AGILITY");
    registerMapping("Stat_Will","WILL");
    registerMapping("Stat_Vitality","VITALITY");
    registerMapping("Stat_Fate","FATE");
    if (live)
    {
      registerMapping("Combat_Class_CriticalPoints_Unified","CRITICAL_RATING");
    }
    registerMapping("Combat_Class_CriticalPoints_Unified","CRITICAL_RATING");
    registerMapping("Combat_SuperCritical_Magnitude_PercentAddMod","DEVASTATE_MAGNITUDE_PERCENTAGE");
    registerMapping("Combat_CriticalHitChanceAddMod","CRITICAL_MELEE_PERCENTAGE");
    registerMapping("Combat_RangedCriticalHitChanceAddMod","CRITICAL_RANGED_PERCENTAGE");
    registerMapping("Combat_MagicCriticalHitChanceAddMod","CRITICAL_TACTICAL_PERCENTAGE");
    registerMapping("Combat_FinessePoints_Modifier","FINESSE");
    registerMapping("LoE_Light_Modifier","LIGHT_OF_EARENDIL");
    registerMapping("Resist_ClassPoints_Resistance_TheOneResistance","RESISTANCE");
    registerMapping("Combat_DamageQualifier_Melee_Defense","MELEE_DEFENCE_PERCENTAGE");
    registerMapping("Combat_DamageQualifier_Magic_Defense","TACTICAL_DEFENCE_PERCENTAGE");
    registerMapping("Combat_DamageQualifier_Ranged_Defense","RANGED_DEFENCE_PERCENTAGE");
    registerMapping("Combat_Unified_Critical_Defense","CRITICAL_DEFENCE");
    registerMapping("Combat_Melee_Critical_Defense","MELEE_CRITICAL_DEFENCE");
    registerMapping("Combat_Ranged_Critical_Defense","RANGED_CRITICAL_DEFENCE");
    registerMapping("Combat_Tactical_Critical_Defense","TACTICAL_CRITICAL_DEFENCE");
    registerMapping("Combat_Unified_Critical_Defense_Percent_Mod","CRITICAL_DEFENCE_PERCENTAGE");
    registerMapping("Combat_BlockPoints_Modifier","BLOCK");
    registerMapping("Combat_BlockChanceModifier","BLOCK_PERCENTAGE");
    registerMapping("Combat_PartialBlock_PercentageAddMod","PARTIAL_BLOCK_PERCENTAGE");
    registerMapping("Combat_PartialBlock_Mitigation_Mod","PARTIAL_BLOCK_MITIGATION_PERCENTAGE");
    registerMapping("Combat_EvadePoints_Modifier","EVADE");
    registerMapping("Combat_EvadeChanceModifier","EVADE_PERCENTAGE");
    registerMapping("Combat_PartialEvade_PercentageAddMod","PARTIAL_EVADE_PERCENTAGE");
    registerMapping("Combat_PartialEvade_Mitigation_Mod","PARTIAL_EVADE_MITIGATION_PERCENTAGE");
    registerMapping("Combat_ParryPoints_Modifier","PARRY");
    registerMapping("Combat_ParryChanceModifier","PARRY_PERCENTAGE");
    registerMapping("Combat_PartialParry_PercentageAddMod","PARTIAL_PARRY_PERCENTAGE");
    registerMapping("Combat_PartialParry_Mitigation_Mod","PARTIAL_PARRY_MITIGATION_PERCENTAGE");
    registerMapping("Vital_PowerPeaceRegenAddMod","OCPR");
    registerMapping("Vital_PowerCombatRegenAddMod","ICPR");
    registerMapping("Vital_HealthPeaceRegenAddMod","OCMR");
    registerMapping("Vital_HealthCombatRegenAddMod","ICMR");
    registerMapping("Mood_Hope_Level","HOPE");
    registerMapping("Combat_ActionDurationMultiplierMod","ATTACK_DURATION_PERCENTAGE");
    registerMapping("Combat_DamageQualifier_Melee_Offense","MELEE_DAMAGE_PERCENTAGE");
    registerMapping("Combat_DamageQualifier_Magic_Offense","TACTICAL_DAMAGE_PERCENTAGE");
    registerMapping("Combat_DamageQualifier_Ranged_Offense","RANGED_DAMAGE_PERCENTAGE");
    registerMapping("Combat_IncomingHealing_Points_Current","INCOMING_HEALING");
    registerMapping("Combat_IncomingHealing_Modifier_Current","INCOMING_HEALING_PERCENTAGE");
    registerMapping("Combat_Modifier_OutgoingHealing_Points","OUTGOING_HEALING");
    registerMapping("Combat_Modifier_OutgoingHealing_Percent","OUTGOING_HEALING_PERCENTAGE");
    registerMapping("Combat_ArmorDefense_PointsModifier_UnifiedPhysical","PHYSICAL_MITIGATION");
    if (live)
    {
      registerMapping("Combat_MitigationPercentage_UnifiedPhysical","PHYSICAL_MITIGATION_PERCENTAGE");
    }
    else
    {
      registerMapping("Combat_MitigationPercentage_Common","PHYSICAL_MITIGATION_PERCENTAGE"); // Should be only for common damage type
    }
    registerMapping("Combat_ArmorDefense_PointsModifier_UnifiedTactical","TACTICAL_MITIGATION");
    registerMapping("Combat_MitigationPercentage_UnifiedTactical","TACTICAL_MITIGATION_PERCENTAGE");
    registerMapping("Combat_ArmorDefense_PointsModifier_Frost","FROST_MITIGATION");
    registerMapping("Combat_MitigationPercentage_Frost","FROST_MITIGATION_PERCENTAGE");
    registerMapping("Combat_ArmorDefense_PointsModifier_Acid","ACID_MITIGATION");
    registerMapping("Combat_MitigationPercentage_Acid","ACID_MITIGATION_PERCENTAGE");
    registerMapping("Combat_ArmorDefense_PointsModifier_Fire","FIRE_MITIGATION");
    registerMapping("Combat_MitigationPercentage_Fire","FIRE_MITIGATION_PERCENTAGE");
    registerMapping("Combat_ArmorDefense_PointsModifier_Shadow","SHADOW_MITIGATION");
    registerMapping("Combat_MitigationPercentage_Shadow","SHADOW_MITIGATION_PERCENTAGE");
    registerMapping("Combat_MitigationPercentage_Lightning","LIGHTNING_MITIGATION_PERCENTAGE");
    registerMapping("Combat_ArmorDefense_PointsModifier_OrcCraft","OCFW_MITIGATION");
    registerMapping("Combat_MitigationPercentage_OrcCraft","OCFW_MITIGATION_PERCENTAGE");
    registerMapping("Combat_PhysicalMastery_Modifier_Unified","PHYSICAL_MASTERY");
    registerMapping("Combat_TacticalMastery_Modifier_Unified","TACTICAL_MASTERY");
    //registerMapping("Combat_Class_PhysicalMastery_Unified","PHYSICAL_MASTERY");
    //registerMapping("Combat_Class_TacticalMastery_Unified","TACTICAL_MASTERY");
    registerMapping("Stealth_StealthLevelModifier","STEALTH_LEVEL");
    registerMapping("Craft_Weaponsmith_CriticalChanceAddModifier","WEAPONSMITH_CRIT_CHANCE_PERCENTAGE");
    registerMapping("Craft_Metalsmith_CriticalChanceAddModifier","METALSMITH_CRIT_CHANCE_PERCENTAGE");
    registerMapping("Craft_Woodworker_CriticalChanceAddModifier","WOODWORKER_CRIT_CHANCE_PERCENTAGE");
    registerMapping("Craft_Cook_CriticalChanceAddModifier","COOK_CRIT_CHANCE_PERCENTAGE");
    registerMapping("Craft_Scholar_CriticalChanceAddModifier","SCHOLAR_CRIT_CHANCE_PERCENTAGE");
    registerMapping("Craft_Jeweller_CriticalChanceAddModifier","JEWELLER_CRIT_CHANCE_PERCENTAGE");
    registerMapping("Craft_Tailor_CriticalChanceAddModifier","TAILOR_CRIT_CHANCE_PERCENTAGE");
    registerMapping("Skill_InductionDuration_MiningMod","PROSPECTOR_MINING_DURATION");
    registerMapping("Skill_InductionDuration_WoodHarvestMod","FORESTER_CHOPPING_DURATION");
    registerMapping("Skill_InductionDuration_FarmHarvestMod","FARMER_MINING_DURATION");
    registerMapping("Skill_InductionDuration_ResearchingMod","SCHOLAR_RESEARCHING_DURATION");
    registerMapping("MountEndurance_MaxLevel","WARSTEED_ENDURANCE");
    registerMapping("MountPower_MaxLevel","WARSTEED_POWER");
    registerMapping("Stat_MountAgility","WARSTEED_AGILITY");
    registerMapping("Stat_MountStrength","WARSTEED_STRENGTH");
    registerMapping("ItemWear_ChanceMod_CombatHit","ITEM_WEAR_CHANCE_ON_HIT");
    registerMapping("Skill_DamageMultiplier_LightintheDark","BALLAD_AND_CODA_DAMAGE_PERCENTAGE");
    registerMapping("Combat_EffectCriticalMultiplierMod","TACTICAL_CRITICAL_MULTIPLIER");
    registerMapping("Trait_PvMP_BattleRank","AUDACITY");
    registerMapping("Skill_VitalCost_Champion_AOEMod","BLADE_LINE_AOE_POWER_COST_PERCENTAGE");
    registerMapping("Skill_VitalCost_Champion_StrikeMod","STRIKE_SKILLS_POWER_COST_PERCENTAGE");
    registerMapping("Burglar_TricksPowerReduce","TRICKS_POWER_COST_PERCENTAGE");
    registerMapping("Skill_VitalCost_Book_SigilAnimalMod","SIGN_OF_THE_WILD_POWER_COST_PERCENTAGE");
    registerMapping("TotalThreatModifier_Player","PERCEIVED_THREAT");
    registerMapping("Skill_InductionDuration_AllSkillsMod","ALL_SKILL_INDUCTION");
    registerMapping("Combat_Current_DefensePoints_Melee","MELEE_DEFENCE");
    registerMapping("Combat_Current_DefensePoints_Ranged","RANGED_DEFENCE");
    registerMapping("Combat_Current_DefensePoints_Tactical","TACTICAL_DEFENCE");
    registerMapping("Skill_AutoAttackCriticalHitChance","RANGED_AUTO_ATTACKS_CRIT_CHANCE_PERCENTAGE");
  }

  private static void registerMapping(String gameKey, String legacyKey)
  {
    StatDescription oldStat=_stats.getByKey(legacyKey);
    if (oldStat!=null)
    {
      LOGGER.warn("Legacy key already used: "+legacyKey);
      return;
    }
    StatDescription stat=_stats.getByKey(gameKey);
    if (stat!=null)
    {
      // Add legacy key
      stat.setLegacyKey(legacyKey);
      // Remove/add to update maps with the new legacy key
      _stats.removeStat(stat);
      _stats.addStat(stat);
    }
    else
    {
      System.out.println("Stat not found: "+gameKey);
    }
  }
}
