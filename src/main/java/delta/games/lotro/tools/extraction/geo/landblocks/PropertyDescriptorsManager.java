package delta.games.lotro.tools.extraction.geo.landblocks;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Manager for property desc(riptors?).
 * @author DAM
 */
public class PropertyDescriptorsManager
{
  private static final Logger LOGGER=LoggerFactory.getLogger(PropertyDescriptorsManager.class);

  private DataFacade _facade;
  private Map<Integer,PropertiesDescriptor> _descriptors;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public PropertyDescriptorsManager(DataFacade facade)
  {
    _facade=facade;
    _descriptors=new HashMap<Integer,PropertiesDescriptor>();
    init();
  }

  private void loadPropertyDescEntry(ByteArrayInputStream bis, PropertiesDescriptor storage)
  {
    // Property ID
    int propertyID=BufferUtils.readUInt32(bis);
    int echo=BufferUtils.readUInt32(bis);
    if (echo!=propertyID)
    {
      throw new IllegalArgumentException("Mismatch property ID: "+propertyID);
    }
    PropertyDefinition propertyDef=_facade.getPropertiesRegistry().getPropertyDef(propertyID);
    if (propertyDef==null)
    {
      throw new IllegalStateException("Property definition not found: ID="+propertyID);
    }
    // Block map key
    int blockMapKey=BufferUtils.readUInt32(bis);
    /*int unknown=*/BufferUtils.readUInt8(bis); // 0 or 1
    int count=BufferUtils.readUInt32(bis);
    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
    for(int i=0;i<count;i++)
    {
      PropertyValue propertyValue=propsLoader.decodeProperty(bis,false);
      storage.addPropertyValue(blockMapKey,propertyValue);
    }
  }

  /**
   * Load some property descriptors.
   * @param propertyDescId Property descriptor ID.
   * @return the loaded data.
   */
  public PropertiesDescriptor loadPropertiesDescriptor(int propertyDescId)
  {
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
    PropertiesDescriptor ret=new PropertiesDescriptor();
    int count=BufferUtils.readTSize(bis);
    for(int i=0;i<count;i++)
    {
      loadPropertyDescEntry(bis,ret);
    }
    int available=bis.available();
    if (available>0)
    {
      LOGGER.warn("Available bytes: {}",Integer.valueOf(available));
    }
    return ret;
  }

  private void handleRegion(int region, int propertyDescId)
  {
    PropertiesDescriptor descriptor=loadPropertiesDescriptor(propertyDescId);
    _descriptors.put(Integer.valueOf(region),descriptor);
  }

  private void init()
  {
    handleRegion(1,0x18000000);
    if (Context.isLive())
    {
      handleRegion(2,0x18000014);
      handleRegion(3,0x18000015);
      handleRegion(4,0x1800001a);
      handleRegion(5,0x1800001a);
      handleRegion(14,0x18000014);
    }
  }

  /**
   * Get the properties descriptor for a region.
   * @param region Region to use.
   * @return A properties descriptor or <code>null</code> if not found.
   */
  public PropertiesDescriptor getDescriptorForRegion(int region)
  {
    return _descriptors.get(Integer.valueOf(region));
  }
}
