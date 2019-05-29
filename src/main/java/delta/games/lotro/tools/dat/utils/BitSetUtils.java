package delta.games.lotro.tools.dat.utils;

import java.util.BitSet;

import delta.games.lotro.dat.data.enums.EnumMapper;

/**
 * Utility methods related to bit sets.
 * @author DAM
 */
public class BitSetUtils
{
  /**
   * Build a BitSet from flags stored in a long.
   * @param bitset Input data.
   * @return A BitSet.
   */
  public static BitSet getBitSetFromFlags(long bitset)
  {
    BitSet ret=new BitSet(64);
    long mask=1;
    for(int i=1;i<=64;i++)
    {
      if ((bitset&mask)!=0)
      {
        ret.set(i-1);
      }
      mask<<=1;
    }
    return ret;
  }

  /**
   * Build a BitSet from flags stored in a long.
   * @param bitset Input data.
   * @return A BitSet.
   */
  public static BitSet getBitSetFromFlags(int bitset)
  {
    BitSet ret=new BitSet(32);
    int mask=1;
    for(int i=1;i<=32;i++)
    {
      if ((bitset&mask)!=0)
      {
        ret.set(i-1);
      }
      mask<<=1;
    }
    return ret;
  }

  /**
   * Build a string from a bit set and an enum mapper.
   * @param data Bit set.
   * @param enumMapper Enum mapper.
   * @param separator Separator to use.
   * @return A string or <code>null</code> if no bit set.
   */
  public static String getStringFromBitSet(BitSet data, EnumMapper enumMapper, String separator)
  {
    int length=data.length();
    if (length>0)
    {
      StringBuilder sb=new StringBuilder();
      for(int i=0;i<length;i++)
      {
        if (data.get(i))
        {
          String itemStr=enumMapper.getString(i+1);
          if (itemStr!=null)
          {
            if (sb.length()>0)
            {
              sb.append(separator);
            }
            sb.append(itemStr);
          }
        }
      }
      return sb.toString();
    }
    return null;
  }
}
