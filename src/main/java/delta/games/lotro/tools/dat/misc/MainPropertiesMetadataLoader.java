package delta.games.lotro.tools.dat.misc;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get property definitions from DAT files.
 * @author DAM
 */
public class MainPropertiesMetadataLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainPropertiesMetadataLoader.class);

  private DataFacade _facade;
  private PropertiesRegistry _registry;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainPropertiesMetadataLoader(DataFacade facade)
  {
    _facade=facade;
    _registry=_facade.getPropertiesRegistry();
  }

  /*
Sample properties:
************* 1879048848 *****************
PropertyMetaData_AllowedOperatorList: 
  #1: 7
  #2: 6
  #3: 8
  #4: 2
  #5: 5
PropertyMetaData_AuctionPropModCategory: 5
PropertyMetaData_ExaminationMod: 
  PropertyMetaData_ExaminationMod_PropertyOp: 0
PropertyMetaData_HideIdentityExaminationMod: 1
PropertyMetaData_Name: 
  #1: Might
PropertyMetaData_Property: 268436928
  */

  private void load(int indexDataId)
  {
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      //System.out.println("************* "+indexDataId+" *****************");
      //System.out.println(properties.dump());
    }
    else
    {
      LOGGER.warn("Could not handle property metadata ID="+indexDataId);
    }
    // Name
    String propertyName=DatUtils.getStringProperty(properties,"PropertyMetaData_Name");
    // Property ID
    int propertyId=((Integer)properties.getProperty("PropertyMetaData_Property")).intValue();
    // Property definition
    PropertyDefinition propertyDefinition=_registry.getPropertyDef(propertyId);
    String propertyKey=propertyDefinition.getName();
    // Percentage?
    Integer percentage=(Integer)properties.getProperty("PropertyMetaData_DisplayAsPercentage");
    boolean isPercentage=((percentage!=null) && (percentage.intValue()==1));
    String display="ID: "+propertyId+", key="+propertyKey+", name="+propertyName;
    if (isPercentage) {
      display+=", percentage";
    }
    System.out.println(display);
  }

  private void doIt()
  {
    PropertiesSet indexProperties=_facade.loadProperties(1879048724+0x09000000);
    Object[] idsArray=(Object[])indexProperties.getProperty("PropertyMetaData_PropertyMetaDataList");
    for(Object idObj : idsArray)
    {
      int id=((Integer)idObj).intValue();
      load(id);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainPropertiesMetadataLoader(facade).doIt();
    facade.dispose();
  }
}
