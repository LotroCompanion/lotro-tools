package delta.games.lotro.tools.dat.effects;

import java.io.File;
import java.util.List;

import delta.games.lotro.common.effects.Effect2;
import delta.games.lotro.common.effects.io.xml.EffectXMLParser2;
import delta.games.lotro.common.effects.io.xml.EffectXMLWriter2;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Test read then write of the effects file.
 * @author DAM
 */
public class MainTestEffectsReadWrite
{
  private void doIt()
  {
    EffectXMLParser2 p=new EffectXMLParser2(null);
    List<Effect2> effects=p.parseEffectsFile(GeneratedFiles.EFFECTS2);
    File to=new File(GeneratedFiles.EFFECTS2.getParentFile(),"effect2-rewrite.xml");
    EffectXMLWriter2 w=new EffectXMLWriter2();
    w.write(to,effects);
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainTestEffectsReadWrite().doIt();
  }
}
