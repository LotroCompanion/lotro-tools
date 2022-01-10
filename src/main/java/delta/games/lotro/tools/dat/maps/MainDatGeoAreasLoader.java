package delta.games.lotro.tools.dat.maps;

import delta.common.utils.files.archives.DirectoryArchiver;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.maps.io.xml.GeoAreasXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Get geographic areas from DAT files.
 * @author DAM
 */
public class MainDatGeoAreasLoader
{
  private DataFacade _facade;
  private GeoAreasLoader _loader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatGeoAreasLoader(DataFacade facade)
  {
    _facade=facade;
    _loader=new GeoAreasLoader(facade);
  }

  /**
   * Load all geographic areas.
   */
  public void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      byte[] data=_facade.loadData(id);
      if (data!=null)
      {
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==690)
        {
          _loader.getArea(id);
        }
        else if (classDefIndex==692)
        {
          _loader.getTerritory(id);
        }
        else if (classDefIndex==180)
        {
          _loader.getRegion(id);
        }
      }
    }
    _loader.addMissingRegions();
    // Save geo areas
    boolean ok=GeoAreasXMLWriter.writeGeoAreasFile(GeneratedFiles.GEO_AREAS,_loader.getGeoManager());
    if (ok)
    {
      System.out.println("Wrote geographic areas file: "+GeneratedFiles.GEO_AREAS);
    }
    // Write area icons
    DirectoryArchiver archiver=new DirectoryArchiver();
    ok=archiver.go(GeneratedFiles.AREA_ICONS,GeoAreasLoader.AREA_ICONS_DIR);
    if (ok)
    {
      System.out.println("Wrote area icons archive: "+GeneratedFiles.AREA_ICONS);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatGeoAreasLoader(facade).doIt();
    facade.dispose();
  }
}
