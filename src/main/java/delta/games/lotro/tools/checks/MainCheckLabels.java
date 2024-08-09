package delta.games.lotro.tools.checks;

import java.io.File;
import java.util.List;

import delta.common.utils.files.FilesFinder;
import delta.common.utils.files.filter.ExtensionPredicate;
import delta.common.utils.i18n.LabelsFacade;
import delta.common.utils.i18n.SingleLocaleLabelsManager;
import delta.common.utils.io.Console;
import delta.games.lotro.config.DataFiles;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.Locales;

/**
 * Check consistency of labels.
 * @author DAM
 */
public class MainCheckLabels
{
  private void doIt()
  {
    File rootDir=LotroCoreConfig.getInstance().getFile(DataFiles.LABELS);
    LabelsFacade labels=new LabelsFacade(rootDir);
    // Find label files
    ExtensionPredicate predicate=new ExtensionPredicate(".xml");
    FilesFinder finder=new FilesFinder();
    File enLabelsDir=new File(rootDir,Locales.EN);
    List<File> files=finder.find(FilesFinder.ABSOLUTE_MODE,enLabelsDir,predicate,false);
    for(File labelFile : files)
    {
      String name=labelFile.getName();
      String labelKey=name.substring(0,name.length()-4);
      handleLabelsSet(labels,labelKey);
    }
  }

  private void handleLabelsSet(LabelsFacade labels, String labelsKey)
  {
    Console.println("Labels set: "+labelsKey);
    SingleLocaleLabelsManager enMgr=labels.getLabelsMgr(labelsKey,Locales.EN);
    List<String> keys=enMgr.getKeys();
    int nbEnLabels=keys.size();
    for(String localeKey : Locales.LOCALE_KEYS)
    {
      // Skip english
      if (Locales.EN.equals(localeKey))
      {
        continue;
      }
      SingleLocaleLabelsManager localeMgr=labels.getLabelsMgr(labelsKey,localeKey);
      int nbLabels=localeMgr.getKeys().size();
      if (nbLabels!=nbEnLabels)
      {
        Console.println("\tLabels count mismatch: "+Locales.EN+"="+nbEnLabels+", "+localeKey+"="+nbLabels);
      }
      for(String key : keys)
      {
        String label=localeMgr.getLabel(key);
        if (label==null)
        {
          Console.println("\tLabel '"+key+"' not defined for locale "+localeKey);
        }
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainCheckLabels().doIt();
  }
}
