package delta.games.lotro.tools.extraction.effects;

import java.util.Collections;
import java.util.List;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectsManager;
import delta.games.lotro.common.effects.io.xml.EffectXMLParser;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Load all currently defined effects and re-write them.
 * @author DAM
 */
public class MainExtractEffects
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    EffectsManager effectsMgr=EffectsManager.getInstance();
    List<Effect> effects=effectsMgr.getEffects();
    Collections.sort(effects,new IdentifiableComparator<Effect>());
    effects.clear();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    for(Effect effect : effects)
    {
      effectsLoader.getEffect(effect.getIdentifier());
    }
    effectsLoader.save();
  }
}
