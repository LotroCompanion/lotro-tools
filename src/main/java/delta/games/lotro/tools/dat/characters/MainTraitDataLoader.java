package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get trait definitions from DAT files.
 * @author DAM
 */
public class MainTraitDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainTraitDataLoader.class);

  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private I18nUtils _i18n;
  private Map<Integer,Integer> _traitIds2PropMap;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainTraitDataLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("traits",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18n);
  }

  /**
   * Load trait data.
   */
  public void doIt()
  {
    loadPropertiesMap();
    loadTraits();
    SkimirshTraitsLoader skirmishTraitsLoader=new SkimirshTraitsLoader(_facade);
    skirmishTraitsLoader.doIt();
  }

  private void loadTraits()
  {
    TraitsManager traitsMgr=TraitsManager.getInstance();

    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        //System.out.println(classDefIndex);
        if ((classDefIndex==1477) || (classDefIndex==1478) || (classDefIndex==1483) ||
            (classDefIndex==1494) || (classDefIndex==2525) || (classDefIndex==3438) || (classDefIndex==3509))
        {
          // Traits
          TraitDescription trait=loadTrait(did);
          if (trait!=null)
          {
            Integer propertyId=_traitIds2PropMap.get(Integer.valueOf(trait.getIdentifier()));
            if (propertyId!=null)
            {
              PropertyDefinition propertyDef=_facade.getPropertiesRegistry().getPropertyDef(propertyId.intValue());
              trait.setTierPropertyName(propertyDef.getName());
            }
            traitsMgr.registerTrait(trait);
          }
        }
      }
    }
    saveTraits();
  }

  @SuppressWarnings("unchecked")
  private void loadPropertiesMap()
  {
    WStateDataSet wstate=_facade.loadWState(0x7000025B);
    List<Integer> refs=wstate.getOrphanReferences();
    if (refs.size()!=1)
    {
      LOGGER.warn("Unexpected number of references!");
      return;
    }
    ClassInstance traitControl=(ClassInstance)wstate.getValueForReference(refs.get(0).intValue());
    _traitIds2PropMap=new HashMap<Integer,Integer>();
    Map<Integer,Integer> props2traitIdsMap=(Map<Integer,Integer>)traitControl.getAttributeValue("m_aahVirtues");
    if (props2traitIdsMap==null)
    {
      props2traitIdsMap=(Map<Integer,Integer>)traitControl.getAttributeValue(0);
    }
    for(Map.Entry<Integer,Integer> entry : props2traitIdsMap.entrySet())
    {
      Integer oldValue=_traitIds2PropMap.put(entry.getValue(),entry.getKey());
      if (oldValue!=null)
      {
        LOGGER.warn("Multiple properties for trait: "+oldValue);
      }
    }
  }


  /**
   * Load a trait.
   * @param traitId Trait identifier.
   * @return the loaded trait description or <code>null</code> if not found.
   */
  private TraitDescription loadTrait(int traitId)
  {
    PropertiesSet traitProperties=_facade.loadProperties(traitId+DATConstants.DBPROPERTIES_OFFSET);
    if (traitProperties==null)
    {
      return null;
    }
    //System.out.println("*********** Trait: "+traitId+" ****************");
    TraitDescription ret=new TraitDescription();
    ret.setIdentifier(traitId);
    // Name
    String traitName=_i18n.getNameStringProperty(traitProperties,"Trait_Name",traitId,I18nUtils.OPTION_REMOVE_MARKS);
    ret.setName(traitName);
    // Description
    String description=_i18n.getStringProperty(traitProperties,"Trait_Description");
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
    String tooltip=_i18n.getStringProperty(traitProperties,"Trait_Tooltip");
    ret.setTooltip(tooltip);
    // Cosmetic
    Integer cosmeticCode=(Integer)traitProperties.getProperty("Trait_Cosmetic");
    boolean cosmetic=((cosmeticCode!=null) && (cosmeticCode.intValue()!=0));
    ret.setCosmetic(cosmetic);
    // Stats
    StatsProvider statsProvider=_statUtils.buildStatProviders(traitProperties);
    ret.setStatsProvider(statsProvider);
    // Build icon file
    if (iconId!=null)
    {
      String iconFilename=iconId+".png";
      File to=new File(GeneratedFiles.TRAIT_ICONS_DIR,iconFilename).getAbsoluteFile();
      if (!to.exists())
      {
        boolean ok=DatIconsUtils.buildImageFile(_facade,iconId.intValue(),to);
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
  private void saveTraits()
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
      LOGGER.info("Wrote traits file: "+GeneratedFiles.TRAITS);
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
    new MainTraitDataLoader(facade).doIt();
    facade.dispose();
  }
}
