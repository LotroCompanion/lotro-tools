package delta.games.lotro.tools.extraction.utils;

/**
 * Misc. utilities.
 * @author DAM
 */
public class Utils
{
  /**
   * Get a boolean value from an Integer property value.
   * @param value Input value.
   * @param defaultValue Default value.
   * @return A boolean value.
   */
  public static boolean getBooleanValue(Integer value, boolean defaultValue)
  {
    if (value==null)
    {
      return defaultValue;
    }
    return (value.intValue()==1);
  }

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
