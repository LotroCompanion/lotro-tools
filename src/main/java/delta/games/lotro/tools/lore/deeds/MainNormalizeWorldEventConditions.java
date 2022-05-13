package delta.games.lotro.tools.lore.deeds;

import java.util.List;

import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;
import delta.games.lotro.lore.quests.io.xml.QuestXMLWriter;
import delta.games.lotro.lore.worldEvents.AbstractWorldEventCondition;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.WorldEventConditionNormalizer;

/**
 * Normalizer for world event conditions.
 * @author DAM
 */
public class MainNormalizeWorldEventConditions
{
  private WorldEventConditionNormalizer _normalizer=new WorldEventConditionNormalizer();

  private void handleAchievable(Achievable achievable)
  {
    AbstractWorldEventCondition condition=achievable.getWorldEventsRequirement();
    AbstractWorldEventCondition normalized=_normalizer.normalize(condition);
    achievable.setWorldEventsRequirement(normalized);
  }

  private void doIt()
  {
    List<QuestDescription> quests=QuestsManager.getInstance().getAll();
    for(QuestDescription quest : quests)
    {
      handleAchievable(quest);
    }
    QuestXMLWriter.writeQuestsFile(GeneratedFiles.QUESTS,quests);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainNormalizeWorldEventConditions().doIt();
  }
}
