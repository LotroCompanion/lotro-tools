package delta.games.lotro.tools.reports;

import delta.common.utils.variables.VariablesResolver;
import delta.games.lotro.character.BaseCharacterSummary;
import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.classes.WellKnownCharacterClassKeys;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.common.Genders;
import delta.games.lotro.dat.data.strings.renderer.StringRenderer;
import delta.games.lotro.utils.strings.ContextVariableValueProvider;

/**
 * Utility methods related to reports.
 * @author DAM
 */
public class ReportUtils
{
  private static final BaseCharacterSummary DEFAULT_SUMMARY=buildDefaultSummary();

  private static BaseCharacterSummary buildDefaultSummary()
  {
    BaseCharacterSummary ret=new BaseCharacterSummary();
    // Class
    ClassDescription characterClass=ClassesManager.getInstance().getCharacterClassByKey(WellKnownCharacterClassKeys.CHAMPION);
    ret.setCharacterClass(characterClass);
    // Race
    RaceDescription race=RacesManager.getInstance().getByKey("man");
    ret.setRace(race);
    // Gender
    ret.setCharacterSex(Genders.MALE);
    // Level
    ret.setLevel(150);
    // Name
    ret.setName("(character name)"); // I18n
    // Surname
    ret.setSurname("(surname)"); // I18n
    // Rank
    ret.setRankCode(null);
    return ret;
  }

  /**
   * Build a renderer for reports.
   * @return A renderer.
   */
  public static VariablesResolver buildRenderer()
  {
    ContextVariableValueProvider provider=new ContextVariableValueProvider();
    provider.setup(DEFAULT_SUMMARY);
    StringRenderer renderer=new StringRenderer(provider);
    return renderer.getResolver();
  }
}
