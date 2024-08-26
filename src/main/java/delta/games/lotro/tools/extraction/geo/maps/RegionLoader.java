package delta.games.lotro.tools.extraction.geo.maps;

import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.DIDMapper;
import delta.games.lotro.dat.data.enums.DIDMapper.DidMapEntry;
import delta.games.lotro.dat.loaders.DataIdMapLoader;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Region description loader.
 * @author DAM
 */
public class RegionLoader
{
  private static final Logger LOGGER=Logger.getLogger(RegionLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Facade.
   */
  public RegionLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void doIt()
  {
    byte[] data=_facade.loadData(0x28000000);
    DIDMapper map=DataIdMapLoader.decodeDataIdMap(data);
    Integer dataId=map.getDataIdForLabel("REGION");
    if (dataId!=null)
    {
      data=_facade.loadData(dataId.intValue());
      DIDMapper subMap=DataIdMapLoader.decodeDataIdMap(data);
      for(Integer key : subMap.getKeys())
      {
        LOGGER.debug("Key="+key);
        DidMapEntry entry=subMap.getEntry(key.intValue());
        int did=entry.getDID();
        String label=entry.getLabel();
        LOGGER.debug("\tDID="+did+", label="+label);
        if (did!=0)
        {
          loadRegion(did,label);
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void loadRegion(int did, String label)
  {
    byte[] buffer=_facade.loadData(did);
    ByteArrayInputStream bis=new ByteArrayInputStream(buffer);
    int did2=BufferUtils.readUInt32(bis);
    int region=BufferUtils.readUInt32(bis);
    int imageDID=BufferUtils.readUInt32(bis);
    int always2=BufferUtils.readUInt32(bis);
    int originLX=BufferUtils.readUInt32(bis);
    int originLY=BufferUtils.readUInt32(bis);
    float alwaysZero=BufferUtils.readFloat(bis);
    alwaysZero=BufferUtils.readFloat(bis);
    float originToMap=BufferUtils.readFloat(bis);
    BufferUtils.skip(bis,6); // always 1 1 0 0 0 0
    int sceneDescDID=BufferUtils.readUInt32(bis);
    BufferUtils.skip(bis,6); // "
    int terrainDescDID=BufferUtils.readUInt32(bis);
    BufferUtils.skip(bis,6); // "
    int encounterDescDID=BufferUtils.readUInt32(bis);
    BufferUtils.skip(bis,6); // "
    int skyDescDID=BufferUtils.readUInt32(bis);
    BufferUtils.skip(bis,6); // "
    int waterDescDID=BufferUtils.readUInt32(bis);
    BufferUtils.skip(bis,6); // "
    int fogDescDID=BufferUtils.readUInt32(bis);
    BufferUtils.skip(bis,6); // "
    int propertyDescDID=BufferUtils.readUInt32(bis);
    BufferUtils.skip(bis,6); // "
    int terrainTypeTableDID=BufferUtils.readUInt32(bis);
    LOGGER.debug("Property Descriptor ID: "+propertyDescDID);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new RegionLoader(facade).doIt();
  }
}
