package delta.games.lotro.tools.dat.characters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.wlib.ClassInstance;

/**
 * Get trait definitions from DAT files.
 * @author DAM
 */
public class MainTraitDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainTraitDataLoader.class);

  private DataFacade _facade;
  private Map<Integer,Integer> _traitIds2PropMap;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainTraitDataLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load trait data.
   */
  public void doIt()
  {
    loadPropertiesMap();
    loadTraits();
    SkimirshTraitsLoader skirmishTraitsLoader=new SkimirshTraitsLoader(_facade);
    skirmishTraitsLoader.doIt();
  }

  private void loadTraits()
  {
    TraitsManager traitsMgr=TraitsManager.getInstance();

    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        //System.out.println(classDefIndex);
        if ((classDefIndex==1477) || (classDefIndex==1478) || (classDefIndex==1483) ||
            (classDefIndex==1494) || (classDefIndex==2525) || (classDefIndex==3438) || (classDefIndex==3509))
        {
          // Traits
          TraitDescription trait=TraitLoader.loadTrait(_facade,did);
          if (trait!=null)
          {
            Integer propertyId=_traitIds2PropMap.get(Integer.valueOf(trait.getIdentifier()));
            if (propertyId!=null)
            {
              PropertyDefinition propertyDef=_facade.getPropertiesRegistry().getPropertyDef(propertyId.intValue());
              trait.setTierPropertyName(propertyDef.getName());
            }
            traitsMgr.registerTrait(trait);
          }
        }
      }
    }
    TraitLoader.saveTraits();
  }

  @SuppressWarnings("unchecked")
  private void loadPropertiesMap()
  {
    WStateDataSet wstate=_facade.loadWState(0x7000025B);
    List<Integer> refs=wstate.getOrphanReferences();
    if (refs.size()!=1)
    {
      LOGGER.warn("Unexpected number of references!");
      return;
    }
    ClassInstance traitControl=(ClassInstance)wstate.getValueForReference(refs.get(0).intValue());
    _traitIds2PropMap=new HashMap<Integer,Integer>();
    Map<Integer,Integer> props2traitIdsMap=(Map<Integer,Integer>)traitControl.getAttributeValue("m_aahVirtues");
    if (props2traitIdsMap==null)
    {
      props2traitIdsMap=(Map<Integer,Integer>)traitControl.getAttributeValue(0);
    }
    for(Map.Entry<Integer,Integer> entry : props2traitIdsMap.entrySet())
    {
      Integer oldValue=_traitIds2PropMap.put(entry.getValue(),entry.getKey());
      if (oldValue!=null)
      {
        LOGGER.warn("Multiple properties for trait: "+oldValue);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainTraitDataLoader(facade).doIt();
    facade.dispose();
  }
}
