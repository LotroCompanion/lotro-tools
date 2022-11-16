package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.character.traits.io.xml.TraitDescriptionXMLWriter;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.common.enums.TraitNature;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.StringUtils;

/**
 * Traits loader.
 * @author DAM
 */
public class TraitLoader
{
  private static final Logger LOGGER=Logger.getLogger(TraitLoader.class);

  /**
   * Get a trait.
   * @param facade Data facade.
   * @param traitId Trait identifier.
   * @return the trait description or <code>null</code> if not found/loaded.
   */
  public static TraitDescription getTrait(DataFacade facade, int traitId)
  {
    TraitsManager traitsMgr=TraitsManager.getInstance();
    TraitDescription trait=traitsMgr.getTrait(traitId);
    if (trait==null)
    {
      LOGGER.warn("Could not find trait ID="+traitId);
    }
    return trait;
  }

  /**
   * Load a trait.
   * @param facade Data facade.
   * @param traitId Trait identifier.
   * @return the loaded trait description or <code>null</code> if not found.
   */
  public static TraitDescription loadTrait(DataFacade facade, int traitId)
  {
    PropertiesSet traitProperties=facade.loadProperties(traitId+DATConstants.DBPROPERTIES_OFFSET);
    if (traitProperties==null)
    {
      return null;
    }
    //System.out.println("*********** Trait: "+traitId+" ****************");
    TraitDescription ret=new TraitDescription();
    ret.setIdentifier(traitId);
    // Name
    String traitName=DatUtils.getStringProperty(traitProperties,"Trait_Name");
    traitName=StringUtils.fixName(traitName);
    ret.setName(traitName);
    // Description
    String description=DatUtils.getStringProperty(traitProperties,"Trait_Description");
    ret.setDescription(description);
    // Icon
    Integer iconId=(Integer)traitProperties.getProperty("Trait_Icon");
    if (iconId!=null)
    {
      ret.setIconId(iconId.intValue());
    }
    // Min level
    Integer minLevelInt=(Integer)traitProperties.getProperty("Trait_Minimum_Level");
    int minLevel=(minLevelInt!=null)?minLevelInt.intValue():1;
    ret.setMinLevel(minLevel);
    // Tier
    //int traitTier=((Integer)traitProperties.getProperty("Trait_Tier")).intValue();
    Integer maxTier=(Integer)traitProperties.getProperty("Trait_Virtue_Maximum_Rank");
    if ((maxTier!=null) && (maxTier.intValue()>1))
    {
      ret.setTiersCount(maxTier.intValue());
    }
    //System.out.println("Trait name: "+traitName+" (min level="+minLevel+")");

    LotroEnumsRegistry registry=LotroEnumsRegistry.getInstance();
    // Category
    Integer categoryCode=(Integer)traitProperties.getProperty("Trait_Category");
    if ((categoryCode!=null) && (categoryCode.intValue()>0))
    {
      LotroEnum<SkillCategory> categoryMgr=registry.get(SkillCategory.class);
      SkillCategory category=categoryMgr.getEntry(categoryCode.intValue());
      ret.setCategory(category);
    }
    // Nature
    Integer natureCode=(Integer)traitProperties.getProperty("Trait_Nature");
    if ((natureCode!=null) && (natureCode.intValue()>0))
    {
      LotroEnum<TraitNature> natureMgr=registry.get(TraitNature.class);
      TraitNature nature=natureMgr.getEntry(natureCode.intValue());
      ret.setNature(nature);
    }
    // Tooltip
    String tooltip=DatUtils.getStringProperty(traitProperties,"Trait_Tooltip");
    ret.setTooltip(tooltip);
    // Cosmetic
    Integer cosmeticCode=(Integer)traitProperties.getProperty("Trait_Cosmetic");
    boolean cosmetic=((cosmeticCode!=null) && (cosmeticCode.intValue()!=0));
    ret.setCosmetic(cosmetic);
    // Stats
    DatStatUtils._doFilterStats=false;
    DatStatUtils.STATS_USAGE_STATISTICS.reset();
    StatsProvider statsProvider=DatStatUtils.buildStatProviders(facade,traitProperties);
    ret.setStatsProvider(statsProvider);
    // Build icon file
    if (iconId!=null)
    {
      String iconFilename=iconId+".png";
      File to=new File(GeneratedFiles.TRAIT_ICONS_DIR,iconFilename).getAbsoluteFile();
      if (!to.exists())
      {
        boolean ok=DatIconsUtils.buildImageFile(facade,iconId.intValue(),to);
        if (!ok)
        {
          LOGGER.warn("Could not build trait icon: "+iconFilename);
        }
      }
    }
    // Skills
    Object[] skillArray=(Object[])traitProperties.getProperty("Trait_Skill_Array");
    if (skillArray!=null) 
    {
      SkillsManager skillsMgr=SkillsManager.getInstance();
      for(Object skillIdObj : skillArray)
      {
        int skillId=((Integer)skillIdObj).intValue();
        SkillDescription skill=skillsMgr.getSkill(skillId);
        if (skill!=null)
        {
          ret.addSkill(skill);
        }
      }
    }
    return ret;
  }

  /**
   * Save traits to disk.
   */
  public static void saveTraits()
  {
    TraitsManager traitsManager=TraitsManager.getInstance();
    new TraitKeyGenerator(traitsManager).setup();
    List<TraitDescription> traits=traitsManager.getAll();
    int nbTraits=traits.size();
    LOGGER.info("Writing "+nbTraits+" traits");
    // Write traits file
    boolean ok=TraitDescriptionXMLWriter.write(GeneratedFiles.TRAITS,traits);
    if (ok)
    {
      System.out.println("Wrote traits file: "+GeneratedFiles.TRAITS);
    }
  }
}
