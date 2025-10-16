package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.CountDownEffect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for 'count down' effects.
 * @param <T> Type of managed effects.
 * @author DAM
 */
public class CountDownEffectLoader<T extends CountDownEffect> extends PropertyModificationEffectLoader<T>
{
  @Override
  public void loadSpecifics(T effect, PropertiesSet effectProps)
  {
    super.loadSpecifics(effect,effectProps);
    // 'on expire' effects
    Object[] expireEffectsList=(Object[])effectProps.getProperty("EffectGenerator_Countdown_ExpireEffectList");
    if (expireEffectsList!=null)
    {
      for(Object entry : expireEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        effect.addOnExpireEffect(generator);
      }
    }
    // 'on removal' effect
    PropertiesSet onRemovalProps=(PropertiesSet)effectProps.getProperty("EffectGenerator_OnRemoval_Effect");
    if (onRemovalProps!=null)
    {
      EffectGenerator generator=loadGenerator(onRemovalProps);
      effect.setOnRemovalEffect(generator);
    }
  }
}
