package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.effects.InstantFellowshipEffect;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatStringUtils;

/**
 * Loader for 'instant fellowship' effects.
 * @author DAM
 */
public class InstantFellowshipEffectLoader extends AbstractEffectLoader<InstantFellowshipEffect>
{
  @Override
  public void loadSpecifics(InstantFellowshipEffect effect, PropertiesSet effectProps)
  {
    Object[] effectsList=(Object[])effectProps.getProperty("EffectGenerator_InstantFellowship_AppliedEffectList");
    // Effects
    for(Object entry : effectsList)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      EffectGenerator generator=loadGenerator(entryProps);
      effect.addEffect(generator);
    }
    // Flags
    Integer raidGroups=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToRaidGroups");
    if (raidGroups!=null)
    {
      effect.setAppliesToRaidGroups(raidGroups.intValue()==1);
    }
    Integer pets=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToPets");
    if (pets!=null)
    {
      effect.setAppliesToPets(pets.intValue()==1);
    }
    Integer target=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToTarget");
    if (target!=null)
    {
      effect.setAppliesToTarget(target.intValue()==1);
    }
    // String override
    String rawOverride=DatStringUtils.getStringProperty(effectProps,"Effect_InstantFellowship_StringOverrideForFellowship");
    if ((rawOverride!=null) && (!rawOverride.isEmpty()))
    {
      String override=getStringProperty(effectProps,"Effect_InstantFellowship_StringOverrideForFellowship");
      effect.setFellowshipStringOverride(override);
    }
    // Range
    Float range=(Float)effectProps.getProperty("Effect_InstantFellowship_MaxRange");
    if ((range!=null) && (range.floatValue()>0))
    {
      effect.setRange(range.floatValue());
    }
  }
}
