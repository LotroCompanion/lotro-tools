package delta.games.lotro.tools.extraction.effects;

import java.io.File;
import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.Interactable;
import delta.games.lotro.common.effects.ApplicationProbability;
import delta.games.lotro.common.effects.ApplyOverTimeEffect;
import delta.games.lotro.common.effects.AreaEffect;
import delta.games.lotro.common.effects.AreaEffectFlags;
import delta.games.lotro.common.effects.BaseVitalEffect;
import delta.games.lotro.common.effects.BubbleEffect;
import delta.games.lotro.common.effects.ComboEffect;
import delta.games.lotro.common.effects.CountDownEffect;
import delta.games.lotro.common.effects.DispelByResistEffect;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectAndProbability;
import delta.games.lotro.common.effects.EffectDuration;
import delta.games.lotro.common.effects.EffectFlags;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.effects.EffectsManager;
import delta.games.lotro.common.effects.GenesisEffect;
import delta.games.lotro.common.effects.Hotspot;
import delta.games.lotro.common.effects.InduceCombatStateEffect;
import delta.games.lotro.common.effects.InstantFellowshipEffect;
import delta.games.lotro.common.effects.InstantVitalEffect;
import delta.games.lotro.common.effects.ProcEffect;
import delta.games.lotro.common.effects.PropertyModificationEffect;
import delta.games.lotro.common.effects.ReactiveChange;
import delta.games.lotro.common.effects.ReactiveVitalChange;
import delta.games.lotro.common.effects.ReactiveVitalEffect;
import delta.games.lotro.common.effects.RecallEffect;
import delta.games.lotro.common.effects.ReviveEffect;
import delta.games.lotro.common.effects.ReviveVitalData;
import delta.games.lotro.common.effects.TieredEffect;
import delta.games.lotro.common.effects.TravelEffect;
import delta.games.lotro.common.effects.VitalChangeDescription;
import delta.games.lotro.common.effects.VitalOverTimeEffect;
import delta.games.lotro.common.effects.io.xml.EffectXMLWriter;
import delta.games.lotro.common.enums.CombatState;
import delta.games.lotro.common.enums.DamageQualifier;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.ResistCategory;
import delta.games.lotro.common.enums.SkillType;
import delta.games.lotro.common.enums.VitalType;
import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.common.math.LinearFunction;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.common.progressions.ProgressionUtils;
import delta.games.lotro.tools.extraction.utils.DatStatUtils;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.utils.Proxy;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loads effect data.
 * @author DAM
 */
