package delta.games.lotro.tools.extraction.skills;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.character.skills.SkillCostData;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillVitalCost;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.skills.attack.SkillAttack;
import delta.games.lotro.character.skills.attack.SkillAttackFlags;
import delta.games.lotro.common.enums.DamageQualifier;
import delta.games.lotro.common.enums.DamageQualifiers;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.VitalType;
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
 * @author dmorcellet
 */
public class SkillDetailsLoader
{
  private DataFacade _facade;
  private EffectLoader _effectsLoader;
  private Map<Integer,Float> _inductionDurations;
  private Map<Integer,Float> _channelingDurations;

  public SkillDetailsLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectsLoader=effectsLoader;
    _inductionDurations=new HashMap<Integer,Float>();
    _channelingDurations=new HashMap<Integer,Float>();
  }

  private Float getInductionDuration(int skillInductionActionID)
  {
    Integer key=Integer.valueOf(skillInductionActionID);
    Float ret=_inductionDurations.get(key);
    if (ret==null)
    {
      PropertiesSet props=_facade.loadProperties(skillInductionActionID+DATConstants.DBPROPERTIES_OFFSET);
      ret=(Float)props.getProperty("Induction_Duration");
      _inductionDurations.put(key,ret);
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
    Float Skill_ActionDurationContribution=(Float)props.getProperty("Skill_ActionDurationContribution");
    Integer Skill_InductionActionID=(Integer)props.getProperty("Skill_InductionAction");

    Float Skill_Toggle_ChannelingDurationOverride=(Float)props.getProperty("Skill_Toggle_ChannelingDurationOverride");
    Float ChannelingDuration=null;
    if (Skill_Toggle_ChannelingDurationOverride==null)
    {
      Integer Skill_Toggle_ChannelingState=(Integer)props.getProperty("Skill_Toggle_ChannelingState");
      if (Skill_Toggle_ChannelingState!=null)
      {
        ChannelingDuration=getChannelingDuration(Skill_Toggle_ChannelingState.intValue());
      }
    }
    else
    {
      ChannelingDuration=Skill_Toggle_ChannelingDurationOverride;
    }

    // Cooldown
    Float Skill_SkillRecoveryTime=(Float)props.getProperty("Skill_SkillRecoveryTime");

    // Skill flags
    // Fast?
    Integer Skill_IgnoresResetTimeInt=(Integer)props.getProperty("Skill_IgnoresResetTime");
    boolean fast=(Skill_IgnoresResetTimeInt!=null)&&(Skill_IgnoresResetTimeInt.intValue()==1);
    // Immediate?
    Integer Skill_ImmediateInt=(Integer)props.getProperty("Skill_Immediate"); //Immediate
    boolean immediate=(Skill_ImmediateInt!=null)&&(Skill_ImmediateInt.intValue()==1);

    WStateDataSet dataSet=_facade.loadWState(skillID);
    ClassInstance SkillData=(ClassInstance) dataSet.getValueForReference(0);
    Integer usesMagicInt=(Integer)SkillData.getAttributeValue("m_bUsesMagic");
    boolean usesMagic=(usesMagicInt!=null)&&(usesMagicInt.intValue()==1);
    Integer usesMeleeInt=(Integer)SkillData.getAttributeValue("m_bUsesMelee");
    boolean usesMelee=(usesMeleeInt!=null)&&(usesMeleeInt.intValue()==1);
    Integer usesRangedInt=(Integer)SkillData.getAttributeValue("m_bUsesRanged");
    boolean usesRanged=(usesRangedInt!=null)&&(usesRangedInt.intValue()==1);
    /*
      SkillFlags=Skill_IgnoresResetTime*1+
          Skill_Immediate*2+
          m_bUsesMagic*4+
          m_bUsesMelee*8+
          m_bUsesRanged*16;
          */

    Float radius=null;
    Float Skill_AEDetectionVolume_ArcRadius=(Float)props.getProperty("Skill_AEDetectionVolume_ArcRadius");
    Float Skill_AEDetectionVolume_BoxLength=(Float)props.getProperty("Skill_AEDetectionVolume_BoxLength");
    Float Skill_AEDetectionVolume_SphereRadius=(Float)props.getProperty("Skill_AEDetectionVolume_SphereRadius");
    if ((Skill_AEDetectionVolume_ArcRadius != null) && (Skill_AEDetectionVolume_ArcRadius.floatValue()!=0.0f))
    {
      radius=Skill_AEDetectionVolume_ArcRadius;
      //SkillFlags += 32;
    }
    else if ((Skill_AEDetectionVolume_BoxLength != null) && (Skill_AEDetectionVolume_BoxLength.floatValue()!=0.0f))
    {
      radius=Skill_AEDetectionVolume_BoxLength;
      //SkillFlags += 64;
    }
    else if ((Skill_AEDetectionVolume_SphereRadius != null) && (Skill_AEDetectionVolume_SphereRadius.floatValue()!=0.0f))
    {
      radius=Skill_AEDetectionVolume_SphereRadius;
      //SkillFlags += 128;
    }

    Integer Skill_AreaEffectDetectionAnchor=(Integer)props.getProperty("Skill_AreaEffectDetectionAnchor");
    Integer Skill_AreaEffectMaxTargets=(Integer)props.getProperty("Skill_AreaEffectMaxTargets");
    ModPropertyList Skill_AreaEffectMaxTargets_Mod_Array=getStatModifiers(props,"Skill_AreaEffectMaxTargets_Mod_Array");

    Float Skill_MaxRange=(Float)props.getProperty("Skill_MaxRange");
    Float Skill_MinRange=(Float)props.getProperty("Skill_MinRange");
    ModPropertyList Skill_MaxRange_ModifierArray=getStatModifiers(props,"Skill_MaxRange_ModifierArray");

    // UsesToggle
    Integer Skill_Toggle_Hook_Number=(Integer)props.getProperty("Skill_Toggle_Hook_Number");
    if ((Skill_Toggle_Hook_Number != null) && (Skill_Toggle_Hook_Number.intValue() > 0))
    {
      //SkillFlags += 256;
    }

    Integer Skill_Resist_Category=(Integer)props.getProperty("Skill_Resist_Category");
    BitSet Skill_DisplaySkillType=(BitSet)props.getProperty("Skill_DisplaySkillType");

    List<Object> sCriticalEffectList=getSkillEffectList(props,"Skill_CriticalEffectList"); //Apply to self on critical:
    List<Object> sToggleEffectList=GetSkillEffectList(props,"Skill_Toggle_Effect_List","Skill_Toggle_Effect_ImplementUsage");
    List<Object> sToggleUserEffectList=GetSkillEffectList(props,"Skill_Toggle_User_Effect_List","Skill_Toggle_User_Effect_ImplementUsage");
    List<Object> sUserEffectList=getSkillEffectList(props,"Skill_UserEffectList");

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

    if (props.hasProperty("Skill_Pip_AffectedType"))
    {
      loadPipData(props);
    }

    loadGambitData(props);

    //Skill_Toggle_Hook_Number: 1 - Toggle Skill
    Object[] attackHookObjs=(Object[])props.getProperty("Skill_AttackHookList");
    if (attackHookObjs != null)
    {
      for (Object attackHookObj : attackHookObjs)
      {
        PropertiesSet attackHookInfo=(PropertiesSet)attackHookObj;
        loadAttackHook(attackHookInfo);
      }
    }
    Integer Skill_Icon=(Integer)props.getProperty("Skill_Icon");
    String Skill_Desc=(String)props.getProperty("Skill_Desc");
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
    Object sCriticalTargetEffectList=getSkillEffectList(attackHookProperties,"Skill_AttackHook_CriticalTargetEffectList");
    Object sPositionalTargetEffectList=getSkillEffectList(attackHookProperties,"Skill_AttackHook_PositionalTargetEffectList");
    Object sSuperCriticalTargetEffectList=getSkillEffectList(attackHookProperties,"Skill_AttackHook_SuperCriticalTargetEffectList");
    Object sTargetEffectList=getSkillEffectList(attackHookProperties,"Skill_AttackHook_TargetEffectList");

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

  private void loadPipData(PropertiesSet props)
  {
    Integer Skill_Pip_AffectedType=(Integer)props.getProperty("Skill_Pip_AffectedType");
    Integer Skill_Pip_Change=(Integer)props.getProperty("Skill_Pip_Change");
    ModPropertyList mods=getStatModifiers(props,"Skill_PipChange_Mod_Array");
    Integer Skill_Pip_RequiredMinValue=(Integer)props.getProperty("Skill_Pip_RequiredMinValue");
    ModPropertyList modsRequiredMin=getStatModifiers(props,"Skill_PipRequiredMin_Mod_Array");
    Integer Skill_Pip_RequiredMaxValue=(Integer)props.getProperty("Skill_Pip_RequiredMaxValue");
    ModPropertyList modsRequiredMax=getStatModifiers(props,"Skill_PipRequiredMax_Mod_Array");
    Integer Skill_Pip_Toward_Home=(Integer)props.getProperty("Skill_Pip_Toward_Home");
    Integer Skill_Toggle_PipChangePerInterval=(Integer)props.getProperty("Skill_Toggle_PipChangePerInterval");
    ModPropertyList modsPipChangePerInterval=getStatModifiers(props,"Skill_TogglePipChangePerInterval_Mod_Array");
    Float Skill_Toggle_SecondsPerPipChange=(Float)props.getProperty("Skill_Toggle_SecondsPerPipChange");
    ModPropertyList modsSecondsPerPipChange=getStatModifiers(props,"Skill_ToggleSecondsPerPipChange_Mod_Array");
  }

  private void loadGambitData(PropertiesSet props)
  {
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
  }

  private List<Object> getSkillEffectList(PropertiesSet props, String sSkillEffectListPropName)
  {
    return GetSkillEffectList(props,sSkillEffectListPropName,null);
  }
  
  private List<Object> GetSkillEffectList(PropertiesSet props, String sSkillEffectListPropName, String sEffectImplementUsagePropName)
  {
    Object[] Skill_EffectList=(Object[])props.getProperty(sSkillEffectListPropName);
    if (Skill_EffectList == null)
    {
      return null;
    }
    Integer Skill_EffectImplementUsage=0;
    if (sEffectImplementUsagePropName != null)
    {
      Skill_EffectImplementUsage=(Integer)props.getProperty(sEffectImplementUsagePropName);
      if (Skill_EffectImplementUsage == null) Skill_EffectImplementUsage=0;
    }
    // TODO A list
    for (Object Skill_EffectObj : Skill_EffectList)
    {
      PropertiesSet Skill_EffectData=(PropertiesSet)Skill_EffectObj;
      Integer skillEffectID=(Integer)Skill_EffectData.getProperty("Skill_Effect");
      if ((skillEffectID == null) || (skillEffectID.intValue()<=0))
      {
        continue;
      }
      _effectsLoader.getEffect(skillEffectID.intValue());
      // Duration
      Float skillEffectDuration=(Float)Skill_EffectData.getProperty("Skill_EffectDuration");
      // Implement
      if (sEffectImplementUsagePropName==null)
      {
        Skill_EffectImplementUsage=(Integer)Skill_EffectData.getProperty("Skill_EffectImplementUsage");
      }
      // Spellcraft
      Float Skill_EffectSpellcraft=(Float)Skill_EffectData.getProperty("Skill_EffectSpellcraft");
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

  private void loadInduction(int inductionID)
  {
    PropertiesSet props=_facade.loadProperties(inductionID+DATConstants.DBPROPERTIES_OFFSET);
    if (props == null) return;

    Float Induction_Duration=(Float)props.getProperty("Induction_Duration");
    ModPropertyList sAdditiveModifierList=getStatModifiers(props,"Induction_Duration_AdditiveModifierList");
    ModPropertyList sMultiplierModifierList=getStatModifiers(props,"Induction_Duration_MultiplierModifierList");
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
