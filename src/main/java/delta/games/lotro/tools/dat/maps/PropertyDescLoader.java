package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Loader for property desc(riptors?).
 * @author DAM
 */
public class PropertyDescLoader
{
  private static final int[] PROPERTY_DESC_DIDS= { 0x18000000, 0x18000014, 0x18000015, 0x1800001a};

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public PropertyDescLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void loadPropertyDescEntr(ByteArrayInputStream bis, int propertyDescId)
  {
    System.out.println("****** Property descriptor entry:");
    // Property ID
    int propertyID=BufferUtils.readUInt32(bis);
    int echo=BufferUtils.readUInt32(bis);
    if (echo!=propertyID)
    {
      throw new IllegalArgumentException("Mismatch property ID: "+propertyID);
    }
    PropertyDefinition propertyDef=_facade.getPropertiesRegistry().getPropertyDef(propertyID);
    System.out.println(propertyDef);
    // Block map key
    int blockMapKey=BufferUtils.readUInt32(bis);
    System.out.println("Block map key: "+blockMapKey);
    /*int unknown=*/BufferUtils.readUInt8(bis); // 0 or 1
    int count=BufferUtils.readUInt32(bis);
    System.out.println(count+" properties to load!");
    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
    for(int i=0;i<count;i++)
    {
      PropertyValue propertyValue=propsLoader.decodeProperty(bis,false);
      Object value=propertyValue.getValue();
      if (value instanceof Integer)
      {
        int intValue=((Integer)value).intValue();
        System.out.println(intValue);
      }
      else if (value instanceof Float)
      {
        float floatValue=((Float)value).floatValue();
        System.out.println(floatValue);
      }
      else
      {
        System.out.println("Unmanaged value type: "+value.getClass());
      }
    }
  }

  /**
   * Load a property descriptor.
   * @param propertyDescId Property descriptor ID.
   */
  private void loadPropertyDesc(int propertyDescId)
  {
    System.out.println("************** Property descriptor: "+propertyDescId);
    byte[] data=_facade.loadData(propertyDescId);
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    int did=BufferUtils.readUInt32(bis);
    if (did!=propertyDescId)
    {
      throw new IllegalArgumentException("Expected DID for property desc: "+propertyDescId);
    }
    int zero=BufferUtils.readUInt32(bis);
    if (zero!=0)
    {
      throw new IllegalArgumentException("Expected 0 here. Found: "+zero);
    }
    int count=BufferUtils.readTSize(bis);
    System.out.println(count+" property descriptor entries to load!");
    for(int i=0;i<count;i++)
    {
      loadPropertyDescEntr(bis,did);
    }
  }

  private void doIt()
  {
    for(int propertyDescId : PROPERTY_DESC_DIDS)
    {
      loadPropertyDesc(propertyDescId);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    PropertyDescLoader loader=new PropertyDescLoader(facade);
    loader.doIt();
  }
}
