package delta.games.lotro.tools.extraction.effects;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Load effects from hotspots.
 * @author DAM
 */
public class HotspotEffectsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(HotspotEffectsLoader.class);

  private DataFacade _facade;
  private EffectLoader _effectLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader.
   */
  public HotspotEffectsLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectLoader=effectsLoader;
  }

  private void load(int hotspotID)
  {
    PropertiesSet properties=_facade.loadProperties(hotspotID+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      LOGGER.warn("Could not handle hotspot ID="+hotspotID);
      return;
    }
    /*
EffectGenerator_HotspotEffectList: 
  #1: EffectGenerator_EffectStruct 
    EffectGenerator_EffectID: 1879323904
    EffectGenerator_EffectSpellcraft: -1.0
    */
    Object[] effectsList=(Object[])properties.getProperty("EffectGenerator_HotspotEffectList");
    if (effectsList!=null)
    {
      List<EffectGenerator> generators=new ArrayList<EffectGenerator>();
      for(Object entry : effectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=_effectLoader.loadGenerator(entryProps);
        generators.add(generator);
      }
      if (!generators.isEmpty())
      {
        System.out.println("Hotspot ID="+hotspotID+" => "+generators);
      }
    }
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return (classDefIndex==1170);
    }
    return false;
  }

  /**
   * Load NPCs.
   */
  public void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        load(id);
      }
    }
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
    new HotspotEffectsLoader(facade,effectsLoader).doIt();
    facade.dispose();
    effectsLoader.save();
  }
}