public class EffectLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(EffectLoader.class);

  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private I18nUtils _i18nUtils;
  private EffectsManager _effectsMgr;
  private PlacesLoader _placesLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param placesLoader Places loader.
   */
  public EffectLoader(DataFacade facade, PlacesLoader placesLoader)
  {
    _facade=facade;
    _placesLoader=placesLoader;
    _i18nUtils=new I18nUtils("effects",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18nUtils);
    _effectsMgr=EffectsManager.getInstance();
  }

  /**
   * Get an effect using its identifier.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found/loaded.
   */
  public Effect getEffect(int effectId)
  {
    Effect ret=_effectsMgr.getEffectById(effectId);
    if (ret==null)
    {
      ret=loadEffect(effectId);
    }
    return ret;
  }

  /**
   * Load an effect.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found.
   */
  private Effect loadEffect(int effectId)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    if (effectProps==null)
    {
      return null;
    }
    byte[] data=_facade.loadData(effectId);
    if (data==null)
    {
      return null;
    }
    int classIndex=BufferUtils.getDoubleWordAt(data,4);
    Effect ret=buildEffect(classIndex);
    ret.setId(effectId);
    _effectsMgr.addEffect(ret);
    // Name
    String effectName=_i18nUtils.getNameStringProperty(effectProps,"Effect_Name",effectId,0);
    ret.setName(effectName);
    // Description
    String description=getStringProperty(effectProps,"Effect_Definition_Description");
    ret.setDescription(description);
    // Description override
    String descriptionOverride=getStringProperty(effectProps,"Effect_Description_Override");
    ret.setDescriptionOverride(descriptionOverride);
    // Applied description
    String appliedDescription=getStringProperty(effectProps,"Effect_Applied_Description");
    ret.setAppliedDescription(appliedDescription);
    // Icon
    Integer effectIconId=(Integer)effectProps.getProperty("Effect_Icon");
    if (effectIconId!=null)
    {
      ret.setIconId(effectIconId);
    }
    // Probability
    ApplicationProbability probability=getProbability(effectProps);
    ret.setApplicationProbability(probability);
    // Duration
    EffectDuration duration=getDuration(effectProps);
    ret.setEffectDuration(duration);
    // Specifics
    loadSpecifics(ret,effectProps);
    // Icon
    Integer iconId=ret.getIconId();
    if (iconId!=null)
    {
      String iconFilename=iconId+".png";
      File to=new File(GeneratedFiles.EFFECT_ICONS_DIR,iconFilename).getAbsoluteFile();
      if (!to.exists())
      {
        boolean ok=DatIconsUtils.buildImageFile(_facade,iconId.intValue(),to);
        if (!ok)
        {
          LOGGER.warn("Could not build effect icon: {}", iconFilename);
        }
      }
    }
    // Flags
    ret.setBaseFlag(EffectFlags.DEBUFF,getFlag(effectProps,"Effect_Debuff"));
    ret.setBaseFlag(EffectFlags.HARMFUL,getFlag(effectProps,"Effect_Harmful"));
    ret.setBaseFlag(EffectFlags.CURABLE,getFlag(effectProps,"Effect_IsCurable",true));
    ret.setBaseFlag(EffectFlags.REMOVAL_ONLY_IN_COMBAT,getFlag(effectProps,"Effect_RemovalOnlyInCombat",true));
    ret.setBaseFlag(EffectFlags.REMOVE_ON_AWAKEN,getFlag(effectProps,"Effect_RemoveOnAwaken",true));
    ret.setBaseFlag(EffectFlags.REMOVE_ON_DEFEAT,getFlag(effectProps,"Effect_RemoveOnDefeat"));
    ret.setBaseFlag(EffectFlags.REMOVE_ON_PULSE_RESIST,getFlag(effectProps,"Effect_RemoveOnPulseResist"));
    ret.setBaseFlag(EffectFlags.SEND_TO_CLIENT,getFlag(effectProps,"Effect_SentToClient"));
    ret.setBaseFlag(EffectFlags.UI_VISIBLE,getFlag(effectProps,"Effect_UIVisible"));
    ret.setBaseFlag(EffectFlags.DURATION_COMBAT_ONLY,getFlag(effectProps,"Effect_Duration_CombatOnly"));
    ret.setBaseFlag(EffectFlags.DURATION_EXPIRES_IN_REAL_TIME,getFlag(effectProps,"Effect_Duration_ExpiresInRealTime"));
    ret.setBaseFlag(EffectFlags.DURATION_PERMANENT,getFlag(effectProps,"Effect_Duration_Permanent"));
    ret.setBaseFlag(EffectFlags.AUTO_EXAMINATION,getFlag(effectProps,"Effect_Display_Procedurally_Generated_Examination_Information",true));
    return ret;
  }

  private String getStringProperty(PropertiesSet props, String propertyName)
  {
    return _i18nUtils.getStringProperty(props,propertyName);
  }

  private Effect buildEffect(int classDef)
  {
    // Effect PropertyModificationEffect (734) and child classes
    // except those explicitly handled later
    if ((classDef==734) ||
        (classDef==716) || (classDef==717) || (classDef==752) ||
        (classDef==753) || (classDef==739) || (classDef==748) ||
        (classDef==764) || (classDef==780) || (classDef==2156) ||
        (classDef==2259) || (classDef==2441) || (classDef==2459) ||
        (classDef==3218) || (classDef==3690) || (classDef==3833) ||
        (classDef==3842))
    {
      return new PropertyModificationEffect();
    }
    else if (classDef==3686) return new ProcEffect();
    else if (classDef==725) return new InstantVitalEffect();
    else if (classDef==755) return new VitalOverTimeEffect();
    else if (classDef==724) return new InstantFellowshipEffect();
    else if (classDef==736) return new ReactiveVitalEffect();
    else if (classDef==719) return new GenesisEffect();
    else if (classDef==769) return new InduceCombatStateEffect();
    else if (classDef==714) return new DispelByResistEffect();
    else if (classDef==737) return new RecallEffect();
    else if (classDef==749) return new TravelEffect();
    else if (classDef==767) return new ComboEffect();
    else if (classDef==3866) return new TieredEffect();
    else if (classDef==2762) return new AreaEffect();
    else if (classDef==3222) return new BubbleEffect();
    else if (classDef==713) return new CountDownEffect();
    else if (classDef==708) return new ApplyOverTimeEffect();
    else if (classDef==744) return new ReviveEffect();
    return new Effect();
  }

  private void loadSpecifics(Effect effect, PropertiesSet effectProps)
  {
    if (effect instanceof ProcEffect)
    {
      loadProcEffect((ProcEffect)effect,effectProps);
    }
    else if (effect instanceof ReactiveVitalEffect)
    {
      loadReactiveVitalEffect((ReactiveVitalEffect)effect,effectProps);
    }
    else if (effect instanceof BubbleEffect)
    {
      loadBubbleEffect((BubbleEffect)effect,effectProps);
    }
    else if (effect instanceof CountDownEffect)
    {
      loadCountDownEffect((CountDownEffect)effect,effectProps);
    }
    else if (effect instanceof PropertyModificationEffect)
    {
      loadPropertyModificationEffect((PropertyModificationEffect)effect,effectProps);
    }
    else if (effect instanceof InstantVitalEffect)
    {
      loadInstantVitalEffect((InstantVitalEffect)effect,effectProps);
    }
    else if (effect instanceof VitalOverTimeEffect)
    {
      loadVitalOverTimeEffect((VitalOverTimeEffect)effect,effectProps);
    }
    else if (effect instanceof InstantFellowshipEffect)
    {
      loadInstantFellowshipAspect((InstantFellowshipEffect)effect,effectProps);
    }
    else if (effect instanceof GenesisEffect)
    {
      loadGenesisEffect((GenesisEffect)effect,effectProps);
    }
    else if (effect instanceof InduceCombatStateEffect)
    {
      loadInduceCombatStateEffect((InduceCombatStateEffect)effect,effectProps);
    }
    else if (effect instanceof DispelByResistEffect)
    {
      loadDispelByResistEffect((DispelByResistEffect)effect,effectProps);
    }
    else if (effect instanceof RecallEffect)
    {
      loadRecallEffect((RecallEffect)effect,effectProps);
    }
    else if (effect instanceof TravelEffect)
    {
      loadTravelEffect((TravelEffect)effect,effectProps);
    }
    else if (effect instanceof ComboEffect)
    {
      loadComboEffect((ComboEffect)effect,effectProps);
    }
    else if (effect instanceof TieredEffect)
    {
      loadTieredEffect((TieredEffect)effect,effectProps);
    }
    else if (effect instanceof AreaEffect)
    {
      loadAreaEffect((AreaEffect)effect,effectProps);
    }
    else if (effect instanceof ApplyOverTimeEffect)
    {
      loadApplyOverTimeEffect((ApplyOverTimeEffect)effect,effectProps);
    }
    else if (effect instanceof ReviveEffect)
    {
      loadReviveEffect((ReviveEffect)effect,effectProps);
    }
  }

  private void loadPropertyModificationEffect(PropertyModificationEffect effect, PropertiesSet effectProps)
  {
    // Effect PropertyModificationEffect (734) or MountEffect (2459)
    Object modArray=effectProps.getProperty("Mod_Array");
    if (modArray==null)
    {
      return;
    }
    // Stats
    StatsProvider statsProvider=_statUtils.buildStatProviders(effectProps);
    effect.setStatsProvider(statsProvider);
  }

  private void loadProcEffect(ProcEffect effect, PropertiesSet effectProps)
  {
    loadPropertyModificationEffect(effect,effectProps);
    Object[] userEffectsList=(Object[])effectProps.getProperty("EffectGenerator_SkillProc_UserEffectList");
    Object[] targetEffectsList=(Object[])effectProps.getProperty("EffectGenerator_SkillProc_TargetEffectList");

    // TODO Effect_SkillProc_RequiredCombatResult
    // Skill types
    Long skillTypeFlags=(Long)effectProps.getProperty("Effect_SkillProc_SkillTypes");
    if (skillTypeFlags!=null)
    {
      BitSet bitset=BitSetUtils.getBitSetFromFlags(skillTypeFlags.longValue());
      LotroEnum<SkillType> skillTypesEnum=LotroEnumsRegistry.getInstance().get(SkillType.class);
      List<SkillType> skillTypes=skillTypesEnum.getFromBitSet(bitset);
      effect.setSkillTypes(skillTypes);
    }
    // Probability
    Float probability=(Float)effectProps.getProperty("Effect_SkillProc_ProcProbability");
    effect.setProcProbability(probability);
    // Proc'ed effects
    if (userEffectsList!=null)
    {
      for(Object entry : userEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        effect.addProcedEffect(generator);
      }
    }
    if (targetEffectsList!=null)
    {
      for(Object entry : targetEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        effect.addProcedEffect(generator); // TODO Distinguish User/Target
      }
    }
    // Cooldown
    Float cooldown=(Float)effectProps.getProperty("Effect_MinTimeBetweenProcs");
    effect.setCooldown(cooldown);
  }

  private void loadInstantVitalEffect(InstantVitalEffect effect, PropertiesSet effectProps)
  {
    Integer vitalType=(Integer)effectProps.getProperty("Effect_BaseVital_VitalType");
    loadBaseVitalEffect(effect,vitalType,effectProps);
    VitalChangeDescription description=loadVitalChangeDescription(effectProps,"Effect_InstantVital_InitialChange");
    if (description==null)
    {
      LOGGER.warn("No value data for vital change!");
    }
    else
    {
      // VPS multiplier
      Float vpsMultiplier=(Float)effectProps.getProperty("Effect_BaseVital_InitialChangeVPSMultiplier");
      description.setVPSMultiplier(vpsMultiplier);
    }
    effect.setInstantChangeDescription(description);
    // Multiplicative?
    Integer multiplicativeInt=(Integer)effectProps.getProperty("Effect_InstantVital_Multiplicative");
    boolean multiplicative=((multiplicativeInt!=null)&&(multiplicativeInt.intValue()==1));
    effect.setMultiplicative(multiplicative);
    // Initial change multiplier
    Float multiplier=(Float)effectProps.getProperty("Effect_BaseVital_InitialChangeMultiplier");
    if ((multiplier!=null) && (multiplier.floatValue()>0))
    {
      effect.setInitialChangeMultiplier(multiplier);
    }
  }

  private void loadVitalOverTimeEffect(VitalOverTimeEffect effect, PropertiesSet effectProps)
  {
  /*
Effect_VitalOverTime_ChangePerIntervalProgression: 1879068280
Effect_VitalOverTime_ChangePerInterval_Critical_Multiplier: 1.0
Effect_VitalOverTime_ChangePerInterval_ModifierList:
  #1: Effect_ModifierPropertyList_Entry 268437688 (EffectMod_ModType_DamageMultModifier_Add)
Effect_VitalOverTime_InitialChangeProgression: 1879068279
Effect_VitalOverTime_InitialChange_Critical_Multiplier: 1.0
Effect_VitalOverTime_InitialChange_ModifierList:
  #1: Effect_ModifierPropertyList_Entry 268437688 (EffectMod_ModType_DamageMultModifier_Add)
Effect_VitalOverTime_VitalType: 1 (Morale)
Effect_DamageType: 1 (Common) ; OR Effect_DamageType: 0 (Undef)
 */
    Integer vitalType=(Integer)effectProps.getProperty("Effect_VitalOverTime_VitalType");
    loadBaseVitalEffect(effect,vitalType,effectProps);
    // Initial change
    VitalChangeDescription initialChange=loadVitalChangeDescription(effectProps,"Effect_VitalOverTime_InitialChange");
    if (initialChange==null)
    {
      LOGGER.info("No initial change!");
    }
    else
    {
      // VPS multiplier
      Float vpsMultiplier=(Float)effectProps.getProperty("Effect_BaseVital_InitialChangeVPSMultiplier");
      initialChange.setVPSMultiplier(vpsMultiplier);
    }
    effect.setInitialChangeDescription(initialChange);
    // Over Time change
    VitalChangeDescription overTimeChange=loadVitalChangeDescription(effectProps,"Effect_VitalOverTime_ChangePerInterval");
    if (overTimeChange==null)
    {
      LOGGER.warn("No value data for vital change!");
    }
    else
    {
      // VPS multiplier
      Float vpsMultiplier=(Float)effectProps.getProperty("Effect_BaseVital_ChangePerIntervalVPSMultiplier");
      overTimeChange.setVPSMultiplier(vpsMultiplier);
    }
    effect.setOverTimeChangeDescription(overTimeChange);
  }

  private void loadBaseVitalEffect(BaseVitalEffect effect, Integer vitalType, PropertiesSet effectProps)
  {
    // Stat
    StatDescription stat=DatStatUtils.getStatFromVitalType(vitalType.intValue());
    effect.setStat(stat);
    // Damage type
    LotroEnum<DamageType> damageTypeEnum=LotroEnumsRegistry.getInstance().get(DamageType.class);
    Integer damageTypeCode=(Integer)effectProps.getProperty("Effect_DamageType");
    if ((damageTypeCode!=null) && (damageTypeCode.intValue()!=0))
    {
      DamageType damageType=damageTypeEnum.getEntry(damageTypeCode.intValue());
      effect.setDamageType(damageType);
    }
  }

  private void loadReactiveVitalEffect(ReactiveVitalEffect effect, PropertiesSet effectProps)
  {
    loadPropertyModificationEffect(effect,effectProps);
    ReactiveChange attackerChange=loadReactiveChange(effectProps,"Effect_ReactiveVital_Attacker");
    effect.setAttackerReactiveChange(attackerChange);
    ReactiveChange defenderChange=loadReactiveChange(effectProps,"Effect_ReactiveVital_Defender");
    effect.setDefenderReactiveChange(defenderChange);
    // Vital types
    /*
    Object[] vitalTypeList=(Object[])effectProps.getProperty("Effect_VitalInterested_VitalTypeList");
    if (vitalTypeList!=null)
    {
      for(Object vitalTypeObj : vitalTypeList)
      {
        int vitalType=((Integer)vitalTypeObj).intValue();
        StatDescription stat=DatStatUtils.getStatFromVitalType(vitalType);
        ret.addVitalType(stat);
      }
    }
    */
    // Incoming damage types
    LotroEnum<DamageType> damageTypeEnum=LotroEnumsRegistry.getInstance().get(DamageType.class);
    Object[] damageTypeList=(Object[])effectProps.getProperty("Effect_InterestedIncomingDamageTypes");
    if (damageTypeList!=null)
    {
      for(Object damageTypeObj : damageTypeList)
      {
        int damageTypeCode=((Integer)damageTypeObj).intValue();
        DamageType damageType=damageTypeEnum.getEntry(damageTypeCode);
        effect.addDamageType(damageType);
      }
    }
    // Damage qualifiers
    LotroEnum<DamageQualifier> damageQualifierEnum=LotroEnumsRegistry.getInstance().get(DamageQualifier.class);
    Object[] damageQualifierList=(Object[])effectProps.getProperty("Effect_ReactiveVital_RequiredAttacker_DamageQualifier_Array");
    if (damageQualifierList!=null)
    {
      for(Object damageQualifierObj : damageQualifierList)
      {
        int damageQualifierCode=((Integer)damageQualifierObj).intValue();
        if (damageQualifierCode!=0)
        {
          DamageQualifier damageQualifier=damageQualifierEnum.getEntry(damageQualifierCode);
          effect.addDamageQualifier(damageQualifier);
        }
      }
    }
    // Damage type override
    Integer damageTypeOverrideCode=(Integer)effectProps.getProperty("Effect_ReactiveVital_AttackerVitalChange_DamageTypeOverride");
    if (damageTypeOverrideCode!=null)
    {
      DamageType damageTypeOverride=damageTypeEnum.getEntry(damageTypeOverrideCode.intValue());
      effect.setAttackerDamageTypeOverride(damageTypeOverride);
    }
    // Remove on proc
    Integer removeOnProcInt=(Integer)effectProps.getProperty("Effect_ReactiveVital_RemoveOnSuccessfulProc");
    boolean removeOnProc=((removeOnProcInt!=null)&&(removeOnProcInt.intValue()==1));
    effect.setRemoveOnProc(removeOnProc);
  }

  private ReactiveChange loadReactiveChange(PropertiesSet effectProps, String seed)
  {
    ReactiveVitalChange vitalChange=loadReactiveVitalChange(effectProps,seed+"VitalChange_");
    EffectAndProbability effect=loadEffectAndProbability(effectProps,seed+"Effect_");
    if ((vitalChange!=null) || (effect!=null))
    {
      return new ReactiveChange(vitalChange,effect);
    }
    return null;
  }

  private ReactiveVitalChange loadReactiveVitalChange(PropertiesSet effectProps, String seed)
  {
    Float constantFloat=(Float)effectProps.getProperty(seed+"Constant");
    Integer progressionIDInt=(Integer)effectProps.getProperty(seed+"Progression");
    float constant=(constantFloat!=null)?constantFloat.floatValue():0;
    int progressionID=(progressionIDInt!=null)?progressionIDInt.intValue():0;
    if ((Math.abs(constant)<0.0001) && (progressionID==0))
    {
      return null;
    }

    ReactiveVitalChange ret=new ReactiveVitalChange();
    // Probability
    Float probabilityFloat=(Float)effectProps.getProperty(seed+"Probability");
    float probability=(probabilityFloat!=null)?probabilityFloat.floatValue():0;
    if (probability<0)
    {
      probability=1;
    }
    ret.setProbability(probability);
    // Multiplicative/additive
    Integer additiveInt=(Integer)effectProps.getProperty(seed+"Additive");
    Integer multiplicativeInt=(Integer)effectProps.getProperty(seed+"Multiplicative");
    boolean multiplicative=false;
    if ((multiplicativeInt!=null) && (multiplicativeInt.intValue()==1))
    {
      multiplicative=true;
      if ((additiveInt!=null) && (additiveInt.intValue()!=0))
      {
        LOGGER.warn("Additive or multiplicative?");
      }
    }
    ret.setMultiplicative(multiplicative);
    // Constant
    if (Math.abs(constant)>0.0001)
    {
      ret.setConstant(constant);
    }
    if (progressionIDInt!=null)
    {
      Progression progression=ProgressionUtils.getProgression(_facade,progressionIDInt.intValue());
      ret.setProgression(progression);
    }
    Float variance=(Float)effectProps.getProperty(seed+"Variance");
    ret.setVariance(variance);
    return ret;
  }

  private EffectAndProbability loadEffectAndProbability(PropertiesSet effectProps, String seed)
  {
    Float probabilityFloat=(Float)effectProps.getProperty(seed+"Probability");
    float probability=(probabilityFloat!=null)?probabilityFloat.floatValue():0;
    Integer effectIDInt=(Integer)effectProps.getProperty(seed+"Effect");
    int effectID=(effectIDInt!=null)?effectIDInt.intValue():0;
    if (effectID==0)
    {
      return null;
    }
    Effect effect=getEffect(effectID);
    if (effect==null)
    {
      LOGGER.warn("Effect not found: {}",Integer.valueOf(effectID));
      return null;
    }
    EffectAndProbability ret=new EffectAndProbability(effect,probability);
    return ret;
  }

  private VitalChangeDescription loadVitalChangeDescription(PropertiesSet effectProps, String seed)
  {
    Float constant=(Float)effectProps.getProperty(seed+"Constant");
    Integer progressionID=(Integer)effectProps.getProperty(seed+"Progression");
    Float variance=(Float)effectProps.getProperty(seed+"Variance");
    Float min=null;
    Float max=null;
    PropertiesSet randomProps=(PropertiesSet)effectProps.getProperty(seed+"Random");
    if (randomProps!=null)
    {
      min=(Float)randomProps.getProperty("Effect_RandomValueMin");
      max=(Float)randomProps.getProperty("Effect_RandomValueMax");
    }
    if (!((constant!=null) || (progressionID!=null) || ((min!=null) && (max!=null))))
    {
      return null;
    }
    VitalChangeDescription ret=new VitalChangeDescription();
    if (constant!=null)
    {
      ret.setConstant(constant.floatValue());
    }
    else if (progressionID!=null)
    {
      Progression progression=ProgressionUtils.getProgression(_facade,progressionID.intValue());
      ret.setProgression(progression);
    }
    else // Random
    {
      ret.setMinValue(min.floatValue());
      ret.setMaxValue(max.floatValue());
    }
    // Variance
    ret.setVariance(variance);
    // Modifiers
    ModPropertyList modifiers=ModifiersUtils.getStatModifiers(effectProps,seed+"_ModifierList");
    ret.setModifiers(modifiers);
    return ret;
  }

  private void loadInstantFellowshipAspect(InstantFellowshipEffect effect, PropertiesSet effectProps)
  {
    Object[] effectsList=(Object[])effectProps.getProperty("EffectGenerator_InstantFellowship_AppliedEffectList");
    // Effects
    for(Object entry : effectsList)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      EffectGenerator generator=loadGenerator(entryProps);
      effect.addEffect(generator);
    }
    // Flags
    Integer raidGroups=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToRaidGroups");
    if (raidGroups!=null)
    {
      effect.setAppliesToRaidGroups(raidGroups.intValue()==1);
    }
    Integer pets=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToPets");
    if (pets!=null)
    {
      effect.setAppliesToPets(pets.intValue()==1);
    }
    Integer target=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToTarget");
    if (target!=null)
    {
      effect.setAppliesToTarget(target.intValue()==1);
    }
    // String override
    String rawOverride=DatStringUtils.getStringProperty(effectProps,"Effect_InstantFellowship_StringOverrideForFellowship");
    if ((rawOverride!=null) && (!rawOverride.isEmpty()))
    {
      String override=_i18nUtils.getStringProperty(effectProps,"Effect_InstantFellowship_StringOverrideForFellowship");
      effect.setFellowshipStringOverride(override);
    }
    // Range
    Float range=(Float)effectProps.getProperty("Effect_InstantFellowship_MaxRange");
    if ((range!=null) && (range.floatValue()>0))
    {
      effect.setRange(range.floatValue());
    }
  }

  private void loadCountDownEffect(CountDownEffect effect, PropertiesSet effectProps)
  {
    loadPropertyModificationEffect(effect,effectProps);
    // 'on expire' effects
    Object[] expireEffectsList=(Object[])effectProps.getProperty("EffectGenerator_Countdown_ExpireEffectList");
    if (expireEffectsList!=null)
    {
      for(Object entry : expireEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        effect.addOnExpireEffect(generator);
      }
    }
    // 'on removal' effect
    PropertiesSet onRemovalProps=(PropertiesSet)effectProps.getProperty("EffectGenerator_OnRemoval_Effect");
    if (onRemovalProps!=null)
    {
      EffectGenerator generator=loadGenerator(onRemovalProps);
      effect.setOnRemovalEffect(generator);
    }
  }

  private void loadBubbleEffect(BubbleEffect effect, PropertiesSet effectProps)
  {
    loadCountDownEffect(effect,effectProps);
    Integer vitalType=(Integer)effectProps.getProperty("Effect_Bubble_VitalType");
    StatDescription stat=DatStatUtils.getStatFromVitalType(vitalType.intValue());
    effect.setVital(stat);
    Float value=(Float)effectProps.getProperty("Effect_Bubble_Value");
    effect.setValue(value);
    Integer progressionID=(Integer)effectProps.getProperty("Effect_Bubble_Value_Progression");
    if (progressionID!=null)
    {
      Progression progression=ProgressionUtils.getProgression(_facade,progressionID.intValue());
      effect.setProgression(progression);
    }
    Float percentage=(Float)effectProps.getProperty("Effect_Bubble_Percentage");
    effect.setPercentage(percentage);
    ModPropertyList modifier=ModifiersUtils.getStatModifiers(effectProps,"Effect_BubbleValue_ModifierList");
    effect.setModifiers(modifier);
  }

  private void loadApplyOverTimeEffect(ApplyOverTimeEffect effect, PropertiesSet effectProps)
  {
    // 'initially applied' effects
    Object[] initiallyAppliedEffectsList=(Object[])effectProps.getProperty("Effect_ApplyOverTime_Initial_Applied_Effect_Array");
    if (initiallyAppliedEffectsList!=null)
    {
      for(Object entry : initiallyAppliedEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps,"Effect_ApplyOverTime_Applied_Effect","Effect_ApplyOverTime_Applied_Effect_Spellcraft");
        effect.addInitiallyAppliedEffect(generator);
      }
    }
    // 'applied' effects
    Object[] appliedEffectsList=(Object[])effectProps.getProperty("Effect_ApplyOverTime_Applied_Effect_Array");
    if (appliedEffectsList!=null)
    {
      for(Object entry : appliedEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps,"Effect_ApplyOverTime_Applied_Effect","Effect_ApplyOverTime_Applied_Effect_Spellcraft");
        effect.addAppliedEffect(generator);
      }
    }
  }

  private EffectGenerator loadGenerator(PropertiesSet generatorProps)
  {
    return loadGenerator(generatorProps,"EffectGenerator_EffectID","EffectGenerator_EffectSpellcraft");
  }

  private EffectGenerator loadGenerator(PropertiesSet generatorProps, String idPropName, String spellcraftPropName)
  {
    int effectID=((Integer)generatorProps.getProperty(idPropName)).intValue();
    Float spellcraft=(Float)generatorProps.getProperty(spellcraftPropName);
    if ((spellcraft!=null) && (spellcraft.floatValue()<0))
    {
      spellcraft=null;
    }
    Effect effect=getEffect(effectID);
    EffectGenerator ret=new EffectGenerator(effect,spellcraft);
    return ret;
  }

  private ApplicationProbability getProbability(PropertiesSet effectProps)
  {
    Float probabilityFloat=(Float)effectProps.getProperty("Effect_ConstantApplicationProbability");
    float probability=(probabilityFloat!=null)?probabilityFloat.floatValue():1.0f;
    Float varianceFloat=(Float)effectProps.getProperty("Effect_ApplicationProbabilityVariance");
    float variance=(varianceFloat!=null)?varianceFloat.floatValue():0;
    Integer modPropertyInt=(Integer)effectProps.getProperty("Effect_ApplicationProbability_AdditiveModProp");
    int modProperty=(modPropertyInt!=null)?modPropertyInt.intValue():0;
    return ApplicationProbability.from(probability,variance,modProperty);
    // Effect_VariableApplicationProbability, type=Struct
    //   used but always:
    // Effect_VariableApplicationProbability:
    //   Effect_VariableMax: 1.0
    //   Effect_VariableMin: 1.0
    // Effect_ApplicationProbabilityProgression: never used?
    // Effect_SpecialApplicationProbability: never used?
    // Effect_SuppressApplicationProbabilityExamination: never used?
    // Effect_ApplicationProbability_AdditiveModProp_Array: never used?
  }

  private EffectDuration getDuration(PropertiesSet effectProps)
  {
    // Constant duration
    // - value
    Float duration=(Float)effectProps.getProperty("Effect_Duration_ConstantInterval");
    if (duration!=null)
    {
      if (Math.abs(duration.floatValue())<0.0001)
      {
        duration=null;
      }
    }
    // - modifiers
    ModPropertyList durationModifiers=ModifiersUtils.getStatModifiers(effectProps,"Effect_Duration_ConstantInterval_ModifierList");
    // Pulse count
    // - value
    Integer pulseCountInt=(Integer)effectProps.getProperty("Effect_Duration_ConstantPulseCount");
    int pulseCount=(pulseCountInt!=null)?pulseCountInt.intValue():0;
    // - modifiers
    ModPropertyList pulseCountModifiers=ModifiersUtils.getStatModifiers(effectProps,"Effect_PulseCount_AdditiveModifiers");
    // Effect_Duration_ProgressionInterval: unused?
    // Effect_Duration_ProgressionPulseCount: unused?
    EffectDuration ret=new EffectDuration();
    ret.setDuration(duration);
    ret.setDurationModifiers(durationModifiers);
    ret.setPulseCount(pulseCount);
    ret.setPulseCountModifiers(pulseCountModifiers);
    return ret;
  }

  private void loadGenesisEffect(GenesisEffect effect, PropertiesSet effectProps)
  {
    /*
Effect ID=1879163860, class=GenesisEffect (719)
Effect_Genesis_ConstantSummonDuration: 20.0
Effect_Genesis_PermanentSummonDuration: 0
Effect_Genesis_SummonedObject: 1879163733
     */
    int summonedObjectID=((Integer)effectProps.getProperty("Effect_Genesis_SummonedObject")).intValue();
    Float summonDuration=(Float)effectProps.getProperty("Effect_Genesis_ConstantSummonDuration");
    Integer permanent=(Integer)effectProps.getProperty("Effect_Genesis_PermanentSummonDuration");
    handleSummonedObject(summonedObjectID,effect);
    if (summonDuration!=null)
    {
      effect.setSummonDuration(summonDuration.floatValue());
    }
    if ((permanent!=null) && (permanent.intValue()==1))
    {
      effect.setPermanent();
    }
  }

  private void handleSummonedObject(int objectID, GenesisEffect effect)
  {
    PropertiesSet props=_facade.loadProperties(objectID+DATConstants.DBPROPERTIES_OFFSET);
    String name=DatStringUtils.getStringProperty(props,"Name");
    Interactable interactable=null;
    int weenieType=((Integer)props.getProperty("WeenieType")).intValue();
    if (weenieType==262145) // Hotspot
    {
      Hotspot hotspot=loadHotspot(objectID,props);
      effect.setHotspot(hotspot);
    }
    // Cannot use InteractableUtils.findInteractable() here because it's too soon!
    else if (weenieType==131151) // RealNPC
    {
      NpcDescription npc=new NpcDescription(objectID,name);
      interactable=npc;
    }
    else if (weenieType==65615) // Monster
    {
      MobDescription mob=new MobDescription(objectID,name);
      interactable=mob;
    }
    else if (weenieType==129) // Item
    {
      Item item=new Item();
      item.setIdentifier(objectID);
      item.setName(name);
      interactable=item;
    }
    else
    {
      // Ignored, for instance:
      // Generator (65537)
    }
    if (interactable!=null)
    {
      Proxy<Interactable> proxy=new Proxy<Interactable>();
      proxy.setId(objectID);
      proxy.setName(name);
      proxy.setObject(interactable);
      effect.setInteractable(proxy);
    }
  }

  private Hotspot loadHotspot(int hotspotID, PropertiesSet props)
  {
    /*
EffectGenerator_HotspotEffectList:
  #1: EffectGenerator_EffectStruct
    EffectGenerator_EffectID: 1879163549
    EffectGenerator_EffectSpellcraft: -1.0
Name: Greater Emblem of Defence
WeenieType: 262145 (Hotspot)
    */
    Hotspot ret=new Hotspot(hotspotID);
    String name=DatStringUtils.getStringProperty(props,"Name");
    ret.setName(name);
    Object[] effectsList=(Object[])props.getProperty("EffectGenerator_HotspotEffectList");
    if (effectsList!=null)
    {
      for(Object effectEntry : effectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)effectEntry;
        EffectGenerator generator=loadGenerator(entryProps);
        ret.addEffect(generator);
      }
    }
    return ret;
  }

  private void loadInduceCombatStateEffect(InduceCombatStateEffect effect, PropertiesSet effectProps)
  {
    /*
    Effect ID=1879051312, class=InduceCombatStateEffect (769)
    Effect_InduceCombatState_ConstantDuration: 3.0
    Effect_InduceCombatState_StateToInduce: 16384 (Stunned)
    Effect_InduceCombatState_VariableDuration:
      Effect_VariableMax: 5.0
      Effect_VariableMin: 3.0
      Effect_VariableSpellcraftMax: 50.0
      Effect_VariableSpellcraftMin: 25.0
    Effect_CombatState_Induce_StateDuration_ModProp_List:
      #1: Effect_ModifierPropertyList_Entry 268452197 (CombatState_ConjunctionStunned_Duration)
     */
    // Constant duration
    Float duration=(Float)effectProps.getProperty("Effect_InduceCombatState_ConstantDuration");
    if (duration!=null)
    {
      effect.setDuration(duration.floatValue());
    }
    // Duration function
    PropertiesSet durationProps=(PropertiesSet)effectProps.getProperty("Effect_InduceCombatState_VariableDuration");
    if (durationProps!=null)
    {
      LinearFunction function=loadLinearFunction(durationProps);
      effect.setDurationFunction(function);
    }
    // Duration modifiers
    ModPropertyList durationMods=ModifiersUtils.getStatModifiers(effectProps,"Effect_CombatState_Induce_StateDuration_ModProp_List");
    effect.setDurationModifiers(durationMods);
    // Combat state
    Integer bitSetValueInt=(Integer)effectProps.getProperty("Effect_InduceCombatState_StateToInduce");
    if (bitSetValueInt!=null)
    {
      int bitSetValue=bitSetValueInt.intValue();
      LotroEnum<CombatState> damageTypeEnum=LotroEnumsRegistry.getInstance().get(CombatState.class);
      BitSet bitSet=BitSetUtils.getBitSetFromFlags(bitSetValue);
      List<CombatState> states=damageTypeEnum.getFromBitSet(bitSet);
      if (states.size()!=1)
      {
        LOGGER.warn("Unexpected size for combat states: {}", states);
      }
      CombatState state=states.get(0);
      effect.setCombatState(state);
    }
    // Break on harm
    // Ex: 100% break chance on harm after 1s
    Float breakOnHarmfullSkill=(Float)effectProps.getProperty("Effect_CombatState_Induce_BreakOnHarmfulSkill_Override");
    effect.setBreakOnHarmfullSkill(breakOnHarmfullSkill);
    // No modifiers: Effect_CombatState_Induce_BreakOnHarmfulSkill_ModProp_List
    // Break on vital loss. Ex: 3% break chance on damage after 1s
    // - value
    Float breakOnVitalLoss=(Float)effectProps.getProperty("Effect_CombatState_Induce_BreakOnVitalLossProb_Override");
    effect.setBreakOnVitalLossProbability(breakOnVitalLoss);
    // - modifiers
    ModPropertyList breakOnVitalLossMods=ModifiersUtils.getStatModifiers(effectProps,"Effect_CombatState_Induce_BreakOnVitalLoss_ModProp_List");
    effect.setBreakOnVitalLossProbabilityModifiers(breakOnVitalLossMods);
    // Grace period
    Float gracePeriod=(Float)effectProps.getProperty("Effect_CombatState_Induce_BreakOutOfState_GracePeriod_Override");
    effect.setGracePeriod(gracePeriod);
    ModPropertyList gracePeriodMods=ModifiersUtils.getStatModifiers(effectProps,"Effect_CombatState_Induce_BreakOutOfState_GracePeriod_Override_ModifierList");
    effect.setGracePeriodModifiers(gracePeriodMods);
  }

  private LinearFunction loadLinearFunction(PropertiesSet props)
  {
    float minX=((Float)props.getProperty("Effect_VariableSpellcraftMin")).floatValue();
    float minY=((Float)props.getProperty("Effect_VariableMin")).floatValue();
    float maxX=((Float)props.getProperty("Effect_VariableSpellcraftMax")).floatValue();
    float maxY=((Float)props.getProperty("Effect_VariableMax")).floatValue();
    return new LinearFunction(minX,maxX,minY,maxY);
  }

  private void loadDispelByResistEffect(DispelByResistEffect effect, PropertiesSet effectProps)
  {
  /*
Effect ID=1879157351, class=DispelByResistEffect (714)
Effect_DispelByResist_MaximumDispelCount: 1
Effect_DispelByResist_ResistCategoryFilter: 8 (Wound)
Effect_DispelByResist_UseStrengthRestriction: 1
 */

    // Dispel count
    Integer dispelCountInt=(Integer)effectProps.getProperty("Effect_DispelByResist_MaximumDispelCount");
    int dispelCount=(dispelCountInt!=null)?dispelCountInt.intValue():-1;
    effect.setMaxDispelCount(dispelCount);
    // Resist Categories
    int categoriesCode=((Integer)effectProps.getProperty("Effect_DispelByResist_ResistCategoryFilter")).intValue();
    LotroEnum<ResistCategory> categoriesEnum=LotroEnumsRegistry.getInstance().get(ResistCategory.class);
    BitSet bitset=BitSetUtils.getBitSetFromFlags(categoriesCode);
    List<ResistCategory> categories=categoriesEnum.getFromBitSet(bitset);
    for(ResistCategory category : categories)
    {
      effect.addResistCategory(category);
    }
    // Strength restriction
    Integer useStrengthResitriction=(Integer)effectProps.getProperty("Effect_DispelByResist_UseStrengthRestriction");
    if ((useStrengthResitriction!=null) && (useStrengthResitriction.intValue()==1))
    {
      effect.setUseStrengthRestriction(true);
    }
    // Strength offset:
    Float strengthOffset=(Float)effectProps.getProperty("Effect_DispelByResist_StrengthRestrictionOffset");
    if (strengthOffset!=null)
    {
      effect.setStrengthOffset(Integer.valueOf(strengthOffset.intValue()));
    }
  }

  private void loadRecallEffect(RecallEffect effect, PropertiesSet effectProps)
  {
  /*
Effect_Recall_Location_Type: 3 (Telepad)
Effect_Recall_Radius: 0.0
Effect_Recall_Raid: 0
Effect_Recall_Telepad: eredluin_thorinshall_exit
Effect_Recall_Travel_Link: 0 (Undef)
  */
    // Location type
    @SuppressWarnings("unused")
    Integer locationType=(Integer)effectProps.getProperty("Effect_Recall_Location_Type");
    // Position
    String telepad=(String)effectProps.getProperty("Effect_Recall_Telepad");
    if ((telepad!=null) && (!telepad.isEmpty()))
    {
      ExtendedPosition position=_placesLoader.getPositionForName(telepad);
      if (position!=null)
      {
        effect.setPosition(position.getPosition());
      }
    }
  }

  private void loadTravelEffect(TravelEffect effect, PropertiesSet effectProps)
  {
    /*
    EffectGenerator_EffectDataList: 
      #1: EffectGenerator_EffectData_SceneID 1879048837
      #2: EffectGenerator_EffectData_Destination rohan_wold_harwick_meadhall_exit
    Effect_Applied_Description: You travel to Harwick.
    Effect_Travel_PrivateEncounter: 1879262955
    */
    ArrayPropertyValue dataList=(ArrayPropertyValue)effectProps.getPropertyValueByName("EffectGenerator_EffectDataList");
    for(PropertyValue childValue : dataList.getValues())
    {
      String propertyName=childValue.getDefinition().getName();
      Object value=childValue.getValue();
      if ("EffectGenerator_EffectData_Destination".equals(propertyName))
      {
        String destination=(String)value;
        ExtendedPosition position=_placesLoader.getPositionForName(destination);
        if (position!=null)
        {
          effect.setDestination(position.getPosition());
        }
      }
      else if ("EffectGenerator_EffectData_SceneID".equals(propertyName))
      {
        Integer sceneID=(Integer)value;
        effect.setSceneID(sceneID.intValue());
      }
      else if ("EffectGenerator_EffectData_RemoveFromPrivateInstance".equals(propertyName))
      {
        Integer removeFlagInt=(Integer)value;
        boolean removeFlag=((removeFlagInt!=null)&& (removeFlagInt.intValue()==1));
        effect.setRemoveFromInstance(removeFlag);
      }
    }
    Integer privateEncounterID=(Integer)effectProps.getProperty("Effect_Travel_PrivateEncounter");
    if ((privateEncounterID!=null) && (privateEncounterID.intValue()!=0))
    {
      effect.setPrivateEncounterID(privateEncounterID);
    }
  }

  private void loadComboEffect(ComboEffect effect, PropertiesSet effectProps)
  {
/*
******** Properties: 1879453021
Effect_Combo_CasterOnly: 0
Effect_Combo_EffectPresentList:
  #1: Effect_WSLEffect 1879073120
  #2: Effect_WSLEffect 1879453022
  #3: Effect_WSLEffect 0
Effect_Combo_EffectToAddIfNotPresent: 1879073122
Effect_Combo_EffectToAddIfPresent: 1879453022
Effect_Combo_EffectToGiveBackIfPresent: DID
Effect_Combo_EffectToGiveBackIfNotPresent: DID
Effect_Combo_EffectToExamine: 1879073122
Effect_Combo_RemoveAllOldEffectsIfPresent: 0 (bool)
Effect_Combo_RemoveOldEffectIfPresent: 0 (bool)
*/
    Object[] presentArray=(Object[])effectProps.getProperty("Effect_Combo_EffectPresentList");
    if (presentArray!=null)
    {
      for(Object presentObj : presentArray)
      {
        Integer effectID=(Integer)presentObj;
        Proxy<Effect> presentEffect=buildProxy(effectID);
        effect.addPresentEffect(presentEffect);
      }
    }
    // To add if not present
    Integer toAddIfNotPresent=(Integer)effectProps.getProperty("Effect_Combo_EffectToAddIfNotPresent");
    effect.setToAddIfNotPresent(buildProxy(toAddIfNotPresent));
    // To add if present
    Integer toAddIfPresent=(Integer)effectProps.getProperty("Effect_Combo_EffectToAddIfPresent");
    effect.setToAddIfPresent(buildProxy(toAddIfPresent));
    // To give back if not present
    Integer toGiveBackIfNotPresent=(Integer)effectProps.getProperty("Effect_Combo_EffectToGiveBackIfNotPresent");
    effect.setToGiveBackIfNotPresent(buildProxy(toGiveBackIfNotPresent));
    // To give back if present
    Integer toGiveBackIfPresent=(Integer)effectProps.getProperty("Effect_Combo_EffectToGiveBackIfPresent");
    effect.setToGiveBackIfPresent(buildProxy(toGiveBackIfPresent));
    // To examine
    Integer toExamine=(Integer)effectProps.getProperty("Effect_Combo_EffectToExamine");
    effect.setToExamine(buildProxy(toExamine));
  }

  private void loadTieredEffect(TieredEffect effect, PropertiesSet effectProps)
  {
    // From Effect_TierUp_EffectList:
    /*
      #1: EffectGenerator_EffectStruct
        EffectGenerator_EffectID: 1879449300
        EffectGenerator_EffectSpellcraft: -1.0
        ...
      #5: EffectGenerator_EffectStruct
        EffectGenerator_EffectID: 1879449315
        EffectGenerator_EffectSpellcraft: -1.0
    */
    Object[] tierUpList=(Object[])effectProps.getProperty("Effect_TierUp_EffectList");
    for(Object tierUpEntry : tierUpList)
    {
      EffectGenerator generator=loadGenerator((PropertiesSet)tierUpEntry);
      effect.addTierEffect(generator);
    }
    // From Effect_TierUp_FinalEffect:
    /*
    #1: EffectGenerator_EffectStruct
      EffectGenerator_EffectID: 1879449315
      EffectGenerator_EffectSpellcraft: -1.0
    */
    Object[] finalEffectList=(Object[])effectProps.getProperty("Effect_TierUp_FinalEffect");
    if (finalEffectList!=null)
    {
      if (finalEffectList.length>1)
      {
        LOGGER.warn("More than 1 final effect!");
      }
      for(Object finalEntry : finalEffectList)
      {
        EffectGenerator generator=loadGenerator((PropertiesSet)finalEntry);
        effect.setFinalTier(generator);
      }
    }
  }

  private void loadAreaEffect(AreaEffect effect, PropertiesSet effectProps)
  {
    /*
Effect_Area_ShouldAffectCaster: 0
Effect_Area_AffectsMonsterPlayers: 1
Effect_Area_AffectsMonsters: 1
Effect_Area_AffectsPlayers: 1
Effect_Area_AffectsPvPPlayers: 1
Effect_Area_AffectsSessionPlayers: 1
Effect_Area_AffectsDistantBattleUnits: 0
Effect_Area_AffectsPlayerPets: 0
Effect_Area_CheckRangeToTargets: 0

Effect_Area_Applied_Effect_Array: 
  #1: Effect_Area_Applied_Effect_Data 
    Effect_Area_Applied_Effect: 1879284216
    Effect_Area_Applied_Effect_Spellcraft: -1.0
Effect_Area_MaxRange: 5.0
Effect_Area_MaxTargets: 10
Effect_Area_MaxTargets_AdditiveModifiers: 
  #1: Effect_ModifierPropertyList_Entry 268462716 (Skill_AreaEffect_TargetCount)
    */
    // Flags
    effect.setFlag(AreaEffectFlags.SHOULD_AFFECT_CASTER,getFlag(effectProps,"Effect_Area_ShouldAffectCaster"));
    effect.setFlag(AreaEffectFlags.AFFECTS_MONSTER_PLAYERS,getFlag(effectProps,"Effect_Area_AffectsMonsterPlayers"));
    effect.setFlag(AreaEffectFlags.AFFECTS_MONSTERS,getFlag(effectProps,"Effect_Area_AffectsMonsters"));
    effect.setFlag(AreaEffectFlags.AFFECTS_PLAYERS,getFlag(effectProps,"Effect_Area_AffectsPlayers"));
    effect.setFlag(AreaEffectFlags.AFFECTS_PVP_PLAYERS,getFlag(effectProps,"Effect_Area_AffectsPvPPlayers"));
    effect.setFlag(AreaEffectFlags.AFFECTS_SESSION_PLAYERS,getFlag(effectProps,"Effect_Area_AffectsSessionPlayers"));
    effect.setFlag(AreaEffectFlags.AFFECTS_DISTANT_BATTLE_UNITS,getFlag(effectProps,"Effect_Area_AffectsDistantBattleUnits"));
    effect.setFlag(AreaEffectFlags.AFFECTS_PLAYER_PETS,getFlag(effectProps,"Effect_Area_AffectsPlayerPets"));
    effect.setFlag(AreaEffectFlags.CHECK_RANGE_TO_TARGET,getFlag(effectProps,"Effect_Area_CheckRangeToTargets"));
    // Effects
    Object[] effectsList=(Object[])effectProps.getProperty("Effect_Area_Applied_Effect_Array");
    for(Object effectEntry : effectsList)
    {
      EffectGenerator generator=loadGenerator((PropertiesSet)effectEntry,"Effect_Area_Applied_Effect","Effect_Area_Applied_Effect_Spellcraft");
      if (generator!=null)
      {
        effect.addEffect(generator);
      }
    }
    // Range
    Float maxRange=(Float)effectProps.getProperty("Effect_Area_MaxRange");
    effect.setRange((maxRange!=null)?maxRange.floatValue():0f);
    // Detection buffer
    Float detectionBuffer=(Float)effectProps.getProperty("Effect_Area_DetectionBuffer");
    effect.setDetectionBuffer((detectionBuffer!=null)?detectionBuffer.floatValue():0f);
    // Max targets
    Integer maxTargets=(Integer)effectProps.getProperty("Effect_Area_MaxTargets");
    effect.setMaxTargets((maxTargets!=null)?maxTargets.intValue():0);
    ModPropertyList maxTargetsMods=ModifiersUtils.getStatModifiers(effectProps,"Effect_Area_MaxTargets_AdditiveModifiers");
    effect.setMaxTargetsModifiers(maxTargetsMods);
  }

  private void loadReviveEffect(ReviveEffect effect, PropertiesSet props)
  {
    /*
    Effect_Revive_EffectList: 
      #1: Effect_Revive_Effect 1879212850
    Effect_Revive_VitalDataList: 
      #1: Effect_Revive_VitalData 
        Effect_Revive_VitalPercent: 0.8
        Effect_Revive_VitalPercentAdditiveModifiers: 
          #1: Effect_ModifierPropertyList_Entry 0 (Invalid)
          #2: Effect_ModifierPropertyList_Entry 268439066 (EffectMod_ModType_ReviveHealthMultModifier)
          #3: Effect_ModifierPropertyList_Entry 0 (Invalid)
        Effect_Revive_VitalType: 1 (Morale)
      #2: Effect_Revive_VitalData 
        Effect_Revive_VitalPercent: 0.5
        Effect_Revive_VitalPercentAdditiveModifiers: 
          #1: Effect_ModifierPropertyList_Entry 268437238 (EffectMod_ModType_RevivePowerMultModifier)
          #2: Effect_ModifierPropertyList_Entry 0 (Invalid)
          #3: Effect_ModifierPropertyList_Entry 0 (Invalid)
        Effect_Revive_VitalType: 2 (Power)
    */
    // Revive effects
    Object[] effectsList=(Object[])props.getProperty("Effect_Revive_EffectList");
    if (effectsList!=null)
    {
      for(Object effectIdObj : effectsList)
      {
        Integer effectID=(Integer)effectIdObj;
        Proxy<Effect> proxy=buildProxy(effectID);
        if (proxy!=null)
        {
          effect.addReviveEffect(proxy);
        }
      }
    }
    // Vitals
    Object[] vitalEntries=(Object[])props.getProperty("Effect_Revive_VitalDataList");
    if (vitalEntries!=null)
    {
      LotroEnum<VitalType> vitalEnum=LotroEnumsRegistry.getInstance().get(VitalType.class);
      for(Object vitalEntry : vitalEntries)
      {
        PropertiesSet vitalProps=(PropertiesSet)vitalEntry;
        float percentage=((Float)vitalProps.getProperty("Effect_Revive_VitalPercent")).floatValue();
        int vitalTypeCode=((Integer)vitalProps.getProperty("Effect_Revive_VitalType")).intValue();
        VitalType vitalType=vitalEnum.getEntry(vitalTypeCode);
        ReviveVitalData vitalData=new ReviveVitalData(vitalType,percentage);
        // Modifiers
        ModPropertyList modifiers=ModifiersUtils.getStatModifiers(vitalProps,"Effect_Revive_VitalPercentAdditiveModifiers");
        vitalData.setModifiers(modifiers);
        effect.addReviveVitalData(vitalData);
      }
    }
  }

  private static boolean getFlag(PropertiesSet props, String propertyName)
  {
    return getFlag(props,propertyName,false);
  }

  private static boolean getFlag(PropertiesSet props, String propertyName, boolean defaultValue)
  {
    Integer intValue=(Integer)props.getProperty(propertyName);
    if (intValue!=null)
    {
      return (intValue.intValue()==1);
    }
    return defaultValue;
  }

  private Proxy<Effect> buildProxy(Integer effectID)
  {
    if ((effectID==null) || (effectID.intValue()==0))
    {
      return null;
    }
    Proxy<Effect> proxy=null;
    Effect effect=getEffect(effectID.intValue());
    if (effect!=null)
    {
      proxy=new Proxy<Effect>();
      proxy.setId(effectID.intValue());
      proxy.setName(effect.getName());
      proxy.setObject(effect);
    }
    return proxy;
  }

  /**
   * Save loaded data.
   */
  public void save()
  {
    // Effects
    EffectXMLWriter w=new EffectXMLWriter();
    List<Effect> effects=EffectsManager.getInstance().getEffects();
    w.write(GeneratedFiles.EFFECTS,effects);
    _i18nUtils.save();
    // Progressions
    ProgressionUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_EFFECTS);
  }
}
