package delta.games.lotro.tools.extraction.effects.loaders;

import java.util.function.Consumer;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.PersistentComboEffect;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for 'persistent combo' effects.
 * @author DAM
 */
public class PersistentComboEffectLoader extends AbstractEffectLoader<PersistentComboEffect>
{
  @Override
  public void loadSpecifics(PersistentComboEffect effect, PropertiesSet effectProps)
  {
/*
******** Properties: 1879453021
Effect_Combo_CasterOnly: 0
Effect_Combo_EffectClassPresentList: 
  #1: Effect_EquivalenceClass 0 (Undef)
Effect_Combo_EffectPresentList: 
  #1: Effect_WSLEffect 1879314733
Effect_PersistentCombo_EffectsToAddIfAbsent_Array: 
  #1: Effect_ApplyOverTime_Applied_Effect_Data 
    Effect_ApplyOverTime_Applied_Effect: 1879313817
    Effect_ApplyOverTime_Applied_Effect_Spellcraft: -1.0
Effect_PersistentCombo_EffectsToAddIfPresent_Array: 
  #1: Effect_ApplyOverTime_Applied_Effect_Data 
    Effect_ApplyOverTime_Applied_Effect: 1879313816
    Effect_ApplyOverTime_Applied_Effect_Spellcraft: -1.0
*/
    handleEffectsList(effect,effectProps,"Effect_Combo_EffectPresentList",effect::addPresentEffect);
    handleEffectsList(effect,effectProps,"Effect_PersistentCombo_EffectsToAddIfAbsent_Array",effect::addToAddIfAbsent);
    handleEffectsList(effect,effectProps,"Effect_PersistentCombo_EffectsToAddIfPresent_Array",effect::addToAddIfPresent);
    // To examine
    Integer toExamine=(Integer)effectProps.getProperty("Effect_Combo_EffectToExamine");
    effect.setToExamine(buildProxy(toExamine));
  }

  private void handleEffectsList(PersistentComboEffect effect, PropertiesSet effectProps, String propertyName,Consumer<Proxy<Effect>> addFunction)
  {
    Object[] array=(Object[])effectProps.getProperty(propertyName);
    if (array!=null)
    {
      for(Object object : array)
      {
        Integer effectID=null;
        if (object instanceof PropertiesSet)
        {
          PropertiesSet entryProps=(PropertiesSet)object;
          effectID=(Integer)entryProps.getProperty("Effect_ApplyOverTime_Applied_Effect");
        }
        else if (object instanceof Integer)
        {
          effectID=(Integer)object;
        }
        if ((effectID!=null) && (effectID.intValue()>0))
        {
          Proxy<Effect> proxyEffect=buildProxy(effectID);
          addFunction.accept(proxyEffect);
        }
      }
    }
  }
}
