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
   * @return A string or a localization key.
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
      String ret=handleStringInfo(stringInfo);
      return ret;
    }
    Object value=propertyValue.getValue();
    return DatStringUtils.getString(value);
  }

  /**
   * Handle a string info.
   * @param stringInfo String info to handle.
   * @return the generated key.
   */
  public String handleStringInfo(TableEntryStringInfo stringInfo)
  {
    StringsManager defaultStringsMgr=_stringsMgr.getDefaultStringsManager();
    String defaultValue=StringInfoUtils.buildStringFormat(defaultStringsMgr,stringInfo);
    defaultValue=DatStringUtils.cleanupString(defaultValue);
    String key=getKey(stringInfo);
    for(String locale : _locales)
    {
      StringsManager stringsMgr=_stringsMgr.getStringsManager(locale);
      String value=StringInfoUtils.buildStringFormat(stringsMgr,stringInfo);
      if (value==null)
      {
        value=defaultValue;
      }
      value=DatStringUtils.cleanupString(value);
      _storage.setLabel(locale,key,value);
    }
    return key;
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
