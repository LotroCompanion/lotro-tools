package delta.games.lotro.tools.dat.items.legendary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.consumables.Consumable;
import delta.games.lotro.lore.consumables.io.xml.ConsumableXMLWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatEffectUtils;
import delta.games.lotro.utils.FixedDecimalsInteger;

/**
 * Loader for consumables.
 * @author DAM
 */
public class ConsumablesLoader
{
  private DataFacade _facade;
  private Map<Integer,Effect> _parsedEffects;
  private Consumable _currentConsumable;
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
      int id=item.getIdentifier();
      String name=item.getName();
      String category=item.getSubCategory();
      String icon=item.getIcon();

      _currentConsumable=new Consumable(id,name,icon,category);

      // Look for a spellcraft property
      _spellcraftProperty=loadSpellcraftProperty(properties);

      // Iterate on effects
      for(Object effectObj : effectsOnUse)
      {
        PropertiesSet effectProps=(PropertiesSet)effectObj;
        int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
        Float spellCraft=(Float)effectProps.getProperty("EffectGenerator_EffectSpellcraft");
        handleOnUseEffects(item,effectId,spellCraft);
      }
      // Register consumable
      StatsProvider statsProvider=_currentConsumable.getProvider();
      if (statsProvider.getNumberOfStatProviders()>0)
      {
        _consumables.add(_currentConsumable);
        //BasicStatsSet stats=_currentConsumable.getProvider().getStats(1,120);
        //System.out.println(_currentConsumable.getIdentifier()+"\t"+_currentConsumable.getName()+"\t"+_currentConsumable.getIcon()+"\t"+_currentConsumable.getCategory()+"\t"+stats);
        _currentConsumable=null;
      }
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
      PropertiesSet calculatorProps=_facade.loadProperties(spellcraftCalculatorId.intValue()+0x9000000);
      propertyId=(Integer)calculatorProps.getProperty("Spellcraft_Driver_PropertyName");
    }
    return propertyId;
  }

  private void handleOnUseEffects(Item item, int effectGeneratorId, Float spellcraft)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectGeneratorId+0x9000000);
    Object[] effects=(Object[])effectProps.getProperty("EffectGenerator_InstantFellowship_AppliedEffectList");
    if (effects!=null)
    {
      for(Object effectGeneratorObj : effects)
      {
        PropertiesSet effectGeneratorProps=(PropertiesSet)effectGeneratorObj;
        int effectId=((Integer)effectGeneratorProps.getProperty("EffectGenerator_EffectID")).intValue();
        handleUseEffect(item, effectId, spellcraft);
      }
    }
    else
    {
      handleUseEffect(item, effectGeneratorId, spellcraft);
    }
  }

  private void handleUseEffect(Item item, int effectId, Float spellcraft)
  {
    Integer key=Integer.valueOf(effectId);
    Effect effect=_parsedEffects.get(key);
    if (effect==null)
    {
      effect=DatEffectUtils.loadEffect(_facade,effectId);
      _parsedEffects.put(key,effect);
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
      StatsProvider consumableStatsProvider=_currentConsumable.getProvider();
      if (level!=0)
      {
        BasicStatsSet stats=statsProvider.getStats(1,level);
        for(StatDescription stat : stats.getStats())
        {
          FixedDecimalsInteger value=stats.getStat(stat);
          StatProvider provider=new ConstantStatProvider(stat,value.floatValue());
          consumableStatsProvider.addStatProvider(provider);
        }
      }
      else
      {
        int nbStats=statsProvider.getNumberOfStatProviders();
        for(int i=0;i<nbStats;i++)
        {
          StatProvider provider=statsProvider.getStatProvider(i);
          consumableStatsProvider.addStatProvider(provider);
        }
      }
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
