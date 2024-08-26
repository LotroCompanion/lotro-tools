package delta.games.lotro.tools.extraction.geo.landblocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DataIdentificationTools;

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

    List<String> names=new ArrayList<String>();
    for(Integer id : ids)
    {
      PropertiesSet props=facade.loadProperties(id.intValue()+DATConstants.DBPROPERTIES_OFFSET);
      String name=DataIdentificationTools.getNameFromProps(props);
      names.add(name);
    }
    Collections.sort(names);
    for(String name : names)
    {
      System.out.println(name);
    }
  }
}
