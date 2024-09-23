package delta.games.lotro.tools.extraction.maps.finder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;

import delta.games.lotro.lore.maps.ParchmentMapsManager;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;
import delta.games.lotro.tools.extraction.geo.maps.finder.AdvancedMapsManager;
import delta.games.lotro.tools.extraction.geo.maps.finder.DetailedMap;

/**
 * Test class for the advanced maps manager.
 * @author DAM
 */
class AdvancedMapsManagerTest
{
  /**
   * Test the map finder.
   */
  @Test
  void testBestMapForPoint()
  {
    File rootDir=new File("../lotro-maps-db");
    MapsManager mapsManager=new MapsManager(rootDir);
    GeoreferencedBasemapsManager mapsMgr=mapsManager.getBasemapsManager();
    ParchmentMapsManager parchmentMapsMgr=ParchmentMapsManager.getInstance();
    AdvancedMapsManager mgr=new AdvancedMapsManager(mapsMgr,parchmentMapsMgr);
    // Floid and Dewitt position in Evendim
    DetailedMap map=mgr.getBestMapForPoint(1,-73.5065f,-14.199228f);
    assertNotNull(map);
    assertEquals(268439511,map.getIdentifier());
    // Prancing pony. This gives 2 maps, we expect the one of Bree and not the one of Bree-land 
    map=mgr.getBestMapForPoint(1,-51.7f,-30.6f);
    assertNotNull(map);
    assertEquals(268437716,map.getIdentifier());
  }
}
