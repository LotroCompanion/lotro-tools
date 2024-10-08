package delta.games.lotro.tools.checks;

import java.io.File;
import java.util.Locale;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.character.races.io.xml.RaceDescriptionXMLWriter;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.io.xml.ItemXMLWriter;
import delta.games.lotro.lore.quests.AchievableProxiesResolver;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;
import delta.games.lotro.lore.quests.io.xml.QuestXMLWriter;

/**
 * Test localization data.
 * @author DAM
 */
public class MainTestLocalization
{
  private void doIt()
  {
    doItems();
    doDeeds();
    doQuests();
    doRaces();
  }

  private void doItems()
  {
    ItemsManager mgr=ItemsManager.getInstance();
    ItemXMLWriter.writeItemsFile(new File("items.xml"),mgr.getAllItems());
  }

  private void doQuests()
  {
    QuestsManager mgr=QuestsManager.getInstance();
    for(QuestDescription quest : mgr.getAll())
    {
      AchievableProxiesResolver.getInstance().resolveQuest(quest);
    }
    QuestXMLWriter.writeQuestsFile(new File("quests.xml"),mgr.getAll());
  }

  private void doDeeds()
  {
    DeedsManager mgr=DeedsManager.getInstance();
    for(DeedDescription deed : mgr.getAll())
    {
      AchievableProxiesResolver.getInstance().resolveDeed(deed);
    }
    new DeedXMLWriter().writeDeeds(new File("deeds.xml"),mgr.getAll(),EncodingNames.UTF_8);
  }

  private void doRaces()
  {
    RacesManager mgr=RacesManager.getInstance();
    RaceDescriptionXMLWriter.write(new File("races.xml"),mgr.getAll());
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    Locale.setDefault(Locale.ENGLISH);
    new MainTestLocalization().doIt();
  }
}
