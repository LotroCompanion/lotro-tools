package delta.games.lotro.tools.dat.misc.ui;

import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Loader for UI layouts.
 * @author DAM
 */
public class UILayoutLoader
{
  private static final Logger LOGGER=Logger.getLogger(UILayoutLoader.class);

  private static final int MOVIE = 1; // bik file
  private static final int IMAGE = 4;
  private static final int MESSAGE = 5; // see EnumMapper: UIElementMessage
  private static final int ANIMATE_VALUE = 6; // animates a float range
  private static final int ANIMATION = 8; // animates an image sequence
  private static final int COLORED_IMAGE = 9;
  private static final int CURSOR = 10;
  private static final int SOUNDINFO = 11;
  private static final int STATE = 12;
  private static final int SCALE = 13;

  private DataFacade _facade;
  private EnumMapper _uiElementIdMapper;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public UILayoutLoader(DataFacade facade)
  {
    _facade=facade;
    _uiElementIdMapper=_facade.getEnumsManager().getEnumMapper(587202769);
  }

  /**
   * Load a UI layout.
   * @param layoutId Layout ID.
   * @return the loaded UI layout.
   */
  public UILayout loadUiLayout(int layoutId)
  {
    byte[] data=_facade.loadData(layoutId);
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    int did=BufferUtils.readUInt32(bis);
    if (did!=layoutId)
    {
      throw new IllegalArgumentException("Expected DID for UI layout: "+layoutId+". Found: "+did);
    }
    UILayout ret=new UILayout(layoutId);
    int baseWidth=BufferUtils.readUInt32(bis);
    int baseHeight=BufferUtils.readUInt32(bis);
    int count=BufferUtils.readTSize(bis);
    for(int i=0;i<count;i++)
    {
      UIElement uiElement=loadUiElement(bis);
      ret.addUIElement(uiElement);
    }
    return ret;
  }

  /**
   * Load a UI element from the given stream.
   * @param bis Input stream.
   * @return the loaded UI element.
   */
  public UIElement loadUiElement(ByteArrayInputStream bis)
  {
    int id=BufferUtils.readUInt32(bis); // From EnumMapper: UIElementID
    String elementId=_uiElementIdMapper.getString(id);
    System.out.println("**** UI element ID: "+elementId);
    int zero=BufferUtils.readUInt32(bis);
    if (zero!=0)
    {
      throw new IllegalArgumentException("Expected 0 here. Found: "+zero);
    }

    UIElement ret=new UIElement(id);
    int unknown0=BufferUtils.readUInt8(bis); // 0, 1, 2, 3

    int x=BufferUtils.readUInt32(bis); // Relative to parent
    int y=BufferUtils.readUInt32(bis);
    int w=BufferUtils.readUInt32(bis);
    int h=BufferUtils.readUInt32(bis);

    int argb=BufferUtils.readUInt32(bis);
    boolean unknown1=BufferUtils.readBoolean(bis);

    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
    propsLoader.decodeProperties(bis,ret.getProperties());

    int count=BufferUtils.readUInt8(bis);
    for(int i=0;i<count;i++)
    {
      loadUiData(bis);
    }

    int index=BufferUtils.readUInt32(bis); // Within the parent's UIElement array
    int elementID=BufferUtils.readUInt32(bis); // Matches id above
    int typeID=BufferUtils.readUInt32(bis); // EnumMapper: UIElementType
    int baseElementID=BufferUtils.readUInt32(bis); // if 0, so is baseLayoutDID, and vice versa
    int baseLayoutDID=BufferUtils.readUInt32(bis); // another UILAYOUT file 0x22nnnnnn
    int stateID=BufferUtils.readUInt32(bis); // EnumMapper: UIStateID

    int[] margins = new int[4]; // All values in the range 0..5
    for(int i=0;i<margins.length;i++)
    {
      margins[i]=BufferUtils.readUInt32(bis);
    }

    // States
    // See below for UIState.  Basically a switch construct on UIStateID,
    // allowing for example a choice of normal, pressed, highlighted icon.
    count=BufferUtils.readTSize(bis);
    for(int i=0;i<count;i++)
    {
      loadUiState(bis);
    }

    count=BufferUtils.readTSize(bis);
    for(int i=0;i<count;i++)
    {
      loadUiElement(bis);
    }
    return ret;
  }

  private void loadUiState(ByteArrayInputStream bis)
  {
    int stateID=BufferUtils.readUInt32(bis);
    int echo=BufferUtils.readUInt32(bis); // Verify this == stateID
    int unknown=BufferUtils.readUInt32(bis);
    BufferUtils.skip(bis,18); // All 0
   
    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);
    PropertiesSet properties=new PropertiesSet();
    propsLoader.decodeProperties(bis,properties);

    int count=BufferUtils.readUInt8(bis);
    for(int i=0;i<count;i++)
    {
      loadUiData(bis);
    }
  }

  // UIData: A union of various effect types.
  private void loadUiData(ByteArrayInputStream bis)
  {
    int type=BufferUtils.readUInt32(bis);
    int echo=BufferUtils.readUInt32(bis); // Verify echo == type

    if (type==MOVIE)
    {
      int alwaysZero=BufferUtils.readUInt32(bis);
      String name=BufferUtils.readPascalString(bis);
      int alwaysOne=BufferUtils.readUInt32(bis);
    }
    else if (type==IMAGE)
    {
      int imageDID=BufferUtils.readUInt32(bis);
      int unknown=BufferUtils.readUInt32(bis);
      String filename=BufferUtils.readPascalString(bis);
    }
    else if (type==MESSAGE)
    {
      int messageID=BufferUtils.readUInt32(bis);
      float unknown=BufferUtils.readFloat(bis);
    }
    else if (type==ANIMATE_VALUE)
    {
      float startValue=BufferUtils.readFloat(bis);
      float endValue=BufferUtils.readFloat(bis);
      float duration=BufferUtils.readFloat(bis);
    }
    else if (type==ANIMATION)
    {
      float unknown0=BufferUtils.readFloat(bis);
      int unknown1=BufferUtils.readUInt32(bis);
      boolean unknown2=BufferUtils.readBoolean(bis);
      int count=BufferUtils.readUInt32(bis);
      for(int i=0;i<count;i++)
      {
        int imageDID=BufferUtils.readUInt32(bis);
      }
    }
    else if (type==COLORED_IMAGE)
    {
      int imageDID=BufferUtils.readUInt32(bis);
      int unknown=BufferUtils.readUInt32(bis);
      int argb=BufferUtils.readUInt32(bis);
    }
    else if (type==CURSOR)
    {
      int imageDID=BufferUtils.readUInt32(bis);
      int unknown0=BufferUtils.readUInt32(bis);
      int unknown1=BufferUtils.readUInt32(bis);
    }
    else if (type==SOUNDINFO)
    {
      int did=BufferUtils.readUInt32(bis);
    }
    else if (type==STATE)
    {
      int stateID=BufferUtils.readUInt32(bis);
      float unknown=BufferUtils.readFloat(bis);
    }
    else if (type==SCALE)
    {
      float x=BufferUtils.readFloat(bis);
      float y=BufferUtils.readFloat(bis);
    }
    else
    {
      LOGGER.warn("Unsupported UI data type: "+type);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    UILayoutLoader loader=new UILayoutLoader(facade);
    loader.loadUiLayout(0x22000041);
  }
}
