package delta.games.lotro.tools.dat.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
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
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Loader for consumables.
 * @author DAM
 */
public class ConsumablesLoader
{
  private DataFacade _facade;
  private I18nUtils _i18n;
  private DatStatUtils _statUtils;
  private Map<Integer,Effect> _parsedEffects;
  private Integer _spellcraftProperty;
  private List<Consumable> _consumables;
  private Set<Integer> _handledEffectsForCurrentItem;

  private static final int APPARENT_LEVEL_PROPERTY=268457149;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ConsumablesLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("consumables",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18n);
    _parsedEffects=new HashMap<Integer,Effect>();
    _consumables=new ArrayList<Consumable>();
    _handledEffectsForCurrentItem=new HashSet<Integer>();
  }

  /**
   * Reset for a new item.
   */
  public void reset()
  {
    _handledEffectsForCurrentItem.clear();
  }

  /**
   * Find 'on use' effects on an item.
   * @param item Item to use.
   * @param properties Item properties.
   */
  public void handleOnUseEffects(Item item, PropertiesSet properties)
  {
    if (!useItem(item))
    {
      return;
    }
    Object[] effectsOnUse=(Object[])properties.getProperty("EffectGenerator_UsageEffectList");
    if (effectsOnUse!=null)
    {
      //System.out.println(properties.dump());
      Consumable currentConsumable=new Consumable(item);
      // Look for a spellcraft property
      _spellcraftProperty=loadSpellcraftProperty(properties);

      handleEffectGenerators(item,currentConsumable,effectsOnUse,null);
      registerConsumable(currentConsumable);
    }
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

  private void handleEffect(Item item, Consumable currentConsumable, int effectId, Float defaultSpellcraft)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println("Effect props: "+effectProps.dump());
    Object[] effects=(Object[])effectProps.getProperty("EffectGenerator_InstantFellowship_AppliedEffectList");
    if (effects!=null)
    {
      handleEffectGenerators(item,currentConsumable,effects,defaultSpellcraft);
    }
    else
    {
      StatsProvider consumableStatsProvider=currentConsumable.getProvider();
      handleSimpleEffect(item, consumableStatsProvider, effectId, defaultSpellcraft);
    }
  }

  private void handleEffectGenerators(Item item, Consumable currentConsumable, Object[] effects, Float defaultSpellcraft)
  {
    for(Object effectGeneratorObj : effects)
    {
      PropertiesSet effectGeneratorProps=(PropertiesSet)effectGeneratorObj;
      //System.out.println(effectGeneratorProps.dump());
      int childEffectId=((Integer)effectGeneratorProps.getProperty("EffectGenerator_EffectID")).intValue();
      Float spellCraft=(Float)effectGeneratorProps.getProperty("EffectGenerator_EffectSpellcraft");
      spellCraft=fixSpellcraft(spellCraft);
      Float spellcraftToUse=(spellCraft!=null)?spellCraft:defaultSpellcraft;
      handleEffect(item, currentConsumable, childEffectId, spellcraftToUse);
    }
  }

  private void handleSimpleEffect(Item item, StatsProvider consumableStatsProvider, int effectId, Float spellcraft)
  {
    Integer key=Integer.valueOf(effectId);
    Effect effect=_parsedEffects.get(key);
    if (effect==null)
    {
      effect=DatEffectUtils.loadEffect(_statUtils,effectId);
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
    if (_handledEffectsForCurrentItem.contains(key))
    {
      return;
    }
    _handledEffectsForCurrentItem.add(key);
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
        if ((itemLevel!=null) && (itemLevel.intValue()>1))
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
      Consumable currentConsumable=new Consumable(item);
      Float skillSpellcraft=(Float)effectProps.getProperty("Skill_EffectSpellcraft");
      skillSpellcraft=fixSpellcraft(skillSpellcraft);
      //System.out.println("Item: "+item+"  => "+effectID+" (spellcraft: "+skillSpellcraft);
      handleEffect(item,currentConsumable,effectID.intValue(),skillSpellcraft);
      registerConsumable(currentConsumable);
    }
  }

  private Float fixSpellcraft(Float spellcraft)
  {
    if ((spellcraft!=null) && (spellcraft.floatValue()<0))
    {
      spellcraft=null;
    }
    return spellcraft;
  }

  /**
   * Save the loaded consumables to a file.
   */
  public void saveConsumables()
  {
    // Data
    Collections.sort(_consumables,new IdentifiableComparator<Consumable>());
    ConsumableXMLWriter.write(GeneratedFiles.CONSUMABLES,_consumables);
    // Labels
    _i18n.save();
  }
}
