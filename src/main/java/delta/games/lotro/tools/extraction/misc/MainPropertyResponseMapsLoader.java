package delta.games.lotro.tools.extraction.misc;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.effects.EffectLoader;

/**
 * Loader for effect-based buffs.
 * @author DAM
 */
public class MainPropertyResponseMapsLoader
{
  private DataFacade _facade;
  private PropertyResponseMapsLoader _loader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param propertyResponseMapsLoader Property response maps loader.
   */
  public MainPropertyResponseMapsLoader(DataFacade facade, PropertyResponseMapsLoader propertyResponseMapsLoader)
  {
    _facade=facade;
    _loader=propertyResponseMapsLoader;
  }

  private void handleArea(int areaId)
  {
    PropertiesSet props=_facade.loadProperties(areaId+DATConstants.DBPROPERTIES_OFFSET);
    if (props==null)
    {
      return;
    }
    Integer propertyResponseMapID=(Integer)props.getProperty("Area_PropertyResponseMapDID");
    if ((propertyResponseMapID!=null) && (propertyResponseMapID.intValue()>0))
    {
      _loader.handlePropertyResponseMap(propertyResponseMapID.intValue());
    }
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    GeoAreasManager areasMgr=GeoAreasManager.getInstance();
    for(Area area : areasMgr.getAreas())
    {
      handleArea(area.getIdentifier());
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    PropertyResponseMapsLoader propertyResponseMapsLoader=new PropertyResponseMapsLoader(facade,effectsLoader);
    new MainPropertyResponseMapsLoader(facade,propertyResponseMapsLoader).doIt();
    effectsLoader.save();
    facade.dispose();
  }
}
