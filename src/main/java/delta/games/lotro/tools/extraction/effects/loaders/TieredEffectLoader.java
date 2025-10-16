package delta.games.lotro.tools.extraction.effects.loaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.effects.TieredEffect;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for 'tiered' effects.
 * @author DAM
 */
public class TieredEffectLoader extends AbstractEffectLoader<TieredEffect>
{
  private static final Logger LOGGER=LoggerFactory.getLogger(TieredEffectLoader.class);

  @Override
  public void loadSpecifics(TieredEffect effect, PropertiesSet effectProps)
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
    // Show in Examination
    Integer showInExaminationInt=(Integer)effectProps.getProperty("Effect_TierUp_ShowInExamination");
    boolean showInExamination=((showInExaminationInt!=null)&&(showInExaminationInt.intValue()==1));
    effect.setShowInExamination(showInExamination);
  }
}
