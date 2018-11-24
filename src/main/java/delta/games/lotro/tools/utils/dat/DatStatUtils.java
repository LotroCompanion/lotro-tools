package delta.games.lotro.tools.utils.dat;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.character.stats.STAT;
import delta.games.lotro.common.progression.ProgressionsManager;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.RangedStatProvider;
import delta.games.lotro.common.stats.ScalableStatProvider;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatUtils;
import delta.games.lotro.common.stats.TieredScalableStatProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.utils.FixedDecimalsInteger;
import delta.games.lotro.utils.maths.Progression;

/**
 * Utility methods related to stats from DAT files.
 * @author DAM
 */
public class DatStatUtils
{
  private static final Logger LOGGER=Logger.getLogger(DatStatUtils.class);

  private static ProgressionsManager _progressions=ProgressionsManager.getInstance();

  /**
   * Load a set of stats from some properties.
   * @param facade Data facade.
   * @param properties Properties to use to get stats.
   * @return A possibly empty, but not <code>null</code> list of stats providers.
   */
  public static List<StatProvider> buildStatProviders(DataFacade facade, PropertiesSet properties)
  {
    List<StatProvider> ret=new ArrayList<StatProvider>();
    Object[] mods=(Object[])properties.getProperty("Mod_Array");
    if (mods!=null)
    {
      for(int i=0;i<mods.length;i++)
      {
        PropertiesSet statProperties=(PropertiesSet)mods[i];
        Integer statId=(Integer)statProperties.getProperty("Mod_Modified");
        PropertyDefinition def=facade.getPropertiesRegistry().getPropertyDef(statId.intValue());
        STAT stat=DatStatUtils.getStatFromName(def.getName());
        if (stat!=null)
        {
          Number value=null;
          // Always 7 for "add"?
          //Integer modOp=(Integer)statProperties.getProperty("Mod_Op");
          Integer progressId=(Integer)statProperties.getProperty("Mod_Progression");
          if (progressId!=null)
          {
            StatProvider provider=buildStatProvider(facade,stat,progressId.intValue());

            Integer minLevel=(Integer)statProperties.getProperty("Mod_ProgressionFloor");
            Integer maxLevel=(Integer)statProperties.getProperty("Mod_ProgressionCeiling");
            if ((minLevel!=null) || (maxLevel!=null))
            {
              RangedStatProvider rangedProvider=new RangedStatProvider(provider,minLevel,maxLevel);
              ret.add(rangedProvider);
            }
            else
            {
              ret.add(provider);
            }
          }
          else
          {
            value=(Number)statProperties.getProperty(def.getName());
            if (value!=null)
            {
              float statValue=StatUtils.fixStatValue(stat,value.floatValue());
              if (Math.abs(statValue)>0.001)
              {
                ConstantStatProvider constantStat=new ConstantStatProvider(stat,statValue);
                ret.add(constantStat);
              }
            }
            else
            {
              LOGGER.warn("No progression ID and no direct value...");
            }
          }
        }
      }
    }
    return ret;
  }

  /**
   * Load a set of stats from some properties.
   * @param level Level to use.
   * @param facade Data facade.
   * @param properties Properties to use to get stats.
   * @return A set of stats or <code>null</code> if not enough data.
   */
  public static BasicStatsSet loadStats(int level, DataFacade facade, PropertiesSet properties)
  {
    List<StatProvider> providers=buildStatProviders(facade,properties);
    BasicStatsSet ret=new BasicStatsSet();
    for(StatProvider provider : providers)
    {
      Float statValue=provider.getStatValue(1,level);
      if (statValue!=null)
      {
        STAT stat=provider.getStat();
        float value=statValue.floatValue();
        ret.addStat(stat,new FixedDecimalsInteger(value));
      }
    }
    return ret;
  }

  /**
   * Get a progression curve.
   * @param facade Data facade.
   * @param progressId Progression ID.
   * @return A progression curve or <code>null</code> if not found.
   */
  private static Progression getProgression(DataFacade facade, int progressId)
  {
    Progression ret=_progressions.getProgression(progressId);
    if (ret==null)
    {
      int progressPropertiesId=progressId+0x9000000;
      PropertiesSet progressProperties=facade.loadProperties(progressPropertiesId);
      if (progressProperties!=null)
      {
        ret=ProgressionFactory.buildProgression(progressId, progressProperties);
        if (ret!=null)
        {
          _progressions.registerProgression(progressId,ret);
        }
      }
    }
    return ret;
  }

