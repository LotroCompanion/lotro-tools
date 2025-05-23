package delta.games.lotro.tools.tools;

import java.io.File;

import delta.common.utils.io.Console;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.markers.GlobalMarkersManager;
import delta.games.lotro.maps.data.markers.LandblockMarkersManager;
import delta.games.lotro.tools.extraction.geo.maps.MapConstants;
import delta.games.lotro.tools.extraction.geo.markers.MarkerDuplicatesRemover;

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
    for(int region=1;region<=4;region++)
    {
      Console.println("Region "+region);
      for(int blockX=0;blockX<=0xFF;blockX++)
      {
        for(int blockY=0;blockY<=0xFF;blockY++)
        {
          BlockReference block=new BlockReference(region,blockX,blockY);
          doBlock(block);
        }
      }
    }
    int nbRemovedMarkers=_remover.getRemovedMarkers();
    int nbTotalMarkers=_remover.getTotalMarkers();
    Console.println("Removed "+nbRemovedMarkers+" markers / "+nbTotalMarkers);
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
