package delta.games.lotro.tools.dat.agents.npcs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.agents.npcs.io.xml.NPCsXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get NPCs definitions from DAT files.
 * @author DAM
 */
public class MainDatNPCsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatNPCsLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatNPCsLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("npc",facade.getGlobalStringsManager());
  }

  private NpcDescription load(int npcId)
  {
    NpcDescription ret=null;
    PropertiesSet properties=_facade.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      // Name
      String npcName=_i18n.getNameStringProperty(properties,"Name",npcId,I18nUtils.OPTION_REMOVE_MARKS);
      ret=new NpcDescription(npcId,npcName);
      // Title
      String title=_i18n.getStringProperty(properties,"OccupationTitle",I18nUtils.OPTION_REMOVE_TRAILING_MARK);
      ret.setTitle(title);
    }
    else
    {
      LOGGER.warn("Could not handle NPC ID="+npcId);
    }
    return ret;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return (classDefIndex==WStateClass.NPC);
    }
    return false;
  }

  /**
   * Load NPCs.
   */
  public void doIt()
  {
    List<NpcDescription> npcs=new ArrayList<NpcDescription>();
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        NpcDescription npc=load(id);
        if (npc!=null)
        {
          npcs.add(npc);
        }
      }
    }
    // Save
    // - data
    boolean ok=NPCsXMLWriter.writeNPCsFile(GeneratedFiles.NPCS,npcs);
    if (ok)
    {
      LOGGER.info("Wrote NPCs file: "+GeneratedFiles.NPCS);
    }
    // - labels
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=new DataFacade();
    new MainDatNPCsLoader(facade).doIt();
    facade.dispose();
  }
}
