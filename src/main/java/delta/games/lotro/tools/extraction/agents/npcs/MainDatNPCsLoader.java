package delta.games.lotro.tools.extraction.agents.npcs;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.CharacterSex;
import delta.games.lotro.common.Genders;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.agents.npcs.io.xml.NPCsXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.agents.AgentLoader;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.effects.EffectLoader;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Get NPCs definitions from DAT files.
 * @author DAM
 */
public class MainDatNPCsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatNPCsLoader.class);

  private DataFacade _facade;
  private EffectLoader _effectLoader;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader.
   */
  public MainDatNPCsLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectLoader=effectsLoader;
    _i18n=new I18nUtils("npc",facade.getGlobalStringsManager());
  }

  private NpcDescription load(int npcId)
  {
    PropertiesSet properties=_facade.loadProperties(npcId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      LOGGER.warn("Could not handle NPC ID={}",Integer.valueOf(npcId));
      return null;
    }
    // Name
    String npcName=_i18n.getNameStringProperty(properties,"Name",npcId,I18nUtils.OPTION_REMOVE_MARKS);
    NpcDescription ret=new NpcDescription(npcId,npcName);
    // Gender
    CharacterSex gender=extractGender(properties);
    ret.setGender(gender);
    // Title
    String title=_i18n.getStringProperty(properties,"OccupationTitle",I18nUtils.OPTION_REMOVE_TRAILING_MARK);
    ret.setTitle(title);
    // Effects
    AgentLoader.loadEffects(_effectLoader,properties,ret);
    return ret;
  }

  private CharacterSex extractGender(PropertiesSet properties)
  {
    String name=DatStringUtils.getStringProperty(properties,"Name");
    String tag=DatStringUtils.extractTag(name);
    if (tag!=null)
    {
      tag=tag.toLowerCase();
      if (tag.contains("m"))
      {
        return Genders.MALE;
      }
      else if (tag.contains("f"))
      {
        return Genders.FEMALE;
      }
    }
    return null;
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
      LOGGER.info("Wrote NPCs file: {}",GeneratedFiles.NPCS);
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
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    new MainDatNPCsLoader(facade,effectsLoader).doIt();
    facade.dispose();
  }
}
