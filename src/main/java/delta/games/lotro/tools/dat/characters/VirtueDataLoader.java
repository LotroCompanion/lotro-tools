package delta.games.lotro.tools.dat.characters;

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
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.WeenieContentDirectory;
import delta.games.lotro.utils.maths.Progression;

/**
 * Get virtues definitions from DAT files.
 * @author DAM
 */
public class VirtueDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(VirtueDataLoader.class);

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
    PropertiesSet properties=WeenieContentDirectory.loadWeenieContentProps(_facade,"TraitControl");
    //System.out.println(properties.dump());

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
      virtues.add(virtue);
      //System.out.println("Virtue: "+traitId+" - "+trait.getName());
      // Set XP table
      if (xpTable!=null)
      {
        for(int i=0;i<xpTable.length;i++)
        {
          virtue.setXpForTier(i,(int)xpTable[i]);
        }
      }
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
    PropertiesSet virtueProperties=facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (virtueProperties!=null)
    {
      ret=new VirtueDescription();
      ret.setIdentifier(id);
      // Name
      String traitName=DatUtils.getStringProperty(virtueProperties,"Trait_Name");
      if (traitName==null)
      {
        traitName="?";
      }
      ret.setName(traitName);
      // Description
      String description=DatUtils.getStringProperty(virtueProperties,"Trait_Description");
      ret.setDescription(description);
      // Icon
      Integer iconId=(Integer)virtueProperties.getProperty("Trait_Icon");
      if (iconId!=null)
      {
        ret.setIconId(iconId.intValue());
      }
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
      DatStatUtils._doFilterStats=false;
      DatStatUtils.STATS_USAGE_STATISTICS.reset();
      StatsProvider statsProvider=DatStatUtils.buildStatProviders(facade,virtueProperties);
      ret.setStatsProvider(statsProvider);
      // Rank to level
      // ID to be loaded from TraitControl:Trait_Control_VirtueTierToItemLevelProgression
      Progression rankToLevel=DatStatUtils.getProgression(facade,1879387583);
      if (rankToLevel==null)
      {
        LOGGER.warn("Could not find progression rank->level for virtues");
      }
      // Character level to max virtue rank:
      Integer charLevelToMaxRankProgId=(Integer)virtueProperties.getProperty("Trait_Virtue_Maximum_Rank_PlayerPropertyName_Progression");
      if (charLevelToMaxRankProgId!=null)
      {
        Progression charLevelToMaxRankProg=DatStatUtils.getProgression(facade,charLevelToMaxRankProgId.intValue());
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
        Progression maxRankProg=DatStatUtils.getProgression(facade,maxRankProgId.intValue());
        if (maxRankProg!=null)
        {
          ret.setMaxRankForCharacterLevelProgression(maxRankProg);
        }
      }

      // Virtue key
      String virtueKey=null;
      if (id==1879072876) virtueKey="DETERMINATION"; 
      else if (id==1879072876) virtueKey="LOYALTY"; 
      else if (id==1879072876) virtueKey="VALOUR";
      else
      {
        virtueKey=ret.getName().toUpperCase();
      }
      ret.setKey(virtueKey);
    }
    return ret;
  }

  private static StatsProvider handleEffect(DataFacade facade, int effectId)
  {
    PropertiesSet effectProperties=facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
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
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    loadVirtues();
  }
}
