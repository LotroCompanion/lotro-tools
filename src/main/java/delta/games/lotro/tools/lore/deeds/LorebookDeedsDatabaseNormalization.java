package delta.games.lotro.tools.lore.deeds;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class LorebookDeedsDatabaseNormalization
{
  private List<Item> _items;

  private LorebookDeedsDatabaseNormalization()
  {
    _items=ItemsManager.getInstance().getAllItems();
  }

  private void doIt()
  {
    File loreDir=LotroCoreConfig.getInstance().getLoreDir();
    File in=new File(loreDir,"deeds.xml");
    DeedXMLParser parser=new DeedXMLParser();
    List<DeedDescription> deeds=parser.parseXML(in);
    for(DeedDescription deed : deeds)
    {
      normalizeDeed(deed);
    }
    List<Comparator<DeedDescription>> comparators=new ArrayList<Comparator<DeedDescription>>();
    comparators.add(new DeedNameComparator());
    comparators.add(new DeedDescriptionComparator());
    CompoundComparator<DeedDescription> comparator=new CompoundComparator<DeedDescription>(comparators);
    Collections.sort(deeds,comparator);
    File out=new File(loreDir,"deeds_by_name.xml");
    DeedXMLWriter writer=new DeedXMLWriter();
    writer.writeDeeds(out,deeds,EncodingNames.UTF_8);
  }

  private void normalizeDeed(DeedDescription deed)
  {
    // Remove key
    deed.setKey(null);
    // Normalize EOL/LF
    // - description
    String description=deed.getDescription();
    if (description!=null)
    {
      deed.setDescription(description.replace("\r\n","\n"));
      deed.setDescription(deed.getDescription().replace("<br />",""));
    }
    // - objectives
    String objectives=deed.getObjectives();
    if (objectives!=null)
    {
      deed.setObjectives(objectives.replace("\r\n","\n"));
      deed.setObjectives(deed.getObjectives().replace("<br />",""));
    }
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
    new LorebookDeedsDatabaseNormalization().doIt();
  }
}
