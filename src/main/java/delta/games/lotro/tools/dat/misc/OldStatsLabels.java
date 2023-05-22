package delta.games.lotro.tools.dat.misc;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import delta.common.utils.files.TextFileReader;
import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.TextUtils;
import delta.common.utils.url.URLTools;
import delta.games.lotro.dat.archive.DATL10nSupport;

/**
 * Facilities for stat labels.
 * @author DAM
 */
public class OldStatsLabels
{
  private List<String> _keys;
  private ResourceBundle _en;
  private ResourceBundle _de;
  private ResourceBundle _fr;
  private ResourceBundle _percentages;

  /**
   * Constructor.
   */
  public OldStatsLabels()
  {
    String bundleName=OldStatsLabels.class.getName();
    _en=ResourceBundle.getBundle(bundleName,Locale.ENGLISH);
    _de=ResourceBundle.getBundle(bundleName,Locale.GERMAN);
    _fr=ResourceBundle.getBundle(bundleName,Locale.FRENCH);
    // Keys
    _keys=loadKeys();
    // Percentages
    _percentages=ResourceBundle.getBundle(bundleName.replace("Labels","Percentages"),Locale.ENGLISH);
  }

  private List<String> loadKeys()
  {
    List<String> keys=new ArrayList<String>();
    URL url=URLTools.getFromClassPath("OldStatsLabels.properties",this);
    TextFileReader r=new TextFileReader(url,EncodingNames.ISO8859_1);
    List<String> lines=TextUtils.readAsLines(r);
    for(String line : lines)
    {
      int equalsIndex=line.indexOf('=');
      if (equalsIndex!=-1)
      {
        String key=line.substring(0,equalsIndex);
        keys.add(key);
      }
    }
    return keys;
  }

  /**
   * Get the managed keys.
   * @return A set of legacy keys.
   */
  public List<String> getKeys()
  {
    return new ArrayList<String>(_keys);
  }

  /**
   * Get a legacy stat name.
   * @param statKey Stat key.
   * @param locale Locale to use.
   * @return the localized label.
   */
  public String getStatLegacyName(String statKey, String locale)
  {
    if (DATL10nSupport.EN.equals(locale)) return _en.getString(statKey);
    if (DATL10nSupport.DE.equals(locale)) return _de.getString(statKey);
    if (DATL10nSupport.FR.equals(locale)) return _fr.getString(statKey);
    return statKey;
  }

  /**
   * Indicates if the stat defined by the given legacy key is a percentage or not.
   * @param legacyKey Key to use.
   * @return <code>true</code> if it is, <code>false</code> otherwise.
   */
  public boolean isPercentage(String legacyKey)
  {
    return _percentages.containsKey(legacyKey);
  }
}
