package delta.games.lotro.tools.lore.items.dat;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.stats.ItemLevelProgression;
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
   * Build an item level progression from the given properties.
   * @param properties Properties to use.
   * @return A progression or <code>null</code> if not supported.
   */
  public static ItemLevelProgression buildItemLevelProgression(PropertiesSet properties)
  {
    ItemLevelProgression ret=null;
    Object[] charLevel2ItemLevel=(Object[])properties.getProperty("PropertyProgression_Array");
    if (charLevel2ItemLevel!=null)
    {
      int nbPoints=charLevel2ItemLevel.length;
      ret=new ItemLevelProgression(nbPoints);
      Integer levelOffset=(Integer)properties.getProperty("Progression_MinimumIndexValue");
      int delta=((levelOffset!=null)?levelOffset.intValue():0);
      for(int i=0;i<nbPoints;i++)
      {
        int level=i+delta;
        int itemLevel=((Integer)charLevel2ItemLevel[i]).intValue();
        ret.set(i,level,itemLevel);
      }
    }
    return ret;
  }

  /**
   * Build a progression from the given properties.
   * @param properties Properties to use.
   * @return A progression or <code>null</code> if not supported.
   */
  public static Progression buildProgression(PropertiesSet properties)
  {
    Progression ret=buildLinearProgression(properties);
    if (ret==null)
    {
      ret=buildArrayProgression(properties,"FloatProgression_Array");
    }
    if (ret==null)
    {
      ret=buildArrayProgression(properties,"PropertyProgression_Array");
    }
    if (ret==null)
    {
      LOGGER.warn("Could not build progression with properties: "+properties.dump());
    }
    return ret;
  }

  private static ArrayProgression buildArrayProgression(PropertiesSet properties, String arrayProperty)
  {
    ArrayProgression ret=null;
    Object[] progression=(Object[])properties.getProperty(arrayProperty);
    if (progression!=null)
    {
      // Always 1?
      Integer minIndexValue=(Integer)properties.getProperty("Progression_MinimumIndexValue");
      int nbItems=progression.length;
      ret=new ArrayProgression(nbItems);
      for(int i=0;i<nbItems;i++)
      {
        Number value=(Number)progression[i];
        ret.set(i,i+minIndexValue.intValue(),value.floatValue());
      }
    }
    return ret;
  }

  private static LinearInterpolatingProgression buildLinearProgression(PropertiesSet properties)
  {
    LinearInterpolatingProgression ret=null;
    Object[] progression=(Object[])properties.getProperty("LinearInterpolatingProgression_Array");
    if (progression!=null)
    {
      int nbItems=progression.length;
      ret=new LinearInterpolatingProgression(nbItems);
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
