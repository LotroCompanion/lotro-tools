package delta.games.lotro.tools.dat.maps;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.data.ui.UILayoutLoader;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
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

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapsSystemLoader(DataFacade facade)
  {
    _facade=facade;
    _uiElementId=facade.getEnumsManager().getEnumMapper(587202769);
    _geoLoader=new GeoAreasLoader(_facade);
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
    // Map name
    String mapName=DatUtils.getStringProperty(props,"UI_Map_MenuName");
    System.out.println("Map name: "+mapName);
    // Map image
    Integer imageId=(Integer)props.getProperty("UI_Map_MapImage");
    if (imageId!=null)
    {
      File to=new File(mapName+".png");
      if (!to.exists())
      {
        DatIconsUtils.buildImageFile(_facade,imageId.intValue(),to);
      }
    }

    // ActiveElement
    int activeElementId=((Integer)props.getProperty("UI_Map_ActiveElement")).intValue();
    String activeElementName=_uiElementId.getString(activeElementId);
    System.out.println("\tActive element: "+activeElementName+" ("+activeElementId+")");

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
            System.out.println("\t\tChild map: "+childMapUI);
            String tooltip=DatStringUtils.getStringProperty(childProps,"UICore_Element_tooltip_entry");
            System.out.println("\t\tTooltip: "+tooltip);
            Point location=childElement.getRelativeBounds().getLocation();
            System.out.println("\t\tLocation: "+location);
          }
        }
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

    // Bounds
    Rectangle2D.Float bounds=getBounds(activeElementName,scale,props);
    System.out.println("\tBounds: "+bounds);

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

    float internalX=((0-pixelOffsetX.intValue())/scale)+(160*blockX.intValue());
    float longitude0=(1468-(internalX/20))/10;
    float internalY=((pixelOffsetY.intValue()-0)/scale)+(160*blockY.intValue());
    float latitude0=(1244-(internalY/20))/10;
    //System.out.println("\tOrigin: Lat: "+latitude0+", Lon: "+longitude0);

    float internalWidth=((width.intValue()-pixelOffsetX.intValue())/scale)+(160*blockX.intValue());
    float longitudeWidth=(1468-(internalWidth/20))/10;
    float internalHeight=((pixelOffsetY.intValue()-height.intValue())/scale)+(160*blockY.intValue());
    float latitudeHeight=(1244-(internalHeight/20))/10;
    //System.out.println("\tMax: Lat: "+latitudeHeight+", Lon: "+longitudeWidth);

    Rectangle2D.Float r=new Rectangle2D.Float();
    float deltaLat=latitudeHeight-latitude0;
    float deltaLong=longitude0-longitudeWidth;
    r.setRect(longitudeWidth,latitude0,deltaLong,deltaLat);
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
