package delta.games.lotro.tools.extraction.loot;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;

/**
 * Access to loot probabilities.
 * @author DAM
 */
public class LootProbabilities
{
  private static final Logger LOGGER=LoggerFactory.getLogger(LootProbabilities.class);

  private DataFacade _facade;
  private EnumMapper _dropFrequency;
  private Map<Integer,Float> _probabilities;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public LootProbabilities(DataFacade facade)
  {
    _facade=facade;
    _dropFrequency=facade.getEnumsManager().getEnumMapper(587202656);
    _probabilities=new HashMap<Integer,Float>();
    loadProbabilities();
  }

  private void loadProbabilities()
  {
    boolean isLive=LotroCoreConfig.isLive();
    if (isLive)
    {
      loadProbabilitiesLive();
    }
    else
    {
      loadProbabilitiesSoA();
    }
  }

  private void loadProbabilitiesLive()
  {
    // LootGenControl:
    PropertiesSet properties=WeenieContentDirectory.loadWeenieContentProps(_facade,"LootGenControl");
    if (properties==null)
    {
      return;
    }
    Object[] tableArray=(Object[])properties.getProperty("LootGenControl_DropFrequencyTable");
    for(Object tableEntryObj : tableArray)
    {
      PropertiesSet entryProps=(PropertiesSet)tableEntryObj;
      float percentage=((Float)entryProps.getProperty("LootGenControl_DropFrequency_Percentage")).floatValue();
      int code=((Integer)entryProps.getProperty("LootGenControl_DropFrequency_Label")).intValue();
      _probabilities.put(Integer.valueOf(code),Float.valueOf(percentage));
      LOGGER.debug("Probability is "+percentage*100+" for "+_dropFrequency.getString(code));
    }
  }

  private void loadProbabilitiesSoA()
  {
    _probabilities.put(Integer.valueOf(4),Float.valueOf(1f)); // Always
    _probabilities.put(Integer.valueOf(9),Float.valueOf(0.7f)); // Common
    _probabilities.put(Integer.valueOf(12),Float.valueOf(0.68f)); // Trophy
    _probabilities.put(Integer.valueOf(10),Float.valueOf(0.6f)); // Frequent
    _probabilities.put(Integer.valueOf(3),Float.valueOf(0.25f)); // Uncommon
    _probabilities.put(Integer.valueOf(2),Float.valueOf(0.1f)); // Rare
    _probabilities.put(Integer.valueOf(5),Float.valueOf(0.05f)); // UltraRare
    _probabilities.put(Integer.valueOf(8),Float.valueOf(0.01f)); // ImpossiblyRare

    _probabilities.put(Integer.valueOf(11),Float.valueOf(1f)); // EpicRaid

    _probabilities.put(Integer.valueOf(16),Float.valueOf(0.75f)); // ReputationCommon
    _probabilities.put(Integer.valueOf(15),Float.valueOf(0.5f)); // ReputationFrequent
    _probabilities.put(Integer.valueOf(17),Float.valueOf(0.25f)); // ReputationUncommon
    _probabilities.put(Integer.valueOf(14),Float.valueOf(0.04f)); // ReputationRare
    _probabilities.put(Integer.valueOf(13),Float.valueOf(0.02f)); // ReputationVeryRare

    _probabilities.put(Integer.valueOf(20),Float.valueOf(0.6f)); // BarterCommon
    _probabilities.put(Integer.valueOf(22),Float.valueOf(0.4f)); // BarterFrequent
    _probabilities.put(Integer.valueOf(19),Float.valueOf(0.2f)); // BarterUncommon
    _probabilities.put(Integer.valueOf(21),Float.valueOf(0.1f)); // BarterRare
  }

  /**
   * Get a probability using a frequency code.
   * @param frequencyCode Input code.
   * @return A probability.
   */
  public float getProbability(int frequencyCode)
  {
    Float probability=_probabilities.get(Integer.valueOf(frequencyCode));
    if (probability==null)
    {
      LOGGER.warn("Missing probability: frequencyCode="+frequencyCode);
    }
    return (probability!=null)?probability.floatValue():0.0f;
  }
}
