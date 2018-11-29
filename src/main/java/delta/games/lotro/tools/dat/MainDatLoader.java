package delta.games.lotro.tools.dat;

import delta.games.lotro.common.progression.ProgressionsManager;

/**
 * Main entry for loading data from DAT files.
 * @author DAM
 */
public class MainDatLoader
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
    progressions.writeToFile(GeneratedFiles.PROGRESSIONS);
  }

  /**
   * Main method of this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainDatLoader().doIt();
  }
}
