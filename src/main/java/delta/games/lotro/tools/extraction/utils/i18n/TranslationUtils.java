package delta.games.lotro.tools.extraction.utils.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import delta.common.utils.i18n.MultilocalesTranslator;

/**
 * Translation utils.
 * @author DAM
 */
public class TranslationUtils
{
  /**
   * Build a translator that supports the LOTRO locales.
   * @param bundleName Bundle name.
   * @return A multi-locales translator.
   */
  public static MultilocalesTranslator buildMultilocalesTranslator(String bundleName)
  {
    List<Locale> locales=new ArrayList<Locale>();
    locales.add(Locale.ENGLISH);
    locales.add(Locale.FRENCH);
    locales.add(Locale.GERMAN);
    locales.add(new Locale("ru"));
    return new MultilocalesTranslator(bundleName,locales);
  }
}
