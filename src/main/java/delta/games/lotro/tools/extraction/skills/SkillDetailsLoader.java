package delta.games.lotro.tools.extraction.skills;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.skills.SingleTypeSkillEffectsManager;
import delta.games.lotro.character.skills.SkillCostData;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillDetails;
import delta.games.lotro.character.skills.SkillEffectGenerator;
import delta.games.lotro.character.skills.SkillEffectType;
import delta.games.lotro.character.skills.SkillEffectsManager;
import delta.games.lotro.character.skills.SkillFlags;
import delta.games.lotro.character.skills.SkillGambitData;
import delta.games.lotro.character.skills.SkillPipData;
import delta.games.lotro.character.skills.SkillVitalCost;
import delta.games.lotro.character.skills.attack.SkillAttack;
import delta.games.lotro.character.skills.attack.SkillAttackFlags;
import delta.games.lotro.character.skills.attack.SkillAttacks;
import delta.games.lotro.character.skills.geometry.Arc;
import delta.games.lotro.character.skills.geometry.Box;
import delta.games.lotro.character.skills.geometry.SkillGeometry;
import delta.games.lotro.character.skills.geometry.SkillPositionalData;
import delta.games.lotro.character.skills.geometry.Sphere;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.AreaEffectAnchorType;
import delta.games.lotro.common.enums.DamageQualifier;
import delta.games.lotro.common.enums.DamageQualifiers;
import delta.games.lotro.common.enums.GambitIconType;
import delta.games.lotro.common.enums.ImplementUsageType;
import delta.games.lotro.common.enums.ImplementUsageTypes;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.PipType;
import delta.games.lotro.common.enums.ResistCategory;
import delta.games.lotro.common.enums.SkillDisplayType;
import delta.games.lotro.common.enums.VitalType;
import delta.games.lotro.common.inductions.Induction;
import delta.games.lotro.common.inductions.io.xml.InductionXMLWriter;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.items.DamageType;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.progressions.ProgressionUtils;
import delta.games.lotro.tools.extraction.effects.EffectLoader;
import delta.games.lotro.tools.extraction.effects.SkillEffectsLoader;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loader for skill details.
 * @author DAM
 */
