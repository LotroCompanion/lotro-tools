package delta.games.lotro.tools.lore.items.icons;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import delta.common.utils.files.TextFileWriter;
import delta.games.lotro.config.DataFiles;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.io.xml.ItemSaxParser;

/**
 * Builds a LUA/Tulkas items database from the known items.
 * @author DAM
 */
public class BuildItemsDbForLua
{
  private static final File LUA_FILE=new File("items.lua");

  /**
   * Constructor.
   */
  public BuildItemsDbForLua()
  {
    // Nothing!
  }

  /**
   * Build a LUA database using the given items.
   * @param items Items database.
   */
  public void buildDb(List<Item> items)
  {
    StringBuilder sb=new StringBuilder();
    sb.append("_ITEMSDB =").append("\n");
    sb.append("{").append("\n");

    List<String> iconIds=new ArrayList<String>();
    for(Item item : items)
    {
      String name=item.getName();
      if (name==null) name="";
      name=name.replace("\n","");
      name=name.replace("\r","");
      int iconId=item.getIconId();
      String hexIconId=Integer.toHexString(iconId).toUpperCase();
      int backgroundIconId=item.getBackgroundIconId();
      String hexBackgroundIconId=Integer.toHexString(backgroundIconId).toUpperCase();
      int id=item.getIdentifier();
      sb.append("[").append(id).append("]={[1]=\"");
      sb.append(name).append("\";[2]=\"\";[3]=5;[4]=4;[5]=3;[6]=false;[7]=false;[8]=0x");
      sb.append(hexIconId).append(";[9]=0x").append(hexBackgroundIconId).append(";};\n");
      iconIds.add(iconId+"-"+backgroundIconId);
    }
    sb.append("};\n");
    // Write LUA file
    {
      TextFileWriter writer=new TextFileWriter(LUA_FILE);
      writer.start();
      writer.writeSomeText(sb.toString());
      writer.terminate();
    }
  }

  private void doIt()
  {
    File itemsFile=LotroCoreConfig.getInstance().getFile(DataFiles.ITEMS);
    List<Item> items=ItemSaxParser.parseItemsFile(itemsFile);
    buildDb(items);
    System.out.println("Total items count: "+items.size());
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new BuildItemsDbForLua().doIt();
  }
}
