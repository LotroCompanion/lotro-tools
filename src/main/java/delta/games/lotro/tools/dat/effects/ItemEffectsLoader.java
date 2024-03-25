package delta.games.lotro.tools.dat.effects;

import org.apache.log4j.Logger;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.details.SkillToExecute;
import delta.games.lotro.lore.items.effects.ItemEffectsManager;
import delta.games.lotro.tools.dat.utils.Utils;

/**
 * Item effects loader.
 * @author DAM
 */
public class ItemEffectsLoader
{
  private static final Logger LOGGER=Logger.getLogger(ItemEffectsLoader.class);

  private EffectLoader _loader;

  /**
   * Constructor.
   * @param loader Effects loader.
   */
  public ItemEffectsLoader(EffectLoader loader)
  {
    _loader=loader;
  }

  /**
   * Get the managed effects loader.
   * @return the managed effects loader.
   */
  public EffectLoader getEffectsLoader()
  {
    return _loader;
  }

  /**
   * Handle an item.
   * @param item Item to use.
   * @param properties Item properties.
   */
  public void handleItem(Item item, PropertiesSet properties)
  {
    // On equip
    handleOnEquipEffects(item,properties);
    // On use
    handleOnUseEffects(item,properties);
    // Skills
    handleSkillEffects(item,properties);
  }

  private void handleOnEquipEffects(Item item, PropertiesSet properties)
  {
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_EquipperEffectList");
    handleItemEffects(item,effects,ItemEffectsManager.Type.ON_EQUIP);
  }

  private void handleOnUseEffects(Item item, PropertiesSet properties)
  {
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_UsageEffectList");
    handleItemEffects(item,effects,ItemEffectsManager.Type.ON_USE);
  }

  private void handleItemEffects(Item item, Object[] effects, ItemEffectsManager.Type type)
  {
    if (effects==null)
    {
      return;
    }
    for(Object effectObj : effects)
    {
      PropertiesSet effectProps=(PropertiesSet)effectObj;
      int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
      Effect effect=_loader.getEffect(effectId);
      Float spellcraft=(Float)effectProps.getProperty("EffectGenerator_EffectSpellcraft");
      spellcraft=Utils.normalize(spellcraft);
      EffectGenerator generator=new EffectGenerator(effect,spellcraft);
      Item.addEffect(item,type,generator);
    }
  }

  /**
   * Handle skill effects.
   * @param item Parent item.
   * @param properties Item properties.
   */
  private void handleSkillEffects(Item item, PropertiesSet properties)
  {
    Integer skillID=(Integer)properties.getProperty("Usage_SkillToExecute");
    if (skillID==null)
    {
      return;
    }
    Integer skillLevel=(Integer)properties.getProperty("Usage_SkillLevel");
    SkillDescription skill=SkillsManager.getInstance().getSkill(skillID.intValue());
    SkillToExecute detail=new SkillToExecute(skill,skillLevel);
    Item.addDetail(item,detail);
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Set skill for item: "+item+" = "+skill+", level="+skillLevel);
    }
  }
}
