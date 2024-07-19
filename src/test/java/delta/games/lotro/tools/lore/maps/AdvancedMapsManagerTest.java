package delta.games.lotro.tools.lore.maps;

import java.io.File;

import delta.games.lotro.lore.maps.ParchmentMapsManager;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test class for the advanced maps manager.
 * @author DAM
 */
public class AdvancedMapsManagerTest extends TestCase
{
  /**
   * Test the map finder.
   */
  public void testBestMapForPoint()
  {
    File rootDir=new File("../lotro-maps-db");
    MapsManager mapsManager=new MapsManager(rootDir);
    GeoreferencedBasemapsManager mapsMgr=mapsManager.getBasemapsManager();
    ParchmentMapsManager parchmentMapsMgr=ParchmentMapsManager.getInstance();
    AdvancedMapsManager mgr=new AdvancedMapsManager(mapsMgr,parchmentMapsMgr);
    // Floid and Dewitt position in Evendim
    DetailedMap map=mgr.getBestMapForPoint(1,-73.5065f,-14.199228f);
    Assert.assertNotNull(map);
    Assert.assertEquals(268439511,map.getIdentifier());
    // Prancing pony. This gives 2 maps, we expect the one of Bree and not the one of Bree-land 
    map=mgr.getBestMapForPoint(1,-51.7f,-30.6f);
    Assert.assertNotNull(map);
    Assert.assertEquals(268437716,map.getIdentifier());
  }
}
