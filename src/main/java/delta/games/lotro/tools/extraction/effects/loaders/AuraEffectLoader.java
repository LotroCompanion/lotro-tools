package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.AuraEffect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.enums.EffectAuraType;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for 'aura' effects.
 * @author DAM
 */
public class AuraEffectLoader extends AbstractEffectLoader<AuraEffect>
{
  @Override
  public void loadSpecifics(AuraEffect effect, PropertiesSet props)
  {
    /*
Aura_ShouldAffectCaster: 1
Effect_Aura_Applied_Effect_Array: 
  #1: Effect_Aura_Applied_Effect_Data 
    Effect_Aura_Applied_Effect: 1879416043
    Effect_Aura_Applied_Effect_Spellcraft: -1.0
Effect_Aura_Type: 2 (Player)
    */
    // Type
    LotroEnum<EffectAuraType> typeEnum=LotroEnumsRegistry.getInstance().get(EffectAuraType.class);
    int typeCode=((Integer)props.getProperty("Effect_Aura_Type")).intValue();
    EffectAuraType type=typeEnum.getEntry(typeCode);
    effect.setType(type);
    // Should affect caster?
    Integer shouldAffectCasterInt=(Integer)props.getProperty("Aura_ShouldAffectCaster");
    boolean shouldAffectCaster=((shouldAffectCasterInt!=null)&&(shouldAffectCasterInt.intValue()==1));
    effect.setShouldAffectCaster(shouldAffectCaster);
    // Generators
    Object[] effectsList=(Object[])props.getProperty("Effect_Aura_Applied_Effect_Array");
    if (effectsList!=null)
    {
      for(Object entry : effectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps,"Effect_Aura_Applied_Effect","Effect_Aura_Applied_Effect_Spellcraft");
        effect.addAppliedEffect(generator);
      }
    }
  }
}
