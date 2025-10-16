package delta.games.lotro.tools.extraction.effects.loaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectAndProbability;
import delta.games.lotro.common.effects.ReactiveChange;
import delta.games.lotro.common.effects.ReactiveVitalChange;
import delta.games.lotro.common.effects.ReactiveVitalEffect;
import delta.games.lotro.common.enums.DamageQualifier;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.VitalType;
import delta.games.lotro.common.enums.VitalTypes;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loader for 'reactive vital' effects.
 * @author DAM
 */
public class ReactiveVitalEffectLoader extends PropertyModificationEffectLoader<ReactiveVitalEffect>
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ReactiveVitalEffectLoader.class);

  @Override
  public void loadSpecifics(ReactiveVitalEffect effect, PropertiesSet effectProps)
  {
    super.loadSpecifics(effect,effectProps);
    ReactiveChange attackerChange=loadReactiveChange(effectProps,"Effect_ReactiveVital_Attacker");
    effect.setAttackerReactiveChange(attackerChange);
    ReactiveChange defenderChange=loadReactiveChange(effectProps,"Effect_ReactiveVital_Defender");
    effect.setDefenderReactiveChange(defenderChange);
    // Vital types
    Object[] vitalTypeList=(Object[])effectProps.getProperty("Effect_VitalInterested_VitalTypeList");
    if (vitalTypeList!=null)
    {
      for(Object vitalTypeObj : vitalTypeList)
      {
        int vitalTypeCode=((Integer)vitalTypeObj).intValue();
        VitalType vitalType=VitalTypes.getByCode(vitalTypeCode);
        effect.addVitalType(vitalType);
      }
    }
    // Incoming damage types
    LotroEnum<DamageType> damageTypeEnum=LotroEnumsRegistry.getInstance().get(DamageType.class);
    Object[] damageTypeList=(Object[])effectProps.getProperty("Effect_InterestedIncomingDamageTypes");
    if (damageTypeList!=null)
    {
      for(Object damageTypeObj : damageTypeList)
      {
        int damageTypeCode=((Integer)damageTypeObj).intValue();
        DamageType damageType=damageTypeEnum.getEntry(damageTypeCode);
        effect.addDamageType(damageType);
      }
    }
    // Damage qualifiers
    LotroEnum<DamageQualifier> damageQualifierEnum=LotroEnumsRegistry.getInstance().get(DamageQualifier.class);
    Object[] damageQualifierList=(Object[])effectProps.getProperty("Effect_ReactiveVital_RequiredAttacker_DamageQualifier_Array");
    if (damageQualifierList!=null)
    {
      for(Object damageQualifierObj : damageQualifierList)
      {
        int damageQualifierCode=((Integer)damageQualifierObj).intValue();
        if (damageQualifierCode!=0)
        {
          DamageQualifier damageQualifier=damageQualifierEnum.getEntry(damageQualifierCode);
          effect.addDamageQualifier(damageQualifier);
        }
      }
    }
    // Damage type override
    Integer damageTypeOverrideCode=(Integer)effectProps.getProperty("Effect_ReactiveVital_AttackerVitalChange_DamageTypeOverride");
    if (damageTypeOverrideCode!=null)
    {
      DamageType damageTypeOverride=damageTypeEnum.getEntry(damageTypeOverrideCode.intValue());
      effect.setAttackerDamageTypeOverride(damageTypeOverride);
    }
    // Remove on proc
    Integer removeOnProcInt=(Integer)effectProps.getProperty("Effect_ReactiveVital_RemoveOnSuccessfulProc");
    boolean removeOnProc=((removeOnProcInt!=null)&&(removeOnProcInt.intValue()==1));
    effect.setRemoveOnProc(removeOnProc);
  }

  private ReactiveChange loadReactiveChange(PropertiesSet effectProps, String seed)
  {
    ReactiveVitalChange vitalChange=loadReactiveVitalChange(effectProps,seed+"VitalChange_");
    EffectAndProbability effect=loadEffectAndProbability(effectProps,seed+"Effect_");
    if ((vitalChange!=null) || (effect!=null))
    {
      return new ReactiveChange(vitalChange,effect);
    }
    return null;
  }

  private ReactiveVitalChange loadReactiveVitalChange(PropertiesSet effectProps, String seed)
  {
    Float constantFloat=(Float)effectProps.getProperty(seed+"Constant");
    Integer progressionIDInt=(Integer)effectProps.getProperty(seed+"Progression");
    float constant=(constantFloat!=null)?constantFloat.floatValue():0;
    int progressionID=(progressionIDInt!=null)?progressionIDInt.intValue():0;
    if ((Math.abs(constant)<0.0001) && (progressionID==0))
    {
      return null;
    }

    ReactiveVitalChange ret=new ReactiveVitalChange();
    // Probability
    Float probabilityFloat=(Float)effectProps.getProperty(seed+"Probability");
    float probability=(probabilityFloat!=null)?probabilityFloat.floatValue():0;
    if (probability<0)
    {
      probability=1;
    }
    ret.setProbability(probability);
    // Multiplicative/additive
    Integer additiveInt=(Integer)effectProps.getProperty(seed+"Additive");
    Integer multiplicativeInt=(Integer)effectProps.getProperty(seed+"Multiplicative");
    boolean multiplicative=false;
    if ((multiplicativeInt!=null) && (multiplicativeInt.intValue()==1))
    {
      multiplicative=true;
      if ((additiveInt!=null) && (additiveInt.intValue()!=0))
      {
        LOGGER.warn("Additive or multiplicative?");
      }
    }
    ret.setMultiplicative(multiplicative);
    // Constant
    if (Math.abs(constant)>0.0001)
    {
      ret.setConstant(constant);
    }
    if (progressionIDInt!=null)
    {
      Progression progression=getProgression(progressionIDInt.intValue());
      ret.setProgression(progression);
    }
    Float variance=(Float)effectProps.getProperty(seed+"Variance");
    ret.setVariance(variance);
    return ret;
  }

  private EffectAndProbability loadEffectAndProbability(PropertiesSet effectProps, String seed)
  {
    Float probabilityFloat=(Float)effectProps.getProperty(seed+"Probability");
    float probability=(probabilityFloat!=null)?probabilityFloat.floatValue():0;
    Integer effectIDInt=(Integer)effectProps.getProperty(seed+"Effect");
    int effectID=(effectIDInt!=null)?effectIDInt.intValue():0;
    if (effectID==0)
    {
      return null;
    }
    Effect effect=getEffect(effectID);
    if (effect==null)
    {
      LOGGER.warn("Effect not found: {}",Integer.valueOf(effectID));
      return null;
    }
    EffectAndProbability ret=new EffectAndProbability(effect,probability);
    return ret;
  }
}
