package delta.games.lotro.tools.dat.items;

import java.io.File;
import java.util.BitSet;

import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SocketType;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.WeenieContentDirectory;

/**
 * Loader for socket icons.
 * @author DAM
 */
public class SocketIconsLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public SocketIconsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load socket icons.
   */
  public void doIt()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"ItemSocketControl");
    /*
    ItemSocketControl_LevelToEssenceIconOverlay_Array: 
      #1: ItemSocketControl_LevelToEssenceIconOverlay_Entry 
        Icon_Layer_OverlayDID: 1091914756
        Usage_MinLevel: 45
     */
    File rootDir=GeneratedFiles.SOCKET_ICONS_DIR;
    Object[] essenceOverlaysArray=(Object[])props.getProperty("ItemSocketControl_LevelToEssenceIconOverlay_Array");
    for(Object essenceOverlayEntry : essenceOverlaysArray)
    {
      PropertiesSet entryProps=(PropertiesSet)essenceOverlayEntry;
      int minLevel=((Integer)entryProps.getProperty("Usage_MinLevel")).intValue();
      int overlayDID=((Integer)entryProps.getProperty("Icon_Layer_OverlayDID")).intValue();
      DatIconsUtils.buildImageFile(_facade,overlayDID,new File(rootDir,minLevel+"-"+overlayDID+".png"));
    }
    Object[] socketImageArray=(Object[])props.getProperty("ItemSocketControl_Socket_Image_Array");
    for(Object socketImageEntry : socketImageArray)
    {
      PropertiesSet entryProps=(PropertiesSet)socketImageEntry;
      // Socket type
      Long bitfield=(Long)entryProps.getProperty("ItemSocketControl_Image_Bitfield");
      BitSet bitset=BitSetUtils.getBitSetFromFlags(bitfield.longValue());
      int index=bitset.nextSetBit(0);
      LotroEnum<SocketType> socketTypeEnum=LotroEnumsRegistry.getInstance().get(SocketType.class);
      SocketType socketType=socketTypeEnum.getEntry(index+1);
      int overlayDID=((Integer)entryProps.getProperty("Icon_Layer_OverlayDID")).intValue();
      File overlayFile=new File(rootDir,"overlay-"+socketType.getCode()+".png");
      DatIconsUtils.buildImageFile(_facade,overlayDID,overlayFile);
      int backgroundDID=((Integer)entryProps.getProperty("Icon_Layer_BackgroundDID")).intValue();
      File backgroundFile=new File(rootDir,"background-"+socketType.getCode()+".png");
      DatIconsUtils.buildImageFile(_facade,backgroundDID,backgroundFile);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new SocketIconsLoader(facade).doIt();
    facade.dispose();
  }
}
