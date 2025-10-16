package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.RandomEffect;
import delta.games.lotro.common.effects.RandomEffectGenerator;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for 'random' effects.
 * @author DAM
 */
public class RandomEffectLoader extends AbstractEffectLoader<RandomEffect>
{
  @Override
  public void loadSpecifics(RandomEffect effect, PropertiesSet props)
  {
    /*
Effect_RandomEffect_Array: 
  #1: Effect_RandomEffect_Struct 
    Effect_RandomEffect_DataID: 1879103509
    Effect_RandomEffect_ToCaster: 1 (defaults to false)
    Effect_RandomEffect_Weight: 90.0
    also: Effect_RandomEffect_Spellcraft
    */
    Object[] entries=(Object[])props.getProperty("Effect_RandomEffect_Array");
    for(Object entryObj : entries)
    {
      PropertiesSet entryProps=(PropertiesSet)entryObj;
      RandomEffectGenerator generator=new RandomEffectGenerator();
      // Generator basics
      loadGenerator(entryProps,generator,"Effect_RandomEffect_DataID","Effect_RandomEffect_Spellcraft");
      // To caster?
      Integer toCasterInt=(Integer)entryProps.getProperty("Effect_RandomEffect_ToCaster");
      boolean toCaster=((toCasterInt!=null)&&(toCasterInt.intValue()==1));
      generator.setToCaster(toCaster);
      // Weight
      float weight=((Float)entryProps.getProperty("Effect_RandomEffect_Weight")).floatValue();
      generator.setWeight(weight);
      effect.addEffect(generator);
    }
  }
}
