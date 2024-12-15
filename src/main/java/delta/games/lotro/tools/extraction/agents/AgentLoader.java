package delta.games.lotro.tools.extraction.agents;

import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.agents.AgentDescription;
import delta.games.lotro.tools.extraction.effects.EffectLoader;

/**
 * Loading utilities for agents.
 * @author DAM
 */
public class AgentLoader
{
  /**
   * Load startup effects.
   * @param effectsLoader Effects loader.
   * @param props Properties.
   * @param agent Target agent.
   */
  public static void loadEffects(EffectLoader effectsLoader, PropertiesSet props, AgentDescription agent)
  {
    /*
    Effect_MonsterStartupEffect_Array: 
      #1: Effect_MonsterStartupEffect_Struct 
        Effect_StartupEffectID: 1879328773
        Effect_StartupEffectSpellcraft: -1.0
    */
    Object[] effectsList=(Object[])props.getProperty("Effect_MonsterStartupEffect_Array");
    if (effectsList!=null)
    {
      for(Object entry : effectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=effectsLoader.loadGenerator(entryProps,"Effect_StartupEffectID","Effect_StartupEffectSpellcraft");
        agent.addStartupEffect(generator);
      }
    }
  }
}
