package delta.games.lotro.tools.dat.skills.mounts;

import java.util.List;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.lore.collections.mounts.MountDescription;
import delta.games.lotro.lore.collections.mounts.MountsManager;

/**
 * Check for missing mounts.
 * @author DAM
 */
public class MainMountsCheck
{
  private void doIt()
  {
    SkillsManager skillsMgr=SkillsManager.getInstance();
    LotroEnum<SkillCategory> categoryEnum=LotroEnumsRegistry.getInstance().get(SkillCategory.class);
    SkillCategory category=categoryEnum.getEntry(88); // Mounts
    List<SkillDescription> skills=skillsMgr.getSkillsByCategory(category);
    MountsManager mountsMgr=MountsManager.getInstance();
    for(SkillDescription skill : skills)
    {
      int id=skill.getIdentifier();
      MountDescription mount=mountsMgr.getMount(id);
      if (mount==null)
      {
        System.out.println("Missing mount: "+skill);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainMountsCheck().doIt();
  }
}
