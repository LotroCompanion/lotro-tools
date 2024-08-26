package delta.games.lotro.tools.extraction.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.character.virtues.VirtueDescription;
import delta.games.lotro.character.virtues.io.xml.VirtueDescriptionXMLWriter;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.progressions.ProgressionUtils;
import delta.games.lotro.tools.extraction.utils.DatStatUtils;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get virtues definitions from DAT files.
 * @author DAM
 */
public class VirtueDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(VirtueDataLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  private DatStatUtils _statUtils;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public VirtueDataLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("virtues",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18n);
  }

  private List<VirtueDescription> loadVirtues()
  {
    List<VirtueDescription> virtues=new ArrayList<VirtueDescription>();
    PropertiesSet properties=WeenieContentDirectory.loadWeenieContentProps(_facade,"TraitControl");

    long[] xpTable=null;
    Integer xpTableId=(Integer)properties.getProperty("Trait_Control_VirtueTierToExperience_AdvancementTable");
    if (xpTableId!=null)
    {
      WStateDataSet xpTableData=_facade.loadWState(xpTableId.intValue());
      xpTable=(long[])xpTableData.getValue(1);
    }
    boolean live=Context.isLive();
    String propertyName=live?"Trait_Control_FreepTraits":"Trait_Control_Virtues_List";
    Object[] freepTraitsArray=(Object[])properties.getProperty(propertyName);
    for(Object freepTraitObj : freepTraitsArray)
    {
      int traitId=((Integer)freepTraitObj).intValue();
      // Ignore AUDACITY
      if (traitId==1879230292)
      {
        continue;
      }
      VirtueDescription virtue=loadVirtue(_facade,traitId);
      if (virtue==null)
      {
        continue;
      }
      virtues.add(virtue);
      // Set XP table
      if (xpTable!=null)
      {
        for(int i=0;i<xpTable.length;i++)
        {
          virtue.setXpForTier(i,(int)xpTable[i]);
        }
      }
    }
    return virtues;
  }

  /**
   * Load a virtue.
   * @param facade Data facade.
   * @param id Virtue identifier.
   * @return the loaded virtue description.
   */
  private VirtueDescription loadVirtue(DataFacade facade, int id)
  {
    PropertiesSet virtueProperties=facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (virtueProperties==null)
    {
      return null;
    }
    Integer nature=(Integer)virtueProperties.getProperty("Trait_Nature");
    if ((nature==null) || (nature.intValue()!=5))
    {
      return null;
    }
    VirtueDescription ret=new VirtueDescription();
    ret.setIdentifier(id);
    // Name
    String traitName=DatStringUtils.getStringProperty(virtueProperties,"Trait_Name");
    if (traitName==null)
    {
      traitName="?";
    }
    ret.setName(traitName);
    // Rank stat key
    PropertiesRegistry propsRegistry=facade.getPropertiesRegistry();
    int rankPropertyId=((Integer)virtueProperties.getProperty("Trait_Virtue_Rank_PropertyName")).intValue();
    String rankPropertyName=propsRegistry.getPropertyDef(rankPropertyId).getName();
    ret.setRankStatKey(rankPropertyName);
    // XP stat key
    Integer xpPropertyId=(Integer)virtueProperties.getProperty("Trait_Virtue_XP_PropertyName");
    if (xpPropertyId!=null)
    {
      String xpPropertyName=propsRegistry.getPropertyDef(xpPropertyId.intValue()).getName();
      ret.setXpPropertyName(xpPropertyName);
    }
    // Stats
    StatsProvider statsProvider=_statUtils.buildStatProviders(virtueProperties);
    ret.setStatsProvider(statsProvider);
    // Rank to level
    // ID to be loaded from TraitControl:Trait_Control_VirtueTierToItemLevelProgression
    Progression rankToLevel=ProgressionUtils.getProgression(facade,1879387583);
    if (rankToLevel==null)
    {
      LOGGER.warn("Could not find progression rank->level for virtues");
    }
    // Character level to max virtue rank:
    Integer charLevelToMaxRankProgId=(Integer)virtueProperties.getProperty("Trait_Virtue_Maximum_Rank_PlayerPropertyName_Progression");
    if (charLevelToMaxRankProgId!=null)
    {
      Progression charLevelToMaxRankProg=ProgressionUtils.getProgression(facade,charLevelToMaxRankProgId.intValue());
      if (charLevelToMaxRankProg==null)
      {
        LOGGER.warn("Could not find progression char level->max rank for virtue: "+traitName);
      }
      ret.setMaxRankForCharacterLevelProgression(charLevelToMaxRankProg);
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
    Integer maxRankProgId=(Integer)virtueProperties.getProperty("Trait_Virtue_Maximum_Rank_PlayerPropertyName_Progression");
    if (maxRankProgId!=null)
    {
      Progression maxRankProg=ProgressionUtils.getProgression(facade,maxRankProgId.intValue());
      if (maxRankProg!=null)
      {
        ret.setMaxRankForCharacterLevelProgression(maxRankProg);
      }
    }

    // Virtue key
    String virtueKey=ret.getName().toUpperCase();
    ret.setKey(virtueKey);

    // Icons (SoA)
    handleIcons(virtueProperties,virtueKey);

    return ret;
  }

  private void handleIcons(PropertiesSet properties, String virtueKey)
  {
    Integer progressionID=(Integer)properties.getProperty("Trait_Icon_Progression");
    if (progressionID==null)
    {
      return;
    }
    PropertiesSet iconProgression=_facade.loadProperties(progressionID.intValue()+DATConstants.DBPROPERTIES_OFFSET);
    /*
PropertyProgression_Array:
  #1: Trait_Icon 1090553359
  #2: Trait_Icon 1090553360
  #3: Trait_Icon 1090553361
  #4: Trait_Icon 1090553362
  #5: Trait_Icon 1090553363
  #6: Trait_Icon 1090553364
  #7: Trait_Icon 1090553365
  #8: Trait_Icon 1090553366
  #9: Trait_Icon 1090553367
  #10: Trait_Icon 1090553368
     */
    Object[] iconIDsArray=(Object[])iconProgression.getProperty("PropertyProgression_Array");
    if (iconIDsArray!=null)
    {
      int nbEntries=iconIDsArray.length;
      int tier=1;
      for(int i=0;i<nbEntries;i++)
      {
        int iconID=((Integer)iconIDsArray[i]).intValue();
        loadIcon(tier,virtueKey,iconID);
        tier++;
      }
    }
  }

  private void loadIcon(int tier, String virtueKey, int iconID)
  {
    String iconFilename=virtueKey+"-"+tier+".png";
    File to=new File(GeneratedFiles.TRAIT_ICONS_DIR,iconFilename).getAbsoluteFile();
    if (!to.exists())
    {
      boolean ok=DatIconsUtils.buildImageFile(_facade,iconID,to);
      if (!ok)
      {
        LOGGER.warn("Could not build virtue icon: "+iconFilename);
      }
    }
  }

  private StatsProvider handleEffect(DataFacade facade, int effectId)
  {
    PropertiesSet effectProperties=facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    StatsProvider provider=_statUtils.buildStatProviders(effectProperties);
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
      LOGGER.info("Wrote virtues file: "+GeneratedFiles.VIRTUES);
    }
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    List<VirtueDescription> virtues=loadVirtues();
    saveVirtues(virtues);
    _statUtils.showStatistics();
    _i18n.save();
  }
}
