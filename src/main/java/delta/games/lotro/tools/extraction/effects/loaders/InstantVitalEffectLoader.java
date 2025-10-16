package delta.games.lotro.tools.extraction.effects.loaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.effects.InstantVitalEffect;
import delta.games.lotro.common.effects.VitalChangeDescription;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for 'instant vital' effects.
 * @author DAM
 */
public class InstantVitalEffectLoader extends VitalEffectLoader<InstantVitalEffect>
{
  private static final Logger LOGGER=LoggerFactory.getLogger(InstantVitalEffectLoader.class);

  @Override
  public void loadSpecifics(InstantVitalEffect effect, PropertiesSet effectProps)
  {
    Integer vitalType=(Integer)effectProps.getProperty("Effect_BaseVital_VitalType");
    super.loadBaseVitalEffect(effect,vitalType,effectProps);
    VitalChangeDescription description=super.loadVitalChangeDescription(effectProps,"Effect_InstantVital_InitialChange");
    if (description==null)
    {
      LOGGER.warn("No value data for vital change!");
    }
    else
    {
      // VPS multiplier
      Float vpsMultiplier=(Float)effectProps.getProperty("Effect_BaseVital_InitialChangeVPSMultiplier");
      description.setVPSMultiplier(vpsMultiplier);
    }
    effect.setInstantChangeDescription(description);
    // Multiplicative?
    Integer multiplicativeInt=(Integer)effectProps.getProperty("Effect_InstantVital_Multiplicative");
    boolean multiplicative=((multiplicativeInt!=null)&&(multiplicativeInt.intValue()==1));
    effect.setMultiplicative(multiplicative);
    // Initial change multiplier
    Float multiplier=(Float)effectProps.getProperty("Effect_BaseVital_InitialChangeMultiplier");
    if ((multiplier!=null) && (multiplier.floatValue()>0))
    {
      effect.setInitialChangeMultiplier(multiplier);
    }
  }
}
