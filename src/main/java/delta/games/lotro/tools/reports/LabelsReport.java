package delta.games.lotro.tools.reports;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.common.utils.files.FilesFinder;
import delta.common.utils.files.TextFileWriter;
import delta.common.utils.files.filter.ExtensionPredicate;
import delta.common.utils.i18n.LabelsFacade;
import delta.common.utils.i18n.SingleLocaleLabelsManager;
import delta.games.lotro.config.DataFiles;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.Locales;

/**
 * Report of labels.
 * @author DAM
 */
public class LabelsReport
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

    File reportsDir=ReportsConstants.getDataReportsRootDir();
    reportsDir.mkdirs();
    File toFile=new File(reportsDir,"labels.txt");
    TextFileWriter writer=new TextFileWriter(toFile);
    writer.start();
    for(File labelFile : files)
    {
      String name=labelFile.getName();
      String labelKey=name.substring(0,name.length()-4);
      handleLabelsSet(labels,labelKey,writer);
    }
    writer.terminate();
  }

  private void handleLabelsSet(LabelsFacade labels, String labelsKey, TextFileWriter writer)
  {
    writer.writeNextLine("Labels set: "+labelsKey);
    SingleLocaleLabelsManager[] mgrs=new SingleLocaleLabelsManager[Locales.LOCALE_KEYS.length];
    int nbLocales=Locales.LOCALE_KEYS.length;
    Set<String> allKeys=new HashSet<String>();
    for(int i=0;i<nbLocales;i++)
    {
      mgrs[i]=labels.getLabelsMgr(labelsKey,Locales.LOCALE_KEYS[i]);
      allKeys.addAll(mgrs[i].getKeys());
    }
    List<String> keys=new ArrayList<String>(allKeys);
    Collections.sort(keys);
    for(String key : keys)
    {
      StringBuilder line=new StringBuilder();
      line.append(key);
      for(int i=0;i<nbLocales;i++)
      {
        line.append('\t');
        String value=mgrs[i].getLabel(key);
        line.append(value);
      }
      writer.writeNextLine(line.toString());
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new LabelsReport().doIt();
  }
}
