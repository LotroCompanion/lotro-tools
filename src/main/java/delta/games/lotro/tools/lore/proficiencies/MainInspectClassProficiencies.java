package delta.games.lotro.tools.lore.proficiencies;

import java.util.List;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.utils.TraitAndLevel;
import delta.games.lotro.common.enums.SkillCategory;

/**
 * Tool to display class proficiencies.
 * @author DAM
 */
public class MainInspectClassProficiencies
{
  private void doClass(ClassDescription characterClass)
  {
    List<TraitAndLevel> traits=characterClass.getTraits();
    for(TraitAndLevel classTrait : traits)
    {
      int level=classTrait.getRequiredLevel();
      TraitDescription trait=classTrait.getTrait();
      String name=trait.getName();
      int minLevel=trait.getMinLevel();
      SkillCategory category=trait.getCategory();
      System.out.println(level+" => "+name+" (min level="+minLevel+", category="+category+")");
    }
  }

  private void doIt()
  {
    for(ClassDescription characterClass : ClassesManager.getInstance().getAllCharacterClasses())
    {
      System.out.println("************ Class : "+characterClass.getName());
      doClass(characterClass);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainInspectClassProficiencies().doIt();
  }
}
