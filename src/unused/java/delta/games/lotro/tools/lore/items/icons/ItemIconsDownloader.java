package delta.games.lotro.tools.lore.items.icons;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import delta.downloads.DownloadException;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;
import delta.games.lotro.utils.DownloadService;
import delta.games.lotro.utils.LotroLoggers;

/**
 * Downloads item icons from the site lotro.fr
 * @author DAM
 */
public class ItemIconsDownloader
{
  private static final Logger _logger=LotroLoggers.getWebInputLogger();

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

  private File _toDir=new File("d:\\tmp\\icons");

  private void doIt() {
    DownloadService downloader=DownloadService.getInstance();
    File toFile=new File("items.xml").getAbsoluteFile();
    _toDir.mkdirs();
    HashMap<Integer,Item> items=loadItemsFile(toFile);
    System.out.println(items.size());
    for(Integer id : items.keySet())
    {
      System.out.println(id);
      String url="http://lotro.fr/img/bdd/items/"+id+".png";
      File to=new File(_toDir,id+".png");
      try
      {
        downloader.downloadToFile(url,to);
      }
      catch(DownloadException de)
      {
        _logger.error("Cannot fetch icon ["+url+"]!",de);
      }
    }
  }

  /*
  private void doIt2()
  {
    File[] files=_toDir.listFiles();
    for(File file : files)
    {
      String name=file.getName();
      name=name.substring(0,name.length()-4);
      long crc=CRC.computeCRC(file);
      System.out.println(name+"\t"+crc+"\t"+file.length());
      //if (crc==2459958024L) file.delete();
    }
  }
  */

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ItemIconsDownloader().doIt();
  }
}
