package delta.games.lotro.tools.dat.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.enums.ItemClass;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.SpecialEffect;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.consumables.Consumable;
import delta.games.lotro.lore.consumables.io.xml.ConsumableXMLWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;

/**
 * Loader for consumables.
 * @author DAM
 */
public class ConsumablesLoader
{
  private DataFacade _facade;
  private Map<Integer,Effect> _parsedEffects;
  private Integer _spellcraftProperty;
  private List<Consumable> _consumables;

  private static final int APPARENT_LEVEL_PROPERTY=268457149;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ConsumablesLoader(DataFacade facade)
  {
    _facade=facade;
    _parsedEffects=new HashMap<Integer,Effect>();
    _consumables=new ArrayList<Consumable>();
  }

  /**
   * Find 'on use' effects on an item.
   * @param item Item to use.
   * @param properties Item properties.
   */
  public void handleOnUseEffects(Item item, PropertiesSet properties)
  {
    Object[] effectsOnUse=(Object[])properties.getProperty("EffectGenerator_UsageEffectList");
    if (effectsOnUse!=null)
    {
      if (!useItem(item))
      {
        return;
      }

      Consumable currentConsumable=initConsumable(item);
      // Look for a spellcraft property
      _spellcraftProperty=loadSpellcraftProperty(properties);

      // Iterate on effects
      for(Object effectObj : effectsOnUse)
      {
        PropertiesSet effectProps=(PropertiesSet)effectObj;
        int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
        Float spellCraft=(Float)effectProps.getProperty("EffectGenerator_EffectSpellcraft");
        handleOnUseEffects(item,currentConsumable,effectId,spellCraft);
      }
      registerConsumable(currentConsumable);
    }
  }

  private Consumable initConsumable(Item item)
  {
    int id=item.getIdentifier();
    String name=item.getName();
    ItemClass itemClass=item.getItemClass();
    String icon=item.getIcon();

    Consumable ret=new Consumable(id,name,icon,itemClass);
    return ret;
  }

  private void registerConsumable(Consumable currentConsumable)
  {
    // Register consumable
    StatsProvider statsProvider=currentConsumable.getProvider();
    int nbStatProviders=statsProvider.getNumberOfStatProviders();
    int nbEffects=statsProvider.getSpecialEffects().size();
    if ((nbStatProviders>0) || (nbEffects>0))
    {
      _consumables.add(currentConsumable);
    }
 }

  private boolean useItem(Item item)
  {
    String category=item.getSubCategory();
    if ("Quest Item".equals(category)) return false;
    if ("Device".equals(category)) return false;
    //if ("Festival Items".equals(category)) return false;
    return true;
  }

  private Integer loadSpellcraftProperty(PropertiesSet properties)
  {
    Integer propertyId=null;
    Integer spellcraftCalculatorId=(Integer)properties.getProperty("SpellcraftCalculator");
    if (spellcraftCalculatorId!=null)
    {
      PropertiesSet calculatorProps=_facade.loadProperties(spellcraftCalculatorId.intValue()+DATConstants.DBPROPERTIES_OFFSET);
      propertyId=(Integer)calculatorProps.getProperty("Spellcraft_Driver_PropertyName");
    }
    return propertyId;
  }

  private void handleOnUseEffects(Item item, Consumable currentConsumable, int effectGeneratorId, Float spellcraft)
  {
    StatsProvider consumableStatsProvider=currentConsumable.getProvider();
    PropertiesSet effectProps=_facade.loadProperties(effectGeneratorId+DATConstants.DBPROPERTIES_OFFSET);
    Object[] effects=(Object[])effectProps.getProperty("EffectGenerator_InstantFellowship_AppliedEffectList");
    if (effects!=null)
    {
      for(Object effectGeneratorObj : effects)
      {
        PropertiesSet effectGeneratorProps=(PropertiesSet)effectGeneratorObj;
        int effectId=((Integer)effectGeneratorProps.getProperty("EffectGenerator_EffectID")).intValue();
        handleEffect(item, consumableStatsProvider, effectId, spellcraft);
      }
    }
    else
    {
      handleEffect(item, consumableStatsProvider, effectGeneratorId, spellcraft);
    }
  }

