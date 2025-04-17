package delta.games.lotro.tools.extraction.geo.landblocks;

import java.io.File;
import java.io.PrintStream;

import delta.common.utils.files.TextFileWriter;
import delta.common.utils.io.PrintStreamToStringBridge;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.tools.extraction.geo.markers.MarkerUtils;

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
    _analyzer=new LandblockGeneratorsAnalyzer(null);
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

  void dumpLandBlock(LandBlockInfo lbi,PrintStream out)
  {
    //System.out.println("Using region "+region+", block X="+blockX+",Y="+blockY);
    if (lbi!=null)
    {
      LbiInspector inspector=new LbiInspector();
      inspector.dump(lbi,out);
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
    // Ghámgur
    doIt(646815745);
    doIt(662990851);
    doIt(663371787);
    doIt(663564292);
    doIt(679522307);
    // Thorang
    doIt(646815744);
    doIt(662990850);
    doIt(663371789);
    doIt(663564291);
    doIt(679522306);
    // Helchurth
    doIt(646868993);
    doIt(662777856);
    doIt(662990848);
    doIt(663371786);
    doIt(679522304);
    // Woe-weaver
    doIt(646868994);
    doIt(662777857);
    doIt(662990849);
    doIt(663371788);
    doIt(679522305);
  }

  private void doIt(int markerId)
  {
    //long now=System.currentTimeMillis();
    BlockReference block=MarkerUtils.getBlockForMarker(markerId);
    int region=block.getRegion(); int blockX=block.getBlockX(); int blockY=block.getBlockY();
    String name="block-x-"+blockX+"-y-"+blockY+".txt";
    File toFile=new File(name);
    TextFileWriter writer=new TextFileWriter(toFile);
    writer.start();
    PrintStreamToStringBridge b=new PrintStreamToStringBridge();
    LandBlockInfo lbi=_loader.loadLandblockInfo(region,blockX,blockY);
    PrintStream out=b.getPrintStream();
    BlockMapLoader bmLoader=new BlockMapLoader(_facade);
    // Block properties
    PropertiesSet props=bmLoader.loadPropertiesForMapBlock(region,blockX,blockY);
    writer.writeNextLine("Block properties:");
    writer.writeSomeText(props.dump());
    writer.writeNextLine("**************************");
    // Block details
    writer.writeNextLine("Block details:");
    dumpLandBlock(lbi,out);
    writer.writeSomeText(b.getText());
    writer.terminate();
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
