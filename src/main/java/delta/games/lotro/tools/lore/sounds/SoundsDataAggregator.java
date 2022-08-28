package delta.games.lotro.tools.lore.sounds;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.io.streams.IndentableStream;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.lotro.jukebox.core.model.SoundDescription;

/**
 * @author dm
 */
public class SoundsDataAggregator
{
  private SoundsRegistry _soundsRegistry;
  private Map<PropertyDefinition,PropertySoundsRegistry> _propertyBasedRegistry;

  /**
   * Constructor.
   */
  public SoundsDataAggregator()
  {
    _soundsRegistry=new SoundsRegistry();
    _propertyBasedRegistry=new HashMap<PropertyDefinition,PropertySoundsRegistry>();
  }

  /**
   * Get the managed properties.
   * @return the managed properties.
   */
  public List<PropertyDefinition> getProperties()
  {
    List<PropertyDefinition> ret=new ArrayList<PropertyDefinition>(_propertyBasedRegistry.keySet());
    return ret;
  }

  /**
   * Handle a single sound.
   * @param soundID Sound identifier.
   * @param soundChannel Sound channel.
   * @param context Context.
   */
  public void handleSound(int soundID, int soundChannel, Deque<List<PropertyValue>> context)
  {
    int nb=getContextPropertiesCount(context);
    if (nb==0)
    {
      return;
    }
    for(List<PropertyValue> values : context)
    {
      PropertyValue first=values.get(0);
      PropertyDefinition propertyDef=first.getDefinition();
      boolean useProperty=useProperty(propertyDef);
      if (!useProperty)
      {
        continue;
      }
      SoundDescription sound=_soundsRegistry.registerSound(soundID,soundChannel);
      for(PropertyValue propertyValue : values)
      {
        Object value=propertyValue.getValue();
        if (value instanceof Number)
        {
          Number numberValue=(Number)value;
          int intValue=numberValue.intValue();
          registerSound(propertyDef,intValue,sound);
        }
      }
    }
  }

  private void registerSound(PropertyDefinition propertyDefinition, int value, SoundDescription sound)
  {
    PropertySoundsRegistry registry=_propertyBasedRegistry.get(propertyDefinition);
    if (registry==null)
    {
      registry=new PropertySoundsRegistry(propertyDefinition);
      _propertyBasedRegistry.put(propertyDefinition,registry);
    }
    registry.registerSound(value,sound);
  }

  private int getContextPropertiesCount(Deque<List<PropertyValue>> context)
  {
    int nb=0;
    for(List<PropertyValue> values : context)
    {
      nb+=values.size();
    }
    return nb;
  }

  private boolean useProperty(PropertyDefinition propertyDefinition)
  {
    return true;
  }

  /**
   * Dump the contents of this registry to the given stream.
   */
  public void dump()
  {
    IndentableStream out=new IndentableStream(System.out);
    int nbSounds=_soundsRegistry.getKnownSounds().size();
    out.println("Nb sounds: "+nbSounds);
    out.println("Sounds sorted by property");
    for(PropertyDefinition propertyDefinition : getProperties())
    {
      PropertySoundsRegistry registry=_propertyBasedRegistry.get(propertyDefinition);
      registry.dump(out);
    }
  }
}

