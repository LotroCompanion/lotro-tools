package delta.games.lotro.tools.extraction.sounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.id.IdentifiableComparator;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.lotro.jukebox.core.model.base.SoundDescription;

/**
 * Registry for sounds related to a single property.
 * @author DAM
 */
public class PropertySoundsRegistry
{
  private PropertyDefinition _property;
  private Map<Integer,Map<Integer,SoundDescription>> _sounds;

  /**
   * Constructor.
   * @param property Managed property.
   */
  public PropertySoundsRegistry(PropertyDefinition property)
  {
    _property=property;
    _sounds=new HashMap<Integer,Map<Integer,SoundDescription>>();
  }

  /**
   * Get the managed property.
   * @return the managed property.
   */
  public PropertyDefinition getProperty()
  {
    return _property;
  }

  /**
   * Get the managed property values.
   * @return a sorted list of integer values.
   */
  public List<Integer> getPropertyValues()
  {
    List<Integer> ret=new ArrayList<Integer>(_sounds.keySet());
    Collections.sort(ret);
    return ret;
  }

  /**
   * Get the sounds related to a single property value.
   * @param value Value to use.
   * @return A list of sounds, possibly empty but never <code>null</code>.
   */
  public List<SoundDescription> getSoundsForValue(int value)
  {
    Integer key=Integer.valueOf(value);
    List<SoundDescription> ret=new ArrayList<SoundDescription>();
    Map<Integer,SoundDescription> sounds=_sounds.get(key);
    if (sounds!=null)
    {
      ret.addAll(sounds.values());
    }
    Collections.sort(ret,new IdentifiableComparator<SoundDescription>());
    return ret;
   }

  /**
   * Register a sound.
   * @param propertyValue Property value.
   * @param sound Sound to register.
   */
  public void registerSound(int propertyValue, SoundDescription sound)
  {
    Integer key1=Integer.valueOf(propertyValue);
    Map<Integer,SoundDescription> soundsMap=_sounds.get(key1);
    if (soundsMap==null)
    {
      soundsMap=new HashMap<Integer,SoundDescription>();
      _sounds.put(key1,soundsMap);
    }
    Integer soundKey=Integer.valueOf(sound.getIdentifier());
    soundsMap.put(soundKey,sound);
  }
}
