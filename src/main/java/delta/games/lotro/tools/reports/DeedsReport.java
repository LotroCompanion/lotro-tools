package delta.games.lotro.tools.reports;

import java.io.File;

import delta.common.utils.files.TextFileWriter;
import delta.common.utils.text.EndOfLine;
import delta.common.utils.variables.VariablesResolver;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.quests.AchievableProxiesResolver;
import delta.games.lotro.lore.quests.objectives.ObjectivesDisplayBuilder;
import delta.games.lotro.utils.html.HtmlOutput;
import delta.games.lotro.utils.html.SimpleLinkGenerator;
import delta.games.lotro.utils.strings.GenericOutput;
import delta.games.lotro.utils.strings.StringRendering;

/**
 * Report for deeds.
 * @author DAM
 */
public class DeedsReport
{
  private static void dumpDeeds(GenericOutput output, File toFile)
  {
    StringBuilder sb=new StringBuilder();
    output.startDocument(sb);
    output.startBody(sb);
    DeedsManager deedsMgr=DeedsManager.getInstance();
    VariablesResolver resolver=ReportUtils.buildRenderer();
    ObjectivesDisplayBuilder builder=new ObjectivesDisplayBuilder(resolver,output);
    for(DeedDescription deed : deedsMgr.getAll())
    {
      AchievableProxiesResolver.resolve(deed);
      output.startTitle(sb,3);
      output.printText(sb,deed.getIdentifier()+" - "+deed.getName());
      output.endTitle(sb,3);
      output.startBold(sb);
      output.printText(sb,"Description");
      output.endBold(sb);
      output.startParagraph(sb);
      String description=deed.getDescription();
      description=StringRendering.render(resolver,description);
      output.printText(sb,description);
      sb.append(EndOfLine.NATIVE_EOL);
      builder.build(sb,deed);
      sb.append(EndOfLine.NATIVE_EOL);
    }
    output.endBody(sb);
    output.endDocument(sb);
    TextFileWriter w=new TextFileWriter(toFile);
    w.start();
    w.writeSomeText(sb.toString());
    w.terminate();
  }

  /**
   * Do report.
   */
  public void doIt()
  {
    File rootDir=ReportsConstants.getDataReportsRootDir();
    rootDir.mkdirs();
    File toFile=new File(rootDir,"deeds.html");
    GenericOutput output=new HtmlOutput(new SimpleLinkGenerator());
    dumpDeeds(output,toFile);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new DeedsReport().doIt();
  }
}
