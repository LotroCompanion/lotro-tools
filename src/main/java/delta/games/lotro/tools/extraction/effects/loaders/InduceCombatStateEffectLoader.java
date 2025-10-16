package delta.games.lotro.tools.extraction.effects.loaders;

import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.effects.InduceCombatStateEffect;
import delta.games.lotro.common.enums.CombatState;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.math.LinearFunction;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;

/**
 * Loader for 'induce combat state' effects.
 * @author DAM
 */
public class InduceCombatStateEffectLoader extends AbstractEffectLoader<InduceCombatStateEffect>
{
  private static final Logger LOGGER=LoggerFactory.getLogger(InduceCombatStateEffectLoader.class);

  @Override
  public void loadSpecifics(InduceCombatStateEffect effect, PropertiesSet effectProps)
  {
    /*
    Effect ID=1879051312, class=InduceCombatStateEffect (769)
    Effect_InduceCombatState_ConstantDuration: 3.0
    Effect_InduceCombatState_StateToInduce: 16384 (Stunned)
    Effect_InduceCombatState_VariableDuration:
      Effect_VariableMax: 5.0
      Effect_VariableMin: 3.0
      Effect_VariableSpellcraftMax: 50.0
      Effect_VariableSpellcraftMin: 25.0
    Effect_CombatState_Induce_StateDuration_ModProp_List:
      #1: Effect_ModifierPropertyList_Entry 268452197 (CombatState_ConjunctionStunned_Duration)
     */
    // Constant duration
    Float duration=(Float)effectProps.getProperty("Effect_InduceCombatState_ConstantDuration");
    if (duration!=null)
    {
      effect.setDuration(duration.floatValue());
    }
    // Duration function
    PropertiesSet durationProps=(PropertiesSet)effectProps.getProperty("Effect_InduceCombatState_VariableDuration");
    if (durationProps!=null)
    {
      LinearFunction function=loadLinearFunction(durationProps);
      effect.setDurationFunction(function);
    }
    // Duration modifiers
    ModPropertyList durationMods=ModifiersUtils.getStatModifiers(effectProps,"Effect_CombatState_Induce_StateDuration_ModProp_List");
    effect.setDurationModifiers(durationMods);
    // Combat state
    Integer bitSetValueInt=(Integer)effectProps.getProperty("Effect_InduceCombatState_StateToInduce");
    if (bitSetValueInt!=null)
    {
      int bitSetValue=bitSetValueInt.intValue();
      LotroEnum<CombatState> damageTypeEnum=LotroEnumsRegistry.getInstance().get(CombatState.class);
      BitSet bitSet=BitSetUtils.getBitSetFromFlags(bitSetValue);
      List<CombatState> states=damageTypeEnum.getFromBitSet(bitSet);
      if (states.size()!=1)
      {
        LOGGER.warn("Unexpected size for combat states: {}", states);
      }
      CombatState state=states.get(0);
      effect.setCombatState(state);
    }
    // Break on harm
    // Ex: 100% break chance on harm after 1s
    Float breakOnHarmfullSkill=(Float)effectProps.getProperty("Effect_CombatState_Induce_BreakOnHarmfulSkill_Override");
    effect.setBreakOnHarmfullSkill(breakOnHarmfullSkill);
    // No modifiers: Effect_CombatState_Induce_BreakOnHarmfulSkill_ModProp_List
    // Break on vital loss. Ex: 3% break chance on damage after 1s
    // - value
    Float breakOnVitalLoss=(Float)effectProps.getProperty("Effect_CombatState_Induce_BreakOnVitalLossProb_Override");
    effect.setBreakOnVitalLossProbability(breakOnVitalLoss);
    // - modifiers
    ModPropertyList breakOnVitalLossMods=ModifiersUtils.getStatModifiers(effectProps,"Effect_CombatState_Induce_BreakOnVitalLoss_ModProp_List");
    effect.setBreakOnVitalLossProbabilityModifiers(breakOnVitalLossMods);
    // Grace period
    Float gracePeriod=(Float)effectProps.getProperty("Effect_CombatState_Induce_BreakOutOfState_GracePeriod_Override");
    effect.setGracePeriod(gracePeriod);
    ModPropertyList gracePeriodMods=ModifiersUtils.getStatModifiers(effectProps,"Effect_CombatState_Induce_BreakOutOfState_GracePeriod_Override_ModifierList");
    effect.setGracePeriodModifiers(gracePeriodMods);
  }

  private LinearFunction loadLinearFunction(PropertiesSet props)
  {
    float minX=((Float)props.getProperty("Effect_VariableSpellcraftMin")).floatValue();
    float minY=((Float)props.getProperty("Effect_VariableMin")).floatValue();
    float maxX=((Float)props.getProperty("Effect_VariableSpellcraftMax")).floatValue();
    float maxY=((Float)props.getProperty("Effect_VariableMax")).floatValue();
    return new LinearFunction(minX,maxX,minY,maxY);
  }
}
