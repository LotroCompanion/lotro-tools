package delta.games.lotro.tools.dat.maps;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.categories.CategoriesConstants;

/**
 * Marker utilities.
 * @author DAM
 */
public class MarkerUtils
{
  /**
   * Build a marker.
   * @param position Position to use.
   * @param dataId Data identifier.
   * @return A marker or <code>null</code> if rejected.
   */
  public static Marker buildMarker(DatPosition position, DataIdentification dataId)
  {
    if (!accept(dataId))
    {
      return null;
    }
    Marker marker=new Marker();
    float[] lonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
    GeoPoint geoPoint=new GeoPoint(lonLat[0],lonLat[1]);
    int did=dataId.getDid();
    marker.setDid(did);
    marker.setPosition(geoPoint);
    int categoryCode=getCategoryCode(dataId);
    marker.setCategoryCode(categoryCode);
    String text=dataId.getName();
    marker.setLabel(text);
    return marker;
  }

  /**
   * Indicates if the given data identification is eligible to make a marker.
   * @param dataId Data identifier to use.
   * @return <code>true</code> to accept, <code>false</code> to reject.
   */
  public static boolean accept(DataIdentification dataId)
  {
    if (dataId==null)
    {
      return false;
    }
    int classIndex=dataId.getClassIndex();
    if (classIndex==0) return false;
    if (classIndex==663) return false; // AIRemoteDetector
    if (classIndex==1113) return false; // Generator
    if (classIndex==1445) return false; // Relay
    if (classIndex==1843) return false; // BoolEventBox
    if (classIndex==1892) return false; // IntEventBox
    if (classIndex==2516) return false; // WorldEventGenerator
    if (classIndex==1725) return false; // RefereeTemplate
    if (classIndex==1510) return false; // Tripwire
    if (classIndex==1170) return false; // Hotspot
    int did=dataId.getDid();
    if (did==1879131572) return false; // Logic Box Timer
    if (did==1879077024) return false; // Counter Box
    if (did==1879152898) return false; // Skirmish Point Calculator
    if (did==1879343958) return false; // NPC with no name (appears in generators)
    if (did==1879231787) return false; // PhasingBox
    if (did==1879182362) return false; // Boss Manager
    if (did==1879095684) return false; // Camera Shake
    if (did==1879153040) return false; // Drama Director
    if (did==1879199964) return false; // Festival Play Drama Director
    if (did==1879181880) return false; // Encounter Manager
    if (did==1879157753) return false; // Encounter Manager
    if (did==1879078044) return false; // Drama Coach
    if (did==1879095474) return false; // PELockBox
    if (did==1879334488) return false; // PELockBox
    if (did==1879288379) return false; // Private Encounter Shutdown
    if (did==1879086367) return false; // Logic Box
    if (did==1879248354) return false; // Logic Box
    if (did==1879277552) return false; // AI Puppet Combat Zone
    if (did==1879200630) return false; // Cooldown Reset

    String name=dataId.getName();
    if (name.contains("DNT")) return false;
    if (name.contains("TBD")) return false;
    if (name.contains("GNDN")) return false;
    if (name.equals("Random Box")) return false;
    if (name.contains("Invisible Collision Waypoint")) return false;
    if (name.contains("Hotspot Quest Detector")) return false;
    if (name.contains("PatrolBox")) return false;
    return true;
  }

  private static int getCategoryCode(DataIdentification dataId)
  {
    int classIndex=dataId.getClassIndex();
    if (classIndex==1723) return CategoriesConstants.MONSTER; // MonsterTemplate
    if (classIndex==1724) return CategoriesConstants.NPC; // NPC
    if (classIndex==815) return CategoriesConstants.WAYPOINT; // Waypoint
    if (classIndex==794) return CategoriesConstants.CONTAINER; // GameplayContainer
    if (classIndex==804) return CategoriesConstants.MILESTONE; // Milestone
    if (classIndex==793) return CategoriesConstants.DOOR; // Door
    if (classIndex==796) return CategoriesConstants.ITEM; // IItem
    if (classIndex==1210) return CategoriesConstants.LANDMARK; // Landmark
    if (classIndex==1170) return CategoriesConstants.HOTSPOT; // Hotspot
    return CategoriesConstants.OTHER;
  }

  /**
   * Get a block reference from a marker ID.
   * @param markerId Marker identifier.
   * @return A block reference.
   */
  public static BlockReference getBlockForMarker(int markerId)
  {
    int region=((markerId&0xF0000000)>>28)&0xF;
    int bigXBlock=(markerId&0xF000000)>>24;
    int bigYBlock=(markerId&0xF00000)>>20;
    int smallXBlock=(markerId&0xF0000)>>16;
    int smallYBlock=(markerId&0xF000)>>12;
    int blockX=bigXBlock*16+smallXBlock;
    int blockY=bigYBlock*16+smallYBlock;
    return new BlockReference(region,blockX,blockY);
  }
}
