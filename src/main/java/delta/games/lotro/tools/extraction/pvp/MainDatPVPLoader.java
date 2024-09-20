package delta.games.lotro.tools.extraction.pvp;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.pvp.Rank;
import delta.games.lotro.lore.pvp.RankScale;
import delta.games.lotro.lore.pvp.RankScaleEntry;
import delta.games.lotro.lore.pvp.RankScaleKeys;
import delta.games.lotro.lore.pvp.RanksManager;
import delta.games.lotro.lore.pvp.io.xml.PVPDataXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Get PVP data from DAT files.
 * @author DAM
 */
public class MainDatPVPLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatPVPLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatPVPLoader(DataFacade facade)
  {
    _facade=facade;
    _i18n=new I18nUtils("pvp",facade.getGlobalStringsManager());
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"GloryControl");
    RanksManager mgr=new RanksManager(false);
    // Renown
    RankScale renownScale=loadRankScale(RankScaleKeys.RENOWN,"GloryControl_RenownRankNameList","GloryControl_RankAdvancementTable",props);
    mgr.register(renownScale);
    // Infamy
    RankScale infamyScale=loadRankScale(RankScaleKeys.INFAMY,"GloryControl_InfamyRankNameList","GloryControl_RankAdvancementTable",props);
    mgr.register(infamyScale);
    // Prestige
    String advTablePropName=Context.isLive()?"GloryControl_PrestigeAdvancementTable":"GloryControl_RankAdvancementTable";
    RankScale prestigeScale=loadRankScale(RankScaleKeys.PRESTIGE,"GloryControl_PrestigeNameList",advTablePropName,props);
    mgr.register(prestigeScale);
    // Save
    boolean ok=new PVPDataXMLWriter().write(GeneratedFiles.PVP,mgr,EncodingNames.UTF_8);
    if (ok)
    {
      LOGGER.info("Wrote PVP file: "+GeneratedFiles.PVP);
    }
    // Labels
    _i18n.save();
  }

  private RankScale loadRankScale(String key, String rankNameListProperty, String advancementTableProperty, PropertiesSet props)
  {
    RankScale ret=new RankScale(key);

    int advancementTableId=((Integer)props.getProperty(advancementTableProperty)).intValue();
    long[] rankValues=loadRankTable(advancementTableId);
    // Rank names
    Object[] entries=(Object[])props.getProperty(rankNameListProperty);
    int index=0;
    for(Object entryObj : entries)
    {
      PropertiesSet entryProps=(PropertiesSet)entryObj;
      String rankName=_i18n.getStringProperty(entryProps,"GloryControl_Name",I18nUtils.OPTION_REMOVE_TRAILING_MARK);
      int code=((Integer)entryProps.getProperty("GloryControl_Value")).intValue();
      Rank rank=new Rank(code,rankName);
      int value=(int)rankValues[index];
      RankScaleEntry entry=new RankScaleEntry(value,rank);
      ret.addEntry(entry);
      index++;
    }
    return ret;
  }

  private long[] loadRankTable(int tableId)
  {
    WStateDataSet wstate=_facade.loadWState(tableId);
    int reference=wstate.getOrphanReferences().get(0).intValue();
    ClassInstance advancementTable=(ClassInstance)wstate.getValue(reference);
    long[] ret;
    if (Context.isLive())
    {
      ret=(long[])advancementTable.getAttributeValue("267720940");
    }
    else
    {
      @SuppressWarnings("unchecked")
      Map<Integer,Long> map=(Map<Integer,Long>)advancementTable.getAttributeValue("Attr0");
      ret=new long[map.size()+1];
      for(Map.Entry<Integer,Long> entry : map.entrySet())
      {
        ret[entry.getKey().intValue()]=entry.getValue().longValue();
      }
    }
    return ret;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    new MainDatPVPLoader(facade).doIt();
    facade.dispose();
  }
}
