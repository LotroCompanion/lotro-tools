package delta.games.lotro.tools.lore.deeds.lorebook;

import java.io.File;
import java.util.List;

import delta.games.lotro.LotroCoreConfig;
import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.objects.ObjectItem;
import delta.games.lotro.common.objects.ObjectsSet;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLParser;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.tools.lore.deeds.DeedsContainer;

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
    File out=new File(loreDir,"deeds_by_name.xml");
    DeedsContainer.writeSortedDeeds(deeds,out);
  }

  private void normalizeDeed(DeedDescription deed)
  {
    // Remove key
    deed.setKey(null);
    // Normalize EOL/LF
    // - description
    deed.setDescription(normalizeText(deed.getDescription()));
    // - objectives
    deed.setObjectives(normalizeText(deed.getObjectives()));
    // Find item IDs
    // TODO Use ItemsResolver
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

  private String normalizeText(String text)
  {
    if (text!=null)
    {
      text=text.replace("\r\n","\n");
      text=text.replace("<br />","");
      while(true)
      {
        int index=text.indexOf("<");
        if (index==-1) break;
        int index2=text.indexOf(">",index);
        if (index2==-1) break;
        text=text.substring(0,index)+text.substring(index2+1);
      }
      text=text.replace("</span>","");
    }
    return text;
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
