package delta.games.lotro.tools.dat.maps.landblocks;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.tools.dat.maps.MarkerUtils;
import delta.games.lotro.tools.dat.maps.data.LandBlockInfo;

/**
 * Simple test class for the landblock info loader.
 * @author DAM
 */
public class MainTestLandblockInfoLoader
{
  private DataFacade _facade;
  private LandblockInfoLoader _loader;
  private LandblockGeneratorsAnalyzer _analyzer;

  private MainTestLandblockInfoLoader()
  {
    _facade=new DataFacade();
    _loader=new LandblockInfoLoader(_facade);
    _analyzer=new LandblockGeneratorsAnalyzer(_facade,null);
  }

  void handleLandBlock(LandBlockInfo lbi)
  {
    //System.out.println("Using region "+region+", block X="+blockX+",Y="+blockY);
    if (lbi!=null)
    {
      LbiInspector inspector=new LbiInspector();
      inspector.handleLbi(lbi);
    }
  }

  void dumpLandBlock(LandBlockInfo lbi)
  {
    //System.out.println("Using region "+region+", block X="+blockX+",Y="+blockY);
    if (lbi!=null)
    {
      LbiInspector inspector=new LbiInspector();
      inspector.dump(lbi,System.out);
    }
  }

  void analyzeBlock(LandBlockInfo lbi)
  {
    if (lbi!=null)
    {
      _analyzer.handleLandblock(lbi);
    }
  }

  private void doIt()
  {
    long now=System.currentTimeMillis();
    /*
    LandBlockInfo lbi=_loader.loadLandblockInfo(1,779/8,1338/8);
    analyzeBlock(lbi); // Itä-mâ (r1 lx779 ly1338 ox69.00 oy59.60 oz434.04 h296.7)
    */
    //DatPosition position=PositionDecoder.fromLatLon(-20,-30);
    DatPosition position=PositionDecoder.fromLatLon(-62.5f,-14.7f);
    System.out.println("Position: "+position);
    //int region=1; int blockX=position.getBlockX(); int blockY=position.getBlockY();
    //int region=2; int blockX=252; int blockY=83;
    //int region=2; int blockX=0x71; int blockY=0xB5;
    //int region=2; int blockX=252; int blockY=82;
    //int region=2; int blockX=248; int blockY=198;
    //int region=1; int blockX=2000/8; int blockY=904/8;
    //int region=4; int blockX=252; int blockY=232;
    //int region=14; int blockX=14; int blockY=234;
    //int markerId=-521232379; // Tales of Yore
    int markerId=1263431680; // Echad Uial
    BlockReference block=MarkerUtils.getBlockForMarker(markerId);
    int region=block.getRegion(); int blockX=block.getBlockX(); int blockY=block.getBlockY();
    BlockMapLoader bmLoader=new BlockMapLoader(_facade);
    PropertiesSet props=bmLoader.loadPropertiesForMapBlock(region,blockX,blockY);
    System.out.println("Block map props: "+props.dump());
    LandBlockInfo lbi=_loader.loadLandblockInfo(region,blockX,blockY);
    dumpLandBlock(lbi);
    /*
    for(int region=1;region<=1;region++)
    {
      System.out.println("Region "+region);
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        System.out.println("X="+blockX);
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          LandBlockInfo lbi=_loader.loadLandblockInfo(region,blockX,blockY);
          analyzeBlock(lbi);
        }
      }
    }
    */
    long now2=System.currentTimeMillis();
    System.out.println("Done in "+(now2-now)+"ms");
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    MainTestLandblockInfoLoader loader=new MainTestLandblockInfoLoader();
    loader.doIt();
  }
}
