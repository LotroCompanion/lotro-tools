package delta.games.lotro.tools.extraction.items;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;

/**
 * Loader for decay data.
 * @author DAM
 */
public class ItemDecayLoader
{
  private DataFacade _facade;
  private Map<Integer,Float> _durations;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ItemDecayLoader(DataFacade facade)
  {
    _facade=facade;
    initProbabilities();
  }

  private void initProbabilities()
  {
    _durations=new HashMap<Integer,Float>();
    PropertiesSet properties=WeenieContentDirectory.loadWeenieContentProps(_facade,"ItemDecayControl");
    if (properties==null)
    {
      return;
    }
    Object[] map=(Object[])properties.getProperty("ItemDecayControl_DurationMapList");
    for(Object entryObj : map)
    {
      PropertiesSet entryProps=(PropertiesSet)entryObj;
      float duration=((Float)entryProps.getProperty("ItemDecayControl_DurationValue")).floatValue();
      int type=((Integer)entryProps.getProperty("ItemDecayControl_DurationType")).intValue();
      _durations.put(Integer.valueOf(type),Float.valueOf(duration));
    }
  }

  /**
   * Get the duration associated to the given code.
   * @param type Duration type.
   * @return A duration (seconds) or <code>null</code>.
   */
  public Float getDuration(Integer type)
  {
    if (type!=null)
    {
      return _durations.get(type);
    }
    return null;
  }
}
