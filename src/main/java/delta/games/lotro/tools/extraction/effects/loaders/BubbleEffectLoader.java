package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.BubbleEffect;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.utils.DatStatUtils;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loader for 'bubble' effects.
 * @author DAM
 */
public class BubbleEffectLoader extends CountDownEffectLoader<BubbleEffect>
{
  @Override
  public void loadSpecifics(BubbleEffect effect, PropertiesSet effectProps)
  {
    super.loadSpecifics(effect,effectProps);
    Integer vitalType=(Integer)effectProps.getProperty("Effect_Bubble_VitalType");
    StatDescription stat=DatStatUtils.getStatFromVitalType(vitalType.intValue());
    effect.setVital(stat);
    Float value=(Float)effectProps.getProperty("Effect_Bubble_Value");
    effect.setValue(value);
    Integer progressionID=(Integer)effectProps.getProperty("Effect_Bubble_Value_Progression");
    if (progressionID!=null)
    {
      Progression progression=getProgression(progressionID.intValue());
      effect.setProgression(progression);
    }
    Float percentage=(Float)effectProps.getProperty("Effect_Bubble_Percentage");
    effect.setPercentage(percentage);
    ModPropertyList modifier=ModifiersUtils.getStatModifiers(effectProps,"Effect_BubbleValue_ModifierList");
    effect.setModifiers(modifier);
  }
}
