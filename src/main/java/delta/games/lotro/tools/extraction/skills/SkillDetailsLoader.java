package delta.games.lotro.tools.extraction.skills;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.tools.extraction.effects.EffectLoader;

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
    Integer Skill_Category=(Integer)props.getProperty("Skill_Category");
    String Skill_Name=(String)props.getProperty("Skill_Name");

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
    Integer m_bUsesMagic=(Integer)SkillData.getAttributeValue("m_bUsesMagic");
    Integer m_bUsesMelee=(Integer)SkillData.getAttributeValue("m_bUsesMelee");
    Integer m_bUsesRanged=(Integer)SkillData.getAttributeValue("m_bUsesRanged");
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
    List<Integer> Skill_AreaEffectMaxTargets_Mod_Array=GetStatModArray(props,"Skill_AreaEffectMaxTargets_Mod_Array");

    Float Skill_MaxRange=(Float)props.getProperty("Skill_MaxRange");
    Float Skill_MinRange=(Float)props.getProperty("Skill_MinRange");
    List<Integer> Skill_MaxRange_ModifierArray=GetStatModArray(props,"Skill_MaxRange_ModifierArray");

    // UsesToggle
    Integer Skill_Toggle_Hook_Number=(Integer)props.getProperty("Skill_Toggle_Hook_Number");
    if ((Skill_Toggle_Hook_Number != null) && (Skill_Toggle_Hook_Number.intValue() > 0))
    {
      //SkillFlags += 256;
    }

    Integer Skill_Resist_Category=(Integer)props.getProperty("Skill_Resist_Category");
    BitSet Skill_DisplaySkillType=(BitSet)props.getProperty("Skill_DisplaySkillType");
    if ((Skill_DisplaySkillType!=null) && (Skill_DisplaySkillType.size()==0))
    {
      Skill_DisplaySkillType=null;
    }

    List<Object> sCriticalEffectList=GetSkillEffectList(props,"Skill_CriticalEffectList"); //Apply to self on critical:
    List<Object> sToggleEffectList=GetSkillEffectList(props,"Skill_Toggle_Effect_List","Skill_Toggle_Effect_ImplementUsage");
    List<Object> sToggleUserEffectList=GetSkillEffectList(props,"Skill_Toggle_User_Effect_List","Skill_Toggle_User_Effect_ImplementUsage");
    List<Object> sUserEffectList=GetSkillEffectList(props,"Skill_UserEffectList");

    Object sToggleVitalCostPerSecondListMorale=GetVitalCostList(props,"Skill_Toggle_VitalCostPerSecondList",1);
    Object sToggleVitalCostPerSecondListPower=GetVitalCostList(props,"Skill_Toggle_VitalCostPerSecondList",2);
    Object sVitalCostListMorale=GetVitalCostList(props,"Skill_VitalCostList",1);
    Object sVitalCostListPower=GetVitalCostList(props,"Skill_VitalCostList",2);

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

  private void loadAttackHook(PropertiesSet Skill_AttackHookInfo)
  {
    Integer Skill_AttackHook_DamageQualifier=(Integer)Skill_AttackHookInfo.getProperty("Skill_AttackHook_DamageQualifier");
    Object sCriticalTargetEffectList=GetSkillEffectList(Skill_AttackHookInfo,"Skill_AttackHook_CriticalTargetEffectList");
    Object sPositionalTargetEffectList=GetSkillEffectList(Skill_AttackHookInfo,"Skill_AttackHook_PositionalTargetEffectList");
    Object sSuperCriticalTargetEffectList=GetSkillEffectList(Skill_AttackHookInfo,"Skill_AttackHook_SuperCriticalTargetEffectList");
    Object sTargetEffectList=GetSkillEffectList(Skill_AttackHookInfo,"Skill_AttackHook_TargetEffectList");

    List<Integer> sDPSAddModModArray=GetStatModArray(Skill_AttackHookInfo,"Skill_AttackHook_DPSAddMod_Mod_Array");
    List<Integer> sHookDamageMaxModArray=GetStatModArray(Skill_AttackHookInfo,"Skill_AttackHook_HookDamageMax_Mod_Array");
    List<Integer> sHookDamageModifierModArray=GetStatModArray(Skill_AttackHookInfo,"Skill_AttackHook_HookDamageModifier_Mod_Array");

    Integer Skill_AttackHook_DamageType=(Integer)Skill_AttackHookInfo.getProperty("Skill_AttackHook_DamageType");
    Float Skill_AttackHook_DamageAddContributionMultiplier=(Float)Skill_AttackHookInfo.getProperty("Skill_AttackHook_DamageAddContributionMultiplier");
    Integer Skill_AttackHook_DPSAddMod_Progression=(Integer)Skill_AttackHookInfo.getProperty("Skill_AttackHook_DPSAddMod_Progression");
    Float Skill_AttackHook_HookDamageMax=(Float)Skill_AttackHookInfo.getProperty("Skill_AttackHook_HookDamageMax");
    Float Skill_AttackHook_HookDamageMaxVariance=(Float)Skill_AttackHookInfo.getProperty("Skill_AttackHook_HookDamageMaxVariance");
    Integer Skill_AttackHook_HookDamageMax_Progression=(Integer)Skill_AttackHookInfo.getProperty("Skill_AttackHook_HookDamageMax_Progression");
    Float Skill_AttackHook_HookDamageModifier=(Float)Skill_AttackHookInfo.getProperty("Skill_AttackHook_HookDamageModifier");

    Float Skill_AttackHook_ImplementContributionMultiplier=(Float)Skill_AttackHookInfo.getProperty("Skill_AttackHook_ImplementContributionMultiplier");
    Integer Skill_AttackHook_UsesNatural=(Integer)Skill_AttackHookInfo.getProperty("Skill_AttackHook_UsesNatural");
    Integer Skill_AttackHook_UsesPrimaryImplement=(Integer)Skill_AttackHookInfo.getProperty("Skill_AttackHook_UsesPrimaryImplement");
    Integer Skill_AttackHook_UsesRangedImplement=(Integer)Skill_AttackHookInfo.getProperty("Skill_AttackHook_UsesRangedImplement");
    Integer Skill_AttackHook_UsesSecondaryImplement=(Integer)Skill_AttackHookInfo.getProperty("Skill_AttackHook_UsesSecondaryImplement");
    Integer Skill_AttackHook_UsesTactical=(Integer)Skill_AttackHookInfo.getProperty("Skill_AttackHook_UsesTactical");
    Integer UsesImplement=Skill_AttackHook_UsesNatural * 5 +
            Skill_AttackHook_UsesPrimaryImplement * 2 +
            Skill_AttackHook_UsesRangedImplement * 3 +
            Skill_AttackHook_UsesSecondaryImplement * 6 +
            Skill_AttackHook_UsesTactical * 4;
  }

  private void loadPipData(PropertiesSet props)
  {
    Integer Skill_Pip_AffectedType=(Integer)props.getProperty("Skill_Pip_AffectedType");
    Integer Skill_Pip_Change=(Integer)props.getProperty("Skill_Pip_Change");
    List<Integer> mods=GetStatModArray(props,"Skill_PipChange_Mod_Array");
    Integer Skill_Pip_RequiredMinValue=(Integer)props.getProperty("Skill_Pip_RequiredMinValue");
    List<Integer> modsRequiredMin=GetStatModArray(props,"Skill_PipRequiredMin_Mod_Array");
    Integer Skill_Pip_RequiredMaxValue=(Integer)props.getProperty("Skill_Pip_RequiredMaxValue");
    List<Integer> modsRequiredMax=GetStatModArray(props,"Skill_PipRequiredMax_Mod_Array");
    Integer Skill_Pip_Toward_Home=(Integer)props.getProperty("Skill_Pip_Toward_Home");
    Integer Skill_Toggle_PipChangePerInterval=(Integer)props.getProperty("Skill_Toggle_PipChangePerInterval");
    List<Integer> modsPipChangePerInterval=GetStatModArray(props,"Skill_TogglePipChangePerInterval_Mod_Array");
    Float Skill_Toggle_SecondsPerPipChange=(Float)props.getProperty("Skill_Toggle_SecondsPerPipChange");
    List<Integer> modsSecondsPerPipChange=GetStatModArray(props,"Skill_ToggleSecondsPerPipChange_Mod_Array");
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

  private static List<Object> GetSkillEffectList(PropertiesSet props, String sSkillEffectListPropName)
  {
    return GetSkillEffectList(props,sSkillEffectListPropName,null);
  }
  
  private static List<Object> GetSkillEffectList(PropertiesSet props, String sSkillEffectListPropName, String sEffectImplementUsagePropName)
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
      Integer Skill_Effect=(Integer)Skill_EffectData.getProperty("Skill_Effect");
      if ((Skill_Effect == null) || (Skill_Effect.intValue()<=0))
      {
        continue;
      }
      // Duration
      Float Skill_EffectDuration=(Float)Skill_EffectData.getProperty("Skill_EffectDuration");
      // Implement
      if (sEffectImplementUsagePropName == null)
      {
        Skill_EffectImplementUsage=(Integer)Skill_EffectData.getProperty("Skill_EffectImplementUsage");
      }
      // Spellcraft
      Float Skill_EffectSpellcraft=(Float)Skill_EffectData.getProperty("Skill_EffectSpellcraft");
    }
    return null;
  }

  private static List<Integer> GetStatModArray(PropertiesSet props, String sStatModArrayPropName)
  {
    Object[] statIDObjs=(Integer[])props.getProperty(sStatModArrayPropName);
    if (statIDObjs == null)
    {
      return null;
    }
    List<Integer> ret=new ArrayList<Integer>();
    for (Object statIDObj : statIDObjs)
    {
      Integer statID=(Integer)statIDObj;
      if ((statID != null) && (statID.intValue()>0))
      {
        ret.add(statID);
      }
    }
    return ret;
  }

  private static Object GetVitalCostList(PropertiesSet props, String sVitalCostListPropName, int iVitalType)
  {
    Object[] Skill_VitalCostList=(Object[])props.getProperty(sVitalCostListPropName);
    if (Skill_VitalCostList==null)
    {
      return null;
    }
    for (Object Skill_VitalInfoObj : Skill_VitalCostList)
    {
      PropertiesSet Skill_VitalInfo=(PropertiesSet)Skill_VitalInfoObj;
      int Skill_Vital_Type=((Integer)Skill_VitalInfo.getProperty("Skill_Vital_Type")).intValue();
      if (iVitalType==Skill_Vital_Type)
      {
        // TODO Dedicated object
        Integer Skill_Vital_Consumes_All=(Integer)Skill_VitalInfo.getProperty("Skill_Vital_Consumes_All");
        List<Integer> Skill_Vital_Mod_Array=GetStatModArray(Skill_VitalInfo,"Skill_Vital_Mod_Array");
        Float Skill_Vital_Percent=(Float)Skill_VitalInfo.getProperty("Skill_Vital_Percent");
        Float Skill_Vital_Points=(Float)Skill_VitalInfo.getProperty("Skill_Vital_Points");
        Integer Skill_Vital_Points_Progression=(Integer)Skill_VitalInfo.getProperty("Skill_Vital_Points_Progression");
        break;
      }
    }
    return null;
  }

  private void loadInduction(int inductionID)
  {
    PropertiesSet props=_facade.loadProperties(inductionID+DATConstants.DBPROPERTIES_OFFSET);
    if (props == null) return;

    Float Induction_Duration=(Float)props.getProperty("Induction_Duration");
    List<Integer> sAdditiveModifierList=GetStatModArray(props,"Induction_Duration_AdditiveModifierList");
    List<Integer> sMultiplierModifierList=GetStatModArray(props,"Induction_Duration_MultiplierModifierList");
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

  public static void main(String[] args)
  {
  }
}