  private void handleEffect(Item item, StatsProvider consumableStatsProvider, int effectId, Float spellcraft)
  {
    Integer key=Integer.valueOf(effectId);
    Effect effect=_parsedEffects.get(key);
    if (effect==null)
    {
      effect=DatEffectUtils.loadEffect(_facade,effectId);
      // Remove icon: it is not interesting for consumable effects
      effect.setIconId(null);
      _parsedEffects.put(key,effect);
      /*
      StatsProvider statsProvider=effect.getStatsProvider();
      if (statsProvider.getNumberOfStatProviders()>0)
      {
        System.out.println("Item: "+item+"  => "+effectId+((spellcraft!=null)?" (spellcraft: "+spellcraft+")":""));
        System.out.println(effect);
      }
      */
    }
    StatsProvider statsProvider=effect.getStatsProvider();
    if (statsProvider.getNumberOfStatProviders()>0)
    {
      int level=0;
      if ((spellcraft!=null) && (spellcraft.floatValue()>0))
      {
        level=spellcraft.intValue();
      }
      else if ((_spellcraftProperty!=null) && (_spellcraftProperty.intValue()==APPARENT_LEVEL_PROPERTY))
      {
        // Use character level
      }
      else
      {
        Integer itemLevel=item.getItemLevel();
        if (itemLevel!=null)
        {
          level=itemLevel.intValue();
        }
      }
      int nbStats=statsProvider.getNumberOfStatProviders();
      for(int i=0;i<nbStats;i++)
      {
        StatProvider provider=statsProvider.getStatProvider(i);
        if (level!=0)
        {
          StatDescription stat=provider.getStat();
          Float value=provider.getStatValue(1,level);
          StatProvider consumableProvider=new ConstantStatProvider(stat,value.floatValue());
          consumableProvider.setOperator(provider.getOperator());
          consumableProvider.setDescriptionOverride(provider.getDescriptionOverride());
          consumableStatsProvider.addStatProvider(consumableProvider);
        }
        else
        {
          consumableStatsProvider.addStatProvider(provider);
        }
      }
    }
    for(SpecialEffect specialEffect : statsProvider.getSpecialEffects())
    {
      consumableStatsProvider.addSpecialEffect(specialEffect);
    }
  }

  /**
   * Handle skill effects.
   * @param item Parent items.
   * @param properties Item properties.
   */
  public void handleSkillEffects(Item item, PropertiesSet properties)
  {
    Integer skillID=(Integer)properties.getProperty("Usage_SkillToExecute");
    if (skillID==null)
    {
      return;
    }
    PropertiesSet skillProps=_facade.loadProperties(skillID.intValue()+DATConstants.DBPROPERTIES_OFFSET);
    if (skillProps==null)
    {
      return;
    }
    handleSkillProps(item,skillProps);
  }

  private void handleSkillProps(Item item, PropertiesSet skillProps)
  {
    /*
Skill_AttackHookList: 
  #1: Skill_AttackHookInfo 
    Skill_AttackHook_ActionDurationContributionMultiplier: 0.0
    Skill_AttackHook_TargetEffectList: 
     */
    Object[] attackHookList=(Object[])skillProps.getProperty("Skill_AttackHookList");
    if ((attackHookList==null) || (attackHookList.length==0))
    {
      return;
    }
    for(Object attackHookInfoObj : attackHookList)
    {
      PropertiesSet attackHookInfoProps=(PropertiesSet)attackHookInfoObj;
      Object[] effectList=(Object[])attackHookInfoProps.getProperty("Skill_AttackHook_TargetEffectList");
      if ((effectList!=null) && (effectList.length>0))
      {
        for(Object effectEntry : effectList)
        {
          PropertiesSet effectProps=(PropertiesSet)effectEntry;
          handleSkillEffect(item,effectProps);
        }
      }
    }
  }

  private void handleSkillEffect(Item item, PropertiesSet effectProps)
  {
    Integer effectID=(Integer)effectProps.getProperty("Skill_Effect");
    if ((effectID!=null) && (effectID.intValue()!=0))
    {
      Consumable currentConsumable=initConsumable(item);
      Float skillSpellcraft=(Float)effectProps.getProperty("Skill_EffectSpellcraft");
      if ((skillSpellcraft!=null) && (skillSpellcraft.floatValue()<0))
      {
        skillSpellcraft=null;
      }
      //System.out.println("Item: "+item+"  => "+effectID+" (spellcraft: "+skillSpellcraft);
      handleEffect(item,currentConsumable.getProvider(),effectID.intValue(),skillSpellcraft);
      registerConsumable(currentConsumable);
    }
  }

  /**
   * Save the loaded consumables to a file.
   */
  public void saveConsumables()
  {
    Collections.sort(_consumables,new IdentifiableComparator<Consumable>());
    ConsumableXMLWriter.write(GeneratedFiles.CONSUMABLES,_consumables);
  }
}
