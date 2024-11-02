package delta.games.lotro.tools.extraction.utils;

import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.values.ArrayValue;
import delta.games.lotro.values.EnumValue;
import delta.games.lotro.values.StructValue;

/**
 * Converts internal values (from lotro-dat-utils) to value types
 * that can be serialized and used in lotro-core (Java standard types or types from lotro-values). 
 * @author DAM
 */
public class StatValueConverter
{
  private static final Logger LOGGER=LoggerFactory.getLogger(StatValueConverter.class);

  /**
   * Convert a stat value.
   * @param type Property type.
   * @param propertyValue Value to convert.
   * @return the converted value (may be <code>null</code>).
   */
  public static Object convertStatValue(PropertyType type, Object propertyValue)
  {
    if (propertyValue==null)
    {
      return null;
    }
    if (type==PropertyType.LONG64)
    {
      LOGGER.warn("Found long64 type!");
    }
    if ((type==PropertyType.INT) || (type==PropertyType.FLOAT) ||
        (type==PropertyType.LONG64) || (type==PropertyType.BIT_FIELD))
    {
      return propertyValue;
    }
    Object ret=buildValue(type,propertyValue);
    return ret;
  }

  private static Object buildValue(PropertyType type, Object propertyValue)
  {
    if (type==PropertyType.BIT_FIELD32)
    {
      return buildBitSet(((Integer)propertyValue).intValue());
    }
    else if (type==PropertyType.BITFIELD_64)
    {
      return buildBitSet(((Long)propertyValue).longValue());
    }
    else if (type==PropertyType.DATA_FILE)
    {
      return Long.valueOf(((Integer)propertyValue).longValue());
    }
    else if (type==PropertyType.ENUM_MAPPER)
    {
      return buildEnumValue((Integer)propertyValue);
    }
    else if (type==PropertyType.BOOLEAN)
    {
      int value=((Integer)propertyValue).intValue();
      return (value==1)?Boolean.TRUE:Boolean.FALSE;
    }
    return buildGenericValue(propertyValue);
  }

  private static Object buildGenericValue(Object propertyValue)
  {
    if (propertyValue instanceof Object[])
    {
      return buildArray((Object[])propertyValue);
    }
    else if (propertyValue instanceof PropertiesSet)
    {
      return buildStruct((PropertiesSet)propertyValue);
    }
    else if (propertyValue instanceof BitSet)
    {
      return propertyValue;
    }
    else if (propertyValue instanceof Number)
    {
      return propertyValue;
    }
    LOGGER.warn("Unmanaged value: {} => {}", propertyValue.getClass(), propertyValue);
    return propertyValue;
  }

  private static BitSet buildBitSet(int value)
  {
    BitSet ret=new BitSet(32);
    int mask=1;
    for(int i=0;i<32;i++)
    {
      if ((value&mask)!=0)
      {
        ret.set(i);
      }
      mask<<=1;
    }
    return ret;
  }

  private static BitSet buildBitSet(long value)
  {
    BitSet ret=new BitSet(32);
    long mask=1;
    for(int i=0;i<64;i++)
    {
      if ((value&mask)!=0)
      {
        ret.set(i);
      }
      mask<<=1;
    }
    return ret;
  }

  private static ArrayValue buildArray(Object[] arrayValue)
  {
    ArrayValue ret=new ArrayValue();
    for(Object entryValue : arrayValue)
    {
      Object childValue=buildGenericValue(entryValue);
      ret.addValue(childValue);
    }
    return ret;
  }

  private static StructValue buildStruct(PropertiesSet properties)
  {
    StructValue ret=new StructValue();
    for(String key : properties.getPropertyNames())
    {
      Object value=properties.getProperty(key);
      Object convertedValue=buildGenericValue(value);
      ret.setValue(key,convertedValue);
    }
    return ret;
  }

  private static EnumValue buildEnumValue(Integer code)
  {
    EnumValue ret=new EnumValue();
    ret.setValue(code);
    return ret;
  }
}
