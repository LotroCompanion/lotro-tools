package delta.games.lotro.tools.dat.effects;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.io.streams.IndentableStream;
import delta.games.lotro.common.Interactable;
import delta.games.lotro.common.effects.ApplicationProbability;
import delta.games.lotro.common.effects.DispelByResistEffect;
import delta.games.lotro.common.effects.DumpEffect2;
import delta.games.lotro.common.effects.Effect2;
import delta.games.lotro.common.effects.EffectAndProbability;
import delta.games.lotro.common.effects.EffectDuration;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.effects.GenesisEffect;
import delta.games.lotro.common.effects.Hotspot;
import delta.games.lotro.common.effects.InduceCombatStateEffect;
import delta.games.lotro.common.effects.InstantFellowshipEffect;
import delta.games.lotro.common.effects.InstantVitalEffect;
import delta.games.lotro.common.effects.ProcEffect;
import delta.games.lotro.common.effects.ReactiveVitalChange;
import delta.games.lotro.common.effects.ReactiveVitalEffect;
import delta.games.lotro.common.effects.StatsEffect;
import delta.games.lotro.common.effects.VitalChangeDescription;
import delta.games.lotro.common.effects.VitalOverTimeEffect;
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
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.dat.wlib.ClassDefinition;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.ProgressionUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loads effect data.
 * @author DAM
 */
public class EffectLoader2
{
  private static final Logger LOGGER=Logger.getLogger(EffectLoader2.class);

  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private I18nUtils _i18nUtils;
  private Map<Integer,Effect2> _loadedEffects;
  private DumpEffect2 _dump;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public EffectLoader2(DataFacade facade)
  {
    _facade=facade;
    //_i18nUtils=new I18nUtils("effects2",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18nUtils);
    _loadedEffects=new HashMap<Integer,Effect2>();
    IndentableStream is=new IndentableStream(System.out);
    _dump=new DumpEffect2(is);
  }

  /**
   * Constructor.
   * @param statUtils
   * @param i18nUtils
   */
  public EffectLoader2(DatStatUtils statUtils, I18nUtils i18nUtils)
  {
    _statUtils=statUtils;
    _facade=statUtils.getFacade();
    _i18nUtils=i18nUtils;
    _loadedEffects=new HashMap<Integer,Effect2>();
  }

  /**
   * Get an effect using its identifier.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found/loaded.
   */
  public Effect2 getEffect(int effectId)
  {
    Integer key=Integer.valueOf(effectId);
    if (_loadedEffects.containsKey(key))
    {
      return _loadedEffects.get(key);
    }
    Effect2 ret=loadEffect(effectId);
    _loadedEffects.put(key,ret);
    return ret;
  }

