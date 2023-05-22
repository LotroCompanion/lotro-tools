package delta.games.lotro.tools.dat.misc;

/**
 * A tool to migrate the old stats enum and its labels.
 * @author DAM
 */
public class MainBuildOldEnumStatLabels
{
  private void doIt()
  {
    doIt1();
    doIt2();
    doIt3();
  }

  private void doIt1()
  {
    /*
    StatsRegistry registry=StatsRegistry.getInstance();
    for(OldStatEnum entry : OldStatEnum.values())
    {
      String key=entry.getKey();
      String name=entry.getName();
      StatDescription stat=registry.getByKey(key);
      if (stat==null)
      {
        System.out.println("Stat not found: "+key);
      }
      if (name==null) name="";
      int id=stat.getIdentifier();
      String enName=stat.getInternalName();
      if (id>=0)
      {
        if (enName.equals(name))
        {
          name="";
        }
      }
      else
      {
        name=enName;
      }
      System.out.println(key+"="+name);
    }
    */
  }

  private void doIt2()
  {
    /*
    StatsRegistry registry=StatsRegistry.getInstance();
    for(OldStatEnum entry : OldStatEnum.values())
    {
      String key=entry.getKey();
      String name=entry.getName();
      StatDescription stat=registry.getByKey(key);
      if (stat==null)
      {
        System.out.println("Stat not found: "+key);
      }
      boolean statIsPercentage=stat.isPercentage();
      boolean entryIsPercentage=entry.isPercentage();
      if (statIsPercentage!=entryIsPercentage)
      {
        System.out.println("Warn: diff on "+key);
      }
      if (statIsPercentage)
      {
        System.out.println(key+"=");
      }
    }
    */
  }

  private void doIt3()
  {
    /*
    int nbKeys=OldStatEnum.values().length;
    StatsRegistry registry=StatsRegistry.getInstance();
    int keysCount=0;
    for(StatDescription stat : registry.getAll())
    {
      String key=stat.getLegacyKey();
      if ((key!=null) && (key.length()>0))
      {
        keysCount++;
      }
    }
    System.out.println("enum: "+nbKeys);
    System.out.println("Keys count: "+keysCount);
    */
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainBuildOldEnumStatLabels().doIt();
  }
}
