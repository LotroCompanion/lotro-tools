package delta.games.lotro.tools.dat.characters;

import delta.games.lotro.character.xp.XPTable;
import delta.games.lotro.character.xp.io.xml.XPTableXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;

/**
 * Loader for the XP table.
 * @author DAM
 */
public class MainXpTableLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainXpTableLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    XPTable table=loadXPForLevels();
    save(table);
  }

  private XPTable loadXPForLevels()
  {
    WStateDataSet wstate=_facade.loadWState(1879064041);
    int reference=wstate.getOrphanReferences().get(0).intValue();
    ClassInstance advancementTable=(ClassInstance)wstate.getValue(reference);
    long[] table=(long[])advancementTable.getAttributeValue("267720940");
    XPTable ret=new XPTable();
    ret.setXpTable(table);
    return ret;
  }

  private void save(XPTable xpTable)
  {
    XPTableXMLWriter.write(GeneratedFiles.XP_TABLE,xpTable);
  }

  /**
   * Main method for this loader.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    new MainXpTableLoader(facade).doIt();
  }
}
