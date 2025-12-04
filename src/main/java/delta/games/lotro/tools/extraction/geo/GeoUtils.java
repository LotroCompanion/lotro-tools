package delta.games.lotro.tools.extraction.geo;

import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.common.geo.Position;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.lore.maps.Zone;
import delta.games.lotro.lore.maps.ZoneUtils;
import delta.games.lotro.lore.maps.landblocks.Landblock;
import delta.games.lotro.lore.maps.landblocks.LandblocksManager;

/**
 * Utility methods related to geographic stuff.
 * @author DAM
 */
public class GeoUtils
{
  /**
   * Build an extended position from a DAT position.
   * @param position Position to use.
   * @return An extended position.
   */
  public static ExtendedPosition buildPosition(DatPosition position)
  {
    if (position==null)
    {
      return null;
    }
    ExtendedPosition ret=new ExtendedPosition();
    float[] lonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
    Position pos=new Position(position.getRegion(),lonLat[0],lonLat[1]);
    ret.setPosition(pos);
    Zone zone=null;
    Integer zoneID=getZoneID(position);
    if (zoneID!=null)
    {
      zone=ZoneUtils.getZone(zoneID.intValue());
      ret.setZone(zone);
    }
    return ret;
  }

  /**
   * Get the parent zone (area or dungeon) for a position.
   * @param position Position to use.
   * @return A parent zone identifier or <code>null</code>.
   */
  public static Integer getZoneID(DatPosition position)
  {
    if (position==null)
    {
      return null;
    }
    Integer zoneID=null;
    int region=position.getRegion();
    int blockX=position.getBlockX();
    int blockY=position.getBlockY();
    LandblocksManager mgr=LandblocksManager.getInstance();
    Landblock landblock=mgr.getLandblock(region,blockX,blockY);
    if (landblock!=null)
    {
      int cell=position.getCell();
      zoneID=landblock.getParentData(cell,position.getPosition());
    }
    return zoneID;
  }

  /**
   * Get the regions.
   * @return an array of region IDs.
   */
  public static int[] getRegions()
  {
    boolean isLive=Context.isLive();
    int[] regions=isLive?new int[]{1,2,3,4,5,14}:new int[]{1};
    return regions;
  }

  /**
   * Indicates if the given region ID is supported or not.
   * @param region Region ID.
   * @return <code>true</code> if it is, <code>false</code> otherwise.
   */
  public static boolean isSupportedRegion(int region)
  {
    if (((region<1) || (region>5)) && (region!=14))
    {
      return false;
    }
    return true;
  }
}
