package delta.games.lotro.tools.dat.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillEffectGenerator;
import delta.games.lotro.character.skills.SkillEffectsManager;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.effects.InstantFellowshipEffect;
import delta.games.lotro.common.effects.PropertyModificationEffect;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.SpecialEffect;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.lore.consumables.Consumable;
import delta.games.lotro.lore.consumables.io.xml.ConsumableXMLWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemUtils;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.details.SkillToExecute;
import delta.games.lotro.lore.items.effects.ItemEffectsManager;
import delta.games.lotro.lore.items.effects.ItemEffectsManager.Type;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Loader for consumables.
 * @author DAM
 */
public class ConsumablesLoader
{
  private Integer _spellcraftProperty;
  private List<Consumable> _consumables;
  private Set<Integer> _handledEffectsForCurrentItem;
  private Consumable _current;

  private static final int APPARENT_LEVEL_PROPERTY=268457149;

  /**
   * Constructor.
   */
  public ConsumablesLoader()
  {
    _consumables=new ArrayList<Consumable>();
    _handledEffectsForCurrentItem=new HashSet<Integer>();
  }

  /**
   * Reset for a new item.
   */
  public void reset()
  {
    _handledEffectsForCurrentItem.clear();
    _current=null;
  }

  /**
   * Find 'on use' effects on an item.
   * @param item Item to use.
   */
  public void handleOnUseEffects(Item item)
  {
    if (!useItem(item))
    {
      return;
    }
    ItemEffectsManager effectsMgr=item.getEffects();
    if (effectsMgr==null)
    {
      return;
    }
    EffectGenerator[] effectGenerators=effectsMgr.getEffects(Type.ON_USE);
    if (effectGenerators.length>0)
    {
      // Look for a spellcraft property
      //_spellcraftProperty=loadSpellcraftProperty(properties);
      for(EffectGenerator effectGenerator : effectGenerators)
      {
        handleEffectGenerators(item,effectGenerator,null);
      }
    }
  }

  private void registerConsumable(Consumable currentConsumable)
  {
    if (currentConsumable==null)
    {
      return;
    }
    // Register consumable if needed
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

  /*
   * TODO: handle spellcraft calculator.
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
   */

  private void handleEffect(Item item, Effect effect, Float defaultSpellcraft)
  {
    //System.out.println("Effect props: "+effectProps.dump());
    if (effect instanceof InstantFellowshipEffect)
    {
      InstantFellowshipEffect fellowshipEffect=(InstantFellowshipEffect)effect;
      List<EffectGenerator> effectGenerators=fellowshipEffect.getEffects();
      for(EffectGenerator effectGenerator : effectGenerators)
      {
        handleEffectGenerators(item,effectGenerator,defaultSpellcraft);
      }
    }
    else
    {
      handleSimpleEffect(item, effect, defaultSpellcraft);
    }
  }

  private void handleEffectGenerators(Item item, EffectGenerator effectGenerator, Float defaultSpellcraft)
  {
    Effect childEffect=effectGenerator.getEffect();
    Float spellCraft=effectGenerator.getSpellcraft();
    Float spellcraftToUse=(spellCraft!=null)?spellCraft:defaultSpellcraft;
    handleEffect(item, childEffect, spellcraftToUse);
  }

  private void handleSimpleEffect(Item item, Effect effect, Float spellcraft)
  {
    Integer key=Integer.valueOf(effect.getIdentifier());
    if (_handledEffectsForCurrentItem.contains(key))
    {
      return;
    }
    _handledEffectsForCurrentItem.add(key);
    if (effect instanceof PropertyModificationEffect)
    {
      PropertyModificationEffect propModEffect=(PropertyModificationEffect)effect;
      handleStatsProvider(item,propModEffect.getStatsProvider(),spellcraft);
    }
  }

  private void handleStatsProvider(Item item, StatsProvider statsProvider, Float spellcraft)
  {
    if (statsProvider==null)
    {
      return;
    }
    if (statsProvider.getNumberOfStatProviders()>0)
    {
      if (_current==null)
      {
        _current=new Consumable(item);
      }
      StatsProvider consumableStatsProvider=_current.getProvider();
      int level=getLevel(item,spellcraft);
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
      if (_current==null)
      {
        _current=new Consumable(item);
      }
      _current.getProvider().addSpecialEffect(specialEffect);
    }
  }

  private int getLevel(Item item, Float spellcraft)
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
    return level;
  }

  /**
   * Handle skill effects.
   * @param item Parent items.
   */
  public void handleSkillEffects(Item item)
  {
    SkillToExecute skillToExecute=ItemUtils.getDetail(item,SkillToExecute.class);
    if (skillToExecute==null)
    {
      return;
    }
    SkillDescription skill=skillToExecute.getSkill();
    SkillEffectsManager effectsMgr=skill.getEffects();
    if (effectsMgr==null)
    {
      return;
    }
    SkillEffectGenerator[] effectGenerators=effectsMgr.getEffects();
    if (effectGenerators.length==0)
    {
      return;
    }
    Integer skillLevel=skillToExecute.getLevel();
    Float defaultLevel=(skillLevel!=null)?Float.valueOf(skillLevel.intValue()):null;
    for(SkillEffectGenerator effectGenerator : effectGenerators)
    {
      Float skillSpellcraft=effectGenerator.getSpellcraft();
      Float spellcraft=(skillSpellcraft!=null)?skillSpellcraft:defaultLevel;
      Effect effect=effectGenerator.getEffect();
      handleEffect(item,effect,spellcraft);
    }
  }

  /**
   * Save the loaded consumables to a file.
   */
  public void saveConsumables()
  {
    // Data
    Collections.sort(_consumables,new IdentifiableComparator<Consumable>());
    ConsumableXMLWriter.write(GeneratedFiles.CONSUMABLES,_consumables);
  }

  /**
   * Do consumables.
   */
  public void doIt()
  {
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      handleItem(item);
    }
    saveConsumables();
  }

  private void handleItem(Item item)
  {
    reset();
    handleOnUseEffects(item);
    handleSkillEffects(item);
    registerConsumable(_current);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ConsumablesLoader().doIt();
  }
}
