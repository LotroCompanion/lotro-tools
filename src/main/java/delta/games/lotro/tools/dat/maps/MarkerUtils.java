package delta.games.lotro.tools.dat.maps;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.wlib.ClassDefinition;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.Marker;

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
    ClassDefinition classDef=dataId.getWClass();
    if (classDef==null)
    {
      return false;
    }
    int classIndex=classDef.getClassIndex();
    if (classIndex==663) return false; // AIRemoteDetector
    if (classIndex==1113) return false; // Generator
    if (classIndex==1445) return false; // Relay
    if (classIndex==1843) return false; // BoolEventBox
    if (classIndex==1892) return false; // IntEventBox
    if (classIndex==2516) return false; // WorldEventGenerator
    String name=dataId.getName();
    if (name.contains("DNT")) return false;
    if (name.contains("TBD")) return false;
    if (name.contains("GNDN")) return false;
    if (name.contains("Invisible Collision Waypoint")) return false;
    if (name.contains("Hotspot Quest Detector")) return false;
    return true;
  }

  private static int getCategoryCode(DataIdentification dataId)
  {
    int classIndex=dataId.getWClass().getClassIndex();
    if (classIndex==1723) return 44; // MonsterTemplate (red circle)
    if (classIndex==1724) return 35; // NPCTemplate (green circle)
    return 59;
  }
}
