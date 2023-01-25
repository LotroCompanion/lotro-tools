package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.AbstractClassDescription;
import delta.games.lotro.character.classes.ClassSkill;
import delta.games.lotro.character.classes.ClassTrait;
import delta.games.lotro.character.classes.MonsterClassDescription;
import delta.games.lotro.character.classes.WellKnownMonsterClassKeys;
import delta.games.lotro.character.classes.io.xml.ClassDescriptionXMLWriter;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.WeenieContentDirectory;

/**
 * Get monster class definitions from DAT files.
 * @author DAM
 */
public class MonsterClassDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(MonsterClassDataLoader.class);

  private DataFacade _facade;
  private List<MonsterClassDescription> _classes;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MonsterClassDataLoader(DataFacade facade)
  {
    _facade=facade;
    _classes=new ArrayList<MonsterClassDescription>();
  }

  private void handleClass(int classId)
  {
    PropertiesSet properties=_facade.loadProperties(classId+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(properties.dump());
    PropertiesSet classInfo=(PropertiesSet)properties.getProperty("MonsterPlay_MonsterInfo");
    // Code
    int classCode=((Integer)properties.getProperty("MonsterPlay_Class")).intValue();
    // Key
    String classKey=getClassKeyFromId(classCode);
    MonsterClassDescription classDescription=new MonsterClassDescription(classId,classCode,classKey);
    LOGGER.info("Handling class: "+classKey);
    // Name
    String className=DatUtils.getStringProperty(classInfo,"MonsterPlay_MonsterName");
    classDescription.setName(className);
    // Abbreviation
    String classAbbreviation=DatUtils.getStringProperty(classInfo,"MonsterPlay_AbbreviatedMonsterName");
    LOGGER.debug("Class abbreviation: "+classAbbreviation);
    classDescription.setAbbreviation(classAbbreviation);
    // Class description
    String description=DatUtils.getStringProperty(classInfo,"MonsterPlay_MonsterDesc");
    LOGGER.debug("Class description: "+description);
    classDescription.setDescription(description);
    // Icons
    // Normal size (48 pixels)
    int classIconId=((Integer)classInfo.getProperty("MonsterPlay_ClassIcon")).intValue();
    File classIconFile=getIconFile(classIconId);
    DatIconsUtils.buildImageFile(_facade,classIconId,classIconFile);
    classDescription.setIconId(classIconId);

    loadTraits(classDescription,properties);
    loadSkills(classDescription,properties);
  }

  private File getIconFile(int iconID)
  {
    File iconFile=new File(GeneratedFiles.CLASS_ICONS_DIR,iconID+".png").getAbsoluteFile();
    return iconFile;
  }

  private void loadTraits(AbstractClassDescription description, PropertiesSet properties)
  {
/*
MonsterPlay_TraitList: 
  #1: MonsterPlay_TraitEntry 
    MonsterPlay_Trait: 1879072425
    MonsterPlay_TraitMinLevel: 95
    MonsterPlay_TraitMinRank: 0
    MonsterPlay_TraitTrainable: 1
 */
    Set<String> knownTraits=new HashSet<String>();
    Object[] traitsProperties=(Object[])properties.getProperty("MonsterPlay_TraitList");
    for(Object traitPropertiesObj : traitsProperties)
    {
      PropertiesSet traitProperties=(PropertiesSet)traitPropertiesObj;
      int level=((Integer)traitProperties.getProperty("MonsterPlay_TraitMinLevel")).intValue();
      Integer rank=(Integer)traitProperties.getProperty("MonsterPlay_TraitMinRank");
      int traitId=((Integer)traitProperties.getProperty("MonsterPlay_Trait")).intValue();
      String key=level+"#"+traitId;
      if (!knownTraits.contains(key))
      {
        LOGGER.debug("Level: "+level+" (rank="+rank+")");
        TraitDescription trait=TraitLoader.getTrait(_facade,traitId);
        ClassTrait classTrait=new ClassTrait(level,trait);
        description.addTrait(classTrait);
        knownTraits.add(key);
      }
    }
  }

  private void loadSkills(AbstractClassDescription description, PropertiesSet properties)
  {
/*
MonsterPlay_SkillList: 
  #1: MonsterPlay_SkillEntry 
    MonsterPlay_Skill: 1879073674
    MonsterPlay_SkillCost: 0
    MonsterPlay_SkillMinLevel: 95
    MonsterPlay_SkillMinRank: 0
 */
    SkillsManager skillsMgr=SkillsManager.getInstance();
    Object[] skillsProperties=(Object[])properties.getProperty("MonsterPlay_SkillList");
    for(Object skillPropertiesObj : skillsProperties)
    {
      PropertiesSet skillProperties=(PropertiesSet)skillPropertiesObj;
      int level=((Integer)skillProperties.getProperty("MonsterPlay_SkillMinLevel")).intValue();
      int skillId=((Integer)skillProperties.getProperty("MonsterPlay_Skill")).intValue();
      SkillDescription skill=skillsMgr.getSkill(skillId);
      if (skill!=null)
      {
        ClassSkill classSkill=new ClassSkill(level,skill);
        description.addSkill(classSkill);
      }
    }
  }

  /**
   * Get a character class key from a DAT enum code.
   * @param id Input code.
   * @return A character class key or <code>null</code> if not supported.
   */
  public static String getClassKeyFromId(int id)
  {
    if (id==71) return WellKnownMonsterClassKeys.REAVER;
    if (id==128) return WellKnownMonsterClassKeys.DEFILER;
    if (id==127) return WellKnownMonsterClassKeys.WEAVER;
    if (id==179) return WellKnownMonsterClassKeys.BLACKARROW;
    if (id==52) return WellKnownMonsterClassKeys.WARLEADER;
    if (id==126) return WellKnownMonsterClassKeys.STALKER;
    return "Unknown";
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    PropertiesSet properties=WeenieContentDirectory.loadWeenieContentProps(_facade,"MPLevelTableDirectory");
    Object[] classIds=(Object[])properties.getProperty("AdvTable_EvilMLTList");
    for(Object classId : classIds)
    {
      handleClass(((Integer)classId).intValue());
    }
    // Save classes descriptions
    ClassDescriptionXMLWriter.write(GeneratedFiles.MONSTER_CLASSES,_classes);
  }
}
