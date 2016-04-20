package delta.games.lotro.tools.lore.items.icons;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import delta.common.utils.files.FileCopy;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;

/**
 * Legacy icons extractor.
 * @author DAM
 */
public class LegacyIconsExtractor
{
  private File _legacyIconsDir=new File("D:\\tmp\\item icons\\legacy icons");
  private File _iconsDbDir=new File("d:\\tmp\\legacy icons");

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

  private String normalizeUrl(String url)
  {
    String ret=url;
    String seed1="http://content.turbine.com/sites/lorebook.lotro.com/images/";
    if (url.startsWith(seed1))
    {
      ret=url.substring(seed1.length());
    }
    String seed2="http://my.lotro.com/sites/lorebook.lotro.com/images/";
    if (url.startsWith(seed2))
    {
      ret=url.substring(seed2.length());
    }
    String seed3="https://my.lotro.com/sites/lorebook.lotro.com/images/";
    if (url.startsWith(seed3))
    {
      ret=url.substring(seed3.length());
    }
    return ret;
  }

  private void doIt()
  {
    _iconsDbDir.mkdirs();
    File fromFile=new File("items.xml").getAbsoluteFile();
    HashMap<Integer,Item> items=loadItemsFile(fromFile);
    System.out.println(items.size());
    for(Integer id : items.keySet())
    {
      Item item=items.get(id);
      String iconUrl=item.getProperty(ItemPropertyNames.ICON_URL);
      if (iconUrl!=null)
      {
        String url=normalizeUrl(iconUrl);
        //System.out.println(url);
        File iconFile=getIconFile(url);
        if (iconFile.exists())
        {
          File to=new File(_iconsDbDir,id+".png");
          FileCopy.copy(iconFile,to);
        }
        else
        {
          System.out.println("Not found: "+url);
        }
      }
    }
  }

  private File getIconFile(String url)
  {
    url=url.replace('/','\\');
    File from=new File(_legacyIconsDir,url);
    return from;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new LegacyIconsExtractor().doIt();
  }
}
