  package delta.games.lotro.tools.dat.utils;

  import java.util.HashMap;
  import java.util.Map;

  import delta.games.lotro.dat.data.DataFacade;
  import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Mobs loader.
 * @author DAM
 */
public class MobLoader
{
  //private static final Logger LOGGER=Logger.getLogger(MobLoader.class);

  private static Map<Integer,String> _names=new HashMap<Integer,String>();

  /**
   * Load a mob.
   * @param facade Data facade.
   * @param mobId Mob identifier.
   * @return the mob name.
   */
  public static String loadMob(DataFacade facade, int mobId)
  {
    String ret=_names.get(Integer.valueOf(mobId));
    if (ret==null)
    {
      PropertiesSet properties=facade.loadProperties(mobId+0x09000000);
      if (properties!=null)
      {
        ret=DatUtils.getStringProperty(properties,"Name");
      }
    }
    return ret;
  }
}