public class SkillDetailsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(SkillDetailsLoader.class);

  private DataFacade _facade;
  private SkillEffectsLoader _effectsLoader;
  private Map<Integer,Induction> _inductions;
  private Map<Integer,Float> _channelingDurations;
  private LotroEnum<ResistCategory> _resistCategoryEnum;
  private LotroEnum<SkillDisplayType> _skillDisplayTypeEnum;
  private LotroEnum<PipType> _pipTypeEnum;
  private LotroEnum<AreaEffectAnchorType> _areaEffectAnchorTypeEnum;
  private LotroEnum<GambitIconType> _gambitIconTypeEnum;

  /**
   * Constructor.
   * @param facade Facade.
   * @param effectsLoader Effects loader.
   */
  public SkillDetailsLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectsLoader=new SkillEffectsLoader(effectsLoader);
    _inductions=new HashMap<Integer,Induction>();
    _channelingDurations=new HashMap<Integer,Float>();
    LotroEnumsRegistry lotroEnumRegistry=LotroEnumsRegistry.getInstance();
    _resistCategoryEnum=lotroEnumRegistry.get(ResistCategory.class);
    _skillDisplayTypeEnum=lotroEnumRegistry.get(SkillDisplayType.class);
    _pipTypeEnum=lotroEnumRegistry.get(PipType.class);
    _areaEffectAnchorTypeEnum=lotroEnumRegistry.get(AreaEffectAnchorType.class);
    _gambitIconTypeEnum=lotroEnumRegistry.get(GambitIconType.class);
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

  /**
   * Load skill details.
   * @param skill Parent skill.
   * @param props Skill properties.
   * @return the loaded details.
   */
  public SkillDetails loadSkillDetails(SkillDescription skill, PropertiesSet props)
  {
    int skillID=skill.getIdentifier();
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
    if ((skillRecoveryTime!=null) && (skillRecoveryTime.floatValue()>0))
    {
      ret.setCooldown(skillRecoveryTime);
    }
    ModPropertyList cooldownMods=ModifiersUtils.getStatModifiers(props,"Skill_SkillRecoveryTime_Mod_Array");
    ret.setCooldownMods(cooldownMods);

    // Skill flags
    // Fast?
    Integer ignoresResetTimeInt=(Integer)props.getProperty("Skill_IgnoresResetTime");
    boolean fast=(ignoresResetTimeInt!=null)&&(ignoresResetTimeInt.intValue()==1);
    ret.setFlag(SkillFlags.FAST,fast);
    // Immediate?
    Integer immediateInt=(Integer)props.getProperty("Skill_Immediate");
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
    ModPropertyList areaEffectMaxTargetsMods=ModifiersUtils.getStatModifiers(props,"Skill_AreaEffectMaxTargets_Mod_Array");
    ret.setMaxTargetsMods(areaEffectMaxTargetsMods);

    // Toggle
    Integer toggleHookNumber=(Integer)props.getProperty("Skill_Toggle_Hook_Number");
    boolean isToggle=(toggleHookNumber!=null)&&(toggleHookNumber.intValue()==1);
    ret.setFlag(SkillFlags.IS_TOGGLE,isToggle);

    // Resist
    Integer resistCategoryCode=(Integer)props.getProperty("Skill_Resist_Category");
    if ((resistCategoryCode!=null) && (resistCategoryCode.intValue()!=0))
    {
      BitSet resistCategoryBitSet=BitSetUtils.getBitSetFromFlags(resistCategoryCode.intValue());
      List<ResistCategory> resistCategories=_resistCategoryEnum.getFromBitSet(resistCategoryBitSet);
      ResistCategory resistCategory=resistCategories.get(0);
      ret.setResistCategory(resistCategory);
      if (resistCategories.size()!=1)
      {
        LOGGER.warn("Bad number of resist categories: {}", resistCategories);
      }
    }
    // Display type(s)
    BitSet displayTypesBitSet=(BitSet)props.getProperty("Skill_DisplaySkillType");
    if (displayTypesBitSet!=null)
    {
      List<SkillDisplayType> displayTypes=_skillDisplayTypeEnum.getFromBitSet(displayTypesBitSet);
      ret.setDisplayTypes(displayTypes);
    }

    SkillEffectsManager mgr=new SkillEffectsManager();
    mgr.setEffects(SkillEffectType.SELF_CRITICAL,getSkillEffectList(props,SkillEffectsProperties.CRITICAL));
    mgr.setEffects(SkillEffectType.TOGGLE,getSkillEffectList(props,SkillEffectsProperties.TOGGLE));
    mgr.setEffects(SkillEffectType.USER_TOGGLE,getSkillEffectList(props,SkillEffectsProperties.TOGGLE_USER));
    mgr.setEffects(SkillEffectType.USER,getSkillEffectList(props,SkillEffectsProperties.USER));
    ret.setEffects(mgr);

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

    // Harmful?
    Integer harmfulInt=(Integer)props.getProperty("Skill_Harmful");
    boolean harmful=(harmfulInt!=null)&&(harmfulInt.intValue()==1);
    ret.setFlag(SkillFlags.HARMFUL,harmful);

    // Attacks
    loadAttacks(props,ret);

    return ret;
  }

  private void loadAttacks(PropertiesSet props, SkillDetails skillDetails)
  {
    Object[] attackHookObjs=(Object[])props.getProperty("Skill_AttackHookList");
    if (attackHookObjs!=null)
    {
      List<SkillAttack> attacks=new ArrayList<SkillAttack>();
      for (Object attackHookObj : attackHookObjs)
      {
        PropertiesSet attackHookInfo=(PropertiesSet)attackHookObj;
        SkillAttack attack=loadAttackHook(attackHookInfo);
        attacks.add(attack);
      }
      if (!attacks.isEmpty())
      {
        SkillAttacks attacksMgr=new SkillAttacks();
        for(SkillAttack attack : attacks)
        {
          attacksMgr.addAttack(attack);
        }
        skillDetails.setAttacks(attacksMgr);
      }
    }
  }

  private SkillGeometry loadGeometry(PropertiesSet props)
  {
    SkillGeometry ret=new SkillGeometry();
    // Shape
    Float arcRadius=(Float)props.getProperty("Skill_AEDetectionVolume_ArcRadius");
    Float boxLength=(Float)props.getProperty("Skill_AEDetectionVolume_BoxLength");
    Float sphereRadius=(Float)props.getProperty("Skill_AEDetectionVolume_SphereRadius");
    int shapesCount=0;
    if ((arcRadius!=null) && (arcRadius.floatValue()!=0.0f))
    {
      Arc arc=new Arc();
      arc.setRadius(arcRadius.floatValue());
      Float degrees=(Float)props.getProperty("Skill_AEDetectionVolume_ArcDegrees");
      if (degrees!=null)
      {
        arc.setDegrees(degrees.floatValue());
      }
      Float heading=(Float)props.getProperty("Skill_AEDetectionVolume_HeadingOffset");
      arc.setHeadingOffset(heading);
      ret.setShape(arc);
      shapesCount++;
    }
    if ((boxLength!=null) && (boxLength.floatValue()!=0.0f))
    {
      Box box=new Box();
      box.setLength(boxLength.floatValue());
      Float width=(Float)props.getProperty("Skill_AEDetectionVolume_BoxWidth");
      box.setWidth(width.floatValue());
      ret.setShape(box);
      shapesCount++;
    }
    if ((sphereRadius!=null) && (sphereRadius.floatValue()!=0.0f))
    {
      Sphere sphere=new Sphere();
      sphere.setRadius(sphereRadius.floatValue());
      ret.setShape(sphere);
      shapesCount++;
    }
    if (shapesCount>1)
    {
      LOGGER.warn("Bad shapes count: {}",Integer.valueOf(shapesCount));
    }
    // Positional data
    SkillPositionalData positionalData=loadPositionalData(props);
    ret.setPositionalData(positionalData);
    // Detection anchor
    Integer anchorCode=(Integer)props.getProperty("Skill_AreaEffectDetectionAnchor");
    if ((anchorCode!=null) && (anchorCode.intValue()!=0))
    {
      AreaEffectAnchorType anchorType=_areaEffectAnchorTypeEnum.getEntry(anchorCode.intValue());
      ret.setDetectionAnchor(anchorType);
    }
    // Range
    Float minRange=(Float)props.getProperty("Skill_MinRange");
    if ((minRange!=null) && (minRange.floatValue()>0))
    {
      ret.setMinRange(minRange);
    }
    Float maxRange=(Float)props.getProperty("Skill_MaxRange");
    if ((maxRange!=null) && (maxRange.floatValue()>0))
    {
      ret.setMaxRange(maxRange);
    }
    ModPropertyList maxRangeModifiers=ModifiersUtils.getStatModifiers(props,"Skill_MaxRange_ModifierArray");
    ret.setMaxRangeMods(maxRangeModifiers);
    if (ret.hasValues()) 
    {
      return ret;
    }
    return null;
  }

  private SkillPositionalData loadPositionalData(PropertiesSet props)
  {
    SkillPositionalData ret=null;
    Integer heading=(Integer)props.getProperty("Skill_PositionalHeading");
    int headingValue=(heading!=null)?heading.intValue():0;
    if (headingValue==-1)
    {
      headingValue=0;
    }
    Integer spread=(Integer)props.getProperty("Skill_PositionalSpread");
    int spreadValue=(spread!=null)?spread.intValue():0;
    if (spreadValue==-1)
    {
      spreadValue=0;
    }
    if ((headingValue!=0) || (spreadValue!=0))
    {
      ret=new SkillPositionalData();
      ret.setHeading(headingValue);
      ret.setSpread(spreadValue);
    }
    return ret;
  }

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
    SkillEffectsManager mgr=new SkillEffectsManager();
    mgr.setEffects(SkillEffectType.ATTACK_CRITICAL,getSkillEffectList(attackHookProperties,SkillEffectsProperties.ATTACK_CRITICAL));
    mgr.setEffects(SkillEffectType.ATTACK_POSITIONAL,getSkillEffectList(attackHookProperties,SkillEffectsProperties.ATTACK_POSITIONAL));
    mgr.setEffects(SkillEffectType.ATTACK_SUPERCRITICAL,getSkillEffectList(attackHookProperties,SkillEffectsProperties.ATTACK_SUPERCRITICAL));
    mgr.setEffects(SkillEffectType.ATTACK,getSkillEffectList(attackHookProperties,SkillEffectsProperties.ATTACK));
    ret.setEffects(mgr);

    // Modifiers
    ModPropertyList dpsMods=ModifiersUtils.getStatModifiers(attackHookProperties,"Skill_AttackHook_DPSAddMod_Mod_Array");
    ret.setDPSMods(dpsMods);
    ModPropertyList maxDamageMods=ModifiersUtils.getStatModifiers(attackHookProperties,"Skill_AttackHook_HookDamageMax_Mod_Array");
    ret.setMaxDamageMods(maxDamageMods);
    ModPropertyList damageModifiersMods=ModifiersUtils.getStatModifiers(attackHookProperties,"Skill_AttackHook_HookDamageModifier_Mod_Array");
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
    ModPropertyList changeMods=ModifiersUtils.getStatModifiers(props,"Skill_PipChange_Mod_Array");
    ret.setChangeMods(changeMods);
    Integer requiredMinValue=(Integer)props.getProperty("Skill_Pip_RequiredMinValue");
    ret.setRequiredMinValue(requiredMinValue);
    ModPropertyList requiredMinValueMods=ModifiersUtils.getStatModifiers(props,"Skill_PipRequiredMin_Mod_Array");
    ret.setRequiredMinValueMods(requiredMinValueMods);
    Integer requiredMaxValue=(Integer)props.getProperty("Skill_Pip_RequiredMaxValue");
    ret.setRequiredMaxValue(requiredMaxValue);
    ModPropertyList requiredMaxValueMods=ModifiersUtils.getStatModifiers(props,"Skill_PipRequiredMax_Mod_Array");
    ret.setRequiredMaxValueMods(requiredMaxValueMods);
    Integer towardHome=(Integer)props.getProperty("Skill_Pip_Toward_Home");
    ret.setTowardHome(towardHome);
    Integer changePerInterval=(Integer)props.getProperty("Skill_Toggle_PipChangePerInterval");
    ret.setChangePerInterval(changePerInterval);
    ModPropertyList changePerIntervalMods=ModifiersUtils.getStatModifiers(props,"Skill_TogglePipChangePerInterval_Mod_Array");
    ret.setSecondsPerPipChangeMods(changePerIntervalMods);
    Float secondsPerPipChange=(Float)props.getProperty("Skill_Toggle_SecondsPerPipChange");
    ret.setSecondsPerPipChange(secondsPerPipChange);
    ModPropertyList secondsPerPipChangeMods=ModifiersUtils.getStatModifiers(props,"Skill_ToggleSecondsPerPipChange_Mod_Array");
    ret.setSecondsPerPipChangeMods(secondsPerPipChangeMods);
    return ret;
  }

  private SkillGambitData loadGambitData(PropertiesSet props)
  {
    SkillGambitData ret=null;
    // Always null: Skill_RequiredGambitIcon, Skill_RequiredGambitIcons, Skill_AppliedGambitIcons
    // Gambits to add
    Integer appliedGambitInt=(Integer)props.getProperty("Skill_AppliedGambit");
    int appliedGambit=(appliedGambitInt!=null)?appliedGambitInt.intValue():0;
    Integer appliedGambitsCountInt=(Integer)props.getProperty("Skill_AppliedGambitIconCount");
    int appliedGambitsCount=(appliedGambitsCountInt!=null)?appliedGambitsCountInt.intValue():0;
    boolean appliesGambits=((appliedGambit>0) || (appliedGambitsCount>0));
    // Gambits to remove
    // Count of gambits to remove (lifo): -1 --Clears All Gambits ; 1 --Clears 1 Gambit
    Integer removedGambitsInt=(Integer)props.getProperty("Skill_RemovedGambits");
    int removedGambits=(removedGambitsInt!=null)?removedGambitsInt.intValue():0;
    // Gambit requirements
    // - required gambits
    Integer requiredGambit=(Integer)props.getProperty("Skill_RequiredGambit");
    // - requires a gambit
    // If yes, see requiredGambit for required gambits, or "Requires: an active Gambit" if none required (skill:Gambit Default)
    Integer requiresGambitsInt=(Integer)props.getProperty("Skill_RequiresGambits");
    boolean requiresGambits=((requiresGambitsInt!=null) && (requiresGambitsInt.intValue()==1));

    if ((appliesGambits) || (removedGambits!=0) || (requiresGambits))
    {
      ret=new SkillGambitData();
      // Gambits to add
      if (appliesGambits)
      {
        List<GambitIconType> toAdd=getGambits(appliedGambit);
        if (toAdd.size()!=appliedGambitsCount)
        {
          LOGGER.warn("Mismatch on add gambits: {} ; size={}",toAdd,Integer.valueOf(appliedGambitsCount));
        }
        ret.setToAdd(toAdd);
      }
      // Removal
      if (removedGambitsInt!=null)
      {
        if (removedGambitsInt.intValue()<0)
        {
          ret.setClearAllGambits();
        }
        else
        {
          ret.setToRemove(removedGambitsInt.intValue());
        }
      }
      // Required gambits
      if (requiresGambits)
      {
        if (requiredGambit!=null)
        {
          List<GambitIconType> required=getGambits(requiredGambit.intValue());
          ret.setRequired(required);
        }
        else
        {
          ret.setRequired(new ArrayList<GambitIconType>());
        }
      }
    }
    return ret;
  }

  private List<GambitIconType> getGambits(int value)
  {
    List<GambitIconType> ret=new ArrayList<GambitIconType>();
    while(true)
    {
      int code=value&0xF;
      if (code==0) break;
      GambitIconType gambit=_gambitIconTypeEnum.getEntry(code);
      ret.add(gambit);
      value>>=4;
    }
    return ret;
  }

  private SingleTypeSkillEffectsManager getSkillEffectList(PropertiesSet props, SkillEffectsProperties spec)
  {
    String skillEffectListPropName=spec.getEffectsListProperty();
    Object[] effectList=(Object[])props.getProperty(skillEffectListPropName);
    String additionalEffectsPropName=spec.getAdditiveModsProperty();
    ModPropertyList additiveMods=ModifiersUtils.getStatModifiers(props,additionalEffectsPropName);
    String overridePropName=spec.getOverrideProperty();
    Integer overridePropertyID=null;
    if (overridePropName!=null)
    {
      overridePropertyID=(Integer)props.getProperty(overridePropName);
      if ((overridePropertyID!=null) && (overridePropertyID.intValue()==0))
      {
        overridePropertyID=null;
      }
    }
    SingleTypeSkillEffectsManager mgr=null;
    SkillEffectType type=spec.getType();
    if ((additiveMods!=null) || (overridePropertyID!=null))
    {
      mgr=new SingleTypeSkillEffectsManager(type);
      mgr.setAdditiveModifiers(additiveMods);
      mgr.setOverridePropertyID(overridePropertyID);
    }
    // Implement usage
    ImplementUsageType effectImplementUsage=null;
    String effectImplementUsagePropName=spec.getImplementUsageProperty();
    if (effectImplementUsagePropName!=null)
    {
      Integer effectImplementUsageCode=(Integer)props.getProperty(effectImplementUsagePropName);
      if ((effectImplementUsageCode!=null) && (effectImplementUsageCode.intValue()>0))
      {
        effectImplementUsage=ImplementUsageTypes.getByCode(effectImplementUsageCode.intValue());
      }
    }
    if (effectList!=null)
    {
      mgr=handleEffectsList(effectList,type,effectImplementUsage,mgr);
    }
    return mgr;
  }

  private SingleTypeSkillEffectsManager handleEffectsList(Object[] effectList, SkillEffectType type, ImplementUsageType effectImplementUsage, SingleTypeSkillEffectsManager mgr)
  {
    boolean foundOne=false;
    boolean toggle=((type==SkillEffectType.TOGGLE)||(type==SkillEffectType.USER_TOGGLE));
    for (Object skillEffectObj : effectList)
    {
      PropertiesSet effectData=(PropertiesSet)skillEffectObj;
      SkillEffectGenerator generator;
      if (toggle)
      {
        generator=_effectsLoader.loadSkillToggleEffect(effectData);
      }
      else
      {
        generator=_effectsLoader.loadSkillEffect(effectData);
      }
      if (generator!=null)
      {
        generator.setType(type);
        if (effectImplementUsage!=null)
        {
          generator.setImplementUsage(effectImplementUsage);
        }
        if (mgr==null)
        {
          mgr=new SingleTypeSkillEffectsManager(type);
        }
        mgr.addEffect(generator);
        foundOne=true;
      }
      else
      {
        if (foundOne)
        {
          break;
        }
      }
    }
    return mgr;
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
        ModPropertyList propertyMods=ModifiersUtils.getStatModifiers(vitalCostProps,"Skill_Vital_Mod_Array");
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
    ModPropertyList addMods=ModifiersUtils.getStatModifiers(props,"Induction_Duration_AdditiveModifierList");
    ret.setAddMods(addMods);
    ModPropertyList multiplyMods=ModifiersUtils.getStatModifiers(props,"Induction_Duration_MultiplierModifierList");
    ret.setMultiplyMods(multiplyMods);
    return ret;
  }

  private void saveInductions()
  {
    List<Induction> inductions=new ArrayList<Induction>(_inductions.values());
    Collections.sort(inductions,new IdentifiableComparator<Induction>());
    InductionXMLWriter.writeInductionsFile(GeneratedFiles.INDUCTIONS,inductions);
  }

  /**
   * Save loaded annex data.
   */
  public void save()
  {
    saveInductions();
    ProgressionUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_SKILLS);
  }
}
