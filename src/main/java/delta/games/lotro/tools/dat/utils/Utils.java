package delta.games.lotro.tools.dat.utils;

/**
 * Misc. utilities.
 * @author DAM
 */
public class Utils
{
  /**
   * Normalize a float value.
   * @param value Input value.
   * @return the result value.
   */
  public static Float normalize(Float value)
  {
    if (value==null)
    {
      return null;
    }
    if (value.floatValue()<0)
    {
      return null;
    }
    return value;
  }
}
