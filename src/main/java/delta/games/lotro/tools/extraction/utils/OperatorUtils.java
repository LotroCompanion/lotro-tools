package delta.games.lotro.tools.extraction.utils;

import delta.games.lotro.common.utils.ComparisonOperator;

/**
 * Utility methods related to operators.
 * @author DAM
 */
public class OperatorUtils
{
  /**
   * Build a comparison operator from its internal code.
   * @param operatorCode Code to use.
   * @return An operator or <code>null</code> if not supported.
   */
  public static ComparisonOperator getComparisonOperatorFromCode(int operatorCode)
  {
    if (operatorCode==1) return ComparisonOperator.GREATER_OR_EQUAL;
    if (operatorCode==2) return ComparisonOperator.NOT_EQUAL;
    if (operatorCode==3) return ComparisonOperator.EQUAL;
    if (operatorCode==4) return ComparisonOperator.LESS;
    if (operatorCode==5) return ComparisonOperator.LESS_OR_EQUAL;
    if (operatorCode==6) return ComparisonOperator.GREATER;
    return null;
  }
}
