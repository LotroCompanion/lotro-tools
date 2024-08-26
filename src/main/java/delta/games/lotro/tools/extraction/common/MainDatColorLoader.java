package delta.games.lotro.tools.extraction.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.colors.ColorDescription;
import delta.games.lotro.common.colors.io.xml.ColorXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Get color definitions from DAT files.
 * @author DAM
 */
public class MainDatColorLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatColorLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatColorLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("colors",facade.getGlobalStringsManager());
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
      ColorDescription color=new ColorDescription();
      // Color bit
      Long colorBitSet=(Long)colorProps.getProperty("Wardrobe_ItemClothingColorExt_Bit");
      if (colorBitSet!=null)
      {
        int position=Long.numberOfTrailingZeros(colorBitSet.longValue())+1;
        color.setIntCode(position);
      }
      // Name
      int code=color.getIntCode();
      String colorName=_i18n.getNameStringProperty(colorProps,"ItemMunging_NameLookup_Name",code,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
      color.setName(colorName);
      Float colorValue=(Float)colorProps.getProperty("ItemMunging_NameLookup_Value");
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Color: "+colorName+", value: "+colorValue);
      }
      if (colorValue!=null)
      {
        color.setCode(colorValue.floatValue());
      }
      colors.add(color);
    }
    ColorXMLWriter.writeColorsFile(GeneratedFiles.COLORS,colors);
    _i18n.save();
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
