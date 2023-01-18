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
  /**
   * Remove all [] marks.
   */
  public static final int OPTION_REMOVE_MARKS=1;
  /**
   * Remove only the trailing mark, if any.
   */
  public static final int OPTION_REMOVE_TRAILING_MARK=2;
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
   * @param options Options to apply on the loaded strings.
   * @return A non-localized string or a string in the default locale.
   */
  public String getNameStringProperty(PropertiesSet props, String propertyName, int id, int options)
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
      String defaultValue=getDefaultValue(stringInfo,options);
      handleStringInfo(stringInfo,key,defaultValue,options);
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
    return getStringProperty(props,propertyName,0);
  }

  /**
   * Get the value of a string property.
   * As a side-effect, resolve the localized strings if any.
   * @param props Source properties.
   * @param propertyName Property name.
   * @param options Options to apply on the loaded strings.
   * @return A non-localized string or a localization key.
   */
  public String getStringProperty(PropertiesSet props, String propertyName, int options)
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
      String defaultValue=getDefaultValue(stringInfo,options);
      boolean useLocalisation=handleStringInfo(stringInfo,key,defaultValue,options);
      return useLocalisation?key:"";
    }
    Object value=propertyValue.getValue();
    return DatStringUtils.getString(value);
  }

  private String getDefaultValue(TableEntryStringInfo stringInfo, int options)
  {
    StringsManager defaultStringsMgr=_stringsMgr.getDefaultStringsManager();
    String defaultValue=StringInfoUtils.buildStringFormat(defaultStringsMgr,stringInfo);
    defaultValue=filterValue(defaultValue,options);
    return defaultValue;
  }

  /**
   * Handle a string info.
   * @param stringInfo String info to handle.
   * @param key Localization key to use.
   * @param defaultValue Default value if localized label is not found.
   * @param options Options to apply on the loaded strings.
   * @return <code>null</code> to use localization, <code>false</code> otherwise.
   */
  private boolean handleStringInfo(TableEntryStringInfo stringInfo, String key, String defaultValue, int options)
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
      value=filterValue(value,options);
      if (value.length()>0)
      {
        _storage.setLabel(locale,key,value);
        hasLabel=true;
      }
    }
    return hasLabel;
  }

  private String filterValue(String value, int options)
  {
    value=DatStringUtils.cleanupString(value);
    if ((options&OPTION_REMOVE_MARKS)!=0)
    {
      value=StringUtils.removeMarks(value);
    }
    if ((options&OPTION_REMOVE_TRAILING_MARK)!=0)
    {
      value=DatStringUtils.fixName(value);
    }
    return value;
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
