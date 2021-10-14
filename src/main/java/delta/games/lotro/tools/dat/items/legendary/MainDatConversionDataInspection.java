package delta.games.lotro.tools.dat.items.legendary;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.ProgressionFactory;
import delta.games.lotro.utils.StringUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * A tool to inspect 'conversion' data for the new LI system.
 * @author DAM
 */
public class MainDatConversionDataInspection
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatConversionDataInspection(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load legendary titles.
   */
  public void doIt()
  {
    // ItemAdvancementControl
    PropertiesSet properties=_facade.loadProperties(1879108262+DATConstants.DBPROPERTIES_OFFSET);
    System.out.println("Non imbued");
    doArray(properties,"ItemAdvancement_LI2ConversionReward_UnimbuedTotalLevel_Array");
    System.out.println("Imbued");
    doArray(properties,"ItemAdvancement_LI2ConversionReward_ImbuedLegacy_Array");
    // ItemAdvancement_MaxILevelProgression: 1879422471
    doMaxILevelProgression(properties);
  }

  private void doMaxILevelProgression(PropertiesSet props)
  {
    int progressionId=((Integer)props.getProperty("ItemAdvancement_MaxILevelProgression")).intValue();
    PropertiesSet progressionProps=_facade.loadProperties(progressionId+DATConstants.DBPROPERTIES_OFFSET);
    Progression prog=ProgressionFactory.buildProgression(progressionId,progressionProps);
    System.out.println("Max Item Level progression: "+prog);
  }

  private void doArray(PropertiesSet props, String propName)
  {
    Object[] mainArray=(Object[])props.getProperty(propName);
    for(Object entry : mainArray)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      /*
    ItemAdvancement_LI2ConversionReward_Item: 1879424263
    ItemAdvancement_LI2ConversionReward_Progression: 1879430913
       */
      System.out.println("************* entry **************");
      int itemId=((Integer)entryProps.getProperty("ItemAdvancement_LI2ConversionReward_Item")).intValue();
      int progressionId=((Integer)entryProps.getProperty("ItemAdvancement_LI2ConversionReward_Progression")).intValue();
      handleItemIdAndProgression(itemId,progressionId);
    }
  }

  private void handleItemIdAndProgression(int itemId, int progressionId)
  {
    PropertiesSet itemProps=_facade.loadProperties(itemId+DATConstants.DBPROPERTIES_OFFSET);
    PropertiesSet progressionProps=_facade.loadProperties(progressionId+DATConstants.DBPROPERTIES_OFFSET);
    Integer weenieType=(Integer)itemProps.getProperty("WeenieType");
    if (weenieType!=null)
    {
      String itemName=DatUtils.getStringProperty(itemProps,"Name");
      itemName=StringUtils.fixName(itemName);
      System.out.println("Item name: "+itemName);
      Progression prog=ProgressionFactory.buildProgression(progressionId,progressionProps);
      System.out.println(" => "+prog);
    }
    else
    {
      System.out.println("Level to item progression");
      /*
      ArrayProgression itemProg=(ArrayProgression)ProgressionFactory.buildProgression(itemId,itemProps);
      int nbPoints=itemProg.getNumberOfPoints();
      for(int i=0;i<nbPoints;i++)
      {
        int level=itemProg.getX(i);
        int itemId2=itemProg.getYAsInt(i);
        if (itemId2!=0)
        {
          PropertiesSet childProps=_facade.loadProperties(itemId2+DATConstants.DBPROPERTIES_OFFSET);
          String itemName=DatUtils.getStringProperty(childProps,"Name");
          itemName=StringUtils.fixName(itemName);
          Object itemQuality=childProps.getProperty("Item_Quality");
          System.out.println("Level "+level+" => Item name: "+itemName+"; quality: "+itemQuality);
        }
      }
      */
      Progression prog=ProgressionFactory.buildProgression(progressionId,progressionProps);
      System.out.println(" => "+prog);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatConversionDataInspection(facade).doIt();
    facade.dispose();
  }
}
