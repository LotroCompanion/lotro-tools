package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.BaseVitalEffect;
import delta.games.lotro.common.effects.VitalChangeDescription;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.tools.extraction.utils.DatStatUtils;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Base loader for 'vital' effects.
 * @param <T> Type of managed effects.
 * @author DAM
 */
public abstract class VitalEffectLoader<T extends BaseVitalEffect> extends AbstractEffectLoader<T>
{
  protected void loadBaseVitalEffect(T effect, Integer vitalType, PropertiesSet effectProps)
  {
    // Stat
    StatDescription stat=DatStatUtils.getStatFromVitalType(vitalType.intValue());
    effect.setStat(stat);
    // Damage type
    LotroEnum<DamageType> damageTypeEnum=LotroEnumsRegistry.getInstance().get(DamageType.class);
    Integer damageTypeCode=(Integer)effectProps.getProperty("Effect_DamageType");
    if ((damageTypeCode!=null) && (damageTypeCode.intValue()!=0))
    {
      DamageType damageType=damageTypeEnum.getEntry(damageTypeCode.intValue());
      effect.setDamageType(damageType);
    }
  }

  protected VitalChangeDescription loadVitalChangeDescription(PropertiesSet effectProps, String seed)
  {
    Float constant=(Float)effectProps.getProperty(seed+"Constant");
    Integer progressionID=(Integer)effectProps.getProperty(seed+"Progression");
    Float variance=(Float)effectProps.getProperty(seed+"Variance");
    Float min=null;
    Float max=null;
    PropertiesSet randomProps=(PropertiesSet)effectProps.getProperty(seed+"Random");
    if (randomProps!=null)
    {
      min=(Float)randomProps.getProperty("Effect_RandomValueMin");
      max=(Float)randomProps.getProperty("Effect_RandomValueMax");
    }
    if (!((constant!=null) || (progressionID!=null) || ((min!=null) && (max!=null))))
    {
      return null;
    }
    VitalChangeDescription ret=new VitalChangeDescription();
    if (constant!=null)
    {
      ret.setConstant(constant.floatValue());
    }
    else if (progressionID!=null)
    {
      Progression progression=getProgression(progressionID.intValue());
      ret.setProgression(progression);
    }
    else // Random
    {
      ret.setMinValue(min.floatValue());
      ret.setMaxValue(max.floatValue());
    }
    // Variance
    ret.setVariance(variance);
    // Modifiers
    ModPropertyList modifiers=ModifiersUtils.getStatModifiers(effectProps,seed+"_ModifierList");
    ret.setModifiers(modifiers);
    return ret;
  }
}
