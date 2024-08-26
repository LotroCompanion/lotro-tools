package delta.games.lotro.tools.extraction.effects;

import java.io.File;
import java.util.Collections;
import java.util.List;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.io.xml.EffectXMLParser;
import delta.games.lotro.common.effects.io.xml.EffectXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;

/**
 * Test read then write of the effects file.
 * @author DAM
 */
public class MainTestEffectsReadWrite
{
  private void doIt()
  {
    EffectXMLParser p=new EffectXMLParser(null);
    List<Effect> effects=p.parseEffectsFile(GeneratedFiles.EFFECTS);
    Collections.sort(effects,new IdentifiableComparator<Effect>());
    File to=new File(GeneratedFiles.EFFECTS.getParentFile(),"effect-rewrite.xml");
    EffectXMLWriter w=new EffectXMLWriter();
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
