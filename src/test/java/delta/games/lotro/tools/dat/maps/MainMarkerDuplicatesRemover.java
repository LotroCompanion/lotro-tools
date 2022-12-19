package delta.games.lotro.tools.dat.maps;

import java.io.File;

import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.markers.GlobalMarkersManager;
import delta.games.lotro.maps.data.markers.LandblockMarkersManager;

/**
 * Main method for the marker duplicates remover.
 * @author DAM
 */
public class MainMarkerDuplicatesRemover
{
  private GlobalMarkersManager _markersMgr;
  private MarkerDuplicatesRemover _remover;

  private MainMarkerDuplicatesRemover()
  {
    File rootDir=MapConstants.getRootDir();
    MapsManager mapsManager=new MapsManager(rootDir);
    _markersMgr=mapsManager.getMarkersManager();
    _remover=new MarkerDuplicatesRemover();
  }

  private void doBlock(BlockReference block)
  {
    LandblockMarkersManager landblockMarkersMgr=_markersMgr.getLandblockMarkersManager(block.getRegion(),block.getBlockX(),block.getBlockY());
    if (landblockMarkersMgr!=null)
    {
      _remover.handleLandblock(landblockMarkersMgr);
    }
  }

  private void doIt()
  {
    //BlockReference block=MarkerUtils.getBlockForMarker(376705024);
    //doBlock(block);
    for(int region=1;region<=4;region++)
    {
      System.out.println("Region "+region);
      for(int blockX=0;blockX<=0xFF;blockX++)
      {
        //System.out.println("X="+blockX);
        for(int blockY=0;blockY<=0xFF;blockY++)
        {
          BlockReference block=new BlockReference(region,blockX,blockY);
          doBlock(block);
        }
      }
    }
    int nbRemovedMarkers=_remover.getRemovedMarkers();
    int nbTotalMarkers=_remover.getTotalMarkers();
    System.out.println("Removed "+nbRemovedMarkers+" markers / "+nbTotalMarkers);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainMarkerDuplicatesRemover().doIt();
  }
}
