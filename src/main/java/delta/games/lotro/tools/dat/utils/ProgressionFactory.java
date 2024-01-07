package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.utils.maths.AbstractArrayProgression;
import delta.games.lotro.utils.maths.ArrayProgression;
import delta.games.lotro.utils.maths.LinearInterpolatingProgression;
import delta.games.lotro.utils.maths.LongArrayProgression;
import delta.games.lotro.utils.maths.Progression;

/**
 * Factory for progression curves.
 * @author DAM
 */
public class ProgressionFactory
{
  private static final Logger LOGGER=Logger.getLogger(ProgressionFactory.class);

  /**
   * Build a progression from the given properties.
   * @param progressionId Progression identifier.
   * @param properties Properties to use.
   * @return A progression or <code>null</code> if not supported.
   */
  public static Progression buildProgression(int progressionId, PropertiesSet properties)
  {
    Progression ret=buildLinearProgression(progressionId, properties);
    if (ret==null)
    {
      ret=buildArrayProgression(progressionId, properties,"FloatProgression_Array");
    }
    if (ret==null)
    {
      ret=buildPropertyProgression(progressionId, properties);
    }
    if (ret==null)
    {
      ret=buildArrayProgression(progressionId, properties,"Combat_BaseDPSArray");
    }
    if (ret==null)
    {
      LOGGER.warn("Could not build progression with properties: "+properties.dump());
    }
    return ret;
  }

  private static AbstractArrayProgression buildPropertyProgression(int progressionId, PropertiesSet properties)
  {
    PropertyValue propertyValue=properties.getPropertyValueByName("PropertyProgression_Array");
    if (propertyValue==null)
    {
      return null;
    }
    // Find value type
    ArrayPropertyValue arrayPropertyValue=(ArrayPropertyValue)propertyValue;
    PropertyValue[] values=arrayPropertyValue.getValues();
    PropertyDefinition propertyDefinition=values[0].getDefinition();
    PropertyType type=propertyDefinition.getPropertyType();
    Class<?> valueClass=getValueClass(type);
    if (valueClass==Long.class)
    {
      return buildLongArrayProgression(progressionId,properties,"PropertyProgression_Array");
    }
    if (valueClass==Float.class)
    {
      return buildArrayProgression(progressionId,properties,"PropertyProgression_Array");
    }
    return null;
  }

  private static Class<? extends Number> getValueClass(PropertyType type)
  {
    if (type==PropertyType.DATA_FILE) return Long.class;
    if (type==PropertyType.FLOAT) return Float.class;
    if (type==PropertyType.INT) return Long.class;
    if (type==PropertyType.ENUM_MAPPER) return Long.class;
    LOGGER.warn("Unsupported property progression type: "+type);
    return Float.class;
  }

  private static ArrayProgression buildArrayProgression(int progressionId, PropertiesSet properties, String arrayProperty)
  {
    ArrayProgression ret=null;
    Object[] progression=(Object[])properties.getProperty(arrayProperty);
    if (progression!=null)
    {
      int nbItems=progression.length;
      ret=new ArrayProgression(progressionId, nbItems);
      // Always 1?
      Integer minIndexValue=(Integer)properties.getProperty("Progression_MinimumIndexValue");
      int minIndex=(minIndexValue!=null)?minIndexValue.intValue():1;
      fillArrayProgression(ret,minIndex,progression);
    }
    return ret;
  }

  private static void fillArrayProgression(ArrayProgression ret, int minIndex, Object[] progression)
  {
    for(int i=0;i<progression.length;i++)
    {
      Number value=(Number)progression[i];
      ret.set(i,i+minIndex,value.floatValue());
    }
  }

  private static LongArrayProgression buildLongArrayProgression(int progressionId, PropertiesSet properties, String arrayProperty)
  {
    LongArrayProgression ret=null;
    Object[] progression=(Object[])properties.getProperty(arrayProperty);
    if (progression!=null)
    {
      int nbItems=progression.length;
      ret=new LongArrayProgression(progressionId, nbItems);
      // Always 1?
      Integer minIndexValue=(Integer)properties.getProperty("Progression_MinimumIndexValue");
      int minIndex=(minIndexValue!=null)?minIndexValue.intValue():1;
      for(int i=0;i<progression.length;i++)
      {
        Number value=(Number)progression[i];
        ret.set(i,i+minIndex,value.longValue());
      }
    }
    return ret;
  }

  private static LinearInterpolatingProgression buildLinearProgression(int progressionId, PropertiesSet properties)
  {
    LinearInterpolatingProgression ret=null;
    Object[] progression=(Object[])properties.getProperty("LinearInterpolatingProgression_Array");
    if (progression!=null)
    {
      int nbItems=progression.length;
      ret=new LinearInterpolatingProgression(progressionId, nbItems);
      for(int i=0;i<nbItems;i++)
      {
        PropertiesSet pointProperties=(PropertiesSet)progression[i];
        Integer key=(Integer)pointProperties.getProperty("LinearInterpolatingProgression_Key");
        Float value=(Float)pointProperties.getProperty("LinearInterpolatingProgression_Value");
        ret.set(i,key.intValue(),value.floatValue());
      }
    }
    return ret;
  }
}