  /**
   * Load an effect.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found.
   */
  public Effect2 loadEffect(int effectId)
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
    System.out.println("******************");
    int classIndex=BufferUtils.getDoubleWordAt(data,4);
    ClassDefinition classDef=_facade.getWLibData().getClass(classIndex);
    String className=(classDef!=null)?classDef.getName():"??";
    System.out.println("Effect ID="+effectId+", class="+className+" ("+classIndex+")");
    System.out.println(effectProps.dump().trim());
    Effect2 ret=new Effect2();
    ret.setId(effectId);
    // Name
    String effectName;
    if (_i18nUtils!=null)
    {
      effectName=_i18nUtils.getNameStringProperty(effectProps,"Effect_Name",effectId,0);
    }
    else
    {
      effectName=DatUtils.getStringProperty(effectProps,"Effect_Name");
    }
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
    ret.setDuration(duration);
    // Aspects
    loadAspects(ret,effectProps,classIndex);
    System.out.println("******");
    _dump.dumpEffect(ret);
    System.out.println("");
    return ret;
  }

  private String getStringProperty(PropertiesSet props, String propertyName)
  {
    String ret;
    if (_i18nUtils!=null)
    {
      ret=_i18nUtils.getStringProperty(props,propertyName);
    }
    else
    {
      ret=DatUtils.getStringProperty(props,propertyName);
    }
    return ret;
  }

  private void loadAspects(Effect2 effect, PropertiesSet effectProps, int classDef)
  {
    StatsEffect statsAspect=loadStatsAspect(effectProps);
    if (statsAspect!=null)
    {
      effect.addAspect(statsAspect);
    }
    ProcEffect procEffect=loadProcAspect(effectProps);
    if (procEffect!=null)
    {
      effect.addAspect(procEffect);
    }
    InstantVitalEffect instantVital=loadInstantVitalEffect(effectProps);
    if (instantVital!=null)
    {
      effect.addAspect(instantVital);
    }
    VitalOverTimeEffect vitalOverTime=loadVitalOverTimeEffect(effectProps);
    if (vitalOverTime!=null)
    {
      effect.addAspect(vitalOverTime);
    }
    InstantFellowshipEffect fellowship=loadInstantFellowshipAspect(effectProps);
    if (fellowship!=null)
    {
      effect.addAspect(fellowship);
    }
    if (classDef==736)
    {
      ReactiveVitalEffect reactiveVitalEffect=loadReactiveVitalChange(effectProps);
      effect.addAspect(reactiveVitalEffect);
    }
    if (classDef==719)
    {
      GenesisEffect genesisEffect=loadGenesisEffect(effectProps);
      effect.addAspect(genesisEffect);
    }
    if (classDef==769)
    {
      InduceCombatStateEffect induceCombatStateEffect=loadInduceCombatStateEffect(effectProps);
      effect.addAspect(induceCombatStateEffect);
    }
    if (classDef==714)
    {
      DispelByResistEffect dispelByResistEffect=loadDispelByResistEffect(effectProps);
      effect.addAspect(dispelByResistEffect);
    }
  }

  private StatsEffect loadStatsAspect(PropertiesSet effectProps)
  {
    // Effect PropertyModificationEffect (734) or MountEffect (2459)
    Object modArray=effectProps.getProperty("Mod_Array");
    if (modArray==null)
    {
      return null;
    }
    StatsEffect ret=null;
    // Stats
    StatsProvider statsProvider=_statUtils.buildStatProviders(effectProps);
    if (statsProvider.getNumberOfStatProviders()>0)
    {
      ret=new StatsEffect();
      ret.setStatsProvider(statsProvider);
    }
    return ret;
  }

  private ProcEffect loadProcAspect(PropertiesSet effectProps)
  {
    Object[] userEffectsList=(Object[])effectProps.getProperty("EffectGenerator_SkillProc_UserEffectList");
    Object[] targetEffectsList=(Object[])effectProps.getProperty("EffectGenerator_SkillProc_TargetEffectList");
    if ((userEffectsList==null) && (targetEffectsList==null))
    {
      return null;
    }
    
    ProcEffect ret=new ProcEffect();
    // TODO Effect_SkillProc_RequiredCombatResult
    // Skill types
    Long skillTypeFlags=(Long)effectProps.getProperty("Effect_SkillProc_SkillTypes");
    if (skillTypeFlags!=null)
    {
      BitSet bitset=BitSetUtils.getBitSetFromFlags(skillTypeFlags.longValue());
      LotroEnum<SkillType> skillTypesEnum=LotroEnumsRegistry.getInstance().get(SkillType.class);
      List<SkillType> skillTypes=skillTypesEnum.getFromBitSet(bitset);
      ret.setSkillTypes(skillTypes);
    }
    // Probability
    Float probability=(Float)effectProps.getProperty("Effect_SkillProc_ProcProbability");
    ret.setProcProbability(probability);
    // Proc'ed effects
    if (userEffectsList!=null)
    {
      for(Object entry : userEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        ret.addProcedEffect(generator);
      }
    }
    if (targetEffectsList!=null)
    {
      for(Object entry : targetEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        ret.addProcedEffect(generator); // TODO Distinguish User/Target
      }
    }
    // Cooldown
    Float cooldown=(Float)effectProps.getProperty("Effect_MinTimeBetweenProcs");
    ret.setCooldown(cooldown);
    return ret;
  }

  private InstantVitalEffect loadInstantVitalEffect(PropertiesSet effectProps)
  {
    Integer vitalType=(Integer)effectProps.getProperty("Effect_BaseVital_VitalType");
    if (vitalType==null)
    {
      return null;
    }
    InstantVitalEffect ret=new InstantVitalEffect();
    VitalChangeDescription description=loadVitalChangeDescription(effectProps,"Effect_InstantVital_InitialChange");
    if (description==null)
    {
      LOGGER.warn("No value data for vital change!");
    }
    ret.setInstantChangeDescription(description);
    // Stat
    StatDescription stat=DatStatUtils.getStatFromVitalType(vitalType.intValue());
    ret.setStat(stat);
    // Multiplicative?
    Integer multiplicativeInt=(Integer)effectProps.getProperty("Effect_InstantVital_Multiplicative");
    boolean multiplicative=((multiplicativeInt!=null)&&(multiplicativeInt.intValue()==1));
    ret.setMultiplicative(multiplicative);
    return ret;
  }

  private VitalOverTimeEffect loadVitalOverTimeEffect(PropertiesSet effectProps)
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
    if (vitalType==null)
    {
      return null;
    }
    VitalOverTimeEffect ret=new VitalOverTimeEffect();
    VitalChangeDescription initialChange=loadVitalChangeDescription(effectProps,"Effect_VitalOverTime_InitialChange");
    ret.setInitialChangeDescription(initialChange);
    VitalChangeDescription overTimeChange=loadVitalChangeDescription(effectProps,"Effect_VitalOverTime_ChangePerInterval");
    if (overTimeChange==null)
    {
      LOGGER.warn("No value data for vital change!");
    }
    ret.setOverTimeChangeDescription(overTimeChange);
    // Stat
    StatDescription stat=DatStatUtils.getStatFromVitalType(vitalType.intValue());
    ret.setStat(stat);
    // Damage type
    LotroEnum<DamageType> damageTypeEnum=LotroEnumsRegistry.getInstance().get(DamageType.class);
    Integer damageTypeCode=(Integer)effectProps.getProperty("Effect_DamageType");
    if ((damageTypeCode!=null) && (damageTypeCode.intValue()!=0))
    {
      DamageType damageType=damageTypeEnum.getEntry(damageTypeCode.intValue());
      ret.setDamageType(damageType);
    }
    return ret;
  }

  private ReactiveVitalEffect loadReactiveVitalChange(PropertiesSet effectProps)
  {
    ReactiveVitalEffect ret=new ReactiveVitalEffect();
    ReactiveVitalChange defenderChange=loadReactiveVitalChange(effectProps,"Effect_ReactiveVital_DefenderVitalChange_");
    ret.setDefenderReactiveVitalChange(defenderChange);
    ReactiveVitalChange attackerChange=loadReactiveVitalChange(effectProps,"Effect_ReactiveVital_AttackerVitalChange_");
    ret.setAttackerReactiveVitalChange(attackerChange);
    EffectAndProbability defenderEffect=loadEffectAndProbability(effectProps,"Effect_ReactiveVital_DefenderEffect_");
    ret.setDefenderEffect(defenderEffect);
    EffectAndProbability attackerEffect=loadEffectAndProbability(effectProps,"Effect_ReactiveVital_AttackerEffect_");
    ret.setAttackerEffect(attackerEffect);
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
        ret.addDamageType(damageType);
      }
    }
    // Damage type override
    Integer damageTypeOverrideCode=(Integer)effectProps.getProperty("Effect_ReactiveVital_AttackerVitalChange_DamageTypeOverride");
    if (damageTypeOverrideCode!=null)
    {
      DamageType damageTypeOverride=damageTypeEnum.getEntry(damageTypeOverrideCode.intValue());
      ret.setAttackerDamageTypeOverride(damageTypeOverride);
    }
    // Remove on proc
    Integer removeOnProcInt=(Integer)effectProps.getProperty("Effect_ReactiveVital_RemoveOnSuccessfulProc");
    boolean removeOnProc=((removeOnProcInt!=null)&&(removeOnProcInt.intValue()==1));
    ret.setRemoveOnProc(removeOnProc);
    return ret;
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

  private InstantFellowshipEffect loadInstantFellowshipAspect(PropertiesSet effectProps)
  {
    Object[] effectsList=(Object[])effectProps.getProperty("EffectGenerator_InstantFellowship_AppliedEffectList");
    if (effectsList==null)
    {
      return null;
    }
    InstantFellowshipEffect ret=new InstantFellowshipEffect();
    // Effects
    for(Object entry : effectsList)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      EffectGenerator generator=loadGenerator(entryProps);
      ret.addEffect(generator);
    }
    // Flags
    Integer raidGroups=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToRaidGroups");
    if (raidGroups!=null)
    {
      ret.setAppliesToRaidGroups(raidGroups.intValue()==1);
    }
    Integer pets=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToPets");
    if (pets!=null)
    {
      ret.setAppliesToPets(pets.intValue()==1);
    }
    Integer target=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToTarget");
    if (target!=null)
    {
      ret.setAppliesToTarget(target.intValue()==1);
    }
    // Range
    Float range=(Float)effectProps.getProperty("Effect_InstantFellowship_MaxRange");
    if ((range!=null) && (range.floatValue()>0))
    {
      ret.setRange(range.floatValue());
    }
    return ret;
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

  private GenesisEffect loadGenesisEffect(PropertiesSet effectProps)
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
    GenesisEffect ret=new GenesisEffect();
    handleSummonedObject(summonedObjectID,ret);
    if (summonDuration!=null)
    {
      ret.setDuration(summonDuration.floatValue());
    }
    if ((permanent!=null) && (permanent.intValue()==1))
    {
      ret.setPermanent();
    }
    return ret;
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
    effect.setInteractable(interactable);
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

  private InduceCombatStateEffect loadInduceCombatStateEffect(PropertiesSet effectProps)
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
    InduceCombatStateEffect ret=new InduceCombatStateEffect();
    // Constant duration
    Float duration=(Float)effectProps.getProperty("Effect_InduceCombatState_ConstantDuration");
    if (duration!=null)
    {
      ret.setDuration(duration.floatValue());
    }
    // Duration function
    PropertiesSet durationProps=(PropertiesSet)effectProps.getProperty("Effect_InduceCombatState_VariableDuration");
    if (durationProps!=null)
    {
      LinearFunction function=loadLinearFunction(durationProps);
      ret.setDurationFunction(function);
    }
    // Combat state
    int bitSetValue=((Integer)effectProps.getProperty("Effect_InduceCombatState_StateToInduce")).intValue();
    LotroEnum<CombatState> damageTypeEnum=LotroEnumsRegistry.getInstance().get(CombatState.class);
    BitSet bitSet=BitSetUtils.getBitSetFromFlags(bitSetValue);
    List<CombatState> states=damageTypeEnum.getFromBitSet(bitSet);
    if (states.size()!=1)
    {
      LOGGER.warn("Unexpected size for combat states: "+states);
    }
    CombatState state=states.get(0);
    ret.setCombatState(state);
    // Grace period:
    // TODO
    // Effect_CombatState_Induce_BreakOutOfState_GracePeriod_Override: 1.0
    // 100% break chance on harm after 1s
    // 3% break chance on damage after 1s
    return ret;
  }

  private LinearFunction loadLinearFunction(PropertiesSet props)
  {
    float minX=((Float)props.getProperty("Effect_VariableSpellcraftMin")).floatValue();
    float minY=((Float)props.getProperty("Effect_VariableMin")).floatValue();
    float maxX=((Float)props.getProperty("Effect_VariableSpellcraftMax")).floatValue();
    float maxY=((Float)props.getProperty("Effect_VariableMax")).floatValue();
    return new LinearFunction(minX,maxX,minY,maxY);
  }

  private DispelByResistEffect loadDispelByResistEffect(PropertiesSet effectProps)
  {
  /*
Effect ID=1879157351, class=DispelByResistEffect (714)
Effect_DispelByResist_MaximumDispelCount: 1
Effect_DispelByResist_ResistCategoryFilter: 8 (Wound)
Effect_DispelByResist_UseStrengthRestriction: 1
 */

    DispelByResistEffect ret=new DispelByResistEffect();
    // Dispel count
    Integer dispelCountInt=(Integer)effectProps.getProperty("Effect_DispelByResist_MaximumDispelCount");
    int dispelCount=(dispelCountInt!=null)?dispelCountInt.intValue():-1;
    ret.setMaxDispelCount(dispelCount);
    // Resist Categories
    int categoriesCode=((Integer)effectProps.getProperty("Effect_DispelByResist_ResistCategoryFilter")).intValue();
    LotroEnum<ResistCategory> categoriesEnum=LotroEnumsRegistry.getInstance().get(ResistCategory.class);
    BitSet bitset=BitSetUtils.getBitSetFromFlags(categoriesCode);
    List<ResistCategory> categories=categoriesEnum.getFromBitSet(bitset);
    for(ResistCategory category : categories)
    {
      ret.addResistCategory(category);
    }
    // Strength restriction
    Integer useStrengthResitriction=(Integer)effectProps.getProperty("Effect_DispelByResist_UseStrengthRestriction");
    if ((useStrengthResitriction!=null) && (useStrengthResitriction.intValue()==1))
    {
      ret.setUseStrengthRestriction(true);
    }
    // Strength offset:
    Float strengthOffset=(Float)effectProps.getProperty("Effect_DispelByResist_StrengthRestrictionOffset");
    if (strengthOffset!=null)
    {
      ret.setStrengthOffset(Integer.valueOf(strengthOffset.intValue()));
    }
    return ret;
  }
}
