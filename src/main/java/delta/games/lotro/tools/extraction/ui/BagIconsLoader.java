package delta.games.lotro.tools.extraction.ui;

import java.io.File;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.DatIconsUtils;

/**
 * Loader for bag icons.
 * @author DAM
 */
public class BagIconsLoader
{
  private DataFacade _facade;
  private EnumMapper _artAssetID;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public BagIconsLoader(DataFacade facade)
  {
    _facade=facade;
    _artAssetID=facade.getEnumsManager().getEnumMapper(587202796); // ArtAssetID
  }

  /**
   * Load icons.
   */
  public void doIt()
  {
    PropertiesSet props=_facade.loadProperties(0x78000011);
    Object[] array=(Object[])props.getProperty("UISkin_MappingArray");
    for(Object entry : array)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      int dataId=((Integer)entryProps.getProperty("UISkin_DataID")).intValue();
      int assetID=((Integer)entryProps.getProperty("UISkin_UIArtAssetID")).intValue();
      String assetName=_artAssetID.getLabel(assetID);
      if (assetName.contains("bag"))
      {
        File to=new File(assetName+".png");
        DatIconsUtils.buildImageFile(_facade,dataId,to);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new BagIconsLoader(facade).doIt();
  }
}
