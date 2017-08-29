package delta.games.lotro.tools.lore.items.lotroplan.essences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.io.xml.ItemSaxParser;

/**
 * Generates template sheets to fill item stats.
 * @author DAM
 */
public class MainItemStatsSheetTemplateGenerator
{
  private void doIt()
  {
    File newFile=new File("data\\items\\items_filtered.xml").getAbsoluteFile();
    List<Item> newItems=ItemSaxParser.parseItemsFile(newFile);
    List<Item> essences=getEssences(newItems,10);
    buildTemplate(essences);
  }

  private List<Item> getEssences(List<Item> items, int tier)
  {
    List<Item> ret=new ArrayList<Item>();
    String expectedCategory="Essence:Tier"+tier;
    for(Item item : items)
    {
      String category=item.getSubCategory();
      if (expectedCategory.equals(category))
      {
        ret.add(item);
      }
    }
    return ret;
  }

  private void buildTemplate(List<Item> items)
  {
    StringBuilder sb=new StringBuilder();
    // Headers
    List<String> headers=new ArrayList<String>();
    for(String cell : EssenceStatsInjector.CELLS)
    {
      headers.add(cell);
    }
    putLine(headers,sb);
    // Items
    List<String> cells=new ArrayList<String>(headers);
    for(Item item : items)
    {
      int id=item.getIdentifier();
      cells.set(0,String.valueOf(id));
      String name=item.getName();
      cells.set(1,name);
      putLine(cells,sb);
    }
    System.out.println(sb);
  }

  private void putLine(List<String> cells, StringBuilder sb)
  {
    int nb=cells.size();
    int index=0;
    for(String cell : cells)
    {
      sb.append(cell);
      index++;
      sb.append((index<nb)?'\t':'\n');
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainItemStatsSheetTemplateGenerator().doIt();
  }
}
