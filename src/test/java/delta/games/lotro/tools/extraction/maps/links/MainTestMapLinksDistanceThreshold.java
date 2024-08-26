package delta.games.lotro.tools.extraction.maps.links;

/**
 * Simple test class to assess the threshold used in LinksStorage.
 * @author DAM
 */
public class MainTestMapLinksDistanceThreshold
{
  private static final float THRESHOLD=0.0005f;

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    {
      float deltaLon=-51.333897f+51.331036f;
      float deltaLat=-30.044413f+30.04289f;
      float d2=deltaLat*deltaLat+deltaLon*deltaLon;
      System.out.println("Lalia's D2: "+d2);
      System.out.println("Test: (shall be true)"+(d2<THRESHOLD));
    }
    {
      float deltaLon=51.698338f-51.67031f;
      float deltaLat=17.216991f-17.21747f;
      float d2=deltaLat*deltaLat+deltaLon*deltaLon;
      System.out.println("Haunted burrow D2: "+d2);
      System.out.println("Test: (shall be false)"+(d2<THRESHOLD));
    }
    {
      float deltaLon=-33.578983f+33.578983f;
      float deltaLat=14.172265f-14.15874f;
      float d2=deltaLat*deltaLat+deltaLon*deltaLon;
      System.out.println("Castle Witch King (north) D2: "+d2);
      System.out.println("Test: (shall be true)"+(d2<THRESHOLD));
    }
    {
      float deltaLon=-34.550343f+34.56535f;
      float deltaLat=12.6227045f-12.645596f;
      float d2=deltaLat*deltaLat+deltaLon*deltaLon;
      System.out.println("Carn Dum Sewers D2: "+d2);
      System.out.println("Test: (shall be true)"+(d2<THRESHOLD));
    }
  }
}
