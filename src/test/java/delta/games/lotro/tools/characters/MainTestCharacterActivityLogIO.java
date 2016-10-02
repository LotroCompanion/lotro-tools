package delta.games.lotro.tools.characters;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.MyLotroConfig;
import delta.games.lotro.character.CharacterFile;
import delta.games.lotro.character.log.CharacterLog;
import delta.games.lotro.character.log.CharacterLogsManager;
import delta.games.lotro.character.log.LotroTestUtils;

/**
 * Test for character log parsing.
 * @author DAM
 */
public class MainTestCharacterActivityLogIO
{
  /**
   * Basic main method for test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    List<CharacterFile> toons=new ArrayList<CharacterFile>();
    LotroTestUtils utils=new LotroTestUtils();
    CharacterFile glumlug=utils.getMainToon();
    toons.add(glumlug);

    MyLotroConfig cfg=MyLotroConfig.getInstance();
    CharacterLogPageParser parser=new CharacterLogPageParser();
    for(CharacterFile toon : toons)
    {
      String url=cfg.getCharacterURL(toon.getServerName(),toon.getName());
      CharacterLog log=parser.parseLogPages(url,null);
      if (log!=null)
      {
        System.out.println(log);
        CharacterLogsManager manager=toon.getLogsManager();
        boolean ok=manager.writeNewLog(log);
        if (ok)
        {
          System.out.println("OK");
        }
      }
    }
  }
}
