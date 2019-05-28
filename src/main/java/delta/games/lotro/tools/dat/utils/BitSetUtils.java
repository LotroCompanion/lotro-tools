package delta.games.lotro.tools.dat.utils;

import java.util.BitSet;

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
}
