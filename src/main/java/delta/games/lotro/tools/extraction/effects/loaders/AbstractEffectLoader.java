package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.utils.Proxy;
import delta.games.lotro.utils.maths.Progression;

/**
 * Base class for single effect loaders.
 * @param <T> Type of managed effects.
 * @author DAM
 */
public abstract class AbstractEffectLoader<T extends Effect> implements EffectLoadingUtils
{
  private EffectLoadingUtils _utils;

  /**
   * Set the loading utilities.
   * @param utils Utilities to set.
   */
  public void setUtils(EffectLoadingUtils utils)
  {
    _utils=utils;
  }

  /**
   * Load effect specifics.
   * @param effect Effect to use.
   * @param effectProps Input properties.
   */
  @SuppressWarnings("unchecked")
  public void loadEffectSpecifics(Effect effect, PropertiesSet effectProps)
  {
    loadSpecifics((T)effect,effectProps);
  }

  /**
   * Load effect specifics.
   * @param effect Effect to use.
   * @param effectProps Input properties.
   */
  public abstract void loadSpecifics(T effect, PropertiesSet effectProps);

  @Override
  public Effect getEffect(int effectId)
  {
    return _utils.getEffect(effectId);
  }

  @Override
  public String getStringProperty(PropertiesSet props, String propertyName)
  {
    return _utils.getStringProperty(props,propertyName);
  }

  @Override
  public EffectGenerator loadGenerator(PropertiesSet generatorProps)
  {
    return _utils.loadGenerator(generatorProps);
  }

  @Override
  public void loadGenerator(PropertiesSet generatorProps, EffectGenerator generator)
  {
    _utils.loadGenerator(generatorProps,generator);
  }

  @Override
  public EffectGenerator loadGenerator(PropertiesSet generatorProps, String idPropName, String spellcraftPropName)
  {
    return _utils.loadGenerator(generatorProps,idPropName,spellcraftPropName);
  }

  @Override
  public void loadGenerator(PropertiesSet generatorProps, EffectGenerator generator, String idPropName, String spellcraftPropName)
  {
    _utils.loadGenerator(generatorProps,generator,idPropName,spellcraftPropName);
  }

  @Override
  public Proxy<Effect> buildProxy(Integer effectID)
  {
    return _utils.buildProxy(effectID);
  }

  @Override
  public PropertiesSet loadProperties(int id)
  {
    return _utils.loadProperties(id);
  }

  @Override
  public Progression getProgression(int id)
  {
    return _utils.getProgression(id);
  }

  @Override
  public ExtendedPosition getPositionForName(String telepad)
  {
    return _utils.getPositionForName(telepad);
  }
}
