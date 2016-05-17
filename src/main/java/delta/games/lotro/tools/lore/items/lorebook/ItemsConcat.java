package delta.games.lotro.tools.lore.items.lorebook;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import delta.common.utils.NumericTools;
import delta.common.utils.files.filter.ExtensionPredicate;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.io.xml.ItemXMLParser;

/**
 * Concatenates a series of item files into a single file.
 * @author DAM
 */
public class ItemsConcat
{
  /**
   * Main method.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    System.out.println("Concatenation of legacy items into one file...");
    File itemsDir=new File("d:\\dam\\tmp\\items");
    FileFilter fileFilter=new ExtensionPredicate("xml");
    File[] itemFiles=itemsDir.listFiles(fileFilter);
    if (itemFiles!=null)
    {
      //BonusConverter converter=new BonusConverter();
      ItemXMLParser parser=new ItemXMLParser();
      List<Item> itemsList=new ArrayList<Item>();
      for(File itemFile : itemFiles)
      {
        String idStr=itemFile.getName();
        idStr=idStr.substring(0,idStr.length()-4);
        int id=NumericTools.parseInt(idStr,-1);
        if (id!=-1)
        {
          //System.out.println(id);
          Item item=parser.parseXML(itemFile);
          /*
          List<String> bonuses=item.getBonus();
          if (bonuses.size()>0)
          {
            RawBonusParser bonusParser=new RawBonusParser();
            BonusManager bonusMgr=bonusParser.build(bonuses);
            if (bonusParser.hasWarn())
            {
              String name=item.getName();
              System.out.println("Item: "+id+", name="+name);
              for(String bonus : bonuses)
              {
                System.out.println("\t"+bonus);
              }
            }
            if (bonusMgr!=null)
            {
              BasicStatsSet stats=converter.getStats(bonusMgr);
              item.getStats().setStats(stats);
            }
          }
          */
          item.setIdentifier(id);
          itemsList.add(item);
        }
      }
      File toFile=new File("itemsLegacy.xml").getAbsoluteFile();
      ItemsManager mgr=ItemsManager.getInstance();
      mgr.writeItemsFile(toFile,itemsList);
    }
  }
}
