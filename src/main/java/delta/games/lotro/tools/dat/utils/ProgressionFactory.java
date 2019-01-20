package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.utils.maths.ArrayProgression;
import delta.games.lotro.utils.maths.LinearInterpolatingProgression;
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
      ret=buildArrayProgression(progressionId, properties,"PropertyProgression_Array");
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

  private static ArrayProgression buildArrayProgression(int progressionId, PropertiesSet properties, String arrayProperty)
  {
    ArrayProgression ret=null;
    Object[] progression=(Object[])properties.getProperty(arrayProperty);
    if (progression!=null)
    {
      // Always 1?
      Integer minIndexValue=(Integer)properties.getProperty("Progression_MinimumIndexValue");
      int minIndex=(minIndexValue!=null)?minIndexValue.intValue():1;
      int nbItems=progression.length;
      ret=new ArrayProgression(progressionId, nbItems);
      for(int i=0;i<nbItems;i++)
      {
        Number value=(Number)progression[i];
        ret.set(i,i+minIndex,value.floatValue());
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
