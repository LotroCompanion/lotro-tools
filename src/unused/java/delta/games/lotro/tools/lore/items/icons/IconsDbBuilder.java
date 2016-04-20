package delta.games.lotro.tools.lore.items.icons;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.common.utils.files.FileCopy;
import delta.common.utils.misc.CRC;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;
import delta.games.lotro.utils.LotroLoggers;

/**
 * Icons database builder.
 * @author DAM
 */
public class IconsDbBuilder
{
  private static final Logger _logger=LotroLoggers.getWebInputLogger();

  private File _sourceIconsDir=new File("D:\\tmp\\item icons\\lotro.fr-icons");
  private File _sourceIconsDir2=new File("D:\\tmp\\legacy icons");
  private File _iconsDbDir=new File("d:\\tmp\\icons-db");
  private HashMap<String,List<Integer>> _iconIds2Ids;
  private Set<String> _iconsIds;
  private Set<String> _backgroundIconsIds;

  /**
   * Constructor.
   */
  public IconsDbBuilder()
  {
    _iconIds2Ids=new HashMap<String,List<Integer>>();
    _iconsIds=new HashSet<String>();
    _backgroundIconsIds=new HashSet<String>();
  }

  private int _nbDifferentIconsMissing;
  private int _nbItemsWithIcon;
  private int _nbItemsWithNoIcon;

  private HashMap<Long,List<File>> buildCrcMap()
  {
    HashMap<Long,List<File>> ret=new HashMap<Long,List<File>>();
    File[] files=_sourceIconsDir.listFiles();
    for(File file : files)
    {
      Long crc=Long.valueOf(CRC.computeCRC(file));
      
      List<File> list=ret.get(crc);
      if (list==null)
      {
        list=new ArrayList<File>();
        ret.put(crc,list);
      }
      list.add(file);
    }
    return ret;
  }

  private int pruneIdenticalFiles()
  {
    int nbDeleted=0;
    HashMap<Long,List<File>> map=buildCrcMap();
    for(Map.Entry<Long,List<File>> entry : map.entrySet())
    {
      List<File> files=entry.getValue();
      int nbFiles=files.size();
      if (nbFiles>1)
      {
        List<File> toDelete=new ArrayList<File>();
        for(int i=0;i<nbFiles;i++)
        {
          File refFile=files.get(i);
          for(int j=i+1;j<nbFiles;j++)
          {
            File file=files.get(j);
            boolean same=ComparePngFiles.compareRawFiles(refFile,file);
            if (same)
            {
              toDelete.add(refFile);
              break;
            }
          }
        }
        for(File fileToDelete : toDelete)
        {
          fileToDelete.delete();
          nbDeleted++;
          System.out.println("Removed: "+fileToDelete);
        }
      }
    }
    return nbDeleted;
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

  private void handleIconList(HashMap<Integer,Item> items, String key, List<Integer> ids)
  {
    int nbIcons=ids.size();
    File sourceFile=null;
    int fileIndex=0;
    File[] dirs={_sourceIconsDir, _sourceIconsDir2} ;
    for(int i=0;i<nbIcons;i++)
    {
      String id=ids.get(i).toString()+".png";
      for(File dir : dirs)
      {
        File iconFile=new File(dir, id);
        if (iconFile.exists())
        {
          if (sourceFile==null)
          {
            sourceFile=iconFile;
            _nbItemsWithIcon+=nbIcons;
            File to=new File(_iconsDbDir,key+".png");
            FileCopy.copy(iconFile,to);
            fileIndex++;
          }
          else
          {
            // Compare source file and icon file, warn if different
            boolean same=ComparePngFiles.compareFiles(sourceFile,iconFile);
            if (!same)
            {
              _logger.warn("Icon file differ: source="+sourceFile+", new="+iconFile);
              File to=new File(_iconsDbDir,key+"-"+fileIndex+".png");
              FileCopy.copy(iconFile,to);
              fileIndex++;
            }
          }
        }
      }
    }
    if (sourceFile==null)
    {
      //_logger.warn("No icon for key: "+key);
      _nbDifferentIconsMissing++;
      _nbItemsWithNoIcon+=nbIcons;
      for(Integer itemId : ids)
      {
        Item item=items.get(itemId);
        System.out.println(item.getName());
      }
    }
  }

  void doIt2()
  {
    while(true)
    {
      int nbDeleted=pruneIdenticalFiles();
      if (nbDeleted==0) break;
    }
  }

  private void doIt()
  {
    _iconsDbDir.mkdirs();
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
    System.out.println(_iconIds2Ids.size());
    System.out.println(_iconsIds.size());
    System.out.println(_backgroundIconsIds.size());
    for(Map.Entry<String,List<Integer>> entry : _iconIds2Ids.entrySet())
    {
      handleIconList(items,entry.getKey(),entry.getValue());
      /*
      int size=entry.getValue().size();
      if (size > 10)
      {
        System.out.println(entry.getKey() + " -> " + size);
      }
      */
    }
    System.out.println("Nb items with icon: "+_nbItemsWithIcon);
    System.out.println("Nb items with no icon: "+_nbItemsWithNoIcon);
    System.out.println("Nb icons missing: "+_nbDifferentIconsMissing);
    //System.out.println(_iconIds2Ids);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new IconsDbBuilder().doIt();
  }
}
