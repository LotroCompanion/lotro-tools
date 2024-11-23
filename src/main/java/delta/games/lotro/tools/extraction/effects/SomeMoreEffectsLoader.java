package delta.games.lotro.tools.extraction.effects;

import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Loads more effects (difficulty effects).
 * @author DAM
 */
public class SomeMoreEffectsLoader
{
  private static final int[] IDS= {
      // Difficulty effects
      1879414605, // Difficulty 1: Adventurous
      1879414607, // Difficulty 2: Daring
      1879414608, // Difficulty 3: Fearless
      1879416240, // Difficulty 4: Fearless +1
      1879419739, // Difficulty 5: Fearless +2
      1879419742, // Difficulty 6: Fearless +3
      1879419743, // Difficulty 7: Heroic
      1879419740, // Difficulty 8: Heroic +1
      1879419741, // Difficulty 9: Heroic +2
      // Others
      1879415437, // DNT - landscape difficulty effect trigger
      1879463881, // DNT - Difficulty currency t3
  };

  private EffectLoader _effectsLoader;

  /**
   * Constructor.
   * @param effectsLoader Effects loader.
   */
  public SomeMoreEffectsLoader(EffectLoader effectsLoader)
  {
    _effectsLoader=effectsLoader;
  }

  /**
   * Do it!
   */
  public void doIt()
  {
    for(int id : IDS)
    {
      _effectsLoader.getEffect(id);
    }
    _effectsLoader.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    SomeMoreEffectsLoader loader=new SomeMoreEffectsLoader(effectsLoader);
    loader.doIt();
  }
}
