package delta.games.lotro.tools.extraction.maps.classification;

import java.io.File;
import java.util.List;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.markers.BlockMarkersManager;
import delta.games.lotro.maps.data.markers.GlobalMarkersManager;
import delta.games.lotro.tools.extraction.geo.maps.MapConstants;
import delta.games.lotro.tools.extraction.geo.markers.classification.Classification;
import delta.games.lotro.tools.extraction.geo.markers.classification.MarkerClassifier;

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
    /*
    doBlocks();
    doBlock(1,6,7,markersMgr); // Shire/bree
    doBlock(1,10,3,markersMgr); // Dunland
    doBlock(1,8,8,markersMgr); // Esteldin / Western North Downs
    doBlock(1,15,5,markersMgr); // Shire Auction Hall
    */
    doSimpleClassification();
  }

  void doBlocks()
  {
    File rootDir=MapConstants.getRootDir();
    MapsManager maps=new MapsManager(rootDir);
    GlobalMarkersManager markersMgr=maps.getMarkersManager();
    for(int region=1;region<=4;region++)
    {
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          doBlock(region,blockX,blockY,markersMgr);
        }
      }
    }
  }

  private void doBlock(int region, int x, int y, GlobalMarkersManager markersMgr)
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
