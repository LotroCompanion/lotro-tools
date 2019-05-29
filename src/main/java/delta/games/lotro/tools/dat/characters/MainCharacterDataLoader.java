package delta.games.lotro.tools.dat.characters;

import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;

/**
 * Loader for character data: classes, races, trait trees.
 * @author DAM
 */
public class MainCharacterDataLoader
{
  private void doIt()
  {
    DataFacade facade=new DataFacade();
    TraitsManager traitsManager=new TraitsManager();

    // Load race data
    new RaceDataLoader(facade,traitsManager).doIt();
    // Load character class data
    new CharacterClassDataLoader(facade,traitsManager).doIt();
    // Load virtues data
    new VirtueDataLoader(facade,traitsManager).doIt();

    // Save progressions
    DatStatUtils._progressions.writeToFile(GeneratedFiles.PROGRESSIONS_CHARACTERS);
    // Stats usage statistics
    DatStatUtils._statsUsageStatistics.showResults();
    // Save traits
    TraitLoader.saveTraits(traitsManager);
    // Save skills
    SkillsManager skillsManager=SkillsManager.getInstance();
    SkillLoader.saveSkills(skillsManager);

    facade.dispose();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainCharacterDataLoader().doIt();
  }
}
