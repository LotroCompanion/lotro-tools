package delta.games.lotro.tools.dat.effects;

import java.io.File;
import java.util.BitSet;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.Interactable;
import delta.games.lotro.common.effects.ApplicationProbability;
import delta.games.lotro.common.effects.DispelByResistEffect;
import delta.games.lotro.common.effects.Effect2;
import delta.games.lotro.common.effects.EffectAndProbability;
import delta.games.lotro.common.effects.EffectDuration;
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
import delta.games.lotro.common.effects.VitalChangeDescription;
import delta.games.lotro.common.effects.VitalOverTimeEffect;
import delta.games.lotro.common.effects.io.xml.EffectXMLWriter2;
import delta.games.lotro.common.enums.CombatState;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.ResistCategory;
import delta.games.lotro.common.enums.SkillType;
import delta.games.lotro.common.math.LinearFunction;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.MainProgressionsMerger;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.ProgressionUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.utils.Proxy;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loads effect data.
 * @author DAM
 */
public class EffectLoader
{
  private static final Logger LOGGER=Logger.getLogger(EffectLoader.class);

  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private I18nUtils _i18nUtils;
  private EffectsManager _effectsMgr;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public EffectLoader(DataFacade facade)
  {
    _facade=facade;
    _i18nUtils=new I18nUtils("effects",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18nUtils);
    _effectsMgr=EffectsManager.getInstance();
  }

  /**
   * Get an effect using its identifier.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found/loaded.
   */
  public Effect2 getEffect(int effectId)
  {
    Effect2 ret=_effectsMgr.getEffectById(effectId);
    if (ret==null)
    {
      ret=loadEffect(effectId);
      _effectsMgr.addEffect(ret);
      
    }
    return ret;
  }

