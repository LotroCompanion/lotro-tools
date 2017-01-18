package delta.games.lotro.tools.lore.relics;

import java.io.File;
import java.util.List;

import delta.common.utils.files.FileCopy;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicsCategory;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;

/**
 * Build an icons database for relics.
 * @author DAM
 */
public class BuildRelicsIconsDb
{
  private static File _inputDir=new File("D:\\tmp\\relics");
  private static File _toDir=new File("relicIcons").getAbsoluteFile();

  private void checkRelic(Relic relic)
  {
    String filename=relic.getIconFilename();
    File inputFile=new File(_inputDir,filename);
    if (inputFile.exists())
    {
      File toFile=new File(_toDir,filename);
      if (!toFile.exists())
      {
        _toDir.mkdirs();
        FileCopy.copyToDir(inputFile,_toDir);
      }
    }
    else
    {
      System.err.println("Icon not found:" + filename);
    }
  }

  private void doIt()
  {
    System.out.println("Output dir: "+_toDir);
    RelicsManager relicsMgr=RelicsManager.getInstance();
    List<String> categories=relicsMgr.getCategories();
    for(String categoryName : categories)
    {
      RelicsCategory category=relicsMgr.getRelicCategory(categoryName,false);
      List<Relic> relics=category.getAllRelics();
      for(Relic relic : relics)
      {
        checkRelic(relic);
      }
    }
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new BuildRelicsIconsDb().doIt();
  }
}
