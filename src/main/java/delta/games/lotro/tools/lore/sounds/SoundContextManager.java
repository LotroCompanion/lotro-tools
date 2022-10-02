package delta.games.lotro.tools.lore.sounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyDefinitionComparator;
import delta.lotro.jukebox.core.model.SoundDescription;

/**
 * Manager for context data of sounds.
 * @author DAM
 */
public class SoundContextManager
{
  private Map<PropertyDefinition,PropertySoundsRegistry> _propertyBasedRegistry;

  /**
   * Constructor.
   */
  public SoundContextManager()
  {
    _propertyBasedRegistry=new HashMap<PropertyDefinition,PropertySoundsRegistry>();
  }

  /**
   * Get the managed properties.
   * @return the managed properties.
   */
  public List<PropertyDefinition> getProperties()
  {
    List<PropertyDefinition> ret=new ArrayList<PropertyDefinition>(_propertyBasedRegistry.keySet());
    Collections.sort(ret,new PropertyDefinitionComparator());
    return ret;
  }

  /**
   * Get the sounds registry for the given property.
   * @param propertyDefinition Property to use.
   * @return A sounds registry or <code>null</code> if not known.
   */
  public PropertySoundsRegistry getProperty(PropertyDefinition propertyDefinition)
  {
    return _propertyBasedRegistry.get(propertyDefinition);
  }

  /**
   * Register a single sound with a given property value.
   * @param propertyDefinition Property.
   * @param value Value.
   * @param sound Sound.
   */
  public void registerSound(PropertyDefinition propertyDefinition, int value, SoundDescription sound)
  {
    PropertySoundsRegistry registry=_propertyBasedRegistry.get(propertyDefinition);
    if (registry==null)
    {
      registry=new PropertySoundsRegistry(propertyDefinition);
      _propertyBasedRegistry.put(propertyDefinition,registry);
    }
    registry.registerSound(value,sound);
  }
}
