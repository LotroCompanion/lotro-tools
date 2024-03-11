package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.character.traits.io.xml.TraitDescriptionXMLWriter;
import delta.games.lotro.character.traits.prerequisites.AbstractTraitPrerequisite;
import delta.games.lotro.character.traits.prerequisites.CompoundTraitPrerequisite;
import delta.games.lotro.character.traits.prerequisites.SimpleTraitPrerequisite;
import delta.games.lotro.character.traits.prerequisites.TraitLogicOperator;
import delta.games.lotro.character.traits.prerequisites.TraitPrerequisitesUtils;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.common.enums.TraitGroup;
import delta.games.lotro.common.enums.TraitNature;
import delta.games.lotro.common.enums.TraitSubCategory;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.effects.EffectLoader;
import delta.games.lotro.tools.dat.maps.PlacesLoader;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.ProgressionUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.utils.Proxy;
import delta.games.lotro.utils.maths.ArrayProgression;

/**
 * Get trait definitions from DAT files.
 * @author DAM
 */
public class MainTraitDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainTraitDataLoader.class);

  private DataFacade _facade;
  private EffectLoader _effectsLoader;
  private DatStatUtils _statUtils;
  private I18nUtils _i18n;
  private Map<Integer,Integer> _traitIds2PropMap;
  private List<Proxy<TraitDescription>> _proxies;
  private LotroEnum<TraitGroup> _traitGroupEnum;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader.
   */
  public MainTraitDataLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectsLoader=effectsLoader;
    _i18n=new I18nUtils("traits",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18n);
    _proxies=new ArrayList<Proxy<TraitDescription>>();
    _traitGroupEnum=LotroEnumsRegistry.getInstance().get(TraitGroup.class);
  }

  /**
   * Load trait data.
   */
  public void doIt()
  {
    loadPropertiesMap();
    loadTraits();
    SkirmishTraitsLoader skirmishTraitsLoader=new SkirmishTraitsLoader(_facade);
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
    TraitPrerequisitesUtils.resolveProxies(traitsMgr.getAll(),_proxies);
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
    // Static Icon Overlay
    Integer staticIconOverlayId=(Integer)traitProperties.getProperty("Trait_Static_Icon_Overlay");
    if (staticIconOverlayId!=null)
    {
      ret.setStaticIconOverlayId(staticIconOverlayId);
      loadIconFile(staticIconOverlayId.intValue());
    }
    // Rank Overlay Progression
    Integer rankOverlayProgression=(Integer)traitProperties.getProperty("Trait_Rank_Overlay_Progression");
    if ((rankOverlayProgression!=null) && (rankOverlayProgression.intValue()!=0))
    {
      ArrayProgression progression=(ArrayProgression)ProgressionUtils.getProgression(_facade,rankOverlayProgression.intValue());
      ret.setRankOverlayProgression(progression);
      loadRankIcons(progression);
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
    // Sub-category
    Integer subCategoryCode=(Integer)traitProperties.getProperty("Trait_Sub_Category");
    if ((subCategoryCode!=null) && (subCategoryCode.intValue()>0))
    {
      LotroEnum<TraitSubCategory> subCategoryMgr=registry.get(TraitSubCategory.class);
      TraitSubCategory subCategory=subCategoryMgr.getEntry(subCategoryCode.intValue());
      ret.setSubCategory(subCategory);
    }
    // Trait groups
    Object[] traitGroups=(Object[])traitProperties.getProperty("Trait_Groups");
    if (traitGroups!=null)
    {
      for(Object traitGroupObj : traitGroups)
      {
        int groupCode=((Integer)traitGroupObj).intValue();
        TraitGroup group=_traitGroupEnum.getEntry(groupCode);
        if (group!=null)
        {
          ret.addTraitGroup(group);
        }
      }
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
      loadIconFile(iconId.intValue());
    }
    // Skills
    loadSkills(ret,traitProperties);
    // Effects
    loadEffects(ret,traitProperties);

    // Pre-requisites
    CompoundTraitPrerequisite prerequisites=loadPrerequisites(traitId,traitProperties);
    if (prerequisites!=null)
    {
      AbstractTraitPrerequisite toUse=simplify(prerequisites);
      ret.setTraitPrerequisite(toUse);
    }
    return ret;
  }

  private void loadSkills(TraitDescription trait, PropertiesSet traitProperties)
  {
    SkillsManager skillsMgr=SkillsManager.getInstance();
    {
      Object[] skillArray=(Object[])traitProperties.getProperty("Trait_Skill_Array");
      if (skillArray!=null) 
      {
        for(Object skillIdObj : skillArray)
        {
          int skillId=((Integer)skillIdObj).intValue();
          SkillDescription skill=skillsMgr.getSkill(skillId);
          if (skill!=null)
          {
            trait.addSkill(skill);
          }
          else
          {
            LOGGER.warn("Skill not found: "+skillId);
          }
        }
      }
    }
    // Skills (again)
    /*
Trait_EffectSkill_AtRankSkillsAcquired_Array: 
  #1: Trait_EffectSkill_AtRankSkillsAcquired_Struct 
    Trait_EffectSkill_SkillAcquired_Array: 
      #1: Trait_EffectSkill_SkillAcquired 1879064192
    Trait_EffectSkill_SkillAcquired_Rank: 1
     */
    {
      Object[] skillArray=(Object[])traitProperties.getProperty("Trait_EffectSkill_AtRankSkillsAcquired_Array");
      if (skillArray!=null)
      {
        //System.out.println(ret);
        for(Object entry : skillArray)
        {
          PropertiesSet effectSkillStruct=(PropertiesSet)entry;
          @SuppressWarnings("unused")
          int rank=((Integer)effectSkillStruct.getProperty("Trait_EffectSkill_SkillAcquired_Rank")).intValue();
          Object[] skillIDsArray=(Object[])effectSkillStruct.getProperty("Trait_EffectSkill_SkillAcquired_Array");
          for(Object skillIDObj : skillIDsArray)
          {
            int skillId=((Integer)skillIDObj).intValue();
            //System.out.println("Rank "+rank+" => "+skillId);
            SkillDescription skill=skillsMgr.getSkill(skillId);
            if (skill!=null)
            {
              trait.addSkill(skill);
            }
            else
            {
              LOGGER.warn("Skill not found: "+skillId);
            }
          }
        }
      }
    }
  }

  private void loadEffects(TraitDescription trait, PropertiesSet traitProperties)
  {
    loadEffectsAtRank(trait,traitProperties);
    loadEffectGenerators(trait,traitProperties);
    loadVirtuePassives(trait,traitProperties);
  }

  private void loadEffectGenerators(TraitDescription trait, PropertiesSet traitProperties)
  {
    /*
    EffectGenerator_TraitEffectList: 
      #1: EffectGenerator_EffectStruct 
        EffectGenerator_EffectID: 1879051501
        EffectGenerator_EffectSpellcraft: 0.0
    */
    Object[] effectsArray=(Object[])traitProperties.getProperty("EffectGenerator_TraitEffectList");
    if (effectsArray==null)
    {
      return;
    }
    for(Object entry : effectsArray)
    {
      PropertiesSet generatorProps=(PropertiesSet)entry;
      loadEffectGenerator(trait, generatorProps);
    }
  }

  private void loadVirtuePassives(TraitDescription trait, PropertiesSet traitProperties)
  {
    /*
EffectGenerator_Virtue_PassiveEffectList: 
  #1: EffectGenerator_EffectStruct 
    EffectGenerator_EffectID: 1879389531
     */
    Object[] effectsArray=(Object[])traitProperties.getProperty("EffectGenerator_Virtue_PassiveEffectList");
    if (effectsArray==null)
    {
      return;
    }
    for(Object entry : effectsArray)
    {
      PropertiesSet generatorProps=(PropertiesSet)entry;
      loadEffectGenerator(trait, generatorProps);
    }
  }

  private void loadEffectGenerator(TraitDescription trait, PropertiesSet generatorProps)
  {
    int effectID=((Integer)generatorProps.getProperty("EffectGenerator_EffectID")).intValue(); 
    Float spellcraft=(Float)generatorProps.getProperty("EffectGenerator_EffectSpellcraft");
    if ((spellcraft!=null) && (spellcraft.floatValue()<0))
    {
      spellcraft=null;
    }
    Effect effect=_effectsLoader.getEffect(effectID);
    EffectGenerator generator=new EffectGenerator(effect,spellcraft);
    trait.addEffectGenerator(generator);
  }

  private void loadEffectsAtRank(TraitDescription trait, PropertiesSet traitProperties)
  {
    /*
Trait_EffectSkill_AtRankEffects_Array: 
  #1: Trait_EffectSkill_AtRankEffects_Struct 
    Trait_EffectSkill_AtRankEffects_Rank: 1
    Trait_EffectSkill_Effect_Array: 
      #1: Trait_EffectSkill_EffectDID 1879449317
     */
    Object[] effectsArray=(Object[])traitProperties.getProperty("Trait_EffectSkill_AtRankEffects_Array");
    if (effectsArray==null)
    {
      return;
    }
    for(Object entry : effectsArray)
    {
      PropertiesSet effectSkillStruct=(PropertiesSet)entry;
      int rank=((Integer)effectSkillStruct.getProperty("Trait_EffectSkill_AtRankEffects_Rank")).intValue();
      Object[] effectIDsArray=(Object[])effectSkillStruct.getProperty("Trait_EffectSkill_Effect_Array");
      for(Object effectIDObj : effectIDsArray)
      {
        int effectID=((Integer)effectIDObj).intValue();
        //System.out.println("Rank "+rank+" => "+effectID);
        Effect effect=_effectsLoader.getEffect(effectID);
        if (effect!=null)
        {
          trait.addEffect(effect,rank);
        }
        else
        {
          LOGGER.warn("Effect not found: "+effectID);
        }
      }
    }
  }

  private CompoundTraitPrerequisite loadPrerequisites(int traitId, PropertiesSet properties)
  {
    PropertyValue value=properties.getPropertyValueByName("Trait_Prerequisites_Logic");
    if (value==null)
    {
      return null;
    }
    /*
    StringBuilder sb=new StringBuilder();
    sb.append("ID=").append(traitId).append(EndOfLine.NATIVE_EOL);
    PropertiesUtils.dumpValue(sb,0,value);
    System.out.println(sb);
    */
    ArrayPropertyValue arrayValue=(ArrayPropertyValue)value;
    return loadCompoundPrerequisites(arrayValue);
  }

  private CompoundTraitPrerequisite loadCompoundPrerequisites(ArrayPropertyValue arrayValue)
  {
    PropertyDefinition property=arrayValue.getDefinition();
    TraitLogicOperator operator=findOperatorFromPropertyName(property.getName());
    CompoundTraitPrerequisite ret=new CompoundTraitPrerequisite(operator);
    for(PropertyValue childPropertyValue : arrayValue.getValues())
    {
      Object childValue=childPropertyValue.getValue();
      //System.out.println(childDef.getName()+" => "+childValue);
      if (childPropertyValue instanceof ArrayPropertyValue)
      {
        ArrayPropertyValue childArrayPropertyValue=(ArrayPropertyValue)childPropertyValue;
        CompoundTraitPrerequisite childPrerequisite=loadCompoundPrerequisites(childArrayPropertyValue);
        ret.addPrerequisite(childPrerequisite);
      }
      else
      {
        int traitID=((Integer)childValue).intValue();
        SimpleTraitPrerequisite childPrerequisite=new SimpleTraitPrerequisite();
        Proxy<TraitDescription> proxy=childPrerequisite.getTraitProxy();
        _proxies.add(proxy);
        proxy.setId(traitID);
        ret.addPrerequisite(childPrerequisite);
      }
    }
    return ret;
  }

  private TraitLogicOperator findOperatorFromPropertyName(String name)
  {
    if (name.contains("OneOf")) return TraitLogicOperator.ONE_OF;
    if (name.contains("NoneOf")) return TraitLogicOperator.NONE_OF;
    if (name.contains("AllOf")) return TraitLogicOperator.ALL_OF;
    if (name.contains("Trait_Prerequisites_Logic")) return TraitLogicOperator.ALL_OF;
    return null;
  }

  private AbstractTraitPrerequisite simplify(CompoundTraitPrerequisite input)
  {
    List<AbstractTraitPrerequisite> children=input.getPrerequisites();
    if (children.size()==1)
    {
      AbstractTraitPrerequisite child=children.get(0);
      if (child instanceof CompoundTraitPrerequisite)
      {
        return simplify((CompoundTraitPrerequisite)child);
      }
      return child;
    }
    return input;
  }

  private void loadRankIcons(ArrayProgression progression)
  {
    int nbPoints=progression.getNumberOfPoints();
    for(int i=1;i<nbPoints;i++)
    {
      int iconId=progression.getRawValue(i).intValue();
      loadIconFile(iconId);
    }
  }

  private void loadIconFile(int iconId)
  {
    if (iconId==0)
    {
      return;
    }
    String iconFilename=iconId+".png";
    File to=new File(GeneratedFiles.TRAIT_ICONS_DIR,iconFilename).getAbsoluteFile();
    if (!to.exists())
    {
      boolean ok=DatIconsUtils.buildImageFile(_facade,iconId,to);
      if (!ok)
      {
        LOGGER.warn("Could not build trait icon: "+iconFilename);
      }
    }
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
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    new MainTraitDataLoader(facade,effectsLoader).doIt();
    facade.dispose();
  }
}
