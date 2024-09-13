package delta.games.lotro.tools.extraction.skills;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import delta.games.lotro.character.skills.SkillCostData;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillDetails;
import delta.games.lotro.character.skills.SkillFlags;
import delta.games.lotro.character.skills.SkillGambitData;
import delta.games.lotro.character.skills.SkillPipData;
import delta.games.lotro.character.skills.SkillVitalCost;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.skills.attack.SkillAttack;
import delta.games.lotro.character.skills.attack.SkillAttackFlags;
import delta.games.lotro.character.skills.geometry.Arc;
import delta.games.lotro.character.skills.geometry.Box;
import delta.games.lotro.character.skills.geometry.SkillGeometry;
import delta.games.lotro.character.skills.geometry.Sphere;
import delta.games.lotro.common.enums.AreaEffectAnchorType;
import delta.games.lotro.common.enums.DamageQualifier;
import delta.games.lotro.common.enums.DamageQualifiers;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.PipType;
import delta.games.lotro.common.enums.ResistCategory;
import delta.games.lotro.common.enums.SkillDisplayType;
import delta.games.lotro.common.enums.VitalType;
import delta.games.lotro.common.inductions.Induction;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.common.progressions.ProgressionUtils;
import delta.games.lotro.tools.extraction.effects.EffectLoader;
import delta.games.lotro.tools.utils.DataFacadeBuilder;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loader for skill details.
 * @author DAM
 */
public class SkillDetailsLoader
{
  private DataFacade _facade;
  private EffectLoader _effectsLoader;
  private Map<Integer,Induction> _inductions;
  private Map<Integer,Float> _channelingDurations;
  private LotroEnum<ResistCategory> _resistCategoryEnum;
  private LotroEnum<SkillDisplayType> _skillDisplayTypeEnum;
  private LotroEnum<PipType> _pipTypeEnum;
  private LotroEnum<AreaEffectAnchorType> _areaEffectAnchorTypeEnum;

