package delta.games.lotro.tools.dat.maps.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;

/**
 * Gather all the property descriptors for a single region.
 * @author DAM
 */
public class PropertiesDescriptor
{
  private Map<Integer,List<PropertyValue>> _data;

  /**
   * Constructor.
   */
  public PropertiesDescriptor()
  {
    _data=new HashMap<Integer,List<PropertyValue>>();
  }

  /**
   * Add a property value
   * @param key Key to use.
   * @param value Value to add.
   */
  public void addPropertyValue(int key, PropertyValue value)
  {
    Integer mapKey=Integer.valueOf(key);
    List<PropertyValue> values=_data.get(mapKey);
    if (values==null)
    {
      values=new ArrayList<PropertyValue>();
      _data.put(mapKey,values);
    }
    values.add(value);
  }

  /**
   * Get the value of a property.
   * @param key Key to use.
   * @param index Index to use.
   * @return the property value or <code>null</code> if not found.
   */
  public PropertyValue getPropertyValue(int key, int index)
  {
    PropertyValue ret=null;
    Integer mapKey=Integer.valueOf(key);
    List<PropertyValue> values=_data.get(mapKey);
    if (values!=null)
    {
      if ((index>=0) && (index<values.size()))
      {
        ret=values.get(index);
      }
      else
      {
        // This happens mainly for properties: Ambient_MusicRegion and Ambient_SoundEnum_BlockMapOverride
        // Assume value is null is these cases
      }
    }
    else
    {
      // Property not found. Very often.
    }
    return ret;
  }
}
