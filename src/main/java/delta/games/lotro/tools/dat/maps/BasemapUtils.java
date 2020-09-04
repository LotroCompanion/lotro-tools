package delta.games.lotro.tools.dat.maps;

import java.io.File;
import java.util.List;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.maps.data.GeoreferencedBasemap;
import delta.games.lotro.maps.data.MapLink;
import delta.games.lotro.maps.data.io.xml.MapXMLWriter;

/**
 * Basemap utils.
 * @author DAM
 */
public class BasemapUtils
{
  private static File ROOT_DIR=new File("../lotro-maps-db/maps");

  /**
   * Get the directory for a basemap.
   * @param key Basemap key.
   * @return A directory.
   */
  public static File getBasemapDir(String key)
  {
    return new File(ROOT_DIR,key);
  }

  /**
   * Get the file for the image of a basemap.
   * @param key Basemap key.
   * @return An image file.
   */
  public static File getBasemapImageFile(String key)
  {
    File dir=getBasemapDir(key);
    return new File(dir,"map.png");
  }

  /**
   * Save the basemap definition as a XML file.
   * @param basemap Basemap to use.
   */
  public static void saveBaseMap(GeoreferencedBasemap basemap)
  {
    File dir=getBasemapDir(basemap.getKey());
    File mapFile=new File(dir,"map.xml");
    MapXMLWriter.writeMapFile(mapFile,basemap,EncodingNames.UTF_8);
  }

  /**
   * Save links for a basemap.
   * @param key Basemap key.
   * @param links Link to save.
   */
  public static void saveLinks(String key, List<MapLink> links)
  {
    File dir=getBasemapDir(key);
    File linksFile=new File(dir,"links.xml");
    MapXMLWriter.writeLinksFile(linksFile,links);
  }
}
