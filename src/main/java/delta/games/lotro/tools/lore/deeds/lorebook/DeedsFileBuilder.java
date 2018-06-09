package delta.games.lotro.tools.lore.deeds.lorebook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.LotroCoreConfig;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLParser;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;

/**
 * Builds a single deeds file from a collection of deed files.
 * @author DAM
 */
public class DeedsFileBuilder
{
  private void doIt()
  {
    File loreDir=LotroCoreConfig.getInstance().getLoreDir();
    File deedsDir=new File(loreDir,"deeds");
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    DeedXMLParser parser=new DeedXMLParser();
    for(File deedFile : deedsDir.listFiles())
    {
      List<DeedDescription> newDeeds=parser.parseXML(deedFile);
      deeds.addAll(newDeeds);
    }
    File out=new File(loreDir,"deeds.xml");
    DeedXMLWriter writer=new DeedXMLWriter();
    writer.writeDeeds(out,deeds,EncodingNames.UTF_8);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new DeedsFileBuilder().doIt();
  }
}
