package delta.games.lotro.tools.extraction.common;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.common.geo.Position;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.loaders.GeoLoader;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.tools.extraction.geo.GeoUtils;

/**
 * Loader for places.
 * @author DAM
 */
public class PlacesLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(PlacesLoader.class);

  private static final int PLACES_DID=0x0E000004;

  private DataFacade _facade;
  private Map<String,ExtendedPosition> _map;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public PlacesLoader(DataFacade facade)
  {
    _facade=facade;
    _map=new HashMap<String,ExtendedPosition>();
    loadPlaces();
  }

  private void loadPlace(ByteArrayInputStream bis)
  {
    // Name
    String name=BufferUtils.readPrefixedUtf16String(bis);
    // Position
    DatPosition position=GeoLoader.readPosition(bis);
    Integer zoneID=GeoUtils.getZoneID(position);
    float[] lonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
    Position pos=new Position(position.getRegion(),lonLat[0],lonLat[1]);
    ExtendedPosition extPosition=new ExtendedPosition();
    extPosition.setPosition(pos);
    // Area or Dungeon ID
    int areaDID=BufferUtils.readUInt32(bis);
    // Echo
    int areaDIDEcho=BufferUtils.readUInt32(bis);
    if (areaDIDEcho!=areaDID)
    {
      throw new IllegalArgumentException("Mismatch areaID: "+areaDIDEcho+"!="+areaDID);
    }
    extPosition.setZoneID(zoneID);
    _map.put(name,extPosition);
  }

  /**
   * Load places.
   */
  private void loadPlaces()
  {
    byte[] data=_facade.loadData(PLACES_DID);
    if (data==null)
    {
      return;
    }
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    int did=BufferUtils.readUInt32(bis);
    if (did!=PLACES_DID)
    {
      throw new IllegalArgumentException("Expected DID for places: "+PLACES_DID);
    }
    int count=BufferUtils.readTSize(bis); // > 6k places
    for(int i=0;i<count;i++)
    {
      loadPlace(bis);
    }
    int available=bis.available();
    if (available>0)
    {
      LOGGER.warn("Available bytes: {}",Integer.valueOf(available));
    }
  }

  /**
   * Get a position from its name.
   * @param name Name to search.
   * @return A position or <code>null</code> if not found.
   */
  public ExtendedPosition getPositionForName(String name)
  {
    return _map.get(name);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new PlacesLoader(facade);
  }
}
