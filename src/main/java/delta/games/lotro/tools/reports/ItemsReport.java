package delta.games.lotro.tools.reports;

import java.io.File;
import java.util.List;

import delta.common.utils.files.TextFileWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemUtils;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Report for items.
 * @author DAM
 */
public class ItemsReport
{
  /**
   * Do report.
   */
  public void doIt()
  {
    File rootDir=ReportsConstants.getDataReportsRootDir();
    rootDir.mkdirs();
    File toFile=new File(rootDir,"items.txt");
    TextFileWriter writer=new TextFileWriter(toFile);
    writer.start();
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      showItem(item,writer);
    }
    writer.terminate();
  }

  private void showItem(Item item, TextFileWriter writer)
  {
    writer.writeNextLine("Item: "+item);
    List<String> text=ItemUtils.buildLinesToShowItem(item,null);
    for(String line : text)
    {
      writer.writeNextLine(line);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ItemsReport().doIt();
  }
}
