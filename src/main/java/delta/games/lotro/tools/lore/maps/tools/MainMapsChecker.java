package delta.games.lotro.tools.lore.maps.tools;

import java.io.File;
import java.util.List;

import delta.games.lotro.maps.data.Map;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapLink;
import delta.games.lotro.maps.data.MapsManager;

/**
 * Maps checker.
 * @author DAM
 */
public class MainMapsChecker
{
  private void doIt()
  {
    File rootDir=new File("../lotro-maps-db");
    MapsManager mapsManager=new MapsManager(rootDir);
    mapsManager.load();
    // Check links
    checkLinks(mapsManager);
  }

  private void checkLinks(MapsManager mapsManager)
  {
    int nbErrors=0;
    List<MapBundle> maps=mapsManager.getMaps();
    for(MapBundle mapBundle : maps)
    {
      Map map=mapBundle.getMap();
      String mapKey=map.getKey();
      List<MapLink> links=mapBundle.getLinks();
      int nbErrorsInMap=0;
      for(MapLink link : links)
      {
        String targetMapKey=link.getTargetMapKey();
        MapBundle targetMap=mapsManager.getMapByKey(targetMapKey);
        if (targetMap==null)
        {
          if (nbErrorsInMap==0)
          {
            File linksFile=mapsManager.getLinksFile(mapKey);
            System.out.println("File: "+linksFile);
            /*
            try
            {
              Desktop.getDesktop().open(linksFile);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            */
          }
          System.out.println("Map: "+mapKey+": bad link to "+targetMapKey);
          nbErrors++;
          nbErrorsInMap++;
        }
      }
    }
    if (nbErrors>0)
    {
      System.out.println("Found "+nbErrors+" link errors");
    }
    else
    {
      System.out.println("Found no link error");
    }
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainMapsChecker().doIt();
  }
}
