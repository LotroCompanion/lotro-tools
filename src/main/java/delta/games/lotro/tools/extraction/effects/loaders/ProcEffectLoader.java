package delta.games.lotro.tools.extraction.effects.loaders;

import java.util.BitSet;
import java.util.List;

import delta.games.lotro.common.effects.ProcEffect;
import delta.games.lotro.common.effects.ProcEffectGenerator;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillType;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;

/**
 * Loader for 'proc' effects.
 * @author DAM
 */
public class ProcEffectLoader extends PropertyModificationEffectLoader<ProcEffect>
{
  @Override
  public void loadSpecifics(ProcEffect effect, PropertiesSet effectProps)
  {
    super.loadSpecifics(effect,effectProps);

    Object[] userEffectsList=(Object[])effectProps.getProperty("EffectGenerator_SkillProc_UserEffectList");
    Object[] targetEffectsList=(Object[])effectProps.getProperty("EffectGenerator_SkillProc_TargetEffectList");

    // TODO Effect_SkillProc_RequiredCombatResult
    // Skill types
    Long skillTypeFlags=(Long)effectProps.getProperty("Effect_SkillProc_SkillTypes");
    if (skillTypeFlags!=null)
    {
      BitSet bitset=BitSetUtils.getBitSetFromFlags(skillTypeFlags.longValue());
      LotroEnum<SkillType> skillTypesEnum=LotroEnumsRegistry.getInstance().get(SkillType.class);
      List<SkillType> skillTypes=skillTypesEnum.getFromBitSet(bitset);
      effect.setSkillTypes(skillTypes);
    }
    // Probability
    Float probability=(Float)effectProps.getProperty("Effect_SkillProc_ProcProbability");
    effect.setProcProbability(probability);
    // Proc'ed effects
    if (userEffectsList!=null)
    {
      for(Object entry : userEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        ProcEffectGenerator generator=new ProcEffectGenerator();
        loadGenerator(entryProps,generator);
        generator.setOnTarget(false);
        effect.addProcedEffect(generator);
      }
    }
    if (targetEffectsList!=null)
    {
      for(Object entry : targetEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        ProcEffectGenerator generator=new ProcEffectGenerator();
        loadGenerator(entryProps,generator);
        generator.setOnTarget(true);
        effect.addProcedEffect(generator);
      }
    }
    // Cooldown
    Float cooldown=(Float)effectProps.getProperty("Effect_MinTimeBetweenProcs");
    effect.setCooldown(cooldown);
  }
}
