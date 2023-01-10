package delta.games.lotro.tools.dat.characters;

import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.misc.SlotIconsLoader;
import delta.games.lotro.tools.dat.utils.DatStatUtils;

/**
 * Loader for character data: classes, races, trait trees.
 * @author DAM
 */
public class MainCharacterDataLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainCharacterDataLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load character data.
   */
  public void doIt()
  {
    // Load character class data
    new CharacterClassDataLoader(_facade).doIt();
    // Load virtues data
    new VirtueDataLoader(_facade).doIt();
    // Load progression of class trait points with character level (expect 1879271247)
    Integer progressionId=getLevelToTraitPointsProgressionId();
    if (progressionId!=null)
    {
      DatStatUtils.getProgression(_facade,progressionId.intValue());
    }

    // Save progressions
    DatStatUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_CHARACTERS);
    // Load gear icons
    if (Context.isLive())
    {
      new SlotIconsLoader(_facade).doIt();
    }
  }

  private Integer getLevelToTraitPointsProgressionId()
  {
    PropertiesSet props=_facade.loadProperties(0x7900025B); // TraitControl
    Integer id=(Integer)props.getProperty("Trait_Control_LevelToTotalClassTraitPointsProgression");
    return id;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=new DataFacade();
    new MainCharacterDataLoader(facade).doIt();
    // Stats usage statistics
    DatStatUtils.STATS_USAGE_STATISTICS.showResults();
    facade.dispose();
  }
}
