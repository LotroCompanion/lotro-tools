package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.ReviveEffect;
import delta.games.lotro.common.effects.ReviveVitalData;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.VitalType;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for 'revive' effects.
 * @author DAM
 */
public class ReviveEffectLoader extends AbstractEffectLoader<ReviveEffect>
{
  @Override
  public void loadSpecifics(ReviveEffect effect, PropertiesSet props)
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
}
