package delta.games.lotro.tools.dat.utils.i18n;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import delta.common.utils.i18n.SingleLocaleLabelsManager;
import delta.common.utils.i18n.io.xml.LabelsXMLWriter;
import delta.common.utils.text.EncodingNames;

/**
 * Storage for localized labels.
 * @author DAM
 */
public class LabelsStorage
{
  private Map<String,SingleLocaleLabelsManager> _labelsMgr;

  /**
   * Constructor.
   */
  public LabelsStorage()
  {
    _labelsMgr=new HashMap<String,SingleLocaleLabelsManager>();
  }

  /**
   * Get a label.
   * @param locale Locale.
   * @param key Key.
   * @return A label or <code>null</code> if not found.
   */
  public String getLabel(String locale, String key)
  {
    SingleLocaleLabelsManager mgr=getLabelsMgr(locale);
    return mgr.getLabel(key);
  }

  /**
   * Set the label for a given locale and key.
   * @param locale Locale.
   * @param key Key.
   * @param value Value to set.
   */
  public void setLabel(String locale, String key, String value)
  {
    SingleLocaleLabelsManager mgr=getLabelsMgr(locale);
    mgr.addLabel(key,value);
  }

  private SingleLocaleLabelsManager getLabelsMgr(String locale)
  {
    SingleLocaleLabelsManager mgr=_labelsMgr.get(locale);
    if (mgr==null)
    {
      mgr=new SingleLocaleLabelsManager(locale);
      _labelsMgr.put(locale,mgr);
    }
    return mgr;
  }

  /**
   * Save the labels to XML files.
   * @param toDir Base output directory.
   * @param setKey Key of the labels set.
   */
  public void saveLabels(File toDir, String setKey)
  {
    for(SingleLocaleLabelsManager mgr : _labelsMgr.values())
    {
      String locale=mgr.getLocale();
      File dir=new File(toDir,locale);
      String filename=setKey+".xml";
      File to=new File(dir,filename);
      new LabelsXMLWriter().write(to,mgr,EncodingNames.UTF_8);
    }
  }
}
