package delta.games.lotro.tools.dat.items.legendary;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.legendary2.global.LegendaryData2;
import delta.games.lotro.lore.items.legendary2.global.io.xml.LegendaryData2XMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loader for data related to the legendary items system (reloaded).
 * @author DAM
 */
public class MainDatLegendarySystem2Loader
{
  private DataFacade _facade;
  private LegendaryData2 _data;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatLegendarySystem2Loader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load legendary system data.
   */
  public void doIt()
  {
    _data=new LegendaryData2();
    loadLegendaryData();
    save();
  }

  /**
   * Get the loaded data.
   * @return the loaded data.
   */
  public LegendaryData2 getData()
  {
    return _data;
  }

  /**
   * Load legendary data.
   */
  private void loadLegendaryData()
  {
    // Load properties for ItemAdvancementControl
    PropertiesSet itemAdvancementControlProps=_facade.loadProperties(0x7000EAA6+DATConstants.DBPROPERTIES_OFFSET);

    // Character level to item level progression
    int progressionID=((Integer)itemAdvancementControlProps.getProperty("ItemAdvancement_MaxILevelProgression")).intValue();
    Progression progression=DatStatUtils.getProgression(_facade,progressionID);
    _data.setCharacterLevel2ItemLevelProgression(progression);
  }

  private void save()
  {
    LegendaryData2XMLWriter.write(GeneratedFiles.LEGENDARY_DATA2,_data);
    // Save progressions
    DatStatUtils._progressions.writeToFile(GeneratedFiles.PROGRESSIONS_LEGENDARY);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MainDatLegendarySystem2Loader loader=new MainDatLegendarySystem2Loader(facade);
    loader.doIt();
    facade.dispose();
  }
}
