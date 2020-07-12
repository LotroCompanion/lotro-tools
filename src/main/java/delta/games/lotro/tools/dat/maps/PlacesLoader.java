package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.loaders.GeoLoader;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Loader for places.
 * @author DAM
 */
public class PlacesLoader
{
  private static final Logger LOGGER=Logger.getLogger(PlacesLoader.class);

  private static final int PLACES_DID=0x0E000004;

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public PlacesLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void loadPlace(ByteArrayInputStream bis)
  {
    System.out.println("****** Place:");
    // Name
    String name=BufferUtils.readPrefixedUtf16String(bis);
    System.out.println("Name: "+name);
    // Position
    DatPosition position=GeoLoader.readPosition(bis);
    System.out.println("Position: "+position);
    // Area or Dungeon ID
    int areaDID=BufferUtils.readUInt32(bis);
    if (areaDID!=0)
    {
      //PropertiesSet areaProps=_facade.loadProperties(areaDID+DATConstants.DBPROPERTIES_OFFSET);
      System.out.println("AreaID="+areaDID/*+" => "+areaProps.dump()*/);
    }
    // Echo
    int areaDIDEcho=BufferUtils.readUInt32(bis);
    if (areaDIDEcho!=areaDID)
    {
      throw new IllegalArgumentException("Mismatch areaID: "+areaDIDEcho+"!="+areaDID);
    }
  }

  /**
   * Load places.
   */
  private void loadPlaces()
  {
    byte[] data=_facade.loadData(PLACES_DID);
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    int did=BufferUtils.readUInt32(bis);
    if (did!=PLACES_DID)
    {
      throw new IllegalArgumentException("Expected DID for places: "+PLACES_DID);
    }
    int count=BufferUtils.readTSize(bis); // > 6k places
    System.out.println(count+" places to load!");
    for(int i=0;i<count;i++)
    {
      loadPlace(bis);
    }
    int available=bis.available();
    if (available>0)
    {
      LOGGER.warn("Available bytes: "+available);
    }
  }

  private void doIt()
  {
    loadPlaces();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    PlacesLoader loader=new PlacesLoader(facade);
    loader.doIt();
  }
}
