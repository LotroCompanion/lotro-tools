package delta.games.lotro.tools.extraction.geo.maps;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.Identifiable;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.lore.maps.AbstractMap;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.lore.maps.ParchmentMapsManager;
import delta.games.lotro.lore.maps.ZoneUtils;
import delta.games.lotro.maps.data.GeoBox;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.GeoReference;

/**
 * Utility methods for maps.
 * @author DAM
 */
public class MapUtils
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MapUtils.class);

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
      LOGGER.debug("\tBlock X/Y: {}/{}",blockX,blockY);
    }
    else
    {
      LOGGER.warn("No block data for: {}!",context);
      return null;
    }
    // Pixel
    Integer pixelOffsetX=(Integer)props.getProperty("UI_Map_PixelOffsetX");
    Integer pixelOffsetY=(Integer)props.getProperty("UI_Map_PixelOffsetY");
    if ((pixelOffsetX!=null) && (pixelOffsetY!=null))
    {
      // Pixel offset from the top/left of the map
      // Matches the position blockX/blockY/ox=0/oy=0
      LOGGER.debug("\tPixel offset X/Y: {}/{}",pixelOffsetX,pixelOffsetY);
    }
    else
    {
      LOGGER.warn("No pixel data for: {}!",context);
      return null;
    }

    float internalX=((0-pixelOffsetX.intValue())/scale)+(PositionDecoder.LANDBLOCK_SIZE*blockX.intValue());
    float internalY=((pixelOffsetY.intValue()-0)/scale)+(PositionDecoder.LANDBLOCK_SIZE*blockY.intValue());
    float[] position=PositionDecoder.decodePosition(internalX,internalY);
    return new GeoPoint(position[0],position[1]);
  }

  /**
   * Get the geographic bounding box for a basemap.
   * @param geoRef Geographic reference.
   * @param imageFile Image file.
   * @return A geographic or <code>null</code> if computation fails.
   */
  public static GeoBox computeBoundingBox(GeoReference geoRef, File imageFile)
  {
    GeoPoint start=geoRef.getStart();
    Dimension imageSize=getImageDimension(imageFile);
    if (imageSize==null)
    {
      return null;
    }
    GeoPoint end=geoRef.pixel2geo(imageSize);
    GeoBox ret=new GeoBox(start,end);
    return ret;
  }

  private static Dimension getImageDimension(File imageFile)
  {
    Iterator<ImageReader> iter=ImageIO.getImageReadersBySuffix("png");
    if (iter.hasNext())
    {
      ImageReader reader=iter.next();
      try(ImageInputStream stream=new FileImageInputStream(imageFile))
      {
        reader.setInput(stream);
        int width=reader.getWidth(reader.getMinIndex());
        int height=reader.getHeight(reader.getMinIndex());
        return new Dimension(width,height);
      }
      catch (IOException e)
      {
        // Ignored
      }
      finally
      {
        reader.dispose();
      }
    }
    return null;
  }

  /**
   * Find the map (Dungeon/ParchmentMap) for a given zone (Dungeon/Area)
   * @param zoneId Zone identifier.
   * @return the found map or <code>null</code>.
   */
  public static AbstractMap findMapForZone(int zoneId)
  {
    Identifiable zone=ZoneUtils.getZone(zoneId);
    if (zone instanceof Dungeon)
    {
      return (AbstractMap)zone;
    }
    if (zone instanceof Area)
    {
      // Find parent map...
      ParchmentMapsManager parchmentMapsManager=ParchmentMapsManager.getInstance();
      ParchmentMap parchmentMap=parchmentMapsManager.getParchmentMapForArea(zoneId);
      if (parchmentMap!=null)
      {
        return parchmentMap;
      }
    }
    return null;
  }
}
