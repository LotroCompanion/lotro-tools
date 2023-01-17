package delta.games.lotro.tools.dat.utils.i18n;

import java.util.Set;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.strings.GlobalStringsManager;
import delta.games.lotro.dat.data.strings.StringInfoUtils;
import delta.games.lotro.dat.data.strings.StringsManager;
import delta.games.lotro.dat.data.strings.TableEntryStringInfo;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.utils.StringUtils;

/**
 * Utility methods related to i18n for lotro-tools.
 * @author DAM
 */
public class I18nUtils
{
  private String _setKey;
  private LabelsStorage _storage;
  private GlobalStringsManager _stringsMgr;
  private Set<String> _locales;

  /**
   * Constructor.
   * @param setKey Key for labels set.
   * @param stringsMgr Strings manager to use.
   */
  public I18nUtils(String setKey, GlobalStringsManager stringsMgr)
  {
    _setKey=setKey;
    _storage=new LabelsStorage();
    _stringsMgr=stringsMgr;
    _locales=stringsMgr.getLocales();
  }

  /**
   * Get the value of a string property.
   * As a side-effect, resolve the localized strings if any.
   * @param props Source properties.
   * @param propertyName Property name.
   * @param id Identifier (used as i18n key).
   * @return A non-localized string or a string in the default locale.
   */
  public String getNameStringProperty(PropertiesSet props, String propertyName, int id)
  {
    PropertyValue propertyValue=props.getPropertyValueByName(propertyName);
    if (propertyValue==null)
    {
      return null;
    }
    Object complement=propertyValue.getComplement();
    if (complement instanceof TableEntryStringInfo)
    {
      String key=String.valueOf(id);
      TableEntryStringInfo stringInfo=(TableEntryStringInfo)complement;
      String defaultValue=getDefaultValue(stringInfo,true);
      handleStringInfo(stringInfo,key,defaultValue,true);
      return defaultValue;
    }
    Object value=propertyValue.getValue();
    return DatStringUtils.getString(value);
  }

  /**
   * Get the value of a string property.
   * As a side-effect, resolve the localized strings if any.
   * @param props Source properties.
   * @param propertyName Property name.
   * @return A non-localized string or a localization key.
   */
  public String getStringProperty(PropertiesSet props, String propertyName)
  {
    PropertyValue propertyValue=props.getPropertyValueByName(propertyName);
    if (propertyValue==null)
    {
      return null;
    }
    Object complement=propertyValue.getComplement();
    if (complement instanceof TableEntryStringInfo)
    {
      TableEntryStringInfo stringInfo=(TableEntryStringInfo)complement;
      String key=getKey(stringInfo);
      String defaultValue=getDefaultValue(stringInfo,false);
      boolean useLocalisation=handleStringInfo(stringInfo,key,defaultValue,false);
      return useLocalisation?key:"";
    }
    Object value=propertyValue.getValue();
    return DatStringUtils.getString(value);
  }

  private String getDefaultValue(TableEntryStringInfo stringInfo, boolean removeMarks)
  {
    StringsManager defaultStringsMgr=_stringsMgr.getDefaultStringsManager();
    String defaultValue=StringInfoUtils.buildStringFormat(defaultStringsMgr,stringInfo);
    if (removeMarks)
    {
      defaultValue=StringUtils.removeMarks(defaultValue);
    }
    defaultValue=DatStringUtils.cleanupString(defaultValue);
    return defaultValue;
  }

  /**
   * Handle a string info.
   * @param stringInfo String info to handle.
   * @param key Localization key to use.
   * @param defaultValue Default value if localized label is not found.
   * @param removeMarks Remove marks or not.
   * @return <code>null</code> to use localization, <code>false</code> otherwise.
   */
  private boolean handleStringInfo(TableEntryStringInfo stringInfo, String key, String defaultValue, boolean removeMarks)
  {
    if (defaultValue==null)
    {
      return false;
    }
    boolean hasLabel=false;
    for(String locale : _locales)
    {
      StringsManager stringsMgr=_stringsMgr.getStringsManager(locale);
      String value=StringInfoUtils.buildStringFormat(stringsMgr,stringInfo);
      if (value==null)
      {
        value=defaultValue;
      }
      if (removeMarks)
      {
        value=StringUtils.removeMarks(value);
      }
      value=DatStringUtils.cleanupString(value);
      if (value.length()>0)
      {
        _storage.setLabel(locale,key,value);
        hasLabel=true;
      }
    }
    return hasLabel;
  }

  private String getKey(TableEntryStringInfo stringInfo)
  {
    int tableId=stringInfo.getTableId();
    int tokenId=stringInfo.getTokenId();
    return "key:"+tableId+":"+tokenId;
  }

  /**
   * Save loaded data.
   */
  public void save()
  {
    _storage.saveLabels(GeneratedFiles.LABELS,_setKey);
  }
}
