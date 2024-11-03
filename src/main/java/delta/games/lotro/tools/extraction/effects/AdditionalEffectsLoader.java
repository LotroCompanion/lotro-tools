package delta.games.lotro.tools.extraction.effects;

import java.util.HashSet;
import java.util.Set;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectsManager;
import delta.games.lotro.common.effects.PropertyModificationEffect;
import delta.games.lotro.common.stats.GenericConstantStatProvider;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.utils.DataFacadeBuilder;
import delta.games.lotro.values.ArrayValue;
import delta.games.lotro.values.StructValue;

/**
 * Loads effects found in properties that use effects.
 * @author DAM
 */
public class AdditionalEffectsLoader
{
  private EffectLoader _effectsLoader;
  private Set<Integer> _handledEffects;

  /**
   * Constructor.
   * @param effectsLoader Effects loader.
   */
  public AdditionalEffectsLoader(EffectLoader effectsLoader)
  {
    _effectsLoader=effectsLoader;
    _handledEffects=new HashSet<Integer>();
  }

  /**
   * Do it!
   */
  public void doIt()
  {
    for(Effect effect : EffectsManager.getInstance().getEffects())
    {
      handleEffect(effect);
    }
    _effectsLoader.save();
  }

  private void handleEffect(Effect effect)
  {
    Integer key=Integer.valueOf(effect.getIdentifier());
    if (_handledEffects.contains(key))
    {
      return;
    }
    _handledEffects.add(key);
    if (effect instanceof PropertyModificationEffect)
    {
      PropertyModificationEffect propModEffect=(PropertyModificationEffect)effect;
      StatsProvider statsProvider=propModEffect.getStatsProvider();
      if (statsProvider!=null)
      {
        handleStatProvider(statsProvider);
      }
    }
  }

  private void handleStatProvider(StatsProvider statsProvider)
  {
    for(StatProvider statProvider : statsProvider.getStatProviders())
    {
      if (statProvider instanceof GenericConstantStatProvider)
      {
        GenericConstantStatProvider<?> genericProvider=(GenericConstantStatProvider<?>)statProvider;
        Object value=genericProvider.getRawValue();
        handlePropertyValue(value);
      }
    }
  }

  private void handlePropertyValue(Object value)
  {
    if (value instanceof ArrayValue)
    {
      ArrayValue arrayValue=(ArrayValue)value;
      int size=arrayValue.getSize();
      for(int i=0;i<size;i++)
      {
        Object childValue=arrayValue.getValueAt(i);
        handlePropertyValue(childValue);
      }
    }
    if (value instanceof StructValue)
    {
      StructValue structValue=(StructValue)value;
      Object skillEffectID=structValue.getValue("Skill_Effect");
      if (skillEffectID!=null)
      {
        handleStruct(structValue);
      }
    }
  }

  private void handleStruct(StructValue structValue)
  {
    int skillEffectID=((Integer)structValue.getValue("Skill_Effect")).intValue();
    if (skillEffectID==0)
    {
      return;
    }
    Effect effect=_effectsLoader.getEffect(skillEffectID);
    handleEffect(effect);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    AdditionalEffectsLoader loader=new AdditionalEffectsLoader(effectsLoader);
    loader.doIt();
  }
}
