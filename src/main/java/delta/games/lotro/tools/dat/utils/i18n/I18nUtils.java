package delta.games.lotro.tools.dat.utils.i18n;

import java.util.Set;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.strings.GlobalStringsManager;
import delta.games.lotro.dat.data.strings.StringInfo;
import delta.games.lotro.dat.data.strings.StringInfoUtils;
import delta.games.lotro.dat.data.strings.StringsManager;
import delta.games.lotro.dat.data.strings.TableEntryStringInfo;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.tools.dat.GeneratedFiles;

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
  private DefaultStringProcessor _processor;

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
    _processor=new DefaultStringProcessor();
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
    _processor.setOptions(options);
    return getNameStringProperty(props,propertyName,id,_processor);
  }

  /**
   * Get the value of a string property.
   * As a side-effect, resolve the localized strings if any.
   * @param props Source properties.
   * @param propertyName Property name.
   * @param id Identifier (used as i18n key).
   * @param processor Processor to apply on the loaded strings.
   * @return A non-localized string or a string in the default locale.
   */
  public String getNameStringProperty(PropertiesSet props, String propertyName, int id, StringProcessor processor)
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
      if (processor==null)
      {
        _processor.setOptions(0);
        processor=_processor;
      }
      TableEntryStringInfo stringInfo=(TableEntryStringInfo)complement;
      String defaultValue=getDefaultValue(stringInfo,processor);
      handleStringInfo(stringInfo,key,defaultValue,processor);
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
    _processor.setOptions(options);
    return getStringProperty(props,propertyName,null,_processor);
  }

  /**
   * Get the value of a string property.
   * As a side-effect, resolve the localized strings if any.
   * @param props Source properties.
   * @param propertyName Property name.
   * @param keyToSet Key to use (<code>null</code> to use default key generation).
   * @param processor Processor to apply on the loaded strings.
   * @return A non-localized string or a localization key.
   */
  public String getStringProperty(PropertiesSet props, String propertyName, String keyToSet, StringProcessor processor)
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
      String key=(keyToSet==null)?getKey(stringInfo):keyToSet;
      if (processor==null)
      {
        _processor.setOptions(0);
        processor=_processor;
      }
      String defaultValue=getDefaultValue(stringInfo,processor);
      boolean useLocalisation=handleStringInfo(stringInfo,key,defaultValue,processor);
      return useLocalisation?key:"";
    }
    Object value=propertyValue.getValue();
    return DatStringUtils.getString(value);
  }

  /**
   * Get the value of an enum entry.
   * As a side-effect, resolve the localized strings if any.
   * @param enumMapper Enum mapper.
   * @param token Enum entry code.
   * @param options Options to apply on the loaded strings.
   * @return A non-localized string or a localization key.
   */
  public String getEnumValue(EnumMapper enumMapper, int token, int options)
  {
    StringInfo enumValue=enumMapper.getStringInfo(token);
    if (enumValue instanceof TableEntryStringInfo)
    {
      TableEntryStringInfo stringInfo=(TableEntryStringInfo)enumValue;
      String key=getKey(stringInfo);
      _processor.setOptions(options);
      String defaultValue=getDefaultValue(stringInfo,_processor);
      boolean useLocalisation=handleStringInfo(stringInfo,key,defaultValue,_processor);
      return useLocalisation?key:"";
    }
    return enumMapper.getLabel(token);
  }

  private String getDefaultValue(TableEntryStringInfo stringInfo, StringProcessor processor)
  {
    StringsManager defaultStringsMgr=_stringsMgr.getDefaultStringsManager();
    String defaultValue=StringInfoUtils.buildStringFormat(defaultStringsMgr,stringInfo);
    defaultValue=processor.processString(defaultValue);
    return defaultValue;
  }

  /**
   * Handle a string info.
   * @param stringInfo String info to handle.
   * @param key Localization key to use.
   * @param defaultValue Default value if localized label is not found.
   * @param processor String processor to apply on the loaded strings.
   * @return <code>true</code> to use localization, <code>false</code> otherwise.
   */
  private boolean handleStringInfo(TableEntryStringInfo stringInfo, String key, String defaultValue, StringProcessor processor)
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
      value=processor.processString(value);
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
   * Define a label.
   * @param locale Locale (see constants in DATL10nSupport.LOCALE_KEYS).
   * @param key I18n key.
   * @param value Label to use.
   */
  public void defineLabel(String locale, String key, String value)
  {
    _storage.setLabel(locale,key,value);
  }

  /**
   * Save loaded data.
   */
  public void save()
  {
    _storage.saveLabels(GeneratedFiles.LABELS,_setKey);
  }
}
