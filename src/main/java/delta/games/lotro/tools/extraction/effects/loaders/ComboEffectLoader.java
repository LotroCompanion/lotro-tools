package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.ComboEffect;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for 'combo' effects.
 * @author DAM
 */
public class ComboEffectLoader extends AbstractEffectLoader<ComboEffect>
{
  @Override
  public void loadSpecifics(ComboEffect effect, PropertiesSet effectProps)
  {
/*
******** Properties: 1879453021
Effect_Combo_CasterOnly: 0
Effect_Combo_EffectPresentList:
  #1: Effect_WSLEffect 1879073120
  #2: Effect_WSLEffect 1879453022
  #3: Effect_WSLEffect 0
Effect_Combo_EffectToAddIfNotPresent: 1879073122
Effect_Combo_EffectToAddIfPresent: 1879453022
Effect_Combo_EffectToGiveBackIfPresent: DID
Effect_Combo_EffectToGiveBackIfNotPresent: DID
Effect_Combo_EffectToExamine: 1879073122
Effect_Combo_RemoveAllOldEffectsIfPresent: 0 (bool)
Effect_Combo_RemoveOldEffectIfPresent: 0 (bool)
*/
    Object[] presentArray=(Object[])effectProps.getProperty("Effect_Combo_EffectPresentList");
    if (presentArray!=null)
    {
      for(Object presentObj : presentArray)
      {
        Integer effectID=(Integer)presentObj;
        Proxy<Effect> presentEffect=buildProxy(effectID);
        effect.addPresentEffect(presentEffect);
      }
    }
    // To add if not present
    Integer toAddIfNotPresent=(Integer)effectProps.getProperty("Effect_Combo_EffectToAddIfNotPresent");
    effect.setToAddIfNotPresent(buildProxy(toAddIfNotPresent));
    // To add if present
    Integer toAddIfPresent=(Integer)effectProps.getProperty("Effect_Combo_EffectToAddIfPresent");
    effect.setToAddIfPresent(buildProxy(toAddIfPresent));
    // To give back if not present
    Integer toGiveBackIfNotPresent=(Integer)effectProps.getProperty("Effect_Combo_EffectToGiveBackIfNotPresent");
    effect.setToGiveBackIfNotPresent(buildProxy(toGiveBackIfNotPresent));
    // To give back if present
    Integer toGiveBackIfPresent=(Integer)effectProps.getProperty("Effect_Combo_EffectToGiveBackIfPresent");
    effect.setToGiveBackIfPresent(buildProxy(toGiveBackIfPresent));
    // To examine
    Integer toExamine=(Integer)effectProps.getProperty("Effect_Combo_EffectToExamine");
    effect.setToExamine(buildProxy(toExamine));
  }
}