  /**
   * Build a stat provider from the given progression identifier.
   * @param facade Data facade.
   * @param stat Targeted stat.
   * @param progressId Progression ID.
   * @return A stat provider.
   */
  public static StatProvider buildStatProvider(DataFacade facade, STAT stat, int progressId)
  {
    PropertiesSet properties=facade.loadProperties(progressId+0x9000000);
    Object[] progressionIds=(Object[])properties.getProperty("DataIDProgression_Array");
    if (progressionIds!=null)
    {
      return getTieredProgression(facade,stat,properties);
    }
    Progression progression=getProgression(facade,progressId);
    if (progression==null)
    {
      progression=ProgressionFactory.buildProgression(progressId, properties);
    }
    ScalableStatProvider scalableStat=new ScalableStatProvider(stat,progression);
    return scalableStat;
  }

  /**
   * Get a progression curve.
   * @param facade Data facade.
   * @param stat Involved stat.
   * @param properties Progression properties.
   * @return A progression curve or <code>null</code> if not found.
   */
  private static TieredScalableStatProvider getTieredProgression(DataFacade facade, STAT stat, PropertiesSet properties)
  {
    Object[] progressionIds=(Object[])properties.getProperty("DataIDProgression_Array");
    int nbTiers=progressionIds.length;
    TieredScalableStatProvider ret=new TieredScalableStatProvider(stat,nbTiers);
    int tier=1;
    for(Object progressionIdObj : progressionIds)
    {
      int progressionId=((Integer)progressionIdObj).intValue();
      Progression progression=getProgression(facade,progressionId);
      ret.setProgression(tier,progression);
      tier++;
    }
    return ret;
  }

