package delta.games.lotro.tools.dat.items.legendary;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.character.stats.BasicStatsSet;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.io.xml.EffectXMLWriter;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
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

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ConsumablesLoader(DataFacade facade)
  {
    _facade=facade;
    _parsedEffects=new HashMap<Integer,Effect>();
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
      //SpellcraftCalculator: 1879210172
      Integer spellcraftCalculatorId=(Integer)properties.getProperty("SpellcraftCalculator");
      if (spellcraftCalculatorId!=null)
      {
        loadSpellcraftCalculator(spellcraftCalculatorId.intValue());
      }

      int effectIndex1=1;
      for(Object effectObj : effectsOnUse)
      {
        PropertiesSet effectProps=(PropertiesSet)effectObj;
        int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
        Float spellCraft=(Float)effectProps.getProperty("EffectGenerator_EffectSpellcraft");
        handleOnUseEffects(item,effectId,spellCraft,effectIndex1);
        effectIndex1++;
      }
    }
  }

  private void loadSpellcraftCalculator(int spellcraftCalculatorId)
  {
    PropertiesSet calculatorProps=_facade.loadProperties(spellcraftCalculatorId+0x9000000);
    System.out.println(calculatorProps.dump());
    /*
    if (spellcraftCalculatorId!=null)
    {
      Progression spellcraftProgression=DatStatUtils.getProgression(_facade,spellcraftCalculatorId.intValue());
      System.out.println(spellcraftProgression);
    }
    */
  
  }

  private void handleOnUseEffects(Item item, int effectGeneratorId, Float spellcraft, int effectIndex1)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectGeneratorId+0x9000000);
    Object[] effects=(Object[])effectProps.getProperty("EffectGenerator_InstantFellowship_AppliedEffectList");
    if (effects!=null)
    {
      int effectIndex=1;
      for(Object effectGeneratorObj : effects)
      {
        PropertiesSet effectGeneratorProps=(PropertiesSet)effectGeneratorObj;
        int effectId=((Integer)effectGeneratorProps.getProperty("EffectGenerator_EffectID")).intValue();
        handleUseEffect(item, effectId, spellcraft, effectIndex1, effectIndex);
        effectIndex++;
      }
    }
    else
    {
      handleUseEffect(item, effectGeneratorId, spellcraft, effectIndex1, 0);
    }
  }

  private void handleUseEffect(Item item, int effectId, Float spellcraft, int effectIndex1, int effectIndex)
  {
    Integer key=Integer.valueOf(effectId);
    Effect effect=_parsedEffects.get(key);
    if (effect==null)
    {
      effect=DatEffectUtils.loadEffect(_facade,effectId);
      StatsProvider statsProvider=effect.getStatsProvider();
      //if (statsProvider.getNumberOfStatProviders()>0)
      {
        System.out.println("Item: "+item.getName()+", category: "+item.getSubCategory());
        System.out.println("Effect: #"+effectIndex1+"."+effectIndex+": "+effect);
        Integer effectIconId=effect.getIconId();
        if (effectIconId!=null)
        {
          String filename="effect-"+effectIconId+".png";
          File to=new File(filename);
          if (!to.exists())
          {
            DatIconsUtils.buildImageFile(_facade,effectIconId.intValue(),to);
          }
          System.out.println("Icon: "+effectIconId);
        }

        int level=0;
        if ((spellcraft!=null) && (spellcraft.floatValue()>0))
        {
          level=spellcraft.intValue();
        }
        else
        {
          Integer itemLevel=item.getItemLevel();
          if (itemLevel!=null)
          {
            level=itemLevel.intValue();
          }
        }
        if (level!=0)
        {
          BasicStatsSet stats=statsProvider.getStats(1,level);
          System.out.println("Level: "+level+" => "+stats);
        }
        _parsedEffects.put(key,effect);
      }
    }
  }

  /**
   * Save the loaded consumables to a file.
   */
  public void saveConsumables()
  {
    List<Effect> effects=new ArrayList<Effect>(_parsedEffects.values());
    Collections.sort(effects,new IdentifiableComparator<Effect>());
    EffectXMLWriter.write(GeneratedFiles.CONSUMABLES,effects);
  }
}
