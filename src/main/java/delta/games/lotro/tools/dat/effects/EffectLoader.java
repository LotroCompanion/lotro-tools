package delta.games.lotro.tools.dat.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.io.xml.EffectXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Loads effect data.
 * @author DAM
 */
public class EffectLoader
{
  private DataFacade _facade;
  private I18nUtils _i18nUtils;
  private Map<Integer,Effect> _loadedEffects;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public EffectLoader(DataFacade facade)
  {
    _facade=facade;
    _i18nUtils=new I18nUtils("effects",facade.getGlobalStringsManager());
    _loadedEffects=new HashMap<Integer,Effect>();
  }

  /**
   * Get an effect using its identifier.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found/loaded.
   */
  public Effect getEffect(int effectId)
  {
    Integer key=Integer.valueOf(effectId);
    if (_loadedEffects.containsKey(key))
    {
      return _loadedEffects.get(key);
    }
    Effect ret=loadEffect(effectId);
    _loadedEffects.put(key,ret);
    return ret;
  }

  /**
   * Load an effect.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found.
   */
  public Effect loadEffect(int effectId)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    if (effectProps==null)
    {
      return null;
    }
    byte[] data=_facade.loadData(effectId);
    if (data==null)
    {
      return null;
    }
    Effect ret=new Effect();
    ret.setId(effectId);
    // Name
    String effectName;
    if (_i18nUtils!=null)
    {
      effectName=_i18nUtils.getNameStringProperty(effectProps,"Effect_Name",effectId,0);
    }
    else
    {
      effectName=DatUtils.getStringProperty(effectProps,"Effect_Name");
    }
    ret.setName(effectName);
    // Description
    String description=getStringProperty(effectProps,"Effect_Definition_Description");
    ret.setDescription(description);

    // Icon
    Integer effectIconId=(Integer)effectProps.getProperty("Effect_Icon");
    if (effectIconId!=null)
    {
      ret.setIconId(effectIconId);
    }
    return ret;
  }

  private String getStringProperty(PropertiesSet props, String propertyName)
  {
    String ret;
    if (_i18nUtils!=null)
    {
      ret=_i18nUtils.getStringProperty(props,propertyName);
    }
    else
    {
      ret=DatUtils.getStringProperty(props,propertyName);
    }
    return ret;
  }

  /**
   * Save loaded data.
   */
  public void save()
  {
    List<Effect> effects=new ArrayList<Effect>(_loadedEffects.values());
    Collections.sort(effects,new IdentifiableComparator<Effect>());
    EffectXMLWriter.write(GeneratedFiles.EFFECTS,effects);
    _i18nUtils.save();
  }
}
