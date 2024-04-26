package delta.games.lotro.tools.dat.misc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.colors.ColorDescription;
import delta.games.lotro.common.colors.io.xml.ColorXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.WeenieContentDirectory;

/**
 * Get color definitions from DAT files.
 * @author DAM
 */
public class MainDatColorLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatColorLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatColorLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load colors.
   */
  public void doIt()
  {
    // ItemMungingControl 0x70000252 (1879048786)
    PropertiesSet properties=WeenieContentDirectory.loadWeenieContentProps(_facade,"ItemMungingControl");
    if (properties==null)
    {
      LOGGER.warn("Could not load item munging control properties");
      return;
    }
    List<ColorDescription> colors=new ArrayList<ColorDescription>();
    Object[] entries=(Object[])properties.getProperty("ItemMunging_Control_Color_List");
    for(Object colorEntryObj : entries)
    {
      PropertiesSet colorProps=(PropertiesSet)colorEntryObj;
      String colorName=DatUtils.getStringProperty(colorProps,"ItemMunging_NameLookup_Name");
      Float colorValue=(Float)colorProps.getProperty("ItemMunging_NameLookup_Value");
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Color: "+colorName+", value: "+colorValue);
      }
      ColorDescription color=new ColorDescription();
      color.setName(colorName);
      if (colorValue!=null)
      {
        color.setCode(colorValue.floatValue());
      }
      // Color bit
      Long colorBitSet=(Long)colorProps.getProperty("Wardrobe_ItemClothingColorExt_Bit");
      if (colorBitSet!=null)
      {
        int position=Long.numberOfTrailingZeros(colorBitSet.longValue())+1;
        color.setIntCode(position);
      }
      colors.add(color);
    }
    ColorXMLWriter.writeColorsFile(GeneratedFiles.COLORS,colors);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatColorLoader(facade).doIt();
    facade.dispose();
  }
}
