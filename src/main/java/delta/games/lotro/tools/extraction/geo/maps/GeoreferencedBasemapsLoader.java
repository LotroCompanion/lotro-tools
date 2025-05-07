package delta.games.lotro.tools.extraction.geo.maps;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.maps.data.GeoBox;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.GeoReference;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Loader for georeferenced basemaps.
 * @author DAM
 */
public class GeoreferencedBasemapsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(GeoreferencedBasemapsLoader.class);

  private DataFacade _facade;
  private MapsManager _mapsManager;
  private GeoreferencedBasemapsManager _basemapsManager;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Facade.
   * @param mapsManager Maps manager.
   */
  public GeoreferencedBasemapsLoader(DataFacade facade, MapsManager mapsManager)
  {
    _facade=facade;
    _mapsManager=mapsManager;
    _basemapsManager=mapsManager.getBasemapsManager();
    _i18n=new I18nUtils("basemaps",facade.getGlobalStringsManager());
  }

  /**
   * Get the i18n utils.
   * @return the i18n utils.
   */
  public I18nUtils getI18nUtils()
  {
    return _i18n;
  }

  /**
   * Handle a basemaps.
   * @param mapUiProps Map properties.
   * @param mapId Map identifier.
   * @param imageId Map image identifier.
   * @param mapName Map name.
   * @return the new basemap.
   */
  public GeoreferencedBasemap handleBasemap(PropertiesSet mapUiProps, int mapId, int imageId, String mapName)
  {
    // Map image
    File basemapImageFile=_basemapsManager.getBasemapImageFile(mapId);
    if (!basemapImageFile.exists())
    {
      DatIconsUtils.buildImageFile(_facade,imageId,basemapImageFile);
    }

    // Scale
    // Scale decreases with high level maps:
    // - Mordor/Agarnaith: 0.2375
    // - Mordor: 0.03644
    // - Middle-earth: 0.0173184
    float scale=((Float)mapUiProps.getProperty("UI_Map_Scale")).floatValue();

    // Origin & factor "geo to pixel"
    GeoPoint origin=MapUtils.getOrigin(mapName,scale,mapUiProps);
    float geo2pixel;
    if (origin!=null)
    {
      geo2pixel=scale*200;
    }
    else
    {
      origin=new GeoPoint(0,0);
      geo2pixel=1;
    }
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("\tScale: {}",Float.valueOf(scale));
      LOGGER.debug("\tOrigin: {}",origin);
      LOGGER.debug("\tGeo2pixel: {}",Float.valueOf(geo2pixel));
    }

    GeoReference geoReference=new GeoReference(origin,geo2pixel);
    GeoreferencedBasemap basemap=new GeoreferencedBasemap(mapId,mapName,geoReference);
    // Bounding box
    GeoBox boundingBox=MapUtils.computeBoundingBox(geoReference,basemapImageFile);
    basemap.setBoundingBox(boundingBox);
    // Image ID
    basemap.setImageId(imageId);
    // Register basemap
    _basemapsManager.addBasemap(basemap);
    return basemap;
  }

  /**
   * Write loaded data.
   */
  public void write()
  {
    _basemapsManager.write();
    File toDir=_mapsManager.getLabelsDir();
    _i18n.save(toDir);
  }
}
