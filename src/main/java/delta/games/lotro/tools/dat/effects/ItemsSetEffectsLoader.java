package delta.games.lotro.tools.dat.effects;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.sets.ItemsSet;
import delta.games.lotro.lore.items.sets.SetBonus;
import delta.games.lotro.tools.dat.utils.Utils;

/**
 * Items set effects loader.
 * @author DAM
 */
public class ItemsSetEffectsLoader
{
  private EffectLoader _loader;

  /**
   * Constructor.
   * @param loader Effects loader.
   */
  public ItemsSetEffectsLoader(EffectLoader loader)
  {
    _loader=loader;
  }

  /**
   * Handle an items set.
   * @param set Items set to use.
   * @param properties Items set properties.
   */
  public void handleSetEffects(ItemsSet set,PropertiesSet properties)
  {
    /*
    Set_ActiveCountDataList:
      #1: Set_ActiveCountData
        Set_ActiveCount: 2
        Set_EffectDataList:
          #1: EffectGenerator_EffectStruct
            EffectGenerator_EffectID: 1879098038
            EffectGenerator_EffectSpellcraft: -1.0
    */
    Object[] activeCountDataList=(Object[])properties.getProperty("Set_ActiveCountDataList");
    if (activeCountDataList==null)
    {
      return;
    }
    for(Object activeCountDataEntry : activeCountDataList)
    {
      PropertiesSet entryProps=(PropertiesSet)activeCountDataEntry;
      //int count=((Integer)entryProps.getProperty("Set_ActiveCount")).intValue();
      Object[] effectsList=(Object[])entryProps.getProperty("Set_EffectDataList");
      if (effectsList==null)
      {
        continue;
      }
      int count=((Integer)entryProps.getProperty("Set_ActiveCount")).intValue();
      SetBonus bonus=set.getBonus(count);
      handleSetBonusEffects(bonus,effectsList);
    }
  }

  private void handleSetBonusEffects(SetBonus bonus, Object[] effects)
  {
    for(Object effectObj : effects)
    {
      PropertiesSet effectProps=(PropertiesSet)effectObj;
      int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
      Effect effect=_loader.getEffect(effectId);
      Float spellcraft=(Float)effectProps.getProperty("EffectGenerator_EffectSpellcraft");
      spellcraft=Utils.normalize(spellcraft);
      EffectGenerator generator=new EffectGenerator(effect,spellcraft);
      bonus.addEffect(generator);
      // EffectGenerator_EffectDataList
    }
  }
}
