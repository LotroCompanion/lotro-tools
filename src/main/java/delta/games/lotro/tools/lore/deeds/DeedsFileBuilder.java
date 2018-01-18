package delta.games.lotro.tools.lore.deeds;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import delta.common.utils.collections.CompoundComparator;
import delta.common.utils.text.EncodingNames;
import delta.games.lotro.LotroCoreConfig;
import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.objects.ObjectItem;
import delta.games.lotro.common.objects.ObjectsSet;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.comparators.DeedDescriptionComparator;
import delta.games.lotro.lore.deeds.comparators.DeedNameComparator;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLParser;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Builds a single deeds file from a collection of deed files.
 * @author DAM
 */
public class DeedsFileBuilder
{
  private List<Item> _items;

  private DeedsFileBuilder()
  {
    _items=ItemsManager.getInstance().getAllItems();
  }

  private void doIt()
  {
    File loreDir=LotroCoreConfig.getInstance().getLoreDir();
    File deedsDir=new File(loreDir,"deeds");
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    DeedXMLParser parser=new DeedXMLParser();
    for(File deedFile : deedsDir.listFiles())
    {
      List<DeedDescription> newDeeds=parser.parseXML(deedFile);
      deeds.addAll(newDeeds);
    }
    File out=new File(loreDir,"deeds.xml");
    DeedXMLWriter writer=new DeedXMLWriter();
    writer.writeDeeds(out,deeds,EncodingNames.UTF_8);
  }

  private void doIt2()
  {
    File loreDir=LotroCoreConfig.getInstance().getLoreDir();
    File in=new File(loreDir,"deeds.xml");
    DeedXMLParser parser=new DeedXMLParser();
    List<DeedDescription> deeds=parser.parseXML(in);
    for(DeedDescription deed : deeds)
    {
      normalizeDeed(deed);
    }
    CompoundComparator<DeedDescription> comparator=new CompoundComparator<DeedDescription>(new DeedNameComparator(),new DeedDescriptionComparator());
    Collections.sort(deeds,comparator);
    File out=new File(loreDir,"deeds_by_name.xml");
    DeedXMLWriter writer=new DeedXMLWriter();
    writer.writeDeeds(out,deeds,EncodingNames.UTF_8);
  }

  private void normalizeDeed(DeedDescription deed)
  {
    // Remove key
    deed.setKey(null);
    // Find item IDs
    Rewards rewards=deed.getRewards();
    ObjectsSet objects=rewards.getObjects();
    int nbItems=objects.getNbObjectItems();
    for(int i=0;i<nbItems;i++)
    {
      ObjectItem objectItem=objects.getItem(i);
      String name=objectItem.getName();
      Item item=getItemByName(name);
      if (item!=null)
      {
        objectItem.setItemId(item.getIdentifier());
        objectItem.setObjectURL(null);
        objectItem.setIconURL(null);
      }
      else
      {
        System.out.println("Item not found [" + name + "]");
      }
    }
  }

  private Item getItemByName(String name)
  {
    Item ret=null;
    for(Item item : _items)
    {
      if (name.equals(item.getName()))
      {
        ret=item;
        break;
      }
    }
    return ret;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new DeedsFileBuilder().doIt2();
  }
}
