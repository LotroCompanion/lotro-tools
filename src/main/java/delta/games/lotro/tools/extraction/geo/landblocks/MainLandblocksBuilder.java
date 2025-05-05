package delta.games.lotro.tools.extraction.geo.landblocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.lore.maps.landblocks.Landblock;
import delta.games.lotro.lore.maps.landblocks.LandblocksManager;
import delta.games.lotro.lore.maps.landblocks.io.xml.LandblocksXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.geo.GeoUtils;
import delta.games.lotro.tools.utils.ToolLog;

/**
 * Tool class to load all the landblocks.
 * @author DAM
 */
public class MainLandblocksBuilder
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainLandblocksBuilder.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainLandblocksBuilder(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Build a landblocks manager with all known landblocks.
   * @return a landblocks manager.
   */
  private LandblocksManager build()
  {
    LandblockLoader landblockLoader=new LandblockLoader(_facade);
    LandblocksManager index=new LandblocksManager();
    int nbBlocks=0;
    int[] regions=GeoUtils.getRegions();
    for(int region : regions)
    {
      ToolLog.LOGGER.info("Region {}",Integer.valueOf(region));
      for(int blockX=0;blockX<=0xFF;blockX++)
      {
        ToolLog.LOGGER.info("X={}",Integer.valueOf(blockX));
        for(int blockY=0;blockY<=0xFF;blockY++)
        {
          ToolLog.LOGGER.debug("\tY={}",Integer.valueOf(blockY));
          Landblock data=landblockLoader.buildLandblock(region,blockX,blockY);
          if (data!=null)
          {
            index.addLandblock(data);
            nbBlocks++;
          }
        }
      }
    }
    ToolLog.LOGGER.info("Loaded {} landblocks!",Integer.valueOf(nbBlocks));
    return index;
  }

  /**
   * Load landblock data.
   */
  public void doIt()
  {
    LandblocksManager index=build();
    // Save landblocks.
    boolean ok=LandblocksXMLWriter.writeLandblocksFile(GeneratedFiles.LANDBLOCKS,index);
    if (ok)
    {
      LOGGER.info("Wrote landblocks file: {}",GeneratedFiles.LANDBLOCKS);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainLandblocksBuilder(facade).doIt();
  }
}
