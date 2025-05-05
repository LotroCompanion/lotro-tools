package delta.games.lotro.tools.extraction.skills;

import java.io.File;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillDetails;
import delta.games.lotro.character.skills.SkillEffectGenerator;
import delta.games.lotro.character.skills.SkillEffectsManager;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.skills.TravelSkill;
import delta.games.lotro.character.skills.combos.SkillComboElement;
import delta.games.lotro.character.skills.combos.SkillCombos;
import delta.games.lotro.character.skills.combos.SkillCombosUtils;
import delta.games.lotro.character.skills.io.xml.SkillDescriptionXMLWriter;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.RecallEffect;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.common.enums.TravelLink;
import delta.games.lotro.common.geo.Position;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.collections.mounts.MountDescription;
import delta.games.lotro.lore.collections.pets.CosmeticPetDescription;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.effects.EffectLoader;
import delta.games.lotro.tools.extraction.skills.mounts.MountsLoader;
import delta.games.lotro.tools.extraction.skills.pets.CosmeticPetLoader;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Get skill definitions from DAT files.
 * @author DAM
 */
public class MainSkillDataLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainSkillDataLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  private SkillDetailsLoader _detailsLoader;
  private EffectLoader _effectsLoader;
  private MountsLoader _mountsLoader;
  private CosmeticPetLoader _petsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader.
   */
  public MainSkillDataLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _i18n=new I18nUtils("skills",facade.getGlobalStringsManager());
    _effectsLoader=effectsLoader;
    _detailsLoader=new SkillDetailsLoader(facade,effectsLoader);
    _mountsLoader=new MountsLoader(facade,_i18n);
    _petsLoader=new CosmeticPetLoader(facade,_i18n);
  }

  /**
   * Load skill data.
   */
  public void doIt()
  {
    loadSkills();
    boolean live=LotroCoreConfig.isLive();
    if (live)
    {
      _mountsLoader.loadSizeData();
    }
    saveSkills();
  }

  private void loadSkills()
  {
    SkillsManager skillsMgr=SkillsManager.getInstance();
    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==827)
        {
          PropertiesSet skillProperties=_facade.loadProperties(i+DATConstants.DBPROPERTIES_OFFSET);
          SkillDescription skill=loadSkill(did,skillProperties);
          if (skill!=null)
          {
            SkillDetails details=_detailsLoader.loadSkillDetails(skill,skillProperties);
            skill.setDetails(details);
            updateTravelSpecifics(skill);
            skillsMgr.registerSkill(skill);
          }
        }
      }
    }
    SkillCombosUtils.resolveCombos(skillsMgr.getAll());
  }

  /**
   * Load a skill.
   * @param skillId Skill identifier.
   * @param skillProperties Properties to use.
   * @return the loaded skill description.
   */
  private SkillDescription loadSkill(int skillId, PropertiesSet skillProperties)
  {
    SkillDescription ret=buildSkill(skillProperties);
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
        boolean ok=DatIconsUtils.buildImageFile(_facade,iconId.intValue(),to);
        if (!ok)
        {
          LOGGER.warn("Could not build skill icon: {}",iconFilename);
        }
      }
    }
    // Skill type(s)
    {
      Long typeFlags=(Long)skillProperties.getProperty("Skill_SkillType");
      if (typeFlags!=null)
      {
        EnumMapper skillType=_facade.getEnumsManager().getEnumMapper(587203492);
        BitSet skillTypesBitSet=BitSetUtils.getBitSetFromFlags(typeFlags.longValue());
        if (LOGGER.isDebugEnabled())
        {
          String types=BitSetUtils.getStringFromBitSet(skillTypesBitSet,skillType,"/");
          LOGGER.debug("Skill: {}, types={}",skillName,types);
        }
      }
    }
    // Skill quest flags
    {
      Long skillQuestFlags=(Long)skillProperties.getProperty("Skill_QuestFlags");
      if (skillQuestFlags!=null)
      {
        BitSet skillBitSet=BitSetUtils.getBitSetFromFlags(skillQuestFlags.longValue());
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Skill: {}, flags={}",skillName,skillBitSet);
        }
      }
    }
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
    // Combos
    if (ret!=null)
    {
      SkillCombos combos=loadSkillCombos(skillProperties);
      ret.setCombos(combos);
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
    if (categoryCode!=null)
    {
      if (categoryCode.intValue()==88) // Standard Mounts
      {
        return new MountDescription();
      }
      else if (categoryCode.intValue()==145) // Pets
      {
        return new CosmeticPetDescription();
      }
      else if (categoryCode.intValue()==102) // Travel skills
      {
        return new TravelSkill(null);
      }
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
   * Load skill requirements.
   */
  public void loadRequirements()
  {
    for(SkillDescription skill : SkillsManager.getInstance().getAll())
    {
      int skillId=skill.getIdentifier();
      PropertiesSet skillProperties=_facade.loadProperties(skillId+DATConstants.DBPROPERTIES_OFFSET);
      loadRequirements(skill,skillProperties);
    }
    saveSkills();
  }

  private void loadRequirements(SkillDescription skill, PropertiesSet props)
  {
    // Required trait
    Integer requiredTraitId=(Integer)props.getProperty("Skill_RequiredTrait");
    if (requiredTraitId!=null)
    {
      TraitDescription trait=TraitsManager.getInstance().getTrait(requiredTraitId.intValue());
      if (trait!=null)
      {
        Proxy<TraitDescription> requiredTrait=new Proxy<TraitDescription>();
        requiredTrait.setId(requiredTraitId.intValue());
        requiredTrait.setName(trait.getName());
        requiredTrait.setObject(trait);
        skill.setRequiredTrait(requiredTrait);
      }
      else
      {
        LOGGER.warn("Unknown trait: {}",requiredTraitId);
      }
    }
    // Required effects
    Object[] requiredEffectsArray=(Object[])props.getProperty("Skill_RequiredEffectList");
    if (requiredEffectsArray!=null)
    {
      for(Object requiredEffectEntry : requiredEffectsArray)
      {
        int requiredEffectId=((Integer)requiredEffectEntry).intValue();
        Effect effect=_effectsLoader.getEffect(requiredEffectId);
        if (effect!=null)
        {
          skill.addRequiredEffect(effect);
        }
      }
    }
  }

  private void updateTravelSpecifics(SkillDescription skill)
  {
    if (skill instanceof TravelSkill)
    {
      TravelSkill travelSkill=(TravelSkill)skill;
      loadTravelSkillSpecifics(travelSkill);
    }
  }

  private void loadTravelSkillSpecifics(TravelSkill travelSkill)
  {
    SkillEffectsManager effectsMgr=travelSkill.getDetails().getEffects();
    if (effectsMgr!=null)
    {
      for(SkillEffectGenerator generator : effectsMgr.getEffects())
      {
        Effect effect=generator.getEffect();
        if (effect instanceof RecallEffect)
        {
          RecallEffect recallEffect=(RecallEffect)effect;
          Position position=recallEffect.getPosition();
          travelSkill.setPosition(position);
        }
      }
    }
  }

  private SkillCombos loadSkillCombos(PropertiesSet props)
  {
    /*
    Skill_Combo_ComboList: 
      #1: Skill_Combo_ComboListEntry 
        Combat_Hunter_SkillCombo: 16 (FireArrow)
        Skill_Combo_ComboSkill: 1879218257
    Skill_Combo_DisplayComboOverlay: 0
    Skill_Combo_StateProperty: 268455146 (Combat_Hunter_SkillCombo)
    */
    Integer propertyID=(Integer)props.getProperty("Skill_Combo_StateProperty");
    if ((propertyID==null) || (propertyID.intValue()==0))
    {
      return null;
    }
    Object[] elements=(Object[])props.getProperty("Skill_Combo_ComboList");
    if (elements==null)
    {
      return null;
    }
    PropertyDefinition def=_facade.getPropertiesRegistry().getPropertyDef(propertyID.intValue());
    SkillCombos ret=new SkillCombos(def.getPropertyId());
    for(Object entry : elements)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      Integer value=(Integer)entryProps.getProperty(def.getName());
      if (value==null)
      {
        value=(Integer)entryProps.getProperty("Skill_Combo_ComboMode");
      }
      int skillID=((Integer)entryProps.getProperty("Skill_Combo_ComboSkill")).intValue();
      Proxy<SkillDescription> proxy=new Proxy<SkillDescription>();
      proxy.setId(skillID);
      SkillComboElement element=new SkillComboElement(value.intValue(),proxy);
      ret.addElement(element);
    }
    return ret;
  }

  /**
   * Save skills to disk.
   */
  private void saveSkills()
  {
    List<SkillDescription> skills=SkillsManager.getInstance().getAll();
    int nbSkills=skills.size();
    LOGGER.info("Writing {} skills",Integer.valueOf(nbSkills));
    // Write skills file
    Collections.sort(skills,new IdentifiableComparator<SkillDescription>());
    boolean ok=new SkillDescriptionXMLWriter().write(GeneratedFiles.SKILLS,skills);
    if (ok)
    {
      LOGGER.info("Wrote skills file: {}",GeneratedFiles.SKILLS);
    }
    // Labels
    _i18n.save();
    // Details
    _detailsLoader.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    MainSkillDataLoader loader=new MainSkillDataLoader(facade,effectsLoader);
    loader.doIt();
    loader.loadRequirements();
    effectsLoader.save();
    facade.dispose();
  }
}
