package delta.games.lotro.tools.extraction.items;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SocketType;

/**
 * Utility methods related to socket.
 * @author DAM
 */
public class SocketUtils
{
  private static final Logger LOGGER=LoggerFactory.getLogger(SocketUtils.class);

  /**
   * Get the socket type from a socket code bitset.
   * @param code Input.
   * @return A socket type.
   */
  public static SocketType getSocketType(int code)
  {
    LotroEnumsRegistry enumsRegistry=LotroEnumsRegistry.getInstance();
    LotroEnum<SocketType> socketTypes=enumsRegistry.get(SocketType.class);
    List<SocketType> types=socketTypes.getFromBitSet(code);
    if (types.size()!=1)
    {
      LOGGER.warn("Unsupported socket type code: "+code);
      return null;
    }
    return types.get(0);
  }
}
