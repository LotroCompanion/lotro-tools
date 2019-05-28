package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.files.archives.DirectoryArchiver;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.skills.io.xml.SkillDescriptionXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Skills loader.
 * @author DAM
 */
public class SkillLoader
{
  private static final Logger LOGGER=Logger.getLogger(SkillLoader.class);

  /**
   * Directory for skill icons.
   */
  private static File SKILL_ICONS_DIR=new File("data\\skills\\tmp").getAbsoluteFile();

  /**
   * Get a skill.
   * @param facade Data facade.
   * @param skillId Skill identifier.
   * @return a skill description or <code>null</code> if not found.
   */
  public static SkillDescription getSkill(DataFacade facade, int skillId)
  {
    SkillsManager skillsMgr=SkillsManager.getInstance();
    SkillDescription skill=skillsMgr.getSkill(skillId);
    if (skill==null)
    {
      skill=loadSkill(facade,skillId);
      if (skill!=null)
      {
        skillsMgr.registerSkill(skill);
      }
    }
    if (skill==null)
    {
      LOGGER.warn("Skill not found: "+skillId);
    }
    return skill;
  }

  /**
   * Load a skill.
   * @param facade Data facade.
   * @param skillId Skill identifier.
   * @return the loaded skill description.
   */
  public static SkillDescription loadSkill(DataFacade facade, int skillId)
  {
    SkillDescription ret=null;
    PropertiesSet skillProperties=facade.loadProperties(0x9000000+skillId);
    if (skillProperties!=null)
    {
      //System.out.println("*********** Skill: "+skillId+" ****************");
      ret=new SkillDescription();
      ret.setIdentifier(skillId);
      // Name
      String skillName=DatUtils.getStringProperty(skillProperties,"Skill_Name");
      ret.setName(skillName);
      // Description
      String description=DatUtils.getStringProperty(skillProperties,"Skill_Desc");
      ret.setDescription(description);
      // Icon
      int iconId=((Integer)skillProperties.getProperty("Skill_Icon")).intValue();
      ret.setIconId(iconId);
      // Category
      int categoryId=((Integer)skillProperties.getProperty("Skill_Category")).intValue();
      EnumMapper categoryMapper=facade.getEnumsManager().getEnumMapper(587202586);
      String category=categoryMapper.getString(categoryId);
      ret.setCategory(category);
      // Build icon file
      String iconFilename=iconId+".png";
      File to=new File(SKILL_ICONS_DIR,"skillIcons/"+iconFilename).getAbsoluteFile();
      if (!to.exists())
      {
        boolean ok=DatIconsUtils.buildImageFile(facade,iconId,to);
        if (!ok)
        {
          LOGGER.warn("Could not build skill icon: "+iconFilename);
        }
      }
    }
    return ret;
  }

  /**
   * Save skills to disk.
   * @param skillsManager Skills manager.
   */
  public static void saveSkills(SkillsManager skillsManager)
  {
    List<SkillDescription> skills=skillsManager.getAll();
    int nbSkills=skills.size();
    LOGGER.info("Writing "+nbSkills+" skills");
    // Write skills file
    boolean ok=SkillDescriptionXMLWriter.write(GeneratedFiles.SKILLS,skills);
    if (ok)
    {
      System.out.println("Wrote skills file: "+GeneratedFiles.SKILLS);
    }
    // Write skill icons archive
    DirectoryArchiver archiver=new DirectoryArchiver();
    ok=archiver.go(GeneratedFiles.SKILL_ICONS,SKILL_ICONS_DIR);
    if (ok)
    {
      System.out.println("Wrote skill icons archive: "+GeneratedFiles.SKILL_ICONS);
    }
  }
}
