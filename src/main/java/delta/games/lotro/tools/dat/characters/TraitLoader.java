package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.files.archives.DirectoryArchiver;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.character.traits.io.xml.TraitDescriptionXMLWriter;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Traits loader.
 * @author DAM
 */
public class TraitLoader
{
  private static final Logger LOGGER=Logger.getLogger(TraitLoader.class);

  /**
   * Directory for trait icons.
   */
  private static File TRAIT_ICONS_DIR=new File("data\\traits\\tmp").getAbsoluteFile();

  /**
   * Load a trait.
   * @param facade Data facade.
   * @param traitId Trait identifier.
   * @return the loaded trait description.
   */
  public static TraitDescription loadTrait(DataFacade facade, int traitId)
  {
    TraitDescription ret=null;
    PropertiesSet traitProperties=facade.loadProperties(0x9000000+traitId);
    if (traitProperties!=null)
    {
      //System.out.println("*********** Trait: "+traitId+" ****************");
      ret=new TraitDescription();
      ret.setIdentifier(traitId);
      // Name
      String traitName=DatUtils.getStringProperty(traitProperties,"Trait_Name");
      ret.setName(traitName);
      // Description
      String description=DatUtils.getStringProperty(traitProperties,"Trait_Description");
      ret.setDescription(description);
      // Icon
      int iconId=((Integer)traitProperties.getProperty("Trait_Icon")).intValue();
      ret.setIconId(iconId);
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

      // Category
      // See enum: SkillCharacteristicCategory (id=587202586). 93 = Discounts:

      // Stats
      DatStatUtils.doFilterStats=false;
      StatsProvider statsProvider=DatStatUtils.buildStatProviders(facade,traitProperties);
      ret.setStatsProvider(statsProvider);
      // Build icon file
      String iconFilename=iconId+".png";
      File to=new File(TRAIT_ICONS_DIR,"traitIcons/"+iconFilename).getAbsoluteFile();
      if (!to.exists())
      {
        boolean ok=DatIconsUtils.buildImageFile(facade,iconId,to);
        if (!ok)
        {
          LOGGER.warn("Could not build trait icon: "+iconFilename);
        }
      }
      // Skills
      Object[] skillArray=(Object[])traitProperties.getProperty("Trait_Skill_Array");
      if (skillArray!=null) 
      {
        for(Object skillIdObj : skillArray)
        {
          int skillId=((Integer)skillIdObj).intValue();
          SkillDescription skill=SkillLoader.getSkill(facade,skillId);
          if (skill!=null)
          {
            ret.addSkill(skill);
          }
        }
      }
      
    }
    return ret;
  }

  /**
   * Save traits to disk.
   * @param traitsManager Traits manager.
   */
  public static void saveTraits(TraitsManager traitsManager)
  {
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
    // Write trait icons archive
    DirectoryArchiver archiver=new DirectoryArchiver();
    ok=archiver.go(GeneratedFiles.TRAIT_ICONS,TRAIT_ICONS_DIR);
    if (ok)
    {
      System.out.println("Wrote trait icons archive: "+GeneratedFiles.TRAIT_ICONS);
    }
  }
}