  /**
   * Constructor.
   * @param facade Facade.
   * @param effectsLoader Effects loader.
   */
  public SkillDetailsLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectsLoader=effectsLoader;
    _inductions=new HashMap<Integer,Induction>();
    _channelingDurations=new HashMap<Integer,Float>();
    LotroEnumsRegistry lotroEnumRegistry=LotroEnumsRegistry.getInstance();
    _resistCategoryEnum=lotroEnumRegistry.get(ResistCategory.class);
    _skillDisplayTypeEnum=lotroEnumRegistry.get(SkillDisplayType.class);
    _pipTypeEnum=lotroEnumRegistry.get(PipType.class);
    _areaEffectAnchorTypeEnum=lotroEnumRegistry.get(AreaEffectAnchorType.class);
  }

  private Induction getInduction(int skillInductionActionID)
  {
    Integer key=Integer.valueOf(skillInductionActionID);
    Induction ret=_inductions.get(key);
    if (ret==null)
    {
      ret=loadInduction(skillInductionActionID);
      _inductions.put(key,ret);
    }
    return ret;
  }

  private Float getChannelingDuration(int skillToggleChannelingStateID)
  {
    Integer key=Integer.valueOf(skillToggleChannelingStateID);
    Float ret=_channelingDurations.get(key);
    if (ret==null)
    {
      PropertiesSet props=_facade.loadProperties(skillToggleChannelingStateID+DATConstants.DBPROPERTIES_OFFSET);
      ret=(Float)props.getProperty("Channeling_Duration");
      _channelingDurations.put(key,ret);
    }
    return ret;
  }

  private void handleSkill(int skillID)
  {
    PropertiesSet props=_facade.loadProperties(skillID+DATConstants.DBPROPERTIES_OFFSET);
    if (props==null)
    {
      return;
    }

    SkillDetails ret=new SkillDetails();

    // Duration
    Float actionDurationContribution=(Float)props.getProperty("Skill_ActionDurationContribution");
    ret.setActionDurationContribution(actionDurationContribution);
    // Induction
    Integer inductionActionID=(Integer)props.getProperty("Skill_InductionAction");
    if (inductionActionID!=null)
    {
      Induction induction=getInduction(inductionActionID.intValue());
      ret.setInduction(induction);
    }
    // Channeling duration
    Float channelingDuration=null;
    Float channelingDurationOverride=(Float)props.getProperty("Skill_Toggle_ChannelingDurationOverride");
    if (channelingDurationOverride==null)
    {
      Integer channelingState=(Integer)props.getProperty("Skill_Toggle_ChannelingState");
      if (channelingState!=null)
      {
        channelingDuration=getChannelingDuration(channelingState.intValue());
      }
    }
    else
    {
      channelingDuration=channelingDurationOverride;
    }
    ret.setChannelingDuration(channelingDuration);

    // Cooldown
    Float skillRecoveryTime=(Float)props.getProperty("Skill_SkillRecoveryTime");
    ret.setCooldown(skillRecoveryTime);

    // Skill flags
    // Fast?
    Integer ignoresResetTimeInt=(Integer)props.getProperty("Skill_IgnoresResetTime");
    boolean fast=(ignoresResetTimeInt!=null)&&(ignoresResetTimeInt.intValue()==1);
    ret.setFlag(SkillFlags.FAST,fast);
    // Immediate?
    Integer immediateInt=(Integer)props.getProperty("Skill_Immediate"); //Immediate
    boolean immediate=(immediateInt!=null)&&(immediateInt.intValue()==1);
    ret.setFlag(SkillFlags.IMMEDIATE,immediate);
    WStateDataSet dataSet=_facade.loadWState(skillID);
    ClassInstance skillData=(ClassInstance) dataSet.getValueForReference(0);
    Integer usesMagicInt=(Integer)skillData.getAttributeValue("m_bUsesMagic");
    boolean usesMagic=(usesMagicInt!=null)&&(usesMagicInt.intValue()==1);
    ret.setFlag(SkillFlags.USES_MAGIC,usesMagic);
    Integer usesMeleeInt=(Integer)skillData.getAttributeValue("m_bUsesMelee");
    boolean usesMelee=(usesMeleeInt!=null)&&(usesMeleeInt.intValue()==1);
    ret.setFlag(SkillFlags.USES_MELEE,usesMelee);
    Integer usesRangedInt=(Integer)skillData.getAttributeValue("m_bUsesRanged");
    boolean usesRanged=(usesRangedInt!=null)&&(usesRangedInt.intValue()==1);
    ret.setFlag(SkillFlags.USES_RANGED,usesRanged);

    // Geometry
    SkillGeometry geometry=loadGeometry(props);
    ret.setGeometry(geometry);
    // Targets
    Integer areaEffectMaxTargets=(Integer)props.getProperty("Skill_AreaEffectMaxTargets");
    ret.setMaxTargets(areaEffectMaxTargets);
    ModPropertyList areaEffectMaxTargetsMods=getStatModifiers(props,"Skill_AreaEffectMaxTargets_Mod_Array");
    ret.setMaxTargetsMods(areaEffectMaxTargetsMods);

    // UsesToggle
    Integer toggleHookNumber=(Integer)props.getProperty("Skill_Toggle_Hook_Number");
    boolean isToggle=(toggleHookNumber!=null)&&(toggleHookNumber.intValue()==1);
    ret.setFlag(SkillFlags.IS_TOGGLE,isToggle);

    Integer resistCategoryCode=(Integer)props.getProperty("Skill_Resist_Category");
    if (resistCategoryCode!=null)
    {
      ResistCategory resistCategory=_resistCategoryEnum.getEntry(resistCategoryCode.intValue());
      ret.setResistCategory(resistCategory);
    }
    BitSet displayTypesBitSet=(BitSet)props.getProperty("Skill_DisplaySkillType");
    if (displayTypesBitSet!=null)
    {
      List<SkillDisplayType> displayTypes=_skillDisplayTypeEnum.getFromBitSet(displayTypesBitSet);
      ret.setDisplayTypes(new HashSet<SkillDisplayType>(displayTypes));
    }

    /*List<Object> criticalEffectList=*/getSkillEffectList(props,"Skill_CriticalEffectList"); //Apply to self on critical:
    /*List<Object> toggleEffectList=*/GetSkillEffectList(props,"Skill_Toggle_Effect_List","Skill_Toggle_Effect_ImplementUsage");
    /*List<Object> toggleUserEffectList=*/GetSkillEffectList(props,"Skill_Toggle_User_Effect_List","Skill_Toggle_User_Effect_ImplementUsage");
    /*List<Object> userEffectList=*/getSkillEffectList(props,"Skill_UserEffectList");

    SkillCostData costData=new SkillCostData();
    LotroEnum<VitalType> vitalTypeEnum=LotroEnumsRegistry.getInstance().get(VitalType.class);
    VitalType morale=vitalTypeEnum.getEntry(1);
    SkillVitalCost moraleCostPerSecond=getVitalCostList(props,"Skill_Toggle_VitalCostPerSecondList",morale);
    costData.setMoraleCostPerSecond(moraleCostPerSecond);
    VitalType power=vitalTypeEnum.getEntry(2);
    SkillVitalCost powerCostPerSecond=getVitalCostList(props,"Skill_Toggle_VitalCostPerSecondList",power);
    costData.setPowerCostPerSecond(powerCostPerSecond);
    SkillVitalCost moraleCost=getVitalCostList(props,"Skill_VitalCostList",morale);
    costData.setMoraleCost(moraleCost);
    SkillVitalCost powerCost=getVitalCostList(props,"Skill_VitalCostList",power);
    costData.setPowerCost(powerCost);
    ret.setCostData(costData);

    // PIP
    if (props.hasProperty("Skill_Pip_AffectedType"))
    {
      SkillPipData pipData=loadPipData(props);
      ret.setPIPData(pipData);
    }
    // Gambit
    SkillGambitData gambitData=loadGambitData(props);
    ret.setGambitData(gambitData);

    Object[] attackHookObjs=(Object[])props.getProperty("Skill_AttackHookList");
    if (attackHookObjs != null)
    {
      for (Object attackHookObj : attackHookObjs)
      {
        PropertiesSet attackHookInfo=(PropertiesSet)attackHookObj;
        SkillAttack attack=loadAttackHook(attackHookInfo);
      }
    }
  }

  private SkillGeometry loadGeometry(PropertiesSet props)
  {
    SkillGeometry ret=new SkillGeometry();
    Float Skill_AEDetectionVolume_ArcRadius=(Float)props.getProperty("Skill_AEDetectionVolume_ArcRadius");
    Float Skill_AEDetectionVolume_BoxLength=(Float)props.getProperty("Skill_AEDetectionVolume_BoxLength");
    Float Skill_AEDetectionVolume_SphereRadius=(Float)props.getProperty("Skill_AEDetectionVolume_SphereRadius");
    if ((Skill_AEDetectionVolume_ArcRadius != null) && (Skill_AEDetectionVolume_ArcRadius.floatValue()!=0.0f))
    {
      Arc arc=new Arc();
      arc.setRadius(Skill_AEDetectionVolume_ArcRadius.floatValue());
      Float degrees=(Float)props.getProperty("Skill_AEDetectionVolume_ArcDegrees");
      arc.setDegrees(degrees.floatValue());
      Float heading=(Float)props.getProperty("Skill_AEDetectionVolume_HeadingOffset");
      arc.setHeadingOffset(heading.floatValue());
      ret.setShape(arc);
    }
    else if ((Skill_AEDetectionVolume_BoxLength != null) && (Skill_AEDetectionVolume_BoxLength.floatValue()!=0.0f))
    {
      Box box=new Box();
      box.setLength(Skill_AEDetectionVolume_BoxLength.floatValue());
      Float width=(Float)props.getProperty("Skill_AEDetectionVolume_BoxWidth");
      box.setWidth(width.floatValue());
      ret.setShape(box);
    }
    else if ((Skill_AEDetectionVolume_SphereRadius != null) && (Skill_AEDetectionVolume_SphereRadius.floatValue()!=0.0f))
    {
      Sphere sphere=new Sphere();
      sphere.setRadius(Skill_AEDetectionVolume_SphereRadius.floatValue());
      ret.setShape(sphere);
    }
    Integer Skill_AreaEffectDetectionAnchor=(Integer)props.getProperty("Skill_AreaEffectDetectionAnchor");
    if ((Skill_AreaEffectDetectionAnchor!=null) && (Skill_AreaEffectDetectionAnchor.intValue()!=0))
    {
      AreaEffectAnchorType anchorType=_areaEffectAnchorTypeEnum.getEntry(Skill_AreaEffectDetectionAnchor.intValue());
      ret.setDetectionAnchor(anchorType);
    }
    Float minRange=(Float)props.getProperty("Skill_MinRange");
    ret.setMinRange(minRange);
    Float maxRange=(Float)props.getProperty("Skill_MaxRange");
    ret.setMaxRange(maxRange.floatValue());
    ModPropertyList maxRangeModifiers=getStatModifiers(props,"Skill_MaxRange_ModifierArray");
    ret.setMaxRangeMods(maxRangeModifiers);
    return ret;
  }

  @SuppressWarnings("unused")
  private SkillAttack loadAttackHook(PropertiesSet attackHookProperties)
  {
    SkillAttack ret=new SkillAttack();
    // Damage qualifier
    DamageQualifier damageQualifier=null;
    Integer damageQualifierCode=(Integer)attackHookProperties.getProperty("Skill_AttackHook_DamageQualifier");
    if ((damageQualifierCode!=null) && (damageQualifierCode.intValue()>0))
    {
      damageQualifier=DamageQualifiers.getByCode(damageQualifierCode.intValue());
    }
    ret.setDamageQualifier(damageQualifier);

    // Effects
    Object criticalTargetEffectList=getSkillEffectList(attackHookProperties,"Skill_AttackHook_CriticalTargetEffectList");
    Object positionalTargetEffectList=getSkillEffectList(attackHookProperties,"Skill_AttackHook_PositionalTargetEffectList");
    Object superCriticalTargetEffectList=getSkillEffectList(attackHookProperties,"Skill_AttackHook_SuperCriticalTargetEffectList");
    Object targetEffectList=getSkillEffectList(attackHookProperties,"Skill_AttackHook_TargetEffectList");

    // Modifiers
    ModPropertyList dpsMods=getStatModifiers(attackHookProperties,"Skill_AttackHook_DPSAddMod_Mod_Array");
    ret.setDPSMods(dpsMods);
    ModPropertyList maxDamageMods=getStatModifiers(attackHookProperties,"Skill_AttackHook_HookDamageMax_Mod_Array");
    ret.setMaxDamageMods(maxDamageMods);
    ModPropertyList damageModifiersMods=getStatModifiers(attackHookProperties,"Skill_AttackHook_HookDamageModifier_Mod_Array");
    ret.setDamageModifiersMods(damageModifiersMods);

    // Damage type
    DamageType damageType=null;
    Integer damageTypeCode=(Integer)attackHookProperties.getProperty("Skill_AttackHook_DamageType");
    if ((damageTypeCode!=null) && (damageTypeCode.intValue()>0))
    {
      damageType=DamageType.getDamageTypeByCode(damageTypeCode.intValue());
    }
    ret.setDamageType(damageType);

    // DPS
    Float damageContributionMultiplier=(Float)attackHookProperties.getProperty("Skill_AttackHook_DamageAddContributionMultiplier");
    ret.setDamageContributionMultiplier(damageContributionMultiplier);
    Integer dpsModProgression=(Integer)attackHookProperties.getProperty("Skill_AttackHook_DPSAddMod_Progression");
    if ((dpsModProgression!=null) && (dpsModProgression.intValue()>0))
    {
      Progression progression=ProgressionUtils.getProgression(_facade,dpsModProgression.intValue());
      ret.setDPSModProgression(progression);
    }
    // Damage
    Float maxDamage=(Float)attackHookProperties.getProperty("Skill_AttackHook_HookDamageMax");
    ret.setMaxDamage(maxDamage.floatValue());
    Float maxDamageVariance=(Float)attackHookProperties.getProperty("Skill_AttackHook_HookDamageMaxVariance");
    ret.setMaxDamageVariance(maxDamageVariance.floatValue());
    Integer maxDamageProgression=(Integer)attackHookProperties.getProperty("Skill_AttackHook_HookDamageMax_Progression");
    if ((maxDamageProgression!=null) && (maxDamageProgression.intValue()>0))
    {
      Progression progression=ProgressionUtils.getProgression(_facade,maxDamageProgression.intValue());
      ret.setMaxDamageProgression(progression);
    }
    Float damageModifier=(Float)attackHookProperties.getProperty("Skill_AttackHook_HookDamageModifier");
    ret.setDamageModifier(damageModifier.floatValue());
    Float implementContributionMultiplier=(Float)attackHookProperties.getProperty("Skill_AttackHook_ImplementContributionMultiplier");
    ret.setImplementContributionMultiplier(implementContributionMultiplier);
    // Flags
    Integer usesNaturalInt=(Integer)attackHookProperties.getProperty("Skill_AttackHook_UsesNatural");
    boolean usesNatural=((usesNaturalInt!=null) && (usesNaturalInt.intValue()==1));
    ret.setFlag(SkillAttackFlags.NATURAL,usesNatural);
    Integer usesPrimaryImplementInt=(Integer)attackHookProperties.getProperty("Skill_AttackHook_UsesPrimaryImplement");
    boolean usesPrimary=((usesPrimaryImplementInt!=null) && (usesPrimaryImplementInt.intValue()==1));
    ret.setFlag(SkillAttackFlags.PRIMARY,usesPrimary);
    Integer usesRangedImplementInt=(Integer)attackHookProperties.getProperty("Skill_AttackHook_UsesRangedImplement");
    boolean usesRanged=((usesRangedImplementInt!=null) && (usesRangedImplementInt.intValue()==1));
    ret.setFlag(SkillAttackFlags.RANGED,usesRanged);
    Integer usesSecondaryImplementInt=(Integer)attackHookProperties.getProperty("Skill_AttackHook_UsesSecondaryImplement");
    boolean usesSecondary=((usesSecondaryImplementInt!=null) && (usesSecondaryImplementInt.intValue()==1));
    ret.setFlag(SkillAttackFlags.SECONDARY,usesSecondary);
    Integer usesTacticalInt=(Integer)attackHookProperties.getProperty("Skill_AttackHook_UsesTactical");
    boolean usesTactical=((usesTacticalInt!=null) && (usesTacticalInt.intValue()==1));
    ret.setFlag(SkillAttackFlags.TACTICAL,usesTactical);
    System.out.println(ret);
    return ret;
  }

  private SkillPipData loadPipData(PropertiesSet props)
  {
    Integer affectedTypeCode=(Integer)props.getProperty("Skill_Pip_AffectedType");
    PipType type=null;
    if ((affectedTypeCode!=null) && (affectedTypeCode.intValue()!=0))
    {
      type=_pipTypeEnum.getEntry(affectedTypeCode.intValue());
    }
    if (type==null)
    {
      return null;
    }
    SkillPipData ret=new SkillPipData(type);
    Integer change=(Integer)props.getProperty("Skill_Pip_Change");
    ret.setChange(change);
    ModPropertyList changeMods=getStatModifiers(props,"Skill_PipChange_Mod_Array");
    ret.setChangeMods(changeMods);
    Integer requiredMinValue=(Integer)props.getProperty("Skill_Pip_RequiredMinValue");
    ret.setRequiredMinValue(requiredMinValue);
    ModPropertyList requiredMinValueMods=getStatModifiers(props,"Skill_PipRequiredMin_Mod_Array");
    ret.setRequiredMinValueMods(requiredMinValueMods);
    Integer requiredMaxValue=(Integer)props.getProperty("Skill_Pip_RequiredMaxValue");
    ret.setRequiredMaxValue(requiredMaxValue);
    ModPropertyList requiredMaxValueMods=getStatModifiers(props,"Skill_PipRequiredMax_Mod_Array");
    ret.setRequiredMaxValueMods(requiredMaxValueMods);
    Integer towardHome=(Integer)props.getProperty("Skill_Pip_Toward_Home");
    ret.setTowardHome(towardHome);
    Integer changePerInterval=(Integer)props.getProperty("Skill_Toggle_PipChangePerInterval");
    ret.setChangePerInterval(changePerInterval);
    ModPropertyList changePerIntervalMods=getStatModifiers(props,"Skill_TogglePipChangePerInterval_Mod_Array");
    ret.setSecondsPerPipChangeMods(changePerIntervalMods);
    Float secondsPerPipChange=(Float)props.getProperty("Skill_Toggle_SecondsPerPipChange");
    ret.setSecondsPerPipChange(secondsPerPipChange);
    ModPropertyList secondsPerPipChangeMods=getStatModifiers(props,"Skill_ToggleSecondsPerPipChange_Mod_Array");
    ret.setSecondsPerPipChangeMods(secondsPerPipChangeMods);
    return ret;
  }

  private SkillGambitData loadGambitData(PropertiesSet props)
  {
    SkillGambitData ret=new SkillGambitData();
    Integer Skill_AppliedGambit=(Integer)props.getProperty("Skill_AppliedGambit"); //gambits to add (max 5 in nibbles, least significant first)
    Integer Skill_AppliedGambitIconCount=(Integer)props.getProperty("Skill_AppliedGambitIconCount"); //count of gambits to add (max 5 total, but only 1 or 2 at a time in reality)
    Integer Skill_RemovedGambits=(Integer)props.getProperty("Skill_RemovedGambits"); //count of gambits to remove (lifo) -1 --Clears All Gambits, 1 --Clears 1 Gambit
    Integer Skill_RequiredGambit=(Integer)props.getProperty("Skill_RequiredGambit"); //gambits required (max 5 in nibbles, least significant first)
    Integer Skill_RequiresGambits=(Integer)props.getProperty("Skill_RequiresGambits");  //1=yes, see Skill_RequiredGambit nibbles or "Requires: an active Gambit" if none required (skill:Gambit Default)
    if (Skill_AppliedGambit != null ||
      Skill_AppliedGambitIconCount != null ||
      Skill_RemovedGambits != null ||
      Skill_RequiredGambit != null ||
      Skill_RequiresGambits != null)
    {
      // TODO
    }
    return ret;
  }

  private List<Object> getSkillEffectList(PropertiesSet props, String sSkillEffectListPropName)
  {
    return GetSkillEffectList(props,sSkillEffectListPropName,null);
  }
  
  private List<Object> GetSkillEffectList(PropertiesSet props, String sSkillEffectListPropName, String sEffectImplementUsagePropName)
  {
    Object[] effectList=(Object[])props.getProperty(sSkillEffectListPropName);
    if (effectList == null)
    {
      return null;
    }
    Integer effectImplementUsage=0;
    if (sEffectImplementUsagePropName != null)
    {
      effectImplementUsage=(Integer)props.getProperty(sEffectImplementUsagePropName);
      if (effectImplementUsage == null) effectImplementUsage=0;
    }
    // TODO A list
    for (Object Skill_EffectObj : effectList)
    {
      PropertiesSet effectData=(PropertiesSet)Skill_EffectObj;
      Integer skillEffectID=(Integer)effectData.getProperty("Skill_Effect");
      if ((skillEffectID == null) || (skillEffectID.intValue()<=0))
      {
        continue;
      }
      _effectsLoader.getEffect(skillEffectID.intValue());
      // Duration
      Float skillEffectDuration=(Float)effectData.getProperty("Skill_EffectDuration");
      // Implement
      if (sEffectImplementUsagePropName==null)
      {
        effectImplementUsage=(Integer)effectData.getProperty("Skill_EffectImplementUsage");
      }
      // Spellcraft
      Float effectSpellcraft=(Float)effectData.getProperty("Skill_EffectSpellcraft");
    }
    return null;
  }

  private static ModPropertyList getStatModifiers(PropertiesSet props, String statModArrayPropName)
  {
    Object[] statIDObjs=(Object[])props.getProperty(statModArrayPropName);
    if (statIDObjs == null)
    {
      return null;
    }
    boolean doKeep=false;
    ModPropertyList ret=new ModPropertyList();
    for (Object statIDObj : statIDObjs)
    {
      Integer statID=(Integer)statIDObj;
      if ((statID != null) && (statID.intValue()>0))
      {
        ret.addID(statID.intValue());
        doKeep=true;
      }
    }
    return doKeep?ret:null;
  }

  private SkillVitalCost getVitalCostList(PropertiesSet props, String vitalCostListPropName, VitalType vitalType)
  {
    Object[] vitalCostList=(Object[])props.getProperty(vitalCostListPropName);
    if (vitalCostList==null)
    {
      return null;
    }
    SkillVitalCost cost=null;
    for (Object vitalCostObj : vitalCostList)
    {
      PropertiesSet vitalCostProps=(PropertiesSet)vitalCostObj;
      int readVitalType=((Integer)vitalCostProps.getProperty("Skill_Vital_Type")).intValue();
      if (readVitalType==vitalType.getCode())
      {
        Integer consumesAllInt=(Integer)vitalCostProps.getProperty("Skill_Vital_Consumes_All");
        boolean consumesAll=((consumesAllInt!=null) && (consumesAllInt.intValue()==1));
        ModPropertyList propertyMods=getStatModifiers(vitalCostProps,"Skill_Vital_Mod_Array");
        Float percentage=(Float)vitalCostProps.getProperty("Skill_Vital_Percent");
        if ((percentage!=null) && (percentage.floatValue()<=0))
        {
          percentage=null;
        }
        Float points=(Float)vitalCostProps.getProperty("Skill_Vital_Points");
        if ((points!=null) && (points.floatValue()<=0))
        {
          points=null;
        }
        Progression progression=null;
        Integer progressionID=(Integer)vitalCostProps.getProperty("Skill_Vital_Points_Progression");
        if ((progressionID!=null) && (progressionID.intValue()>0))
        {
          progression=ProgressionUtils.getProgression(_facade,progressionID.intValue());
        }
        if ((consumesAll) || (propertyMods!=null) || (percentage!=null) || (points!=null) || (progression!=null))
        {
          cost=new SkillVitalCost(vitalType);
          cost.setConsumesAll(consumesAll);
          cost.setVitalMods(propertyMods);
          cost.setPercentage(percentage);
          cost.setPoints(points);
          cost.setPointsProgression(progression);
        }
        return cost;
      }
    }
    return cost;
  }

  private Induction loadInduction(int inductionID)
  {
    PropertiesSet props=_facade.loadProperties(inductionID+DATConstants.DBPROPERTIES_OFFSET);
    Induction ret=new Induction(inductionID);
    float duration=((Float)props.getProperty("Induction_Duration")).floatValue();
    ret.setDuration(duration);
    ModPropertyList addMods=getStatModifiers(props,"Induction_Duration_AdditiveModifierList");
    ModPropertyList multiplyMods=getStatModifiers(props,"Induction_Duration_MultiplierModifierList");
    return ret;
  }

  private void loadPipDB()
  {
    PropertiesSet props=_facade.loadProperties(1879048735+DATConstants.DBPROPERTIES_OFFSET);
    Object[] PipControl_Directory=(Object[])(props.getProperty("PipControl_Directory"));
    for(Object PipControl : PipControl_Directory)
    {
      int pipControlID=((Integer)PipControl).intValue();
      props=_facade.loadProperties(pipControlID+DATConstants.DBPROPERTIES_OFFSET);
      Integer pipType=(Integer)props.getProperty("Pip_Type");
      String pipName=(String)props.getProperty("Pip_Name");
      Integer pipMin=(Integer)props.getProperty("Pip_Min");
      Integer pipMax=(Integer)props.getProperty("Pip_Max");
      Integer pipHome=(Integer)props.getProperty("Pip_Home");
      Integer minIcon=(Integer)props.getProperty("Pip_Examination_Min_Icon");
      Integer maxIcon=(Integer)props.getProperty("Pip_Examination_Max_Icon");
      Integer examinationHomeIcon=(Integer)props.getProperty("Pip_Examination_Home_Icon");
    }
  }

  private void doIt()
  {
    for(SkillDescription skill : SkillsManager.getInstance().getAll())
    {
      System.out.println(skill);
      handleSkill(skill.getIdentifier());
    }
  }

  public static void main(String[] args)
  {
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    SkillDetailsLoader skillsLoader=new SkillDetailsLoader(facade,effectsLoader);
    skillsLoader.doIt();
  }
}
