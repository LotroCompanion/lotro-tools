package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.DispelEffect;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for 'dispel' effects.
 * @author DAM
 */
public class DispelEffectLoader extends AbstractEffectLoader<DispelEffect>
{
  @Override
  public void loadSpecifics(DispelEffect effect, PropertiesSet props)
  {
    /*
Effect_Dispel_ByEffectList: 
  #1: Effect_WSLEffect 1879152215
Effect_Dispel_DispelCasters: 0
    */
    // Dispel casters?
    Integer dispelCastersInt=(Integer)props.getProperty("Effect_Dispel_DispelCasters");
    boolean dispelCasters=((dispelCastersInt!=null)&&(dispelCastersInt.intValue()==1));
    effect.setDispelCasters(dispelCasters);
    // Generators
    Object[] effectsList=(Object[])props.getProperty("Effect_Dispel_ByEffectList");
    if (effectsList!=null)
    {
      for(Object entry : effectsList)
      {
        Integer effectID=(Integer)entry;
        if ((effectID!=null) && (effectID.intValue()>0))
        {
          Proxy<Effect> proxy=buildProxy(effectID);
          if (proxy!=null)
          {
            effect.addEffect(proxy);
          }
        }
      }
    }
  }
}
