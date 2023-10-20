package delta.games.lotro.tools.dat;

import delta.games.lotro.common.progression.ProgressionsManager;

/**
 * Tool to merge progression files into a single file.
 * @author DAM
 */
public class MainProgressionsMerger
{
  /**
   * Perform merge.
   */
  public void doIt()
  {
    mergeProgressions();
  }

  private void mergeProgressions()
  {
    ProgressionsManager progressions=new ProgressionsManager();
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_ITEMS);
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_ITEMS_SETS);
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_CHARACTERS);
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_ACHIEVABLES);
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_COMBAT);
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_BUFFS);
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_LEGENDARY);
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_EFFECTS);
    progressions.writeToFile(GeneratedFiles.PROGRESSIONS);
  }

  /**
   * Main method of this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainProgressionsMerger().doIt();
  }
}
