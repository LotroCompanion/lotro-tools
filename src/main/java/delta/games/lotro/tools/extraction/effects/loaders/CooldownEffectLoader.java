package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.CooldownEffect;
import delta.games.lotro.common.enums.AICooldownChannel;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;

/**
 * Loader for 'cooldown' effects.
 * @author DAM
 */
public class CooldownEffectLoader extends AbstractEffectLoader<CooldownEffect>
{
  private LotroEnum<AICooldownChannel> _cooldownChannelsEnum;

  /**
   * Constructor.
   */
  public CooldownEffectLoader()
  {
    _cooldownChannelsEnum=LotroEnumsRegistry.getInstance().get(AICooldownChannel.class);
  }

  @Override
  public void loadSpecifics(CooldownEffect effect, PropertiesSet effectProps)
  {
    // Duration modifiers
    // - base
    float modifier=((Float)effectProps.getProperty("CooldownEffect_CooldownMod")).floatValue();
    effect.setBaseModifier(modifier);
    // Additional modifiers
    ModPropertyList modifiers=ModifiersUtils.getStatModifiers(effectProps,"Effect_CooldownMod_AdditiveModifiers");
    effect.setDurationModifiers(modifiers);
    // Skills
    Object[] skillsList=(Object[])effectProps.getProperty("CooldownEffect_Skill_List");
    if (skillsList!=null)
    {
      for(Object entry : skillsList)
      {
        Integer skillID=(Integer)entry;
        effect.addSkill(skillID.intValue());
      }
    }
    // Cooldown channels
    Object[] cooldownChannels=(Object[])effectProps.getProperty("CooldownEffect_CooldownChannel_List");
    if (cooldownChannels!=null)
    {
      for(Object entry : cooldownChannels)
      {
        Integer cooldownChannelCode=(Integer)entry;
        AICooldownChannel cooldownChannel=_cooldownChannelsEnum.getEntry(cooldownChannelCode.intValue());
        if (cooldownChannel!=null)
        {
          effect.addCooldownChannel(cooldownChannel);
        }
      }
    }
  }
}
