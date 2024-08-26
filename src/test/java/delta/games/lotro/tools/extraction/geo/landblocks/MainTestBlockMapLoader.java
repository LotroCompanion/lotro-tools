package delta.games.lotro.tools.extraction.geo.landblocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Test class for the BlockMapLoader.
 * @author DAM
 */
public class MainTestBlockMapLoader
{
  private Map<String,IntegerHolder> _counts=new HashMap<String,IntegerHolder>();
  private Map<String,Map<Integer,IntegerHolder>> _values=new HashMap<String,Map<Integer,IntegerHolder>>();

  private void doIt()
  {
    DataFacade facade=new DataFacade();
    BlockMapLoader loader=new BlockMapLoader(facade);
    /*
    PropertiesSet props=loader.loadPropertiesForMapBlock(1,243,248);
    if (props!=null)
    {
      System.out.println(props.dump());
    }
    */
    int blocksCount=0;
    for(int region=1;region<=1;region++)
    {
      for(int blockX=0;blockX<=0xFE;blockX++)
      {
        for(int blockY=0;blockY<=0xFE;blockY++)
        {
          PropertiesSet props=loader.loadPropertiesForMapBlock(region,blockX,blockY);
          if (props!=null)
          {
            blocksCount++;
            for(String propertyName : props.getPropertyNames())
            {
              IntegerHolder count=_counts.get(propertyName);
              if (count==null)
              {
                count=new IntegerHolder();
                _counts.put(propertyName,count);
              }
              count.increment();
              Object objValue=props.getProperty(propertyName);
              if (objValue instanceof Integer)
              {
                Integer value=(Integer)objValue;
                Map<Integer,IntegerHolder> values=_values.get(propertyName);
                if (values==null)
                {
                  values=new HashMap<Integer,IntegerHolder>();
                  _values.put(propertyName,values);
                }
                IntegerHolder valueCount=values.get(value);
                if (valueCount==null)
                {
                  valueCount=new IntegerHolder();
                  values.put(value,valueCount);
                }
                valueCount.increment();
              }
            }
          }
        }
      }
    }
    List<String> propertyNames=new ArrayList<String>(_counts.keySet());
    Collections.sort(propertyNames);
    for(String propertyName : propertyNames)
    {
      System.out.println(propertyName+" => "+_counts.get(propertyName));
      Map<Integer,IntegerHolder> valueCounts=_values.get(propertyName);
      if (valueCounts!=null)
      {
        int nbValues=valueCounts.size();
        System.out.println("\t"+nbValues+": "+valueCounts);
      }
      else
      {
        System.out.println("\tNo integer value!");
      }
    }
    System.out.println("Blocks count: "+blocksCount);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainTestBlockMapLoader().doIt();
  }
}
