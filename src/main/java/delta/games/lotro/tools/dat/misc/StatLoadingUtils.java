package delta.games.lotro.tools.dat.misc;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import delta.common.utils.files.TextFileReader;
import delta.common.utils.i18n.MultilocalesTranslator;
import delta.common.utils.i18n.Translator;
import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.TextUtils;
import delta.common.utils.url.URLTools;
import delta.games.lotro.tools.dat.utils.i18n.TranslationUtils;

/**
 * Facilities for stat labels.
 * @author DAM
 */
public class StatLoadingUtils
{
  private MultilocalesTranslator _labels;
  private List<String> _premiumStatsKeys;
  private List<String> _percentages;

  /**
   * Constructor.
   */
  public StatLoadingUtils()
  {
    String bundleName=StatLoadingUtils.class.getPackage().getName()+".StatsLabels";
    _labels=TranslationUtils.buildMultilocalesTranslator(bundleName);
    // Keys
    _premiumStatsKeys=loadLines("PremiumStats.txt");
    // Percentages
    _percentages=loadLines("PercentageStats.txt");
  }

  private List<String> loadLines(String filename)
  {
    List<String> keys=new ArrayList<String>();
    URL url=URLTools.getFromClassPath(filename,this);
    TextFileReader r=new TextFileReader(url,EncodingNames.ISO8859_1);
    List<String> lines=TextUtils.readAsLines(r);
    for(String line : lines)
    {
      keys.add(line);
    }
    r.terminate();
    return keys;
  }

  /**
   * Get the premium stats.
   * @return A set of legacy keys for premium stats.
   */
  public List<String> getPremiumKeys()
  {
    return new ArrayList<String>(_premiumStatsKeys);
  }

  /**
   * Get the managed labels for the given locale.
   * @param locale Locale to use.
   * @return A set of legacy keys.
   */
  public Set<String> getLabelKeys(Locale locale)
  {
    Translator t=_labels.getTranslator(locale);
    return t.getKeys();
  }

  /**
   * Get a legacy stat name.
   * @param statKey Stat key.
   * @param locale Locale to use.
   * @return the localized label.
   */
  public String getStatLegacyName(String statKey, Locale locale)
  {
    Translator t=_labels.getTranslator(locale);
    return t.translate(statKey);
  }

  /**
   * Get stats to set as percentage stats.
   * @return A set of legacy keys.
   */
  public List<String> getPercentageStats()
  {
    return new ArrayList<String>(_percentages);
  }
}
