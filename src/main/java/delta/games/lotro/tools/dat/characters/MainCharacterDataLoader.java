package delta.games.lotro.tools.dat.characters;

import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.dat.data.DataFacade;
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
    // Load race data
    new RaceDataLoader(_facade).doIt();
    // Load character class data
    new CharacterClassDataLoader(_facade).doIt();
    // Load virtues data
    new VirtueDataLoader(_facade).doIt();
    // Load progression of class trait points with character level
    DatStatUtils.getProgression(_facade,1879271247);

    // Save progressions
    DatStatUtils._progressions.writeToFile(GeneratedFiles.PROGRESSIONS_CHARACTERS);
    // Stats usage statistics
    DatStatUtils._statsUsageStatistics.showResults();
    // Save skills
    SkillsManager skillsManager=SkillsManager.getInstance();
    SkillLoader.saveSkills(skillsManager);
    // Load gear icons
    new SlotIconsLoader(_facade).doIt();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainCharacterDataLoader(facade).doIt();
    facade.dispose();
  }
}
