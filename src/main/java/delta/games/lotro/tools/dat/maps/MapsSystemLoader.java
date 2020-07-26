package delta.games.lotro.tools.dat.maps;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.data.ui.UILayoutLoader;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.GeoReference;
import delta.games.lotro.maps.data.Map;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapLink;
import delta.games.lotro.maps.data.io.xml.MapXMLWriter;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Loader for the maps system.
 * @author DAM
 */
public class MapsSystemLoader
{
  private static final Logger LOGGER=Logger.getLogger(MapsSystemLoader.class);

  private static final String LOCALE="en";
  private DataFacade _facade;
  private UILayout _uiLayout;
  private EnumMapper _uiElementId;
  private GeoAreasLoader _geoLoader;
  private File _rootDir;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapsSystemLoader(DataFacade facade)
  {
    _facade=facade;
    _uiElementId=facade.getEnumsManager().getEnumMapper(587202769);
    _geoLoader=new GeoAreasLoader(_facade);
    _rootDir=new File(new File("data","maps"),"output2");
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

  private void handleMapProps(PropertiesSet props)
  {
    // ActiveElement
    int activeElementId=((Integer)props.getProperty("UI_Map_ActiveElement")).intValue();
    String activeElementName=_uiElementId.getString(activeElementId);
    System.out.println("\tActive element: "+activeElementName+" ("+activeElementId+")");

    String key=String.valueOf(activeElementId);
    File rootDir=new File(new File(_rootDir,"maps"),key);
    MapBundle mapBundle=new MapBundle(key,rootDir);
    Map map=mapBundle.getMap();

    // Map name
    String mapName=DatUtils.getStringProperty(props,"UI_Map_MenuName");
    if (mapName==null)
    {
      mapName="?";
    }
    System.out.println("Map name: "+mapName);
    map.getLabels().putLabel(LOCALE,mapName);

    // Map image
    Integer imageId=(Integer)props.getProperty("UI_Map_MapImage");
    if (imageId!=null)
    {
      File mapRootDir=mapBundle.getRootDir();
      File mapImageFile=new File(mapRootDir,"map_"+LOCALE+".png");
      if (!mapImageFile.exists())
      {
        DatIconsUtils.buildImageFile(_facade,imageId.intValue(),mapImageFile);
      }
    }

    // Region ID
    Integer regionId=(Integer)props.getProperty("UI_Map_RegionID");
    System.out.println("\tRegion: "+regionId);

    // Scale
    // Scale decreases with high level maps:
    // - Mordor/Agarnaith: 0.2375
    // - Mordor: 0.03644
    // - Middle-earth: 0.0173184
    float scale=((Float)props.getProperty("UI_Map_Scale")).floatValue();
    System.out.println("\tScale: "+scale);

    GeoPoint origin;
    float geo2pixel;
    // Bounds
    Rectangle2D.Float bounds=getBounds(activeElementName,scale,props);
    System.out.println("\tBounds: "+bounds);
    if (bounds!=null)
    {
      origin=new GeoPoint(bounds.x,bounds.y+bounds.height);
      geo2pixel=scale*20;
    }
    else
    {
      origin=new GeoPoint(0,0);
      geo2pixel=1;
    }
    GeoReference geoReference=new GeoReference(origin,geo2pixel);
    map.setGeoReference(geoReference);

    // Links
    UIElement uiElement=getUIElementById(activeElementId);
    if (uiElement!=null)
    {
      for(UIElement childElement : uiElement.getChildElements())
      {
        //int childId=childElement.getIdentifier();
        int childBaseId=childElement.getBaseElementId();
        //System.out.println("Child ID/base ID: "+childId+"/"+childBaseId);
        UIElement baseElement=getUIElementById(childBaseId);
        if (baseElement!=null)
        {
          PropertiesSet childProps=baseElement.getProperties();
          Integer childMapUI=(Integer)childProps.getProperty("UI_Map_Child_Map");
          if (childMapUI!=null)
          {
            //System.out.println("\t\tChild map: "+childMapUI);
            //String tooltip=DatStringUtils.getStringProperty(childProps,"UICore_Element_tooltip_entry");
            //System.out.println("\t\tTooltip: "+tooltip);
            Point location=childElement.getRelativeBounds().getLocation();
            //System.out.println("\t\tLocation: "+location);

            // Add link
            String target=childMapUI.toString();
            GeoPoint hotPoint=map.getGeoReference().pixel2geo(new Dimension(location.x+32,location.y+32));
            MapLink link=new MapLink(target,hotPoint);
            mapBundle.getLinks().add(link);
          }
        }
      }
    }

    // Write file
    MapXMLWriter writer=new MapXMLWriter();
    writer.writeMapFiles(mapBundle,EncodingNames.UTF_8);

    // Areas
    Object[] areas=(Object[])props.getProperty("UI_Map_AreaDIDs_Array");
    if (areas!=null)
    {
      for(Object areaIdObj : areas)
      {
        int areaId=((Integer)areaIdObj).intValue();
        _geoLoader.getArea(areaId);
      }
    }
    // Sub maps
    Object[] subMaps=(Object[])props.getProperty("UI_Map_AreaData_Array");
    if (subMaps!=null)
    {
      for(Object subMapObj : subMaps)
      {
        PropertiesSet subMapProps=(PropertiesSet)subMapObj;
        handleMapProps(subMapProps);
      }
    }
  }

  private Rectangle2D.Float getBounds(String activeElementName, float scale, PropertiesSet props)
  {
    Integer guideDisabled=(Integer)props.getProperty("UI_Map_QuestGuideDisabled");
    if ((guideDisabled!=null) && (guideDisabled.intValue()>0))
    {
      return null;
    }
    // Block
    Integer blockX=(Integer)props.getProperty("UI_Map_BlockOffsetX");
    Integer blockY=(Integer)props.getProperty("UI_Map_BlockOffsetY");
    float longitude=0;
    float latitude=0;
    if ((blockX!=null) && (blockY!=null))
    {
      System.out.println("\tBlock X/Y: "+blockX+"/"+blockY);
      float[] pos=PositionDecoder.decodePosition(blockX.intValue(),blockY.intValue(),0,0);
      longitude=pos[0];
      latitude=pos[1];
      System.out.println("\tLat: "+latitude+", Lon: "+longitude);
    }
    else
    {
      LOGGER.warn("No block data for: "+activeElementName+"!");
      return null;
    }
    // Pixel
    Integer pixelOffsetX=(Integer)props.getProperty("UI_Map_PixelOffsetX");
    Integer pixelOffsetY=(Integer)props.getProperty("UI_Map_PixelOffsetY");
    if ((pixelOffsetX!=null) && (pixelOffsetY!=null))
    {
      // Pixel offset from the top/left of the map
      // Matches the position blockX/blockY/ox=0/oy=0
      System.out.println("\tPixel offset X/Y: "+pixelOffsetX+"/"+pixelOffsetY);
    }
    else
    {
      LOGGER.warn("No pixel data for: "+activeElementName+"!");
      return null;
    }
    Integer width=(Integer)props.getProperty("UI_Map_PixelWidth");
    Integer height=(Integer)props.getProperty("UI_Map_PixelHeight");
    if ((width!=null) && (height!=null))
    {
      System.out.println("\tWidth/height: "+width+"/"+height);
    }
    else
    {
      LOGGER.warn("No size data for: "+activeElementName+"!");
      return null;
    }

    float internalX0=((0-pixelOffsetX.intValue())/scale)+(PositionDecoder.LANDBLOCK_SIZE*blockX.intValue());
    float internalY0=((pixelOffsetY.intValue()-0)/scale)+(PositionDecoder.LANDBLOCK_SIZE*blockY.intValue());
    float[] lonLat0=PositionDecoder.decodePosition(internalX0,internalY0);
    //System.out.println("\tOrigin: Lat: "+latitude0+", Lon: "+longitude0);

    float internalWidth=((width.intValue()-pixelOffsetX.intValue())/scale)+(PositionDecoder.LANDBLOCK_SIZE*blockX.intValue());
    float internalHeight=((pixelOffsetY.intValue()-height.intValue())/scale)+(PositionDecoder.LANDBLOCK_SIZE*blockY.intValue());
    float[] lonLatMax=PositionDecoder.decodePosition(internalWidth,internalHeight);
    //System.out.println("\tMax: Lat: "+latitudeHeight+", Lon: "+longitudeWidth);

    Rectangle2D.Float r=new Rectangle2D.Float();
    float deltaLat=lonLat0[1]-lonLatMax[1];
    float deltaLong=lonLatMax[0]-lonLat0[0];
    r.setRect(lonLat0[0],lonLatMax[1],deltaLong,deltaLat);
    return r;
  }

  private void doIt()
  {
    PropertiesSet props=loadMapsSystemProperties();
    if (props!=null)
    {
      PropertiesSet areaProps=(PropertiesSet)props.getProperty("UI_Map_AreaData");
      handleMapProps(areaProps);
    }
    _geoLoader.getGeoManager().dump();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MapsSystemLoader loader=new MapsSystemLoader(facade);
    loader.doIt();
  }
}
