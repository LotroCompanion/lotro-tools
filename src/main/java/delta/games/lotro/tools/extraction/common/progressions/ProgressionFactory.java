package delta.games.lotro.tools.extraction.common.progressions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.tools.extraction.utils.StatValueConverter;
import delta.games.lotro.utils.maths.ArrayProgression;
import delta.games.lotro.utils.maths.ArrayProgressionConstants;
import delta.games.lotro.utils.maths.LinearInterpolatingProgression;
import delta.games.lotro.utils.maths.Progression;

/**
 * Factory for progression curves.
 * @author DAM
 */
public class ProgressionFactory
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ProgressionFactory.class);

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
      ret=buildArrayProgression(progressionId,ArrayProgressionConstants.FLOAT,properties,"FloatProgression_Array");
    }
    if (ret==null)
    {
      ret=buildPropertyProgression(progressionId,properties);
    }
    if (ret==null)
    {
      ret=buildArrayProgression(progressionId,ArrayProgressionConstants.FLOAT,properties,"Combat_BaseDPSArray");
    }
    if (ret==null)
    {
      LOGGER.warn("Could not build progression with properties: "+properties.dump());
    }
    return ret;
  }

  private static ArrayProgression buildPropertyProgression(int progressionId, PropertiesSet properties)
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
    String valueType=getValueClass(type);
    return buildArrayProgression(progressionId,valueType,properties,"PropertyProgression_Array");
  }

  private static String getValueClass(PropertyType type)
  {
    if (type==PropertyType.DATA_FILE) return ArrayProgressionConstants.LONG;
    if (type==PropertyType.FLOAT) return ArrayProgressionConstants.FLOAT;
    if (type==PropertyType.INT) return ArrayProgressionConstants.INTEGER;
    return ArrayProgressionConstants.OTHER;
  }

  private static ArrayProgression buildArrayProgression(int progressionId, String type, PropertiesSet properties, String arrayProperty)
  {
    ArrayProgression ret=null;
    ArrayPropertyValue progression=(ArrayPropertyValue)properties.getPropertyValueByName(arrayProperty);
    if (progression!=null)
    {
      // Always 1?
      Integer minXValue=(Integer)properties.getProperty("Progression_MinimumIndexValue");
      int minX=(minXValue!=null)?minXValue.intValue():1;
      int nbItems=progression.getValues().length;
      ret=new ArrayProgression(progressionId,type,minX,nbItems);
      for(int i=0;i<nbItems;i++)
      {
        PropertyValue propValue=progression.getValues()[i];
        Object value=StatValueConverter.convertStatValue(propValue.getDefinition(),propValue.getValue());
        ret.set(i+minX,value);
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
