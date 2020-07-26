package delta.games.lotro.tools.dat.maps;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.maps.data.GeoPoint;

/**
 * Utility methods for maps.
 * @author DAM
 */
public class MapUtils
{
  private static final Logger LOGGER=Logger.getLogger(MapUtils.class);

  /**
   * Compute map origin (lon/lat position of top/left corner.
   * @param context Context name.
   * @param scale Scale.
   * @param props Map properties.
   * @return a GeoPoint or <code>null</code>.
   */
  public static GeoPoint getOrigin(String context, float scale, PropertiesSet props)
  {
    Integer guideDisabled=(Integer)props.getProperty("UI_Map_QuestGuideDisabled");
    if ((guideDisabled!=null) && (guideDisabled.intValue()>0))
    {
      return null;
    }
    // Block
    Integer blockX=(Integer)props.getProperty("UI_Map_BlockOffsetX");
    Integer blockY=(Integer)props.getProperty("UI_Map_BlockOffsetY");
    if ((blockX!=null) && (blockY!=null))
    {
      //System.out.println("\tBlock X/Y: "+blockX+"/"+blockY);
    }
    else
    {
      LOGGER.warn("No block data for: "+context+"!");
      return null;
    }
    // Pixel
    Integer pixelOffsetX=(Integer)props.getProperty("UI_Map_PixelOffsetX");
    Integer pixelOffsetY=(Integer)props.getProperty("UI_Map_PixelOffsetY");
    if ((pixelOffsetX!=null) && (pixelOffsetY!=null))
    {
      // Pixel offset from the top/left of the map
      // Matches the position blockX/blockY/ox=0/oy=0
      //System.out.println("\tPixel offset X/Y: "+pixelOffsetX+"/"+pixelOffsetY);
    }
    else
    {
      LOGGER.warn("No pixel data for: "+context+"!");
      return null;
    }

    float internalX=((0-pixelOffsetX.intValue())/scale)+(PositionDecoder.LANDBLOCK_SIZE*blockX.intValue());
    float internalY=((pixelOffsetY.intValue()-0)/scale)+(PositionDecoder.LANDBLOCK_SIZE*blockY.intValue());
    float[] position=PositionDecoder.decodePosition(internalX,internalY);
    return new GeoPoint(position[0],position[1]);
  }
}
