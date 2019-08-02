package delta.games.lotro.tools.dat;

import delta.games.lotro.common.progression.ProgressionsManager;

/**
 * Tool to merge progression files into a single file.
 * @author DAM
 */
public class MainProgressionsMerger
{
  private void doIt()
  {
    mergeProgressions();
  }

  private void mergeProgressions()
  {
    ProgressionsManager progressions=new ProgressionsManager();
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_ITEMS);
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_CHARACTERS);
    progressions.loadFromFile(GeneratedFiles.PROGRESSIONS_ACHIEVABLES);
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
