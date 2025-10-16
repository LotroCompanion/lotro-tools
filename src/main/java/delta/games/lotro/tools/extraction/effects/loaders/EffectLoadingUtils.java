package delta.games.lotro.tools.extraction.effects.loaders;


import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.agents.AgentDescription;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.utils.Proxy;
import delta.games.lotro.utils.maths.Progression;

/**
 * Interface of the effect loading utilities.
 * @author DAM
 */
public interface EffectLoadingUtils
{
  /**
   * Get an effect using its identifier.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found/loaded.
   */
  Effect getEffect(int effectId);

  /**
   * Get the value of a string property.
   * @param props Source properties.
   * @param propertyName Name of the property to get.
   * @return A string.
   */
  String getStringProperty(PropertiesSet props, String propertyName);

  /**
   * Load a generator (use default properties).
   * @param generatorProps Generator properties.
   * @return the loaded generator.
   */
  EffectGenerator loadGenerator(PropertiesSet generatorProps);

  /**
   * Load an effect generator.
   * @param generatorProps Generator properties.
   * @param generator Storage for the loaded data.
   */
  void loadGenerator(PropertiesSet generatorProps, EffectGenerator generator);

  /**
   * Load an effect generator.
   * @param generatorProps Generator properties.
   * @param idPropName Effect ID property name.
   * @param spellcraftPropName Spellcraft property name.
   * @return the loaded generator.
   */
  EffectGenerator loadGenerator(PropertiesSet generatorProps, String idPropName, String spellcraftPropName);

  /**
   * Load an effect generator
   * @param generatorProps Generator properties.
   * @param generator Storage for the loaded data.
   * @param idPropName Effect ID property name.
   * @param spellcraftPropName Spellcraft property name.
   */
  void loadGenerator(PropertiesSet generatorProps, EffectGenerator generator, String idPropName, String spellcraftPropName);

  /**
   * Build an effect proxy (loading the proxied effect).
   * @param effectID Effect identifier.
   * @return the new proxy.
   */
  Proxy<Effect> buildProxy(Integer effectID);

  /**
   * Load a properties set.
   * @param id Identifier of the properties set to get.
   * @return the loaded properties or <code>null</code>.
   */
  PropertiesSet loadProperties(int id);

  /**
   * Get a progression (load it if necessary).
   * @param id Identifier of the progression to get.
   * @return the loaded properties or <code>null</code>.
   */
  Progression getProgression(int id);

  /**
   * Get the position for a telepad.
   * @param telepad Telepad identifier.
   * @return A position or <code>null</code> if not found.
   */
  ExtendedPosition getPositionForName(String telepad);

  /**
   * Build an agent.
   * @param objectID Agent identifier.
   * @param weenieType Object type.
   * @param name Agent name.
   * @return the new agent.
   */
  public static AgentDescription buildAgent(int objectID, int weenieType, String name)
  {
    AgentDescription agent=null;
    // Cannot use InteractableUtils.findInteractable() here because it's too soon!
    if (weenieType==131151) // RealNPC
    {
      NpcDescription npc=new NpcDescription(objectID,name);
      agent=npc;
    }
    else if (weenieType==65615) // Monster
    {
      MobDescription mob=new MobDescription(objectID,name);
      agent=mob;
    }
    return agent;
  }

  /**
   * Get a flag value.
   * @param props Input properties set.
   * @param propertyName Property name.
   * @return the flag value (<code>false</code> if property was not found)..
   */
  public static boolean getFlag(PropertiesSet props, String propertyName)
  {
    return getFlag(props,propertyName,false);
  }

  /**
   * Get a flag value.
   * @param props Input properties set.
   * @param propertyName Property name.
   * @param defaultValue Default value.
   * @return the flag value.
   */
  public static boolean getFlag(PropertiesSet props, String propertyName, boolean defaultValue)
  {
    Integer intValue=(Integer)props.getProperty(propertyName);
    if (intValue!=null)
    {
      return (intValue.intValue()==1);
    }
    return defaultValue;
  }
}
