package delta.games.lotro.tools.dat.misc;

import java.util.Locale;
import java.util.ResourceBundle;

import delta.games.lotro.dat.archive.DATL10nSupport;

/**
 * Facilities for stat labels.
 * @author DAM
 */
public class OldStatsLabels
{
  private ResourceBundle _en;
  private ResourceBundle _de;
  private ResourceBundle _fr;

  /**
   * Constructor.
   */
  public OldStatsLabels()
  {
    String bundleName=OldStatEnum.class.getName();
    _en=ResourceBundle.getBundle(bundleName,Locale.ENGLISH);
    _de=ResourceBundle.getBundle(bundleName,Locale.GERMAN);
    _fr=ResourceBundle.getBundle(bundleName,Locale.FRENCH);
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
}
