package delta.games.lotro.tools.dat.misc;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for data in ProgressionControl.
 * @author DAM
 */
public class ProgressionControlLoader
{
  private DataFacade _facade;
  private Map<Integer,Integer> _startingLevels;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ProgressionControlLoader(DataFacade facade)
  {
    _facade=facade;
    _startingLevels=new HashMap<Integer,Integer>();
  }

  /**
   * Load progression data.
   */
  public void loadProgressionData()
  {
    // Load properties for ItemAdvancementControl (0x7900F24A)
    PropertiesSet itemAdvancementControlProps=_facade.loadProperties(1879110218+DATConstants.DBPROPERTIES_OFFSET);
    // Starting levels
    Object[] startingLevelTable=(Object[])itemAdvancementControlProps.getProperty("ProgressionControl_TypeStartingLevelTable");
    for(Object startingLevelObj : startingLevelTable)
    {
      PropertiesSet startingLevelProps=(PropertiesSet)startingLevelObj;
      /*
        ProgressionControl_TypeStartingLevel_Level: 47
        ProgressionControl_TypeStartingLevel_Type: 51 (Type1_2_static)
       */
      int typeCode=((Integer)startingLevelProps.getProperty("ProgressionControl_TypeStartingLevel_Type")).intValue();
      int level=((Integer)startingLevelProps.getProperty("ProgressionControl_TypeStartingLevel_Level")).intValue();
      _startingLevels.put(Integer.valueOf(typeCode),Integer.valueOf(level));
    }
    System.out.println(_startingLevels);
  }

  /**
   * Get the starting level for a progression type.
   * @param typeCode Code of the progression type.
   * @return A starting level or <code>null</code>.
   */
  public Integer getStartingLevel(int typeCode)
  {
    return _startingLevels.get(Integer.valueOf(typeCode));
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new ProgressionControlLoader(facade).loadProgressionData();
    facade.dispose();
  }
}