  /**
   * Get a stat from a stat property name.
   * @param name Name of property.
   * @return A stat or <code>null</code> if not found/not supported.
   */
  public static STAT getStatFromName(String name)
  {
    if ("Health_MaxLevel".equals(name)) return STAT.MORALE;
    if ("Power_MaxLevel".equals(name)) return STAT.POWER;
    if ("Stat_Might".equals(name)) return STAT.MIGHT;
    if ("Stat_Agility".equals(name)) return STAT.AGILITY;
    if ("Stat_Will".equals(name)) return STAT.WILL;
    if ("Stat_Vitality".equals(name)) return STAT.VITALITY;
    if ("Stat_Fate".equals(name)) return STAT.FATE;
    if ("Combat_Class_CriticalPoints_Unified".equals(name)) return STAT.CRITICAL_RATING;
    if ("Combat_SuperCritical_Magnitude_PercentAddMod".equals(name)) return STAT.DEVASTATE_MAGNITUDE_PERCENTAGE;
    if ("Combat_CriticalHitChanceAddMod".equals(name)) return STAT.CRITICAL_MELEE_PERCENTAGE;
    if ("Combat_RangedCriticalHitChanceAddMod".equals(name)) return STAT.CRITICAL_RANGED_PERCENTAGE;
    if ("Combat_MagicCriticalHitChanceAddMod".equals(name)) return STAT.CRITICAL_TACTICAL_PERCENTAGE;
    if ("Combat_FinessePoints_Modifier".equals(name)) return STAT.FINESSE;
    if ("LoE_Light_Modifier".equals(name)) return STAT.LIGHT_OF_EARENDIL;
    if ("Resist_ClassPoints_Resistance_TheOneResistance".equals(name)) return STAT.RESISTANCE;
    if ("Combat_Unified_Critical_Defense".equals(name)) return STAT.CRITICAL_DEFENCE;
    // Combat_Unified_Critical_Defense_Percent_Mod for melee/ranged/tactical crit defense % (Exacting Wards)
    if ("Combat_BlockPoints_Modifier".equals(name)) return STAT.BLOCK;
    if ("Combat_BlockChanceModifier".equals(name)) return STAT.BLOCK_PERCENTAGE;
    if ("Combat_PartialBlock_Mitigation_Mod".equals(name)) return STAT.PARTIAL_BLOCK_MITIGATION_PERCENTAGE;
    if ("Combat_EvadePoints_Modifier".equals(name)) return STAT.EVADE;
    if ("Combat_EvadeChanceModifier".equals(name)) return STAT.EVADE_PERCENTAGE;
    if ("Combat_PartialEvade_Mitigation_Mod".equals(name)) return STAT.PARTIAL_EVADE_MITIGATION_PERCENTAGE;
    if ("Combat_ParryPoints_Modifier".equals(name)) return STAT.PARRY;
    if ("Combat_ParryChanceModifier".equals(name)) return STAT.PARRY_PERCENTAGE;
    if ("Combat_PartialParry_Mitigation_Mod".equals(name)) return STAT.PARTIAL_PARRY_MITIGATION_PERCENTAGE;
    if ("Vital_PowerPeaceRegenAddMod".equals(name)) return STAT.OCPR;
    if ("Vital_PowerCombatRegenAddMod".equals(name)) return STAT.ICPR;
    if ("Vital_HealthPeaceRegenAddMod".equals(name)) return STAT.OCMR;
    if ("Vital_HealthCombatRegenAddMod".equals(name)) return STAT.ICMR;
    if ("Mood_Hope_Level".equals(name)) return STAT.HOPE;
    if ("Combat_ActionDurationMultiplierMod".equals(name)) return STAT.ATTACK_DURATION_PERCENTAGE;
    if ("Combat_DamageQualifier_Melee_Offense".equals(name)) return STAT.MELEE_DAMAGE_PERCENTAGE;
    if ("Combat_DamageQualifier_Magic_Offense".equals(name)) return STAT.TACTICAL_DAMAGE_PERCENTAGE;
    if ("Combat_IncomingHealing_Points_Current".equals(name)) return STAT.INCOMING_HEALING;
    if ("Combat_IncomingHealing_Modifier_Current".equals(name)) return STAT.INCOMING_HEALING_PERCENTAGE;
    if ("Combat_Modifier_OutgoingHealing_Points".equals(name)) return STAT.OUTGOING_HEALING;
    if ("Combat_ArmorDefense_PointsModifier_UnifiedPhysical".equals(name)) return STAT.PHYSICAL_MITIGATION;
    if ("Combat_MitigationPercentage_Common".equals(name)) return STAT.PHYSICAL_MITIGATION_PERCENTAGE;
    if ("Combat_ArmorDefense_PointsModifier_UnifiedTactical".equals(name)) return STAT.TACTICAL_MITIGATION;
    if ("Combat_ArmorDefense_PointsModifier_Frost".equals(name)) return STAT.FROST_MITIGATION;
    if ("Combat_ArmorDefense_PointsModifier_Acid".equals(name)) return STAT.ACID_MITIGATION;
    if ("Combat_ArmorDefense_PointsModifier_Fire".equals(name)) return STAT.FIRE_MITIGATION;
    if ("Combat_ArmorDefense_PointsModifier_Shadow".equals(name)) return STAT.SHADOW_MITIGATION;
    if ("Combat_MitigationPercentage_Shadow".equals(name)) return STAT.SHADOW_MITIGATION_PERCENTAGE;
    if ("Combat_PhysicalMastery_Modifier_Unified".equals(name)) return STAT.PHYSICAL_MASTERY;
    if ("Combat_TacticalMastery_Modifier_Unified".equals(name)) return STAT.TACTICAL_MASTERY;
    if ("Combat_Class_PhysicalMastery_Unified".equals(name)) return STAT.PHYSICAL_MASTERY;
    if ("Combat_Class_TacticalMastery_Unified".equals(name)) return STAT.TACTICAL_MASTERY;
    if ("Stealth_StealthLevelModifier".equals(name)) return STAT.STEALTH_LEVEL;
    if ("Craft_Weaponsmith_CriticalChanceAddModifier".equals(name)) return STAT.WEAPONSMITH_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Metalsmith_CriticalChanceAddModifier".equals(name)) return STAT.METALSMITH_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Woodworker_CriticalChanceAddModifier".equals(name)) return STAT.WOODWORKER_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Cook_CriticalChanceAddModifier".equals(name)) return STAT.COOK_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Scholar_CriticalChanceAddModifier".equals(name)) return STAT.SCHOLAR_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Jeweller_CriticalChanceAddModifier".equals(name)) return STAT.JEWELLER_CRIT_CHANCE_PERCENTAGE;
    if ("Craft_Tailor_CriticalChanceAddModifier".equals(name)) return STAT.TAILOR_CRIT_CHANCE_PERCENTAGE;
    if ("Skill_InductionDuration_MiningMod".equals(name)) return STAT.PROSPECTOR_MINING_DURATION;
    if ("Skill_InductionDuration_WoodHarvestMod".equals(name)) return STAT.FORESTER_CHOPPING_DURATION;
    if ("Skill_InductionDuration_FarmHarvestMod".equals(name)) return STAT.FARMER_MINING_DURATION;
    if ("Combat_EffectCriticalMultiplierMod".equals(name)) return STAT.TACTICAL_CRITICAL_MULTIPLIER;

    if ("Skill_HealingMultiplier_Item".equals(name)) return null; // +N% Healing
    if ("Trait_Minstrel_Healing_CriticalMod".equals(name)) return null; // Critical Healing Magnitude

    // Reduces ranged skill induction time
    if ("Skill_InductionDuration_AllSkillsMod".equals(name)) return null;
    // Healing skills power cost
    if ("Skill_VitalCost_LifeSingerMod".equals(name)) return null;

    // Armour value for Herald??? itemId=1879053134,5,6,7
    if ("Trait_Runekeeper_All_Resistance".equals(name)) return null;
    if ("Trait_Loremaster_PetModStat_Slot2".equals(name)) return null;

    // 10% discount at most Ered Luin shops
    if ("Discount_Eredluin".equals(name)) return null;
    // 10% discount at most Bree-land shops
    if ("Discount_Breeland".equals(name)) return null;
    // 10% discount at most Trollshaws shops
    if ("Discount_Trollshaws".equals(name)) return null;
    // 10% discount at most North-down shops
    if ("Discount_Northdowns".equals(name)) return null;

    // Item wear chance on hit
    if ("ItemWear_ChanceMod_CombatHit".equals(name)) return STAT.ITEM_WEAR_CHANCE_ON_HIT;
    // Critical rating, reloaded?
    if ("Combat_CriticalPoints_Modifier_Unified".equals(name)) return null;

    // Minstrel
    if ("Skill_DamageMultiplier_LightintheDark".equals(name)) return STAT.BALLAD_AND_CODA_DAMAGE_PERCENTAGE;

    // Unsupported class specifics
    if ("Itemset_Application_Warden_BoarsRush_Daze".equals(name)) return null;
    if ("Item_Minstrel_SongofSoothing_HealingDebuff".equals(name)) return null;
    if ("Itemset_Application_Burglar_SubtleStab_Debuff".equals(name)) return null;
    if ("Itemset_Runekeeper_CeaselessArgument_SlowRate".equals(name)) return null;
    if ("Itemset_Application_Loremaster_LightningStorm_Daze".equals(name)) return null;
    if ("Itemset_Application_Champion_BrutalStrikes_PIP_Reduction".equals(name)) return null;
    if ("Itemset_Application_Hunter_LowCut_Cleanse".equals(name)) return null;
    if ("Itemset_Application_Guardian_ToTheKing_ParryResponse".equals(name)) return null;
    if ("Itemset_Application_Captain_BattleShout_Fear".equals(name)) return null;

    // Other unsupported stats
    if ("Burglar_Skill_CriticalMagnitude".equals(name)) return null;
    if ("Burglar_Skill_Gamble_Chance".equals(name)) return null;
    if ("CombatStateMod_CC_DurationMultModifier".equals(name)) return null;
    if ("Combat_Agent_Armor_Value_Float".equals(name)) return null;
    if ("Combat_Agent_MountArmor_Value_Float".equals(name)) return null;
    if ("Combat_DamageQualifier_Magic_Defense".equals(name)) return null;
    if ("Combat_DamageQualifier_Melee_Defense".equals(name)) return null;
    if ("Combat_DamageQualifier_Ranged_Defense".equals(name)) return null;
    if ("Combat_DamageQualifier_Ranged_Offense".equals(name)) return null;
    if ("Combat_MeleeDmgQualifier_WeaponProcEffect".equals(name)) return null;
    if ("Combat_SkillDamageMultiplier_Fire".equals(name)) return null;
    if ("Combat_SkillDamageMultiplier_Light".equals(name)) return null;
    if ("Combat_TacticalDPS_Modifier".equals(name)) return null;
    if ("Combat_TacticalHPS_Modifier".equals(name)) return null;
    if ("Combat_WeaponDamageMultiplier_1HClub".equals(name)) return null;
    if ("Combat_WeaponDamageMultiplier_1HSword".equals(name)) return null;
    if ("Combat_WeaponDamageMultiplier_Dagger".equals(name)) return null;
    if ("Combat_WeaponDamageMultiplier_Halberd".equals(name)) return null;
    if ("Combat_WeaponDamageMultiplier_Spear".equals(name)) return null;
    if ("EffectMod_HealthHeal_Guardian_CatchaBreath".equals(name)) return null;
    if ("Effect_Self_Revive_Cooldown".equals(name)) return null;
    if ("Fellowship_SharedTracking_Trait".equals(name)) return null;
    if ("ForwardSource_Combat_Loremaster_SkillCombo".equals(name)) return null;
    if ("ForwardSource_Combat_TraitCombo".equals(name)) return null;
    if ("Gambit_GambitIconCountMax".equals(name)) return null;
    if ("Hobby_Fishing_ProficiencyModifier".equals(name)) return null;
    if ("IA_Burglar_Application_AllInTheWrist_Reset".equals(name)) return null;
    if ("IA_Burglar_BuffDuration_DPSExtremeCrit".equals(name)) return null;
    if ("IA_Burglar_Stratagem_Movement".equals(name)) return null;
    if ("IA_Burglar_StrategicStrike_Damage".equals(name)) return null;
    if ("IA_Captain_Application_Cries_Reset".equals(name)) return null;
    if ("IA_Captain_ChanceCriticalHit_CryWrath".equals(name)) return null;
    if ("IA_Captain_RallyTheRiders_Healbuff".equals(name)) return null;
    if ("IA_Captain_WordsofCourage_Pulses".equals(name)) return null;
    if ("IA_Champion_ChanceCriticalHit_MountedStrike".equals(name)) return null;
    if ("IA_Champion_Duration_Recuperate".equals(name)) return null;
    if ("IA_Guardian_Application_ClashOfArms_Reset".equals(name)) return null;
    if ("IA_Guardian_ChanceCriticalHit_RohansEdge".equals(name)) return null;
    if ("IA_Guardian_Magnitude_StaggeringSlash".equals(name)) return null;
    if ("IA_Hunter_BuffDamage_BurnHot".equals(name)) return null;
    if ("IA_Hunter_ChanceCriticalHit_NobleArrow".equals(name)) return null;
    if ("IA_Hunter_CriticalDamageMultiplier_FocusBowSkills".equals(name)) return null;
    if ("IA_Hunter_CriticalDamageMultiplier_InductionBowSkills".equals(name)) return null;
    if ("IA_Hunter_DismountChance_KillShot".equals(name)) return null;
    if ("IA_Hunter_MissChanceMod_EstablishingShot".equals(name)) return null;
    if ("IA_Hunter_PowerCost_InductionBowSkills".equals(name)) return null;
    if ("IA_Hunter_SnareMagnitude_QuickShot".equals(name)) return null;
    if ("IA_Loremaster_BurningEmbersFirstStrike_Damage".equals(name)) return null;
    if ("IA_Loremaster_EnduringEmbers_Damage".equals(name)) return null;
    if ("IA_Loremaster_FireDot_Damage".equals(name)) return null;
    if ("IA_Loremaster_SoP_ProcRate".equals(name)) return null;
    if ("IA_Minstrel_Application_CodaAccess".equals(name)) return null;
    if ("IA_Minstrel_Application_CodaDispel".equals(name)) return null;
    if ("IA_Minstrel_Ballad_Powercost".equals(name)) return null;
    if ("IA_Minstrel_BalladofWar_Buff".equals(name)) return null;
    if ("IA_Minstrel_BolsterCourage_Heal".equals(name)) return null;
    if ("IA_Minstrel_Motivation_Healing".equals(name)) return null;
    if ("IA_Minstrel_PiercingCry_Damage".equals(name)) return null;
    if ("IA_Runekeeper_Application_InvokeTheElements_TacticalDebuff".equals(name)) return null;
    if ("IA_Runekeeper_Application_Resolution_CritChance".equals(name)) return null;
    if ("IA_Runekeeper_BigHeal_Healing".equals(name)) return null;
    if ("IA_Runekeeper_HealHoT_Healing".equals(name)) return null;
    if ("IA_Runekeeper_InspirationalVerse_Pulses".equals(name)) return null;
    if ("IA_Runekeeper_ShockingTouch_Resistance".equals(name)) return null;
    if ("IA_Runekeeper_SmallHoT_Pulses".equals(name)) return null;
    if ("IA_Runekeeper_TieredHot_Healing".equals(name)) return null;
    if ("IA_Warden_ChanceCriticalHit_Clash".equals(name)) return null;
    if ("IA_Warden_Duration_SkillOfTheEorlingas".equals(name)) return null;
    if ("IA_Warden_Eorlingas_Magnitude".equals(name)) return null;
    if ("Inventory_AllowSecondaryWeapon".equals(name)) return null;
    if ("Inventory_AllowedEquipmentCategories".equals(name)) return null;
    if ("Item_Dummy_ICMR".equals(name)) return null;
    if ("Item_Hunter_FireArrow_EvadeChance".equals(name)) return null;
    if ("Item_Hunter_FireArrow_MissChance".equals(name)) return null;
    if ("Item_Hunter_LightArrow_EvadeChance".equals(name)) return null;
    if ("Item_Hunter_LightArrow_MissChance".equals(name)) return null;
    if ("Item_InductionDuration_Burglar_Looting".equals(name)) return null;
    if ("Item_Minstrel_Oathbreaker_Damagetype".equals(name)) return null;
    if ("Item_Root_Duration".equals(name)) return null;
    if ("Item_Warden_FireJavelin_MissChance".equals(name)) return null;
    if ("Item_Warden_LightJavelin_MissChance".equals(name)) return null;
    if ("MC_Trait_Champion_Application_Fervour_Consumption".equals(name)) return null;
    if ("MountEndurance_MaxLevel".equals(name)) return null;
    if ("MountPower_MaxLevel".equals(name)) return null;
    if ("Mount_Combat_EvadePoints_Modifier".equals(name)) return null;
    if ("Mount_Movement_Acceleration".equals(name)) return null;
    if ("Mount_Movement_Turn_Rate".equals(name)) return null;
    if ("Resist_AdditionalPoints_Resistance_TheOneResistance".equals(name)) return null;
    if ("Resist_Additional_Resistance_Fear".equals(name)) return null;
    if ("Runekeeper_BattleWrit_PowerCost".equals(name)) return null;
    if ("Runekeeper_HealingWrit_PowerCost".equals(name)) return null;
    if ("SkillMod_Damage_Guardian_SweepingCut".equals(name)) return null;
    if ("Skill_AdditionalThreat_Hunter_AllRangeSkillsMod".equals(name)) return null;
    if ("Skill_AllMeleeSkills_MaxMeleeRange".equals(name)) return null;
    if ("Skill_Attunement_Runekeeper_Lightning_Shaken_Damage".equals(name)) return null;
    if ("Skill_Attunement_Runekeeper_Lightning_Stun_StunRate".equals(name)) return null;
    if ("Skill_Captain_VocalHealBonus".equals(name)) return null;
    if ("Skill_ChampionMeleeSkills_MaxMeleeRange".equals(name)) return null;
    if ("Skill_CriticalDamageMultiplier_ChampionAll".equals(name)) return null;
    if ("Skill_DamageMultiplier_CelebrationofSkill".equals(name)) return null;
    if ("Skill_DamageMultiplier_Champion_BracingAttackHeal".equals(name)) return null;
    if ("Skill_DamageMultiplier_MightBlow".equals(name)) return null;
    if ("Skill_DamageMultiplier_RendDot".equals(name)) return null;
    if ("Skill_DamageMultiplier_Safeguard".equals(name)) return null;
    if ("Skill_DamageMultiplier_Trait_Race_Conjuction_Guile".equals(name)) return null;
    if ("Skill_DamageMultiplier_Trait_Race_Conjuction_Might".equals(name)) return null;
    if ("Skill_DamageMultiplier_Trait_Stealth_LineBonus".equals(name)) return null;
    if ("Skill_DamageMultiplier_Warden_SpearDot".equals(name)) return null;
    if ("Skill_Duration_Trait_Captain_TacticalProwess".equals(name)) return null;
    if ("Skill_Duration_Warden_ClubDebuff".equals(name)) return null;
    if ("Skill_HealingMultiplier_Trait_Race_Conjuction_Conviction".equals(name)) return null;
    if ("Skill_Healing_Induction_MP_PowerofFear".equals(name)) return null;
    if ("Skill_InductionDuration_ResearchingMod".equals(name)) return null;
    if ("Skill_Loremaster_BeaconofHope_HealMultiplier".equals(name)) return null;
    if ("Skill_Minstrel_CriticalChance_Ballad".equals(name)) return null;
    if ("Skill_Minstrel_CriticalChance_Coda".equals(name)) return null;
    if ("Skill_PowerMoraleMultiplier_Trait_Race_Conjuction_Tactics".equals(name)) return null;
    if ("Skill_ProcRate_Warden_ClubDebuff".equals(name)) return null;
    if ("Skill_ProcRate_Warden_SpearDot".equals(name)) return null;
    if ("Skill_RangedAttack_MaxRangeMod".equals(name)) return null;
    if ("Skill_RangedDefaultAttack_MaxRangeMod".equals(name)) return null;
    if ("Skill_RiftSet_Absorb_Fire".equals(name)) return null;
    if ("Skill_Runekeeper_Balance_PowerCost".equals(name)) return null;
    if ("Skill_Runekeeper_Battle_PowerCost".equals(name)) return null;
    if ("Skill_Runekeeper_Healing_PowerCost".equals(name)) return null;
    if ("Skill_Runekeeper_PreludeToHope_InitialPulse".equals(name)) return null;
    if ("Skill_Stagger_PositionalDamageMod".equals(name)) return null;
    if ("Skill_ToHitBonus_Warden_Swords".equals(name)) return null;
    if ("Skill_VitalCost_Champion_AOEMod".equals(name)) return null;
    if ("Skill_VitalCost_Champion_StrikeMod".equals(name)) return null;
    if ("Skill_VitalCost_CriesMod".equals(name)) return null;
    if ("Skill_VitalCost_Morale_TimeofNeed".equals(name)) return null;
    if ("Skill_VitalCost_RangedMod".equals(name)) return null;
    if ("Skill_VitalCost_SerenadeDefenseMod".equals(name)) return null;
    if ("Skill_VitalCost_SerenadeOffenseMod".equals(name)) return null;
    if ("Skill_VitalReturn_Captain_ValiantStrike".equals(name)) return null;
    if ("Skill_Warden_MoraleTapDamage".equals(name)) return null;
    if ("Skill_Warden_PersevereHeal".equals(name)) return null;
    if ("Stat_MountAgility".equals(name)) return null;
    if ("Stat_MountStrength".equals(name)) return null;
    if ("Stealth_StealthDetectionLevelModifier".equals(name)) return null;
    if ("ThreatInputModifier_Damage".equals(name)) return null;
    if ("ThreatInputModifier_Healing".equals(name)) return null;
    if ("TotalThreatModifier_Player".equals(name)) return null;
    if ("Trait_Captain_PetModStat_Slot4".equals(name)) return null;
    if ("Trait_Mount_PowerCost_SteedSkills".equals(name)) return null;
    if ("Trait_Mounted_Beorning_Bees_Critical_Chance".equals(name)) return null;
    if ("Trait_Mounted_Beorning_Recuperate_Duration".equals(name)) return null;
    if ("Trait_Mounted_Beorning_Recuperate_Pulses".equals(name)) return null;
    if ("Trait_Mounted_Beorning_Wrath_Generation".equals(name)) return null;
    if ("Trait_Mounted_Critical_Multiplier".equals(name)) return null;
    if ("Trait_Virtue_Rank_Charity".equals(name)) return null;
    if ("Trait_Virtue_Rank_Determination".equals(name)) return null;
    if ("Trait_Virtue_Rank_Discipline".equals(name)) return null;
    if ("Trait_Virtue_Rank_Honesty".equals(name)) return null;
    if ("Trait_Virtue_Rank_Loyalty".equals(name)) return null;
    if ("Trait_Virtue_Rank_Merciful".equals(name)) return null;
    if ("Trait_Virtue_Rank_Patience".equals(name)) return null;
    if ("Trait_Virtue_Rank_Tolerant".equals(name)) return null;
    if ("Trait_Virtue_Rank_Valor".equals(name)) return null;
    if ("Trait_Virtue_Rank_Wisdom".equals(name)) return null;
    if ("Vital_MountEnduranceCombatRegenAddMod".equals(name)) return null;
    if ("Vital_MountPowerCombatRegenAddMod".equals(name)) return null;

    LOGGER.warn("Unknown stat name: "+name);
    return null;
  }
}
