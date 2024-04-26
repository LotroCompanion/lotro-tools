package delta.games.lotro.tools.dat.skills.pets;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.collections.pets.CosmeticPetDescription;
import delta.games.lotro.tools.dat.agents.ClassificationLoader;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get definition of cosmetic pets from DAT files.
 * @author DAM
 */
public class CosmeticPetLoader
{
  private DataFacade _facade;
  private ClassificationLoader _classificationLoader;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param i18n I18n utils.
   */
  public CosmeticPetLoader(DataFacade facade, I18nUtils i18n)
  {
    _facade=facade;
    _i18n=i18n;
    _classificationLoader=new ClassificationLoader(facade);
  }

  /**
   * Load a cosmetic pet definition.
   * @param properties Input properties.
   * @param ret Storage for loaded data.
   */
  public void loadPetData(PropertiesSet properties, CosmeticPetDescription ret)
  {
    // Hidden?
    @SuppressWarnings("unused")
    int hidden=((Integer)properties.getProperty("Collection_Hide_Entry")).intValue();
    // Source description (null for war-steeds)
    String sourceDescription=_i18n.getStringProperty(properties,"Collection_Piece_SourceDesc");
    ret.setSourceDescription(sourceDescription);
    Object[] effectsList=(Object[])properties.getProperty("Skill_Toggle_Effect_List");
    if (effectsList!=null)
    {
      for(Object effectObj : effectsList)
      {
        PropertiesSet effectRefProps=(PropertiesSet)effectObj;
        int effectId=((Integer)effectRefProps.getProperty("Skill_Toggle_Effect")).intValue();
        handleCosmeticPetEffect(ret,effectId);
      }
    }
  }

  private void handleCosmeticPetEffect(CosmeticPetDescription pet, int effectId)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);

    Object[] mods=(Object[])effectProps.getProperty("Mod_Array");
    for(Object modObj : mods)
    {
      PropertiesSet modProps=(PropertiesSet)modObj;
      Integer sourceId=(Integer)modProps.getProperty("CosmeticEntity_CosmeticPet_ForwardSource");
      if (sourceId!=null)
      {
        handleSourceId(pet,sourceId.intValue());
      }
    }
  }

  private void handleSourceId(CosmeticPetDescription pet, int sourceId)
  {
    PropertiesSet sourceProps=_facade.loadProperties(sourceId+DATConstants.DBPROPERTIES_OFFSET);

    /*
     * CosmeticEntity_CosmeticEntity: 1879305757
     * CosmeticEntity_Distance: 3.0
     * CosmeticEntity_Following_Speed: 10.0
     * CosmeticEntity_MovementType: 2 (Following)
     */
    // Initial Name
    String initialName=_i18n.getStringProperty(sourceProps,"CosmeticEntity_Name_Initial",I18nUtils.OPTION_REMOVE_MARKS);
    pet.setInitialName(initialName);

    Integer cosmeticEntityId=(Integer)sourceProps.getProperty("CosmeticEntity_CosmeticEntity");
    if (cosmeticEntityId!=null)
    {
      handleCosmeticEntity(pet,cosmeticEntityId.intValue());
    }
  }

  private void handleCosmeticEntity(CosmeticPetDescription pet, int cosmeticEntityId)
  {
    PropertiesSet cosmeticEntityProps=_facade.loadProperties(cosmeticEntityId+DATConstants.DBPROPERTIES_OFFSET);
    _classificationLoader.loadSpecification(cosmeticEntityProps,pet.getClassification());
  }
}
