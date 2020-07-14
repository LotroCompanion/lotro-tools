package delta.games.lotro.tools.dat.maps;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.tools.dat.utils.DidNamesUtils;

/**
 * Test class for the GeneratorLoader.
 * @author DAM
 */
public class MainTestGeneratorLoader
{
  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    Set<Integer> filter=new HashSet<Integer>();
    filter.add(Integer.valueOf(165)); // resource_scholar
    Set<Integer> ids=new GeneratorLoader(facade).handleGeneratorProfile(1879078543,filter);
    List<String> names=DidNamesUtils.getNamesForIds(facade,ids);
    for(String name : names)
    {
      System.out.println(name);
    }
  }
}
