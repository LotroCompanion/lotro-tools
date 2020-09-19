package delta.games.lotro.tools.dat.maps;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.data.ui.UILayoutLoader;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.lore.maps.io.xml.ParchmentMapsXMLWriter;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.GeoReference;
import delta.games.lotro.maps.data.GeoreferencedBasemap;
import delta.games.lotro.maps.data.links.LinksManager;
import delta.games.lotro.maps.data.links.MapLink;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Loader for the maps system.
 * @author DAM
 */
public class MapsSystemLoader
{
  private static final Logger LOGGER=Logger.getLogger(MapsSystemLoader.class);

  private DataFacade _facade;
  private UILayout _uiLayout;
  private EnumMapper _uiElementId;
  private GeoAreasLoader _geoLoader;
  private List<ParchmentMap> _maps;
  private LinksManager _links;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param links Links storage.
   */
  public MapsSystemLoader(DataFacade facade, LinksManager links)
  {
    _facade=facade;
    _uiElementId=facade.getEnumsManager().getEnumMapper(587202769);
    _geoLoader=new GeoAreasLoader(_facade);
    _maps=new ArrayList<ParchmentMap>();
    _links=links;
  }

  /**
   * Load the properties for maps system.
   * @return the loaded properties, or <code>null</code> if a problem occurred.
   */
  public PropertiesSet loadMapsSystemProperties()
  {
    _uiLayout=buildLayout();
    UIElement mapBackgroundElement=getUIElementById(268437543);
    return mapBackgroundElement.getProperties();
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

  private void handleMapProps(PropertiesSet props, int level)
  {
    // ActiveElement
    int activeElementId=((Integer)props.getProperty("UI_Map_ActiveElement")).intValue();
    String activeElementName=_uiElementId.getString(activeElementId);
    //System.out.println("\tActive element: "+activeElementName+" ("+activeElementId+")");

    // Map name
    String mapName=DatUtils.getStringProperty(props,"UI_Map_MenuName");
    if (mapName==null)
    {
      mapName="?";
    }
    //System.out.println("Map name: "+mapName);
    for(int i=0;i<level;i++) System.out.print("\t");
    System.out.println(mapName);

    int key=activeElementId;
    // Map image
    Integer imageId=(Integer)props.getProperty("UI_Map_MapImage");
    if (imageId!=null)
    {
      File imageFile=BasemapUtils.getBasemapImageFile(key);
      if (!imageFile.exists())
      {
        DatIconsUtils.buildImageFile(_facade,imageId.intValue(),imageFile);
      }
    }
    else
    {
      LOGGER.warn("No map image!");
    }

    // Region ID
    Integer regionId=(Integer)props.getProperty("UI_Map_RegionID");

    // Scale
    // Scale decreases with high level maps:
    // - Mordor/Agarnaith: 0.2375
    // - Mordor: 0.03644
    // - Middle-earth: 0.0173184
    float scale=((Float)props.getProperty("UI_Map_Scale")).floatValue();
    //System.out.println("\tScale: "+scale);

    float geo2pixel;
    // Origin
    GeoPoint origin=MapUtils.getOrigin(activeElementName,scale,props);
    //System.out.println("\tOrigin: "+origin);
    if (origin!=null)
    {
      geo2pixel=scale*200;
    }
    else
    {
      origin=new GeoPoint(0,0);
      geo2pixel=1;
    }
    GeoReference geoReference=new GeoReference(origin,geo2pixel);
    GeoreferencedBasemap basemap=new GeoreferencedBasemap(key);
    basemap.setGeoReference(geoReference);
    basemap.setName(mapName);
    BasemapUtils.saveBaseMap(basemap);

    // Links
    UIElement uiElement=getUIElementById(activeElementId);
    if (uiElement!=null)
    {
      for(UIElement childElement : uiElement.getChildElements())
      {
        Integer childMapUI=findChildMap(childElement);
        if (childMapUI!=null)
        {
          //System.out.println("\t\tChild map: "+childMapUI);
          //String tooltip=DatStringUtils.getStringProperty(childProps,"UICore_Element_tooltip_entry");
          //System.out.println("\t\tTooltip: "+tooltip);
          Point location=childElement.getRelativeBounds().getLocation();
          //System.out.println("\t\tLocation: "+location);

          // Add link
          int target=childMapUI.intValue();
          GeoPoint hotPoint=geoReference.pixel2geo(new Dimension(location.x+32,location.y+32));
          MapLink link=new MapLink(activeElementId,0,target,hotPoint);
          _links.addLink(link);
        }
      }
    }

    ParchmentMap parchmentMap=new ParchmentMap(activeElementId,mapName);
    if (regionId!=null)
    {
      parchmentMap.setRegion(regionId.intValue());
    }
    _maps.add(parchmentMap);
    // Areas
    Object[] areas=(Object[])props.getProperty("UI_Map_AreaDIDs_Array");
    if (areas!=null)
    {
      for(Object areaIdObj : areas)
      {
        int areaId=((Integer)areaIdObj).intValue();
        Area area=_geoLoader.getArea(areaId);
        if (area!=null)
        {
          parchmentMap.addArea(area);
        }
      }
    }
    // Sub maps
    Object[] subMaps=(Object[])props.getProperty("UI_Map_AreaData_Array");
    if (subMaps!=null)
    {
      for(Object subMapObj : subMaps)
      {
        PropertiesSet subMapProps=(PropertiesSet)subMapObj;
        handleMapProps(subMapProps,level+1);
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
    //int childId=childElement.getIdentifier();
    int childBaseId=childElement.getBaseElementId();
    if (childBaseId==0)
    {
      return null;
    }
    //System.out.println("Child ID/base ID: "+childId+"/"+childBaseId);
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
      handleMapProps(areaProps,0);
    }
    // Save parchment maps
    boolean ok=ParchmentMapsXMLWriter.writeParchmentMapsFile(GeneratedFiles.PARCHMENT_MAPS,_maps);
    if (ok)
    {
      System.out.println("Wrote parchment maps file: "+GeneratedFiles.PARCHMENT_MAPS);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    File rootDir=new File("../lotro-maps-db");
    LinksManager linksManager=new LinksManager(rootDir);
    MapsSystemLoader loader=new MapsSystemLoader(facade,linksManager);
    loader.doIt();
  }
}