  /**
   * Load an effect.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found.
   */
  private Effect2 loadEffect(int effectId)
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
    /*
    System.out.println("******************");
    ClassDefinition classDef=_facade.getWLibData().getClass(classIndex);
    String className=(classDef!=null)?classDef.getName():"??";
    System.out.println("Effect ID="+effectId+", class="+className+" ("+classIndex+")");
    System.out.println(effectProps.dump().trim());
    */
    Effect2 ret=buildEffect(classIndex);
    ret.setId(effectId);
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
    loadSpecifics(ret,effectProps,classIndex);
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
          LOGGER.warn("Could not build effect icon: "+iconFilename);
        }
      }
    }
    return ret;
  }

  private String getStringProperty(PropertiesSet props, String propertyName)
  {
    return _i18nUtils.getStringProperty(props,propertyName);
  }

  private Effect2 buildEffect(int classDef)
  {
    // Effect PropertyModificationEffect (734) and child classes
    // expect those explicitly handled later
    if ((classDef==734) || (classDef==713) || (classDef==3222) || 
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
    //System.out.println("Unmanaged class: "+classDef);
    return new Effect2();
  }

  private void loadSpecifics(Effect2 effect, PropertiesSet effectProps, int classDef)
  {
    if (effect instanceof ProcEffect)
    {
      loadProcEffect((ProcEffect)effect,effectProps);
    }
    else if (effect instanceof ReactiveVitalEffect)
    {
      loadReactiveVitalEffect((ReactiveVitalEffect)effect,effectProps);
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
    VitalChangeDescription description=loadVitalChangeDescription(effectProps,"Effect_InstantVital_InitialChange");
    if (description==null)
    {
      LOGGER.warn("No value data for vital change!");
    }
    effect.setInstantChangeDescription(description);
    // Stat
    StatDescription stat=DatStatUtils.getStatFromVitalType(vitalType.intValue());
    effect.setStat(stat);
    // Multiplicative?
    Integer multiplicativeInt=(Integer)effectProps.getProperty("Effect_InstantVital_Multiplicative");
    boolean multiplicative=((multiplicativeInt!=null)&&(multiplicativeInt.intValue()==1));
    effect.setMultiplicative(multiplicative);
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
    VitalChangeDescription initialChange=loadVitalChangeDescription(effectProps,"Effect_VitalOverTime_InitialChange");
    effect.setInitialChangeDescription(initialChange);
    VitalChangeDescription overTimeChange=loadVitalChangeDescription(effectProps,"Effect_VitalOverTime_ChangePerInterval");
    if (overTimeChange==null)
    {
      LOGGER.warn("No value data for vital change!");
    }
    effect.setOverTimeChangeDescription(overTimeChange);
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
    Effect2 effect=getEffect(effectID);
    if (effect==null)
    {
      LOGGER.warn("Effect not found: "+effectID);
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
    // Range
    Float range=(Float)effectProps.getProperty("Effect_InstantFellowship_MaxRange");
    if ((range!=null) && (range.floatValue()>0))
    {
      effect.setRange(range.floatValue());
    }
  }

  private EffectGenerator loadGenerator(PropertiesSet generatorProps)
  {
    int effectID=((Integer)generatorProps.getProperty("EffectGenerator_EffectID")).intValue(); 
    Float spellcraft=(Float)generatorProps.getProperty("EffectGenerator_EffectSpellcraft");
    if ((spellcraft!=null) && (spellcraft.floatValue()<0))
    {
      spellcraft=null;
    }
    Effect2 effect=getEffect(effectID);
    EffectGenerator ret=new EffectGenerator(effect,spellcraft);
    return ret;
  }

  private ApplicationProbability getProbability(PropertiesSet effectProps)
  {
    Float probabilityFloat=(Float)effectProps.getProperty("Effect_ConstantApplicationProbability");
    float probability=(probabilityFloat!=null)?probabilityFloat.floatValue():0;
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
    Float duration=(Float)effectProps.getProperty("Effect_Duration_ConstantInterval");
    if (duration!=null)
    {
      if (Math.abs(duration.floatValue())<0.0001)
      {
        duration=null;
      }
    }
    Integer pulseCountInt=(Integer)effectProps.getProperty("Effect_Duration_ConstantPulseCount");
    int pulseCount=(pulseCountInt!=null)?pulseCountInt.intValue():0;
    // Effect_Duration_ConstantInterval_ModifierList: 
    //   #1: Effect_ModifierPropertyList_Entry 268457993 (Item_Guardian_ShieldSpikes_Duration)
    // Effect_Duration_ExpiresInRealTime: false, sometimes true.
    Integer expiresInRealTimeInt=(Integer)effectProps.getProperty("Effect_Duration_ExpiresInRealTime");
    boolean expiresInRealTime=((expiresInRealTimeInt!=null)&&(expiresInRealTimeInt.intValue()==1));
    // Effect_Duration_Permanent: always 1 (true) when set. Only for legacies?
    // Effect_Duration_CombatOnly: always 0 (false)?
    // Effect_Duration_ProgressionInterval: unused?
    // Effect_Duration_ProgressionPulseCount: unused?
    EffectDuration ret=new EffectDuration();
    ret.setDuration(duration);
    ret.setPulseCount(pulseCount);
    ret.setExpiresInRealTime(expiresInRealTime);
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
    //System.out.println("Summoned other ID="+objectID+": "+name+" (type="+weenieType+")");
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
        LOGGER.warn("Unexpected size for combat states: "+states);
      }
      CombatState state=states.get(0);
      effect.setCombatState(state);
    }
    // Grace period:
    // TODO
    // Effect_CombatState_Induce_BreakOutOfState_GracePeriod_Override: 1.0
    // 100% break chance on harm after 1s
    // 3% break chance on damage after 1s
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

  /**
   * Save loaded data.
   */
  public void save()
  {
    // Effects
    EffectXMLWriter2 w=new EffectXMLWriter2();
    List<Effect2> effects=EffectsManager.getInstance().getEffects();
    w.write(GeneratedFiles.EFFECTS,effects);
    _i18nUtils.save();
    // Progressions
    ProgressionUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_EFFECTS);
    // Tmp:
    new MainProgressionsMerger().doIt();
  }
}
