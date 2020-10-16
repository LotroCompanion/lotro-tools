package delta.games.lotro.tools.dat.maps.data;

/**
 * Height map.
 * @author DAM
 */
public class HeightMap
{
  private int[] _heightMap;

  /**
   * Constructor.
   * @param heightMap Height map raw data.
   */
  public HeightMap(int[] heightMap)
  {
    _heightMap=heightMap;
  }

  /**
   * Get the height of center.
   * @return a height.
   */
  public float getCenterHeight()
  {
    return getHeightAt(16,16);
  }

  /**
   * Get the height approximation for the given point.
   * @param ox X position in landblock.
   * @param oy Y position in landblock.
   * @return the height value.
   */
  public float getHeight(float ox, float oy)
  {
    int x=(int)(ox/5); // 5 is 160/32
    int y=(int)(oy/5);
    float h1=getHeightAt(x,y);
    float h2=getHeightAt(x+1,y);
    float h3=getHeightAt(x,y+1);
    float h4=getHeightAt(x+1,y+1);
    return (h1+h2+h3+h4)/4;
  }

    /*
The heightmap is column-major: bottom left to top left, then next column etc.
The last column (east) and last row (north) are redundant:
they are shared with the neighbour blocks. It's typical to use 2^n+1
like this because with an odd number you can divide in half.
Be careful that you read these heightmap values as unsigned int.
To convert the heightmap value to the ox oy oz value, divide by 32.768.
You can lerp the points between heightmap nodes.
     */

  private float getHeightAt(int x, int y)
  {
    if (x<0) x=0;
    if (y<0) y=0;
    if (x>32) x=32;
    if (y>32) y=32;
    int value=_heightMap[x*33+y];
    return ((float)value)/32768;
  }
}
