package delta.games.lotro.tools.extraction.geo.maps;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.io.Console;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.loaders.ui.UILayoutLoader;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.lore.maps.io.xml.ParchmentMapsXMLWriter;
import delta.games.lotro.maps.data.GeoBox;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.GeoReference;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;
import delta.games.lotro.maps.data.links.LinksManager;
import delta.games.lotro.maps.data.links.MapLink;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Loader for the maps system.
 * @author DAM
 */
public class MapsSystemLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MapsSystemLoader.class);

  private static final int MAP_WINDOW_ID_EOA=268437543;
  private static final int MAP_WINDOW_ID_LIVE=268455092; // MapWindow

  private DataFacade _facade;
  private I18nUtils _i18n;
  private UILayout _uiLayout;
  private EnumMapper _uiElementId;
  private List<ParchmentMap> _maps;
  private MapsManager _mapsManager;
  private GeoreferencedBasemapsLoader _basemapsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param mapsManager Maps manager.
   * @param basemapsLoader Loader for basemaps.
   */
  public MapsSystemLoader(DataFacade facade, MapsManager mapsManager, GeoreferencedBasemapsLoader basemapsLoader)
  {
    _facade=facade;
    _i18n=new I18nUtils("parchmentMaps",facade.getGlobalStringsManager());
    _uiElementId=facade.getEnumsManager().getEnumMapper(587202769);
    _maps=new ArrayList<ParchmentMap>();
    _mapsManager=mapsManager;
    _basemapsLoader=basemapsLoader;
  }

  /**
   * Load the properties for maps system.
   * @return the loaded properties, or <code>null</code> if a problem occurred.
   */
  private PropertiesSet loadMapsSystemProperties()
  {
    _uiLayout=buildLayout();
    boolean live=LotroCoreConfig.isLive();
    int mapWindowID=live?MAP_WINDOW_ID_LIVE:MAP_WINDOW_ID_EOA;
    UIElement mapBackgroundElement=getUIElementById(mapWindowID);
    if (mapBackgroundElement!=null)
    {
      return mapBackgroundElement.getProperties();
    }
    return null;
  }

  private UILayout buildLayout()
  {
    UILayout layout=new UILayoutLoader(_facade).loadUiLayout(0x22000041);
    return layout;
  }

  private UIElement getUIElementById(int id)
  {
    return getUIElementById(id,_uiLayout.getChildElements());
  }

  private UIElement getUIElementById(int id, List<UIElement> elements)
  {
    for(UIElement uiElement : elements)
    {
      int uiElementId=uiElement.getIdentifier();
      if (uiElementId==id)
      {
        return uiElement;
      }
      UIElement foundElement=getUIElementById(id,uiElement.getChildElements());
      if (foundElement!=null)
      {
        return foundElement;
      }
    }
    return null;
  }

  private void handleMapProps(PropertiesSet props, int level, int parentMapId)
  {
    // ActiveElement
    int parchmentMapId=((Integer)props.getProperty("UI_Map_ActiveElement")).intValue();
    String activeElementName=_uiElementId.getString(parchmentMapId);
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Active element: {} ({})",activeElementName,Integer.valueOf(parchmentMapId));
    }
    // Map name
    String mapName=_i18n.getNameStringProperty(props,"UI_Map_MenuName",parchmentMapId,0);
    if (mapName==null)
    {
      mapName="";
    }
    boolean questGuideDisabled=false;
    Integer questGuideDisabledInt=(Integer)props.getProperty("UI_Map_QuestGuideDisabled");
    if ((questGuideDisabledInt!=null) && (questGuideDisabledInt.intValue()==1))
    {
      questGuideDisabled=true;
    }
    Console.println(mapName,level);
    // Push i18n labels in the basemaps file
    I18nUtils i18n=_basemapsLoader.getI18nUtils();
    i18n.getNameStringProperty(props,"UI_Map_MenuName",parchmentMapId,0);

    int imageId=((Integer)props.getProperty("UI_Map_MapImage")).intValue();
    GeoreferencedBasemap basemap=_basemapsLoader.handleBasemap(props,parchmentMapId,imageId,mapName);

    // Links
    UIElement uiElement=getUIElementById(parchmentMapId);
    if (uiElement!=null)
    {
      for(UIElement childElement : uiElement.getChildElements())
      {
        handleChildMap(childElement,basemap);
      }
    }

    ParchmentMap parchmentMap=new ParchmentMap(parchmentMapId,mapName);
    // Region
    Integer regionId=(Integer)props.getProperty("UI_Map_RegionID");
    if (regionId!=null)
    {
      parchmentMap.setRegion(regionId.intValue());
    }
    // Parent map ID
    parchmentMap.setParentMapId(parentMapId);
    // Quest guide disabled?
    parchmentMap.setQuestGuideDisabled(questGuideDisabled);
    // Register map
    _maps.add(parchmentMap);
    // Areas
    handleAreas(props,parchmentMap);
    // Sub maps
    Object[] subMaps=(Object[])props.getProperty("UI_Map_AreaData_Array");
    if (subMaps!=null)
    {
      for(Object subMapObj : subMaps)
      {
        PropertiesSet subMapProps=(PropertiesSet)subMapObj;
        handleMapProps(subMapProps,level+1,parchmentMapId);
      }
    }
  }

  private void handleChildMap(UIElement childElement, GeoreferencedBasemap basemap)
  {
    Integer childMapUI=findChildMap(childElement);
    if (childMapUI!=null)
    {
      Point location=childElement.getRelativeBounds().getLocation();
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("\t\tChild map: {}",childMapUI);
        PropertiesSet childProps=childElement.getProperties();
        String tooltip=DatStringUtils.getStringProperty(childProps,"UICore_Element_tooltip_entry");
        LOGGER.debug("\t\tTooltip: {}",tooltip);
        LOGGER.debug("\t\tLocation: {}",location);
      }
      // Add link
      int target=childMapUI.intValue();
      GeoReference geoReference=basemap.getGeoReference();
      GeoPoint hotPoint=geoReference.pixel2geo(new Dimension(location.x+32,location.y+32));
      GeoBox boundingBox=basemap.getBoundingBox();
      if (boundingBox.isInBox(hotPoint))
      {
        int parchmentMapId=basemap.getIdentifier();
        MapLink link=new MapLink(parchmentMapId,0,target,hotPoint,null);
        LinksManager linksManager=_mapsManager.getLinksManager();
        linksManager.addLink(link);
      }
      else
      {
        LOGGER.warn("Point: {} is not in bounding box: {}",hotPoint,boundingBox);
      }
    }
  }

  private void handleAreas(PropertiesSet props, ParchmentMap parchmentMap)
  {
    Object[] areas=(Object[])props.getProperty("UI_Map_AreaDIDs_Array");
    if (areas!=null)
    {
      GeoAreasManager geoAreasMgr=GeoAreasManager.getInstance();
      for(Object areaIdObj : areas)
      {
        int areaId=((Integer)areaIdObj).intValue();
        Area area=geoAreasMgr.getAreaById(areaId);
        if (area!=null)
        {
          parchmentMap.addArea(area);
        }
      }
    }
  }

  private Integer findChildMap(UIElement childElement)
  {
    PropertiesSet props=childElement.getProperties();
    Integer childMapUI=(Integer)props.getProperty("UI_Map_Child_Map");
    if (childMapUI!=null)
    {
      return childMapUI;
    }
    int childBaseId=childElement.getBaseElementId();
    if (childBaseId==0)
    {
      return null;
    }
    if (childBaseId==childElement.getIdentifier())
    {
      return null;
    }
    UIElement baseElement=getUIElementById(childBaseId);
    if (baseElement!=null)
    {
      return findChildMap(baseElement);
    }
    return null;
  }

  /**
   * Load parchment maps and map links.
   */
  public void doIt()
  {
    PropertiesSet props=loadMapsSystemProperties();
    if (props!=null)
    {
      PropertiesSet areaProps=(PropertiesSet)props.getProperty("UI_Map_AreaData");
      handleMapProps(areaProps,0,0);
    }
    // Setup link labels
    setLabels();
    // Fix some maps
    fixMaps();
    // Save
    // - parchment maps data
    boolean ok=ParchmentMapsXMLWriter.writeParchmentMapsFile(GeneratedFiles.PARCHMENT_MAPS,_maps);
    if (ok)
    {
      LOGGER.info("Wrote parchment maps file: {}",GeneratedFiles.PARCHMENT_MAPS);
    }
    _mapsManager.getLinksManager().write();
    // - labels
    _i18n.save();
  }

  private void setLabels()
  {
    GeoreferencedBasemapsManager basemapsManager=_mapsManager.getBasemapsManager();
    LinksManager linksManager=_mapsManager.getLinksManager();
    for(MapLink link : linksManager.getAll())
    {
      int targetMapId=link.getTargetMapKey();
      GeoreferencedBasemap basemap=basemapsManager.getMapById(targetMapId);
      if (basemap!=null)
      {
        String name=basemap.getName();
        String label="To: "+name;
        link.setLabel(label);
      }
      else
      {
        LOGGER.warn("Unknown map ID={}",Integer.valueOf(targetMapId));
      }
    }
  }

  private void fixMaps()
  {
    // Re-parent Deeping-coomb
    ParchmentMap helmsDeep=getMap(268449767);
    Area deepingCoomb=null;
    if (helmsDeep!=null)
    {
      deepingCoomb=helmsDeep.removeArea(1879277189);
    }
    if (deepingCoomb!=null)
    {
      ParchmentMap westfold=getMap(268449758);
      if (westfold!=null)
      {
        westfold.addArea(deepingCoomb);
      }
    }
    // Remove areas from Eriador
    ParchmentMap eriador=getMap(268437557);
    if (eriador!=null)
    {
      eriador.removeAllAreas();
    }
  }

  private ParchmentMap getMap(int id)
  {
    for(ParchmentMap map : _maps)
    {
      if (map.getIdentifier()==id)
      {
        return map;
      }
    }
    return null;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    File rootDir=MapConstants.getRootDir();
    MapsManager mapsManager=new MapsManager(rootDir);
    GeoreferencedBasemapsLoader basemapsLoader=new GeoreferencedBasemapsLoader(facade,mapsManager);
    MapsSystemLoader loader=new MapsSystemLoader(facade,mapsManager,basemapsLoader);
    loader.doIt();
    basemapsLoader.write();
    mapsManager.getLinksManager().write();
  }
}
