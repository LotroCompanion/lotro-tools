package delta.games.lotro.tools.dat.others;

import java.io.File;
import java.util.BitSet;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get definition of cosmetic pets from DAT files.
 * @author DAM
 */
public class CosmeticPetLoader
{
  private static final Logger LOGGER=Logger.getLogger(CosmeticPetLoader.class);

  private DataFacade _facade;
  private EnumMapper _category;
  private EnumMapper _subCategory;
  private EnumMapper _alignment;
  private EnumMapper _class;
  private EnumMapper _genus;
  private EnumMapper _species;
  private EnumMapper _subSpecies;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public CosmeticPetLoader(DataFacade facade)
  {
    _facade=facade;
    _category=facade.getEnumsManager().getEnumMapper(587202586);
    _subCategory=facade.getEnumsManager().getEnumMapper(587203478);
    _alignment=facade.getEnumsManager().getEnumMapper(587202573);
    _class=facade.getEnumsManager().getEnumMapper(587202574);
    _genus=facade.getEnumsManager().getEnumMapper(587202570);
    _species=facade.getEnumsManager().getEnumMapper(587202571);
    _subSpecies=facade.getEnumsManager().getEnumMapper(587202572);
  }

  /**
   * Load a cosmetic pet definition.
   * @param indexDataId Cosmetic pet skill identifier.
   */
  public void load(int indexDataId)
  {
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      System.out.println("************* "+indexDataId+" *************");
      //System.out.println(properties.dump());

      // Hidden?
      int hidden=((Integer)properties.getProperty("Collection_Hide_Entry")).intValue();
      System.out.println("Hidden: "+hidden);
      // Name
      String name=DatUtils.getStringProperty(properties,"Skill_Name");
      System.out.println("Name: "+name);
      // Description
      String description=DatUtils.getStringProperty(properties,"Skill_Desc");
      System.out.println("Description: "+description);
      // Icon
      int iconId=((Integer)properties.getProperty("Skill_Icon")).intValue();
      int largeIconId=((Integer)properties.getProperty("Skill_LargeIcon")).intValue();
      int smallIconId=((Integer)properties.getProperty("Skill_SmallIcon")).intValue();
      System.out.println("Icons: "+smallIconId+" / "+iconId+" / "+largeIconId);
      if ((iconId!=smallIconId) || (iconId!=largeIconId))
      {
        LOGGER.warn("Icons mismatch: small="+smallIconId+"/regular="+iconId+"/large="+largeIconId);
      }
      File collectionsDir=new File("collections");
      File cosmeticPetsDir=new File(collectionsDir,"cosmeticPets");
      File to=new File(cosmeticPetsDir,iconId+".png");
      DatIconsUtils.buildImageFile(_facade,iconId,to);
      // Source description (null for war-steeds)
      String sourceDescription=DatUtils.getStringProperty(properties,"Collection_Piece_SourceDesc");
      System.out.println("Source description: "+sourceDescription);

      // Category (shall be cosmetic pets)
      int categoryCode=((Integer)properties.getProperty("Skill_Category")).intValue();
      String category=_category.getString(categoryCode);
      System.out.println("Category: "+category);
      // Sub category
      Integer subCategoryCode=(Integer)properties.getProperty("Skill_SubCategory");
      if (subCategoryCode!=null)
      {
        String subCategory=_subCategory.getString(subCategoryCode.intValue());
        System.out.println("Sub category: "+subCategory);
      }

      Object[] effectsList=(Object[])properties.getProperty("Skill_Toggle_Effect_List");
      if (effectsList!=null)
      {
        for(Object effectObj : effectsList)
        {
          PropertiesSet effectRefProps=(PropertiesSet)effectObj;
          int effectId=((Integer)effectRefProps.getProperty("Skill_Toggle_Effect")).intValue();
          handleCosmeticPetEffect(effectId);
        }
      }
    }
    else
    {
      LOGGER.warn("Could not handle cosmetic pet skill ID="+indexDataId);
    }
  }

  private void handleCosmeticPetEffect(int effectId)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(effectProps.dump());

    Object[] mods=(Object[])effectProps.getProperty("Mod_Array");
    for(Object modObj : mods)
    {
      PropertiesSet modProps=(PropertiesSet)modObj;
      Integer sourceId=(Integer)modProps.getProperty("CosmeticEntity_CosmeticPet_ForwardSource");
      if (sourceId!=null)
      {
        handleSourceId(sourceId.intValue());
      }
    }
  }

  private void handleSourceId(int sourceId)
  {
    PropertiesSet sourceProps=_facade.loadProperties(sourceId+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(sourceProps.dump());

    //CosmeticEntity_CosmeticEntity: 1879305757
    //CosmeticEntity_Distance: 3.0
    //CosmeticEntity_Following_Speed: 10.0
    //CosmeticEntity_MovementType: 2 (Following)
    // Initial Name
    String initialName=DatUtils.getStringProperty(sourceProps,"CosmeticEntity_Name_Initial");
    System.out.println("Initial name: "+initialName);
    // Item_Quality

    Integer cosmeticEntityId=(Integer)sourceProps.getProperty("CosmeticEntity_CosmeticEntity");
    if (cosmeticEntityId!=null)
    {
      handleCosmeticEntity(cosmeticEntityId.intValue());
    }
  }

  private void handleCosmeticEntity(int cosmeticEntityId)
  {
    PropertiesSet cosmeticEntityProps=_facade.loadProperties(cosmeticEntityId+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(cosmeticEntityProps.dump());

    // Alignment
    int alignmentCode=((Integer)cosmeticEntityProps.getProperty("Agent_Alignment")).intValue();
    String alignment=_alignment.getString(alignmentCode);
    System.out.println("Alignment: "+alignment);
    // Class
    int classCode=((Integer)cosmeticEntityProps.getProperty("Agent_Class")).intValue();
    String className=_class.getString(classCode);
    System.out.println("Class: "+className);
    // Genus
    int genusCode=((Integer)cosmeticEntityProps.getProperty("Agent_Genus")).intValue();
    BitSet genusBitSet=BitSetUtils.getBitSetFromFlags(genusCode);
    String genuses=BitSetUtils.getStringFromBitSet(genusBitSet,_genus,"/");
    System.out.println("Genus: "+genuses);
    // Species
    int speciesCode=((Integer)cosmeticEntityProps.getProperty("Agent_Species")).intValue();
    String species=_species.getString(speciesCode);
    System.out.println("Species: "+species);
    // Sub-species
    Integer showSubSpecies=(Integer)cosmeticEntityProps.getProperty("Agent_ShowSubspecies");
    if ((showSubSpecies!=null) && (showSubSpecies.intValue()!=0))
    {
      int subSpeciesCode=((Integer)cosmeticEntityProps.getProperty("Agent_Subspecies")).intValue();
      String subSpecies=_subSpecies.getString(subSpeciesCode);
      System.out.println("Sub-species: "+subSpecies);
    }
  }

  private void doIt()
  {
    PropertiesSet cosmeticEntitiesDirectoryProps=_facade.loadProperties(0x7004832E+DATConstants.DBPROPERTIES_OFFSET);
    Object[] cosmeticEntitySkillsList=(Object[])cosmeticEntitiesDirectoryProps.getProperty("CosmeticEntity_SkillList");
    for(Object cosmeticEntitySkillObj : cosmeticEntitySkillsList)
    {
      int cosmeticEntitySkillId=((Integer)cosmeticEntitySkillObj).intValue();
      load(cosmeticEntitySkillId);
    }
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
