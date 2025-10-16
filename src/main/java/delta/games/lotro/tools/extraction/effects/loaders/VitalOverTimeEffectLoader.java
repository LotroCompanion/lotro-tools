package delta.games.lotro.tools.extraction.effects.loaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.effects.VitalChangeDescription;
import delta.games.lotro.common.effects.VitalOverTimeEffect;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for 'vital over time' effects.
 * @author DAM
 */
public class VitalOverTimeEffectLoader extends VitalEffectLoader<VitalOverTimeEffect>
{
  private static final Logger LOGGER=LoggerFactory.getLogger(VitalOverTimeEffectLoader.class);

  @Override
  public void loadSpecifics(VitalOverTimeEffect effect, PropertiesSet effectProps)
  {
  /*
Effect_VitalOverTime_ChangePerIntervalProgression: 1879068280
Effect_VitalOverTime_ChangePerInterval_Critical_Multiplier: 1.0
Effect_VitalOverTime_ChangePerInterval_ModifierList:
  #1: Effect_ModifierPropertyList_Entry 268437688 (EffectMod_ModType_DamageMultModifier_Add)
Effect_VitalOverTime_InitialChangeProgression: 1879068279
Effect_VitalOverTime_InitialChange_Critical_Multiplier: 1.0
Effect_VitalOverTime_InitialChange_ModifierList:
  #1: Effect_ModifierPropertyList_Entry 268437688 (EffectMod_ModType_DamageMultModifier_Add)
Effect_VitalOverTime_VitalType: 1 (Morale)
Effect_DamageType: 1 (Common) ; OR Effect_DamageType: 0 (Undef)
 */
    Integer vitalType=(Integer)effectProps.getProperty("Effect_VitalOverTime_VitalType");
    loadBaseVitalEffect(effect,vitalType,effectProps);
    // Initial change
    VitalChangeDescription initialChange=loadVitalChangeDescription(effectProps,"Effect_VitalOverTime_InitialChange");
    if (initialChange==null)
    {
      LOGGER.info("No initial change!");
    }
    else
    {
      // VPS multiplier
      Float vpsMultiplier=(Float)effectProps.getProperty("Effect_BaseVital_InitialChangeVPSMultiplier");
      initialChange.setVPSMultiplier(vpsMultiplier);
    }
    effect.setInitialChangeDescription(initialChange);
    // Over Time change
    VitalChangeDescription overTimeChange=loadVitalChangeDescription(effectProps,"Effect_VitalOverTime_ChangePerInterval");
    if (overTimeChange==null)
    {
      LOGGER.warn("No value data for vital change!");
    }
    else
    {
      // VPS multiplier
      Float vpsMultiplier=(Float)effectProps.getProperty("Effect_BaseVital_ChangePerIntervalVPSMultiplier");
      overTimeChange.setVPSMultiplier(vpsMultiplier);
    }
    effect.setOverTimeChangeDescription(overTimeChange);
  }
}
