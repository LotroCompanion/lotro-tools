package delta.games.lotro.tools.extraction.effects.loaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.effects.KillProcEffect;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;

/**
 * Loader for 'kill proc' effects.
 * @author DAM
 */
public class KillProcEffectLoader extends PropertyModificationEffectLoader<KillProcEffect>
{
  private static final Logger LOGGER=LoggerFactory.getLogger(KillProcEffectLoader.class);

  private LotroEnum<SubSpecies> _subSpeciesEnum;

  /**
   * Constructor.
   */
  public KillProcEffectLoader()
  {
    _subSpeciesEnum=LotroEnumsRegistry.getInstance().get(SubSpecies.class);
  }

  @Override
  public void loadSpecifics(KillProcEffect effect, PropertiesSet effectProps)
  {
    super.loadSpecifics(effect,effectProps);

    // 'caster' effects
    Object[] casterEffectsList=(Object[])effectProps.getProperty("EffectGenerator_CasterEffectList");
    if (casterEffectsList!=null)
    {
      for(Object entry : casterEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        effect.addCasterEffect(generator);
      }
    }
    // 'user' effects
    Object[] userEffectsList=(Object[])effectProps.getProperty("EffectGenerator_UserEffectList");
    if (userEffectsList!=null)
    {
      for(Object entry : userEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        effect.addUserEffect(generator);
      }
    }
    // Cooldown
    // - base
    float cooldown=((Float)effectProps.getProperty("Effect_KillProc_Cooldown")).floatValue();
    effect.setCooldown(cooldown);
    // - modifiers
    ModPropertyList cooldownModifiers=ModifiersUtils.getStatModifiers(effectProps,"Effect_KillProc_Cooldown_ModifierList");
    effect.setCooldownModifiers(cooldownModifiers);
    // Proc Probability
    // - base
    Float procProbability=(Float)effectProps.getProperty("Effect_KillProc_ProcProbability");
    if (procProbability!=null)
    {
      effect.setProbability(procProbability.floatValue());
    }
    // - modifiers
    ModPropertyList procProbabilityModifiers=ModifiersUtils.getStatModifiers(effectProps,"Effect_KillProc_ProcProbability_ModifierList");
    effect.setProbabilityModifiers(procProbabilityModifiers);
    // Require kill shot
    Integer requireKillShotInt=(Integer)effectProps.getProperty("Effect_KillProc_RequireKillShot");
    boolean requireKillShot=((requireKillShotInt!=null) && (requireKillShotInt.intValue()==1));
    effect.setRequiresKillShot(requireKillShot);
    // On self killed
    Integer onSelfKilledInt=(Integer)effectProps.getProperty("Effect_KillProc_OnSelfKilled");
    boolean onSelfKilled=((onSelfKilledInt!=null) && (onSelfKilledInt.intValue()==1));
    effect.setOnSelfKilled(onSelfKilled);
    // Target required species
    PropertyValue targetProperties=effectProps.getPropertyValueByName("Effect_KillProc_TargetRequiredProperties");
    if (targetProperties!=null)
    {
      if (targetProperties instanceof ArrayPropertyValue)
      {
        ArrayPropertyValue arrayProps=(ArrayPropertyValue)targetProperties;
        for(PropertyValue entry : arrayProps.getValues())
        {
          if ("Agent_Subspecies".equals(entry.getDefinition().getName()))
          {
            int speciesCode=((Integer)entry.getValue()).intValue();
            SubSpecies species=_subSpeciesEnum.getEntry(speciesCode);
            effect.setTargetRequiredSpecies(species);
          }
          else
          {
            LOGGER.warn("Unmanaged property value: {}",entry);
          }
        }
      }
    }
  }
}
