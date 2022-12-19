package delta.games.lotro.tools.dat.maps.classification;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.markers.BlockMarkersManager;
import delta.games.lotro.maps.data.markers.GlobalMarkersManager;
import delta.games.lotro.tools.dat.maps.MapConstants;

/**
 * Test class for the markers classifier.
 * @author DAM
 */
public class MainTestMarkerClassifier
{
  private MarkerClassifier _classifier;

  private MainTestMarkerClassifier()
  {
    DataFacade facade=new DataFacade();
    _classifier=new MarkerClassifier(facade);
  }

  private void doSimpleClassification()
  {
    Classification classif=_classifier.classifyDid(1879410562);
    System.out.println(classif);
  }

  private void doIt()
  {
    //doBlocks();
    doSimpleClassification();
  }

  void doBlocks()
  {
    File rootDir=MapConstants.getRootDir();
    MapsManager maps=new MapsManager(rootDir);
    GlobalMarkersManager markersMgr=maps.getMarkersManager();
    Map<Integer,IntegerHolder> stats=new HashMap<Integer,IntegerHolder>();
    for(int region=1;region<=4;region++)
    {
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          doBlock(region,blockX,blockY,markersMgr,stats);
        }
      }
    }
    /*
    doBlock(1,6,7,markersMgr,stats); // Shire/bree
    doBlock(1,10,3,markersMgr,stats); // Dunland 
    doBlock(1,8,8,markersMgr,stats); // Esteldin / Western North Downs
    doBlock(1,15,5,markersMgr,stats); // Shire Auction Hall
    */
  }

  private void doBlock(int region, int x, int y, GlobalMarkersManager markersMgr, Map<Integer,IntegerHolder> stats)
  {
    BlockMarkersManager blockMgr=markersMgr.getBlockManager(region,x,y);
    List<Marker> markers=blockMgr.getMarkers();
    for(Marker marker : markers)
    {
      _classifier.classifyDid(marker.getDid());
    }
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainTestMarkerClassifier().doIt();
  }
}
