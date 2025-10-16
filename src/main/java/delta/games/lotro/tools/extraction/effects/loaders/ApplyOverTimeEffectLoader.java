package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.ApplyOverTimeEffect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for 'apply over time' effects.
 * @author DAM
 */
public class ApplyOverTimeEffectLoader extends AbstractEffectLoader<ApplyOverTimeEffect>
{
  @Override
  public void loadSpecifics(ApplyOverTimeEffect effect, PropertiesSet effectProps)
  {
    // 'initially applied' effects
    Object[] initiallyAppliedEffectsList=(Object[])effectProps.getProperty("Effect_ApplyOverTime_Initial_Applied_Effect_Array");
    if (initiallyAppliedEffectsList!=null)
    {
      for(Object entry : initiallyAppliedEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps,"Effect_ApplyOverTime_Applied_Effect","Effect_ApplyOverTime_Applied_Effect_Spellcraft");
        effect.addInitiallyAppliedEffect(generator);
      }
    }
    // 'applied' effects
    Object[] appliedEffectsList=(Object[])effectProps.getProperty("Effect_ApplyOverTime_Applied_Effect_Array");
    if (appliedEffectsList!=null)
    {
      for(Object entry : appliedEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps,"Effect_ApplyOverTime_Applied_Effect","Effect_ApplyOverTime_Applied_Effect_Spellcraft");
        effect.addAppliedEffect(generator);
      }
    }
  }
}
