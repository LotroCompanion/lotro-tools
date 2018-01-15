package delta.games.lotro.tools.lore.deeds;

import java.io.File;
import java.util.List;
import java.util.Map;

import delta.games.lotro.plugins.LuaParser;

/**
 * Deeds loader from LotroCompendium data.
 * @author DAM
 */
public class LotroCompendiumDeedsLoader
{
  @SuppressWarnings("rawtypes")
  private void doIt() throws Exception
  {
    File root=new File(new File("data"),"deeds");
    File luaDb=new File(root,"deeds.lua");
    //File luaDb=new File("indexes.lua");
    LuaParser parser=new LuaParser();
    Object map=parser.readObject(luaDb);
    if (map instanceof Map)
    {
      System.out.println(((Map)map).keySet());
    }
    else if (map instanceof List)
    {
      int length=((List)map).size();
      System.out.println("Array of "+length+" items.");
    }
  }

  /**
   * Main method for this loader.
   * @param args Not used.
   * @throws Exception if a problem occurs.
   */
  public static void main(String[] args) throws Exception
  {
    new LotroCompendiumDeedsLoader().doIt();
  }
}
