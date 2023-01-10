package delta.games.lotro.tools.wiki;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.archive.DATL10nSupport;
import delta.games.lotro.dat.data.DatConfiguration;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.StringUtils;

/**
 * Build a table with NPC names in all 3 languages.
 * @author DAM
 */
public class MainBuildNPCTable
{
  private static final Logger LOGGER=Logger.getLogger(MainBuildNPCTable.class);

  private DataFacade _facade;
  private DataFacade _facadeFR;
  private DataFacade _facadeDE;

  /**
   * Constructor.
   */
  public MainBuildNPCTable()
  {
    // EN
    DatConfiguration enCfg=new DatConfiguration();
    enCfg.setLocale(DATL10nSupport.EN);
    _facade=new DataFacade();
    // FR
    DatConfiguration frCfg=new DatConfiguration();
    frCfg.setLocale(DATL10nSupport.FR);
    _facadeFR=new DataFacade(frCfg);
    // DE
    DatConfiguration deCfg=new DatConfiguration();
    deCfg.setLocale(DATL10nSupport.DE);
    _facadeDE=new DataFacade(deCfg);
  }

  private void handleNpc(int npcId)
  {
    // Ignore test NPC
    if (npcId==1879074078)
    {
      return;
    }
    // English
    PropertiesSet enProperties=_facade.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
    String enName=null;
    if (enProperties!=null)
    {
      enName=DatUtils.getStringProperty(enProperties,"Name");
      enName=StringUtils.removeMarks(enName);
    }
    else
    {
      LOGGER.warn("Name not found in English!");
    }
    // French
    PropertiesSet frProperties=_facadeFR.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
    String frName=null;
    if (frProperties!=null)
    {
      frName=DatUtils.getStringProperty(frProperties,"Name");
      frName=StringUtils.fixName(frName);
    }
    else
    {
      LOGGER.warn("Name not found in French!");
    }
    // Deutsch
    PropertiesSet deProperties=_facadeDE.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
    String deName=null;
    if (deProperties!=null)
    {
      deName=DatUtils.getStringProperty(deProperties,"Name");
      deName=StringUtils.fixName(deName);
    }
    else
    {
      LOGGER.warn("Name not found in Deutsch!");
    }
    System.out.println(npcId+"\t"+enName+"\t"+frName+"\t"+deName);
  }

  /**
   * Load barter and vendor data.
   */
  public void doIt()
  {
    System.out.println("NPC ID\tEnglish Name\tFrench Name\tDeutsch Name");
    // Scan for NPCs
    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==WStateClass.NPC)
        {
          handleNpc(i);
        }
      }
    }
    _facade.dispose();
    _facadeFR.dispose();
    _facadeDE.dispose();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainBuildNPCTable().doIt();
  }
}
