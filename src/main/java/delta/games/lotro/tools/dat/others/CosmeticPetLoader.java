package delta.games.lotro.tools.dat.others;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.collections.pets.CosmeticPetDescription;
import delta.games.lotro.lore.collections.pets.io.xml.CosmeticPetXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.agents.ClassificationLoader;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.StringUtils;

/**
 * Get definition of cosmetic pets from DAT files.
 * @author DAM
 */
public class CosmeticPetLoader
{
  private static final Logger LOGGER=Logger.getLogger(CosmeticPetLoader.class);

  private DataFacade _facade;
  private EnumMapper _category;
  private ClassificationLoader _classificationLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public CosmeticPetLoader(DataFacade facade)
  {
    _facade=facade;
    _category=facade.getEnumsManager().getEnumMapper(587202586);
    _classificationLoader=new ClassificationLoader(facade);
  }

  /**
   * Load a cosmetic pet definition.
   * @param indexDataId Cosmetic pet skill identifier.
   * @return the loaded pet or <code>null</code> if not loaded.
   */
  public CosmeticPetDescription load(int indexDataId)
  {
    CosmeticPetDescription ret=null;
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      ret=new CosmeticPetDescription(indexDataId);

      // Hidden?
      //int hidden=((Integer)properties.getProperty("Collection_Hide_Entry")).intValue();
      // Name
      String name=DatUtils.getStringProperty(properties,"Skill_Name");
      name=StringUtils.fixName(name);
      ret.setName(name);
      // Description
      String description=DatUtils.getStringProperty(properties,"Skill_Desc");
      ret.setDescription(description);
      // Icon
      int iconId=((Integer)properties.getProperty("Skill_Icon")).intValue();
      int largeIconId=((Integer)properties.getProperty("Skill_LargeIcon")).intValue();
      int smallIconId=((Integer)properties.getProperty("Skill_SmallIcon")).intValue();
      if ((iconId!=smallIconId) || (iconId!=largeIconId))
      {
        LOGGER.warn("Icons mismatch: small="+smallIconId+"/regular="+iconId+"/large="+largeIconId);
      }
      ret.setIconId(iconId);
      // Source description (null for war-steeds)
      String sourceDescription=DatUtils.getStringProperty(properties,"Collection_Piece_SourceDesc");
      ret.setSourceDescription(sourceDescription);
      // Category (shall be cosmetic pets)
      int categoryCode=((Integer)properties.getProperty("Skill_Category")).intValue();
      if (categoryCode!=145) // Cosmetic Pets
      {
        String category=_category.getString(categoryCode);
        LOGGER.warn("Unexpected category: code="+categoryCode+", name="+category);
      }
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
    else
    {
      LOGGER.warn("Could not handle cosmetic pet skill ID="+indexDataId);
    }
    return ret;
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
    //System.out.println(sourceProps.dump());

    //CosmeticEntity_CosmeticEntity: 1879305757
    //CosmeticEntity_Distance: 3.0
    //CosmeticEntity_Following_Speed: 10.0
    //CosmeticEntity_MovementType: 2 (Following)
    // Initial Name
    String initialName=DatUtils.getStringProperty(sourceProps,"CosmeticEntity_Name_Initial");
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

  /**
   * Save the loaded pets to a file.
   * @param pets Pets to save.
   */
  public void savePets(List<CosmeticPetDescription> pets)
  {
    // Data
    Collections.sort(pets,new IdentifiableComparator<CosmeticPetDescription>());
    CosmeticPetXMLWriter.write(GeneratedFiles.PETS,pets);
  }

  /**
   * Load cosmetic pets.
   */
  public void doIt()
  {
    List<CosmeticPetDescription> pets=new ArrayList<CosmeticPetDescription>();
    PropertiesSet cosmeticEntitiesDirectoryProps=_facade.loadProperties(0x7004832E+DATConstants.DBPROPERTIES_OFFSET);
    Object[] cosmeticEntitySkillsList=(Object[])cosmeticEntitiesDirectoryProps.getProperty("CosmeticEntity_SkillList");
    for(Object cosmeticEntitySkillObj : cosmeticEntitySkillsList)
    {
      int cosmeticEntitySkillId=((Integer)cosmeticEntitySkillObj).intValue();
      CosmeticPetDescription pet=load(cosmeticEntitySkillId);
      if (pet!=null)
      {
        pets.add(pet);
      }
    }
    LOGGER.info("Loaded "+pets.size()+" pets.");
    savePets(pets);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new CosmeticPetLoader(facade).doIt();
    facade.dispose();
  }
}
