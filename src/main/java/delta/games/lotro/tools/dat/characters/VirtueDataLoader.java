package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.files.archives.DirectoryArchiver;
import delta.games.lotro.character.virtues.VirtueDescription;
import delta.games.lotro.character.virtues.io.xml.VirtueDescriptionXMLWriter;
import delta.games.lotro.common.VirtueId;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get virtues definitions from DAT files.
 * @author DAM
 */
public class VirtueDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(VirtueDataLoader.class);

  /**
   * Directory for virtue icons.
   */
  private static File VIRTUE_ICONS_DIR=new File("data\\virtues\\tmp").getAbsoluteFile();

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public VirtueDataLoader(DataFacade facade)
  {
    _facade=facade;
  }

  private void loadVirtues()
  {
    List<VirtueDescription> virtues=new ArrayList<VirtueDescription>();
    PropertiesSet properties=_facade.loadProperties(0x7900025B);
    //System.out.println(properties.dump());
    Object[] freepTraitsArray=(Object[])properties.getProperty("Trait_Control_FreepTraits");
    for(Object freepTraitObj : freepTraitsArray)
    {
      int traitId=((Integer)freepTraitObj).intValue();
      // Ignore AUDACITY
      if (traitId==1879230292)
      {
        continue;
      }
      VirtueDescription virtue=loadVirtue(_facade,traitId);
      virtues.add(virtue);
      //System.out.println("Virtue: "+traitId+" - "+trait.getName());
    }
    saveVirtues(virtues);
  }

  /**
   * Load a virtue.
   * @param facade Data facade.
   * @param id Virtue identifier.
   * @return the loaded virtue description.
   */
  public static VirtueDescription loadVirtue(DataFacade facade, int id)
  {
    VirtueDescription ret=null;
    PropertiesSet virtueProperties=facade.loadProperties(0x9000000+id);
    if (virtueProperties!=null)
    {
      ret=new VirtueDescription();
      ret.setIdentifier(id);
      // Name
      String traitName=DatUtils.getStringProperty(virtueProperties,"Trait_Name");
      ret.setName(traitName);
      // Description
      String description=DatUtils.getStringProperty(virtueProperties,"Trait_Description");
      ret.setDescription(description);
      // Icon
      int iconId=((Integer)virtueProperties.getProperty("Trait_Icon")).intValue();
      ret.setIconId(iconId);

      // Stats
      DatStatUtils.doFilterStats=false;
      StatsProvider statsProvider=DatStatUtils.buildStatProviders(facade,virtueProperties);
      ret.setStatsProvider(statsProvider);
      // Build icon file
      String iconFilename=iconId+".png";
      File to=new File(VIRTUE_ICONS_DIR,"virtueIcons/"+iconFilename).getAbsoluteFile();
      if (!to.exists())
      {
        boolean ok=DatIconsUtils.buildImageFile(facade,iconId,to);
        if (!ok)
        {
          LOGGER.warn("Could not build virtue icon: "+iconFilename);
        }
      }
      // Rank to level
      Progression rankToLevel=DatStatUtils.getProgression(facade,1879387583);
      if (rankToLevel!=null)
      {
        LOGGER.warn("Could not find progression rank->level for virtues");
      }

      // Passives
      Object[] passives=(Object[])virtueProperties.getProperty("EffectGenerator_Virtue_PassiveEffectList");
      if (passives!=null)
      {
        for(Object passiveObj : passives)
        {
          PropertiesSet passiveProps=(PropertiesSet)passiveObj;
          int effectId=((Integer)passiveProps.getProperty("EffectGenerator_EffectID")).intValue();
          StatsProvider provider=handleEffect(facade,effectId);
          ret.setPassiveStatsProvider(provider);
        }
      }

      // Max rank progression
      int maxRankProgId=((Integer)virtueProperties.getProperty("Trait_Virtue_Maximum_Rank_PlayerPropertyName_Progression")).intValue();
      Progression maxRankProg=DatStatUtils.getProgression(facade,maxRankProgId);
      if (maxRankProg!=null)
      {
        ret.setMaxRankForCharacterLevelProgression(maxRankProg);
      }

      // Virtue key
      VirtueId virtueId=null;
      if (id==1879072876) virtueId=VirtueId.DETERMINATION; 
      else if (id==1879072876) virtueId=VirtueId.LOYALTY; 
      else if (id==1879072876) virtueId=VirtueId.VALOUR;
      else
      {
        String virtueIdStr=ret.getName().toUpperCase();
        virtueId=VirtueId.valueOf(virtueIdStr);
      }
      ret.setKey(virtueId.name());
    }
    return ret;
  }

  private static StatsProvider handleEffect(DataFacade facade, int effectId)
  {
    PropertiesSet effectProperties=facade.loadProperties(0x9000000+effectId);
    StatsProvider provider=DatStatUtils.buildStatProviders(facade,effectProperties);
    return provider;
  }

  /**
   * Save virtues to disk.
   * @param virtues Virtues.
   */
  public static void saveVirtues(List<VirtueDescription> virtues)
  {
    int nbVirtues=virtues.size();
    LOGGER.info("Writing "+nbVirtues+" virtues");
    // Write virtues file
    boolean ok=VirtueDescriptionXMLWriter.write(GeneratedFiles.VIRTUES,virtues);
    if (ok)
    {
      System.out.println("Wrote virtues file: "+GeneratedFiles.VIRTUES);
    }
    // Write virtues icons archive
    DirectoryArchiver archiver=new DirectoryArchiver();
    ok=archiver.go(GeneratedFiles.VIRTUE_ICONS,VIRTUE_ICONS_DIR);
    if (ok)
    {
      System.out.println("Wrote virtue icons archive: "+GeneratedFiles.VIRTUE_ICONS);
    }
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    loadVirtues();
  }
}
