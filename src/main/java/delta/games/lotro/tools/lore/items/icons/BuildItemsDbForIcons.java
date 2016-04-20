package delta.games.lotro.tools.lore.items.icons;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import delta.common.utils.NumericTools;
import delta.common.utils.files.TextFileWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;

/**
 * Builds a LUA/Tulkas items database with one item associated
 * to each possible icon.
 * @author DAM
 */
public class BuildItemsDbForIcons
{
  private static final File LUA_FILE=new File("items.lua");
  private static final File ICON_IDS_FILE=new File("iconIds.txt");
  private HashMap<String,List<Integer>> _iconIds2Ids;
  private Set<String> _iconsIds;
  private Set<String> _backgroundIconsIds;

  /**
   * Constructor.
   */
  public BuildItemsDbForIcons()
  {
    _iconIds2Ids=new HashMap<String,List<Integer>>();
    _iconsIds=new HashSet<String>();
    _backgroundIconsIds=new HashSet<String>();
  }

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

  private void buildDb(HashMap<Integer,Item> items)
  {
    StringBuilder sb=new StringBuilder();
    sb.append("_ITEMSDB =").append("\n");
    sb.append("{").append("\n");
    List<Integer> ids=new ArrayList<Integer>();
    for(Map.Entry<String,List<Integer>> entry : _iconIds2Ids.entrySet())
    {
      ids.add(entry.getValue().get(0));
    }
    Collections.sort(ids);

    List<String> iconIds=new ArrayList<String>();
    for(Integer id : ids)
    {
      Item item=items.get(id);
      int iconId=NumericTools.parseInt(item.getProperty(ItemPropertyNames.ICON_ID),0);
      String name=item.getName();
      if (name==null) name="";
      String hexIconId=Integer.toHexString(iconId).toUpperCase();
      int backgroundIconId=NumericTools.parseInt(item.getProperty(ItemPropertyNames.BACKGROUND_ICON_ID),0);
      String hexBackgroundIconId=Integer.toHexString(backgroundIconId).toUpperCase();
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
    // Write icon IDs file
    {
      TextFileWriter writer=new TextFileWriter(ICON_IDS_FILE);
      writer.start();
      for(String iconId : iconIds)
      {
        writer.writeNextLine(iconId);
      }
      writer.terminate();
    }
  }

  private void doIt()
  {
    File toFile=new File("items.xml").getAbsoluteFile();
    HashMap<Integer,Item> items=loadItemsFile(toFile);
    System.out.println(items.size());
    for(Integer id : items.keySet())
    {
      Item item=items.get(id);
      String iconId=item.getProperty(ItemPropertyNames.ICON_ID);
      _iconsIds.add(iconId);
      String backgroundIconId=item.getProperty(ItemPropertyNames.BACKGROUND_ICON_ID);
      _backgroundIconsIds.add(backgroundIconId);
      String key=iconId+"-"+backgroundIconId;
      List<Integer> list=_iconIds2Ids.get(key);
      if (list==null)
      {
        list=new ArrayList<Integer>();
        _iconIds2Ids.put(key,list);
      }
      list.add(id);
    }
    buildDb(items);
    System.out.println(_iconIds2Ids.size());
    System.out.println(_iconsIds.size());
    System.out.println(_backgroundIconsIds.size());
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new BuildItemsDbForIcons().doIt();
  }
}
