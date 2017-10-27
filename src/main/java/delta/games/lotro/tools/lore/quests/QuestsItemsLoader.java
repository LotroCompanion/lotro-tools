package delta.games.lotro.tools.lore.quests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.objects.ObjectItem;
import delta.games.lotro.common.objects.ObjectsSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;
import delta.games.lotro.lore.quests.index.QuestCategory;
import delta.games.lotro.lore.quests.index.QuestSummary;
import delta.games.lotro.lore.quests.index.QuestsIndex;
import delta.games.lotro.lore.quests.io.xml.QuestXMLWriter;
import delta.games.lotro.tools.lore.items.ItemsResolver;
import delta.games.lotro.utils.LotroLoggers;

/**
 * Loader for the reward items of quests.
 * @author DAM
 */
public class QuestsItemsLoader
{
  private static final Logger _logger=LotroLoggers.getWebInputLogger();

  private static final String WIKI_SEED="/wiki/";

  private ItemsResolver _resolver;

  private int _resolved=0;
  private int _missed=0;

  /**
   * Constructor.
   */
  public QuestsItemsLoader()
  {
    _resolver=new ItemsResolver();
  }

  private void handleSet(ObjectsSet set)
  {
    if (set!=null)
    {
      int nb=set.getNbObjectItems();
      for(int i=0;i<nb;i++)
      {
        ObjectItem item=set.getItem(i);
        handleItem(item);
      }
    }
  }

  private void handleItem(ObjectItem itemReference)
  {
    // Use name
    String name=itemReference.getName();
    Item item=_resolver.getItem(name);
    if (item==null)
    {
      // Use item 'key' (Wiki key)
      String url=itemReference.getObjectURL();
      if ((url!=null) && (url.startsWith(WIKI_SEED)))
      {
        String itemKey=url.substring(WIKI_SEED.length());
        item=_resolver.getItem(itemKey);
      }
    }
    if (item==null)
    {
      // Use icon path
      String iconUrl=itemReference.getIconURL();
      if (iconUrl!=null)
      {
        iconUrl=ItemsResolver.normalizeIconUrl(iconUrl);
        item=_resolver.getItem(iconUrl);
      }
    }
    if (item!=null)
    {
      // Found!
      itemReference.setItemId(item.getIdentifier());
      itemReference.setIconURL(null);
      itemReference.setObjectURL(null);
      _resolved++;
    }
    else
    {
      // Not found!
      System.out.println("Not found: "+itemReference);
      _missed++;
    }
  }

  private void handleQuest(int index, QuestDescription q)
  {
    try
    {
      //String key=q.getKey();
      //System.out.println("#"+index+", quest: "+key);
      Rewards r=q.getQuestRewards();
      ObjectsSet set=r.getObjects();
      handleSet(set);
      ObjectsSet set2=r.getSelectObjects();
      handleSet(set2);
    }
    catch(Throwable t)
    {
      t.printStackTrace();
    }
  }

  private void doIt()
  {
    List<QuestDescription> storage=new ArrayList<QuestDescription>();
    QuestsManager qm=QuestsManager.getInstance();
    QuestsIndex index=qm.getIndex();
    if (index!=null)
    {
      String[] categories=index.getCategories();
      System.out.println(Arrays.deepToString(categories));
      for(String category : categories)
      {
        QuestCategory c=index.getCategory(category);
        QuestSummary[] quests=c.getQuests();
        int indexQ=0;
        for(QuestSummary questSum : quests)
        {
          int id=questSum.getIdentifier();
          QuestDescription q=qm.getQuest(id);
          handleQuest(indexQ,q);
          storage.add(q);
          indexQ++;
        }
      }
      System.out.println("Resolved: "+_resolved+", missed: "+_missed);
      writeQuestsDatabase(storage);
    }
    else
    {
      _logger.error("Cannot gets quests index file!");
    }
  }

  private void writeQuestsDatabase(List<QuestDescription> quests)
  {
    QuestsDatabaseGenerator dbBuilder=new QuestsDatabaseGenerator();
    File questsDir=dbBuilder.getQuestsDir();
    questsDir.mkdirs();
    QuestXMLWriter writer=new QuestXMLWriter();
    for(QuestDescription quest : quests)
    {
      int id=quest.getIdentifier();
      String fileName=String.valueOf(id)+".xml";
      File questFile=new File(questsDir,fileName);
      writer.write(questFile,quest,EncodingNames.UTF_8);
    }
    dbBuilder.writeDatabase();
  }

  /**
   * Basic main method for test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new QuestsItemsLoader().doIt();
  }
}
