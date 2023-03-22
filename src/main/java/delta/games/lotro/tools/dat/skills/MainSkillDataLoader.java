package delta.games.lotro.tools.dat.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.TravelSkill;
import delta.games.lotro.character.skills.io.xml.SkillDescriptionXMLWriter;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.common.enums.TravelLink;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.collections.mounts.MountDescription;
import delta.games.lotro.lore.collections.pets.CosmeticPetDescription;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.skills.mounts.MountsLoader;
import delta.games.lotro.tools.dat.skills.pets.CosmeticPetLoader;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get skill definitions from DAT files.
 * @author DAM
 */
public class MainSkillDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainSkillDataLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  private List<SkillDescription> _skills;
  private MountsLoader _mountsLoader;
  private CosmeticPetLoader _petsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainSkillDataLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("skills",facade.getGlobalStringsManager());
    _skills=new ArrayList<SkillDescription>();
    _mountsLoader=new MountsLoader(facade,_i18n);
    _petsLoader=new CosmeticPetLoader(facade,_i18n);
  }

  /**
   * Load trait data.
   */
  public void doIt()
  {
    loadSkills();
    _mountsLoader.loadSizeData();
    saveSkills();
  }

  private void loadSkills()
  {
    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==827)
        {
          SkillDescription skill=loadSkill(_facade,did);
          if (skill!=null)
          {
            _skills.add(skill);
          }
        }
      }
    }
  }

  /**
   * Load a skill.
   * @param facade Data facade.
   * @param skillId Skill identifier.
   * @return the loaded skill description.
   */
  private SkillDescription loadSkill(DataFacade facade, int skillId)
  {
    SkillDescription ret=null;
    PropertiesSet skillProperties=facade.loadProperties(skillId+DATConstants.DBPROPERTIES_OFFSET);
    if (skillProperties!=null)
    {
      //System.out.println("*********** Skill: "+skillId+" ****************");
      ret=buildSkill(skillProperties);
      ret.setIdentifier(skillId);
      // Name
      String skillName=_i18n.getNameStringProperty(skillProperties,"Skill_Name",skillId,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
      ret.setName(skillName);
      // Description
      String description=_i18n.getStringProperty(skillProperties,"Skill_Desc");
      ret.setDescription(description);
      // Icon
      Integer iconId=(Integer)skillProperties.getProperty("Skill_SmallIcon");
      if (iconId!=null)
      {
        ret.setIconId(iconId.intValue());
      }
      // Category
      Integer categoryId=(Integer)skillProperties.getProperty("Skill_Category");
      if (categoryId!=null)
      {
        LotroEnum<SkillCategory> categoryEnum=LotroEnumsRegistry.getInstance().get(SkillCategory.class);
        SkillCategory category=categoryEnum.getEntry(categoryId.intValue());
        ret.setCategory(category);
      }
      // Build icon file
      if (iconId!=null)
      {
        String iconFilename=iconId+".png";
        File to=new File(GeneratedFiles.SKILL_ICONS_DIR,iconFilename).getAbsoluteFile();
        if (!to.exists())
        {
          boolean ok=DatIconsUtils.buildImageFile(facade,iconId.intValue(),to);
          if (!ok)
          {
            LOGGER.warn("Could not build skill icon: "+iconFilename);
          }
        }
      }
      /*
      // Skill type(s)
      {
        long typeFlags=((Long)skillProperties.getProperty("Skill_SkillType")).longValue();
        EnumMapper skillType=facade.getEnumsManager().getEnumMapper(587203492);
        BitSet skillTypesBitSet=BitSetUtils.getBitSetFromFlags(typeFlags);
        String types=BitSetUtils.getStringFromBitSet(skillTypesBitSet,skillType,"/");
        System.out.println("Skill: "+skillName+", types="+types);
      }
      // Skill quest flags
      {
        Long skillQuestFlags=(Long)skillProperties.getProperty("Skill_QuestFlags");
        if (skillQuestFlags!=null)
        {
          BitSet skillBitSet=BitSetUtils.getBitSetFromFlags(skillQuestFlags.longValue());
          System.out.println("Skill: "+skillName+", flags="+skillBitSet);
        }
      }
      */
      if (ret instanceof MountDescription)
      {
        MountDescription mount=(MountDescription)ret;
        if (_mountsLoader.useMount(mount))
        {
          _mountsLoader.loadMountData(skillProperties,mount);
        }
        else
        {
          ret=null;
        }
      }
      if (ret instanceof CosmeticPetDescription)
      {
        CosmeticPetDescription pet=(CosmeticPetDescription)ret;
        _petsLoader.loadPetData(skillProperties,pet);
      }
    }
    return ret;
  }

  private SkillDescription buildSkill(PropertiesSet properties)
  {
    TravelLink travelType=getTravelType(properties);
    if (travelType!=null)
    {
      return new TravelSkill(travelType);
    }
    Integer categoryCode=(Integer)properties.getProperty("Skill_Category");
    if ((categoryCode!=null) && (categoryCode.intValue()==88)) // Standard Mounts
    {
      return new MountDescription();
    }
    if ((categoryCode!=null) && (categoryCode.intValue()==145)) // Pets
    {
      return new CosmeticPetDescription();
    }
    return new SkillDescription();
  }

  private TravelLink getTravelType(PropertiesSet properties)
  {
    Integer toCheckFor=(Integer)properties.getProperty("Skill_TravelLinkToCheckFor");
    if ((toCheckFor!=null) && (toCheckFor.intValue()>0))
    {
      return getTravelType(toCheckFor.intValue());
    }
    Integer toUse=(Integer)properties.getProperty("Skill_TravelLinkToUse");
    if ((toUse!=null) && (toUse.intValue()>0))
    {
      return getTravelType(toUse.intValue());
    }
    return null;
  }

  private TravelLink getTravelType(int code)
  {
    LotroEnum<TravelLink> travelLinkEnum=LotroEnumsRegistry.getInstance().get(TravelLink.class);
    return travelLinkEnum.getEntry(code);
  }

  /**
   * Save skills to disk.
   */
  private void saveSkills()
  {
    int nbSkills=_skills.size();
    LOGGER.info("Writing "+nbSkills+" skills");
    // Write skills file
    Collections.sort(_skills,new IdentifiableComparator<SkillDescription>());
    boolean ok=new SkillDescriptionXMLWriter().write(GeneratedFiles.SKILLS,_skills);
    if (ok)
    {
      System.out.println("Wrote skills file: "+GeneratedFiles.SKILLS);
    }
    // Labels
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainSkillDataLoader(facade).doIt();
    facade.dispose();
  }
}