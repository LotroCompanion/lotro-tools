package delta.games.lotro.tools.lore.items;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;

/**
 * Merges "legacy" items database and "tulkas index" database
 * into a single items database.
 * @author DAM
 */
public class ItemsMerge
{
  private HashMap<Integer,Item> loadItemsFile(File file)
  {
    ItemXMLParser parser=new ItemXMLParser();
    List<Item> items=parser.parseItemsFile(file);
    HashMap<Integer,Item> ret=new HashMap<Integer,Item>();
    for(Item item : items)
    {
      ret.put(Integer.valueOf(item.getIdentifier()),item);
    }
    return ret;
  }

  private void doIt()
  {
    File file1=new File("itemsLegacy.xml");
    HashMap<Integer,Item> map1=loadItemsFile(file1);
    System.out.println(map1.size());
    File file2=new File("itemsTulkasIndex.xml");
    HashMap<Integer,Item> map2=loadItemsFile(file2);
    System.out.println(map2.size());
  }

  /**
   * Main method.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ItemsMerge().doIt();
  }
}
