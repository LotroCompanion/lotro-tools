package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.AreaEffect;
import delta.games.lotro.common.effects.AreaEffectFlags;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;

/**
 * Loader for 'area' effects.
 * @author DAM
 */
public class AreaEffectLoader extends AbstractEffectLoader<AreaEffect>
{
  @Override
  public void loadSpecifics(AreaEffect effect, PropertiesSet effectProps)
  {
    /*
Effect_Area_ShouldAffectCaster: 0
Effect_Area_AffectsMonsterPlayers: 1
Effect_Area_AffectsMonsters: 1
Effect_Area_AffectsPlayers: 1
Effect_Area_AffectsPvPPlayers: 1
Effect_Area_AffectsSessionPlayers: 1
Effect_Area_AffectsDistantBattleUnits: 0
Effect_Area_AffectsPlayerPets: 0
Effect_Area_CheckRangeToTargets: 0

Effect_Area_Applied_Effect_Array: 
  #1: Effect_Area_Applied_Effect_Data 
    Effect_Area_Applied_Effect: 1879284216
    Effect_Area_Applied_Effect_Spellcraft: -1.0
Effect_Area_MaxRange: 5.0
Effect_Area_MaxTargets: 10
Effect_Area_MaxTargets_AdditiveModifiers: 
  #1: Effect_ModifierPropertyList_Entry 268462716 (Skill_AreaEffect_TargetCount)
    */
    // Flags
    effect.setFlag(AreaEffectFlags.SHOULD_AFFECT_CASTER,EffectLoadingUtils.getFlag(effectProps,"Effect_Area_ShouldAffectCaster"));
    effect.setFlag(AreaEffectFlags.AFFECTS_MONSTER_PLAYERS,EffectLoadingUtils.getFlag(effectProps,"Effect_Area_AffectsMonsterPlayers"));
    effect.setFlag(AreaEffectFlags.AFFECTS_MONSTERS,EffectLoadingUtils.getFlag(effectProps,"Effect_Area_AffectsMonsters"));
    effect.setFlag(AreaEffectFlags.AFFECTS_PLAYERS,EffectLoadingUtils.getFlag(effectProps,"Effect_Area_AffectsPlayers"));
    effect.setFlag(AreaEffectFlags.AFFECTS_PVP_PLAYERS,EffectLoadingUtils.getFlag(effectProps,"Effect_Area_AffectsPvPPlayers"));
    effect.setFlag(AreaEffectFlags.AFFECTS_SESSION_PLAYERS,EffectLoadingUtils.getFlag(effectProps,"Effect_Area_AffectsSessionPlayers"));
    effect.setFlag(AreaEffectFlags.AFFECTS_DISTANT_BATTLE_UNITS,EffectLoadingUtils.getFlag(effectProps,"Effect_Area_AffectsDistantBattleUnits"));
    effect.setFlag(AreaEffectFlags.AFFECTS_PLAYER_PETS,EffectLoadingUtils.getFlag(effectProps,"Effect_Area_AffectsPlayerPets"));
    effect.setFlag(AreaEffectFlags.CHECK_RANGE_TO_TARGET,EffectLoadingUtils.getFlag(effectProps,"Effect_Area_CheckRangeToTargets"));
    // Effects
    Object[] effectsList=(Object[])effectProps.getProperty("Effect_Area_Applied_Effect_Array");
    for(Object effectEntry : effectsList)
    {
      EffectGenerator generator=loadGenerator((PropertiesSet)effectEntry,"Effect_Area_Applied_Effect","Effect_Area_Applied_Effect_Spellcraft");
      if (generator!=null)
      {
        effect.addEffect(generator);
      }
    }
    // Range
    Float maxRange=(Float)effectProps.getProperty("Effect_Area_MaxRange");
    effect.setRange((maxRange!=null)?maxRange.floatValue():0f);
    // Detection buffer
    Float detectionBuffer=(Float)effectProps.getProperty("Effect_Area_DetectionBuffer");
    effect.setDetectionBuffer((detectionBuffer!=null)?detectionBuffer.floatValue():0f);
    // Max targets
    Integer maxTargets=(Integer)effectProps.getProperty("Effect_Area_MaxTargets");
    effect.setMaxTargets((maxTargets!=null)?maxTargets.intValue():0);
    ModPropertyList maxTargetsMods=ModifiersUtils.getStatModifiers(effectProps,"Effect_Area_MaxTargets_AdditiveModifiers");
    effect.setMaxTargetsModifiers(maxTargetsMods);
  }
}
