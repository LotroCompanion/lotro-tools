package delta.games.lotro.tools.extraction.common.worldEvents;

import delta.common.utils.io.Console;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.worldEvents.WorldEvent;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;

/**
 * Loader for world events.
 * @author DAM
 */
public class MainWorldEventsLoader
{
  private DataFacade _facade;
  private WorldEventsLoader _loader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainWorldEventsLoader(DataFacade facade)
  {
    _facade=facade;
    _loader=new WorldEventsLoader(facade);
  }

  /**
   * Load world events.
   */
  public void doIt()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"WorldEventControl");
    Object[] ids=(Object[])props.getProperty("WorldEvent_WorldEventList");
    for(Object idObj : ids)
    {
      int id=((Integer)idObj).intValue();
      handleWorldEvent(id);
    }
  }

  private void handleWorldEvent(int worldEventId)
  {
    WorldEvent worldEvent=_loader.getWorldEvent(worldEventId);
    Console.println(worldEvent);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainWorldEventsLoader(facade).doIt();
    facade.dispose();
  }
}
