package delta.games.lotro.tools.reports;

import java.io.File;

import delta.common.utils.files.TextFileWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.cosmetics.ItemCosmetics;
import delta.games.lotro.lore.items.cosmetics.ItemCosmeticsManager;

/**
 * Tool to display cosmetic groups.
 * @author DAM
 */
public class MainInspectSameCosmetics
{
  private void doIt()
  {
    TextFileWriter w=new TextFileWriter(new File("cosmetics.txt"));
    w.start();
    ItemsManager itemsMgr=ItemsManager.getInstance();
    ItemCosmeticsManager mgr=ItemCosmeticsManager.getInstance();
    ItemCosmetics cosmetics=mgr.getData();
    for(Integer id : cosmetics.getCosmeticsIDs())
    {
      w.writeNextLine("ID="+id);
      int[] itemIDs=cosmetics.findItemIDs(id.intValue());
      for(int itemID : itemIDs)
      {
        Item item=itemsMgr.getItem(itemID);
        w.writeNextLine("\t"+item);
      }
    }
    w.terminate();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainInspectSameCosmetics().doIt();
  }
}
