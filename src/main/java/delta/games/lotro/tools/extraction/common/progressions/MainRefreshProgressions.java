package delta.games.lotro.tools.extraction.common.progressions;

import delta.games.lotro.common.progression.ProgressionsManager;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.utils.DataFacadeBuilder;
import delta.games.lotro.utils.maths.Progression;

/**
 * Load all currently defined progressions and re-write them.
 * @author DAM
 */
public class MainRefreshProgressions
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    ProgressionsManager mgr=ProgressionsManager.getInstance();
    for(Progression progression : mgr.getAll())
    {
      int progressionId=progression.getIdentifier();
      ProgressionUtils.getProgression(facade,progressionId);
    }
    ProgressionUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS);
  }
}
