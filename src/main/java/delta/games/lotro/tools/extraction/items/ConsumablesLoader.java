package delta.games.lotro.tools.extraction.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillEffectGenerator;
import delta.games.lotro.character.skills.SkillEffectsUtils;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.effects.InstantFellowshipEffect;
import delta.games.lotro.common.effects.PropertyModificationEffect;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.StatsProviderEntry;
import delta.games.lotro.lore.consumables.Consumable;
import delta.games.lotro.lore.consumables.io.xml.ConsumableXMLWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemUtils;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.details.SkillToExecute;
import delta.games.lotro.lore.items.effects.ItemEffectsManager;
import delta.games.lotro.lore.items.effects.ItemEffectsManager.Type;
import delta.games.lotro.tools.extraction.GeneratedFiles;

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
  private void reset()
  {
    _handledEffectsForCurrentItem.clear();
    _current=null;
  }

  /**
   * Find 'on use' effects on an item.
   * @param item Item to use.
   */
  private void handleOnUseEffects(Item item)
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
    int nbEntries=statsProvider.getEntriesCount();
    if (nbEntries>0)
    {
      _consumables.add(currentConsumable);
    }
 }

  private boolean useItem(Item item)
  {
    String category=item.getSubCategory();
    if ("Quest Item".equals(category)) return false;
    if ("Device".equals(category)) return false;
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
    int nbEntries=statsProvider.getEntriesCount();
    if (nbEntries>0)
    {
      if (_current==null)
      {
        _current=new Consumable(item);
      }
      StatsProvider consumableStatsProvider=_current.getProvider();
      int level=getLevel(item,spellcraft);
      for(int i=0;i<nbEntries;i++)
      {
        StatsProviderEntry entry=statsProvider.getEntry(i);
        if (entry instanceof StatProvider)
        {
          StatProvider provider=(StatProvider)entry;
          Float value=null;
          if (level!=0)
          {
            value=provider.getStatValue(1,level);
          }
          if (value!=null)
          {
            StatDescription stat=provider.getStat();
            ConstantStatProvider consumableProvider=new ConstantStatProvider(stat,value.floatValue());
            consumableProvider.setOperator(provider.getOperator());
            consumableProvider.setDescriptionOverride(provider.getDescriptionOverride());
            consumableProvider.setModifiers(provider.getModifiers());
            consumableStatsProvider.addStatProvider(consumableProvider);
          }
          else
          {
            consumableStatsProvider.addStatProvider(provider);
          }
        }
        else
        {
          consumableStatsProvider.addEntry(entry);
        }
      }
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
  private void handleSkillEffects(Item item)
  {
    SkillToExecute skillToExecute=ItemUtils.getDetail(item,SkillToExecute.class);
    if (skillToExecute==null)
    {
      return;
    }
    SkillDescription skill=skillToExecute.getSkill();
    List<SkillEffectGenerator> effectGenerators=SkillEffectsUtils.getEffects(skill);
    if (effectGenerators.isEmpty())
    {
      return;
    }
    Integer skillLevel=skillToExecute.getLevel();
    Float defaultLevel=(skillLevel!=null)?Float.valueOf(skillLevel.floatValue()):null;
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
  private void saveConsumables()
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
