package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.AIPetEffect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.agents.AgentDescription;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for 'AI pet' effects.
 * @author DAM
 */
public class AIPetEffectLoader extends AbstractEffectLoader<AIPetEffect>
{
  @Override
  public void loadSpecifics(AIPetEffect effect, PropertiesSet effectProps)
  {
    // Summon object
    int summonedObjectID=((Integer)effectProps.getProperty("Effect_AIPet_SummonObject")).intValue();
    Proxy<AgentDescription> agent=handleSummonedPet(summonedObjectID);
    effect.setAgent(agent);
    // Startup effects
    Object[] startupEffectsList=(Object[])effectProps.getProperty("Effect_MonsterStartupEffect_Array");
    if (startupEffectsList!=null)
    {
      for(Object entry : startupEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps,"Effect_StartupEffectID","Effect_StartupEffectSpellcraft");
        effect.addStartupEffect(generator);
      }
    }
    // 'Apply to master' effects
    Object[] applyToMasterEffectsList=(Object[])effectProps.getProperty("Effect_PetApplyToMasterEffect_Array");
    if (applyToMasterEffectsList!=null)
    {
      for(Object entry : applyToMasterEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps,"Effect_StartupEffectID","Effect_StartupEffectSpellcraft");
        effect.addApplyToMasterEffect(generator);
      }
    }
  }

  private Proxy<AgentDescription> handleSummonedPet(int objectID)
  {
    PropertiesSet props=loadProperties(objectID);
    String name=DatStringUtils.getStringProperty(props,"Name");
    name=DatStringUtils.fixName(name);
    int weenieType=((Integer)props.getProperty("WeenieType")).intValue();
    AgentDescription agent=EffectLoadingUtils.buildAgent(objectID,weenieType,name);
    if (agent!=null)
    {
      Proxy<AgentDescription> proxy=new Proxy<AgentDescription>();
      proxy.setId(objectID);
      proxy.setName(name);
      proxy.setObject(agent);
      return proxy;
    }
    return null;
  }
}
