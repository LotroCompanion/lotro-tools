package delta.games.lotro.tools.extraction.housing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.enums.HouseType;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.common.money.Money;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.lore.housing.HouseDefinition;
import delta.games.lotro.lore.housing.HouseTypeInfo;
import delta.games.lotro.lore.housing.HousingManager;
import delta.games.lotro.lore.housing.Neighborhood;
import delta.games.lotro.lore.housing.NeighborhoodTemplate;
import delta.games.lotro.lore.housing.io.xml.HousingXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.utils.Utils;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Loader for housing data.
 * @author DAM
 */
public class MainDatHousingLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatHousingLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  private PlacesLoader _placesLoader;
  private Map<Integer,Float> _priceModifiers;
  private Map<Integer,Float> _upkeepModifiers;
  private LotroEnum<HouseType> _houseTypeEnum;
  private HousingManager _housingMgr;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param placesLoader Places loader.
   */
  public MainDatHousingLoader(DataFacade facade, PlacesLoader placesLoader)
  {
    _facade=facade;
    _i18n=new I18nUtils("housing",facade.getGlobalStringsManager());
    _placesLoader=placesLoader;
    _priceModifiers=new HashMap<Integer,Float>();
    _upkeepModifiers=new HashMap<Integer,Float>();
    _houseTypeEnum=LotroEnumsRegistry.getInstance().get(HouseType.class);
    _housingMgr=new HousingManager();
  }

  /**
   * Just... do it!
   */
  public void doIt()
  {
    loadGlobalHousingData();
    loadNeighborhoods();
    save();
  }

  private void loadGlobalHousingData()
  {
    int id=WeenieContentDirectory.getWeenieID(_facade,"HousingControl");
    PropertiesSet props=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    // Modifiers
    /*
    HousingControl_HouseValueTierArray: 
      #1: HousingControl_HouseValueTierEntry 
        HousingControl_HouseValueTier: 8 (MithrilVeryGood)
        HousingControl_HouseValueTier_PriceModifier: 1.2
        HousingControl_HouseValueTier_UpkeepPriceModifier: 1.1
    HousingControl_UpkeepInterval: 604800
    */
    Object[] modifiersArray=(Object[])props.getProperty("HousingControl_HouseValueTierArray");
    for(Object modifiersEntry : modifiersArray)
    {
      PropertiesSet modifiersProps=(PropertiesSet)modifiersEntry;
      int tier=((Integer)modifiersProps.getProperty("HousingControl_HouseValueTier")).intValue();
      Float priceModifier=(Float)modifiersProps.getProperty("HousingControl_HouseValueTier_PriceModifier");
      Float upkeepModifier=(Float)modifiersProps.getProperty("HousingControl_HouseValueTier_UpkeepPriceModifier");
      _priceModifiers.put(Integer.valueOf(tier),priceModifier);
      _upkeepModifiers.put(Integer.valueOf(tier),upkeepModifier);
    }
    // Type infos
    /*
    HousingControl_HouseTypeInfoArray: 
      #1: HousingControl_HouseTypeInfoEntry 
        HousingControl_HousePrice: 100000
        HousingControl_HouseType: 1 (Standard House)
        HousingControl_HouseUpkeep: 5000
        HousingControl_Icon: 1091402973
        HousingControl_Icon16: 1092239840
        HousingControl_Icon32: 1091405429
        HousingControl_Icon_Large: 1092237727
        HousingControl_Icon_Panorama: 1092240002
    */
    Object[] typeInfoArray=(Object[])props.getProperty("HousingControl_HouseTypeInfoArray");
    for(Object typeInfoEntry : typeInfoArray)
    {
      PropertiesSet typeInfoProps=(PropertiesSet)typeInfoEntry;
      int typeCode=((Integer)typeInfoProps.getProperty("HousingControl_HouseType")).intValue();
      HouseType type=_houseTypeEnum.getEntry(typeCode);
      HouseTypeInfo typeInfo=new HouseTypeInfo(type);
      int price=((Integer)typeInfoProps.getProperty("HousingControl_HousePrice")).intValue();
      Money priceAsMoney=new Money();
      priceAsMoney.setRawValue(price);
      typeInfo.setPrice(priceAsMoney);
      int upkeep=((Integer)typeInfoProps.getProperty("HousingControl_HouseUpkeep")).intValue();
      Money upkeepAsMoney=new Money();
      upkeepAsMoney.setRawValue(upkeep);
      typeInfo.setUpkeep(upkeepAsMoney);
      int iconID=((Integer)typeInfoProps.getProperty("HousingControl_Icon")).intValue();
      typeInfo.setIconID(iconID);
      handleIcon(iconID);
      int icon16ID=((Integer)typeInfoProps.getProperty("HousingControl_Icon16")).intValue();
      typeInfo.setIcon16ID(icon16ID);
      handleIcon(icon16ID);
      int icon32ID=((Integer)typeInfoProps.getProperty("HousingControl_Icon32")).intValue();
      typeInfo.setIcon32ID(icon32ID);
      handleIcon(icon32ID);
      int iconLargeID=((Integer)typeInfoProps.getProperty("HousingControl_Icon_Large")).intValue();
      typeInfo.setIconLargeID(iconLargeID);
      handleIcon(iconLargeID);
      int iconPanoramaID=((Integer)typeInfoProps.getProperty("HousingControl_Icon_Panorama")).intValue();
      typeInfo.setIconPanoramaID(iconPanoramaID);
      handleIcon(iconPanoramaID);
      _housingMgr.registerHouseInfo(typeInfo);
    }
  }

  private void handleIcon(int iconID)
  {
    if (iconID==0)
    {
      return;
    }
    File to=new File(GeneratedFiles.HOUSING_ICONS,String.valueOf(iconID)+".png");
    if (!to.exists())
    {
      boolean ok=DatIconsUtils.buildImageFile(_facade,iconID,to);
      if (!ok)
      {
        LOGGER.warn("Could not load icon ID: {}",Integer.valueOf(iconID));
      }
    }
  }

  private void loadNeighborhoods()
  {
    /*
NeighborhoodDirectory_NeighborhoodArray: 
  #1: NeighborhoodDirectory_Neighborhood 1879415312
     */
    int id=WeenieContentDirectory.getWeenieID(_facade,"NeighborhoodDirectory");
    PropertiesSet props=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    Object[] neighborhoodArray=(Object[])props.getProperty("NeighborhoodDirectory_NeighborhoodArray");
    for(Object neighborhoodEntry : neighborhoodArray)
    {
      int neighborhoodID=((Integer)neighborhoodEntry).intValue();
      Neighborhood neighborhood=loadNeighborhood(neighborhoodID);
      _housingMgr.registerNeighborhood(neighborhood);
    }
  }

  private Neighborhood loadNeighborhood(int neighborhoodID)
  {
    /*
    Neighborhood_Name: Test Estate 11
    Neighborhood_NeighborhoodTemplate: 1879415308
    */
    Neighborhood ret=new Neighborhood(neighborhoodID);
    PropertiesSet props=_facade.loadProperties(neighborhoodID+DATConstants.DBPROPERTIES_OFFSET);
    // Name
    String name=_i18n.getNameStringProperty(props,"Neighborhood_Name",neighborhoodID,0);
    ret.setName(name);
    // neighborhood template
    int templateID=((Integer)props.getProperty("Neighborhood_NeighborhoodTemplate")).intValue();
    NeighborhoodTemplate template=handleNeighborhoodTemplate(templateID);
    ret.setTemplate(template);
    return ret;
  }

  private NeighborhoodTemplate handleNeighborhoodTemplate(int neighborhoodTemplateID)
  {
    NeighborhoodTemplate neighborhoodTemplate=_housingMgr.getNeighborhoodTemplate(neighborhoodTemplateID);
    if (neighborhoodTemplate==null)
    {
      neighborhoodTemplate=loadNeighborhoodTemplate(neighborhoodTemplateID);
      _housingMgr.registerNeighborhoodTemplate(neighborhoodTemplate);
    }
    return neighborhoodTemplate;
  }

  private NeighborhoodTemplate loadNeighborhoodTemplate(int neighborhoodTemplateID)
  {
    /*
NeighborhoodTemplate_BootPosition: house_hobbit_micheldelving_neighborhood_exit
NeighborhoodTemplate_Boundaries: 
  #1: NeighborhoodTemplate_AllowedLandblock R=1,I=0,C=-1,bx=89,by=108,x=0.0,y=0.0,z=0.0 => Lon=-75.6/lat=-38.0
  ...
NeighborhoodTemplate_HouseList: 1879093274
NeighborhoodTemplate_Name: Shire Homesteads
NeighborhoodTemplate_SceneID: 1879101754
NeighborhoodTemplate_Telepad: house_hobbit_micheldelving_neighborhood_entrance
    */
    NeighborhoodTemplate ret=new NeighborhoodTemplate(neighborhoodTemplateID);
    PropertiesSet props=_facade.loadProperties(neighborhoodTemplateID+DATConstants.DBPROPERTIES_OFFSET);
    // Name
    String name=_i18n.getNameStringProperty(props,"NeighborhoodTemplate_Name",neighborhoodTemplateID,0);
    ret.setName(name);
    // Boundaries
    Object[] boundariesArray=(Object[])props.getProperty("NeighborhoodTemplate_Boundaries");
    for(Object boundaryObj : boundariesArray)
    {
      DatPosition position=(DatPosition)boundaryObj;
      BlockReference ref=new BlockReference(position.getRegion(),position.getBlockX(),position.getBlockY());
      ret.addBlock(ref);
    }
    // Boot position
    String bootPositionStr=(String)props.getProperty("NeighborhoodTemplate_BootPosition");
    ExtendedPosition bootPosition=_placesLoader.getPositionForName(bootPositionStr);
    ret.setBoot(bootPosition.getPosition());
    // Entrance
    String entrancePositionStr=(String)props.getProperty("NeighborhoodTemplate_Telepad");
    ExtendedPosition entrancePosition=_placesLoader.getPositionForName(entrancePositionStr);
    ret.setEntrance(entrancePosition.getPosition());
    // Houses
    int houseListID=((Integer)props.getProperty("NeighborhoodTemplate_HouseList")).intValue();
    PropertiesSet houseListProps=_facade.loadProperties(houseListID+DATConstants.DBPROPERTIES_OFFSET);
    /*
HouseList_HouseArray: 
  #1: HouseList_House 1879097416
     */
    Object[] houseArray=(Object[])houseListProps.getProperty("HouseList_HouseArray");
    for(Object houseIDObj : houseArray)
    {
      int houseID=((Integer)houseIDObj).intValue();
      HouseDefinition house=handleHouse(houseID);
      if (house!=null)
      {
        ret.addHouse(house);
      }
    }
    return ret;
  }

  private HouseDefinition handleHouse(int houseID)
  {
    HouseDefinition house=_housingMgr.getHouse(houseID);
    if (house==null)
    {
      house=loadHouse(houseID);
      if (house!=null)
      {
        _housingMgr.registerHouse(house);
      }
    }
    return house;
  }

  private HouseDefinition loadHouse(int houseID)
  {
    /*
    House_Address: 1 Brookbank Street
    House_CharacteristicArray: 
      #1: House_Characteristic 1879101520 => trait "Shire Home"
      #2: House_Characteristic 1879101615 => trait "Travel to Personal House" => skill "1879101510" (Travel to Personal House)
    House_Description: This house offers 74 interior decoration hooks and 7 exterior hooks, with access to shared housing storage.
    House_IsKinshipHouse: 0
    House_IsPremiumHouse: 0
    House_NeighborhoodTemplate: 1879093273
    House_Telepad: house_hobbit_s3_h1_visit
    House_Type: 5 (Deluxe House)
    House_ValueTier: 3 (Average)
    */
    PropertiesSet props=_facade.loadProperties(houseID+DATConstants.DBPROPERTIES_OFFSET);
    HouseDefinition ret=new HouseDefinition(houseID);
    // Address
    String address=_i18n.getNameStringProperty(props,"House_Address",houseID,0);
    ret.setAddress(address);
    // Description
    String description=_i18n.getStringProperty(props,"House_Description");
    ret.setDescription(description);
    // Kinship?
    boolean isKinship=Utils.getBooleanValue((Integer)props.getProperty("House_IsKinshipHouse"),false);
    ret.setKinship(isKinship);
    // Premium?
    boolean isPremium=Utils.getBooleanValue((Integer)props.getProperty("House_IsPremiumHouse"),false);
    ret.setPremium(isPremium);
    // Neighborhood
    int neighborhoodTemplateID=((Integer)props.getProperty("House_NeighborhoodTemplate")).intValue();
    ret.setNeighborhoodTemplateID(neighborhoodTemplateID);
    List<TraitDescription> traits=new ArrayList<TraitDescription>();
    Object[] characteristicsArray=(Object[])props.getProperty("House_CharacteristicArray");
    if (characteristicsArray!=null)
    {
      for(Object characteristicObj : characteristicsArray)
      {
        int traitID=((Integer)characteristicObj).intValue();
        TraitDescription trait=TraitsManager.getInstance().getTrait(traitID);
        if (trait!=null)
        {
          traits.add(trait);
        }
        else
        {
          LOGGER.warn("Trait not found: ID={}",Integer.valueOf(traitID));
        }
      }
    }
    ret.setTraits(traits);
    // Telepad
    String telepad=(String)props.getProperty("House_Telepad");
    ExtendedPosition position=_placesLoader.getPositionForName(telepad);
    ret.setPosition(position.getPosition());
    // Price & upkeep
    int typeCode=((Integer)props.getProperty("House_Type")).intValue();
    HouseType type=_houseTypeEnum.getEntry(typeCode);
    HouseTypeInfo houseTypeInfo=_housingMgr.getHouseInfo(type);
    if (houseTypeInfo==null)
    {
      LOGGER.warn("Ignored house: {} of type {}",address,type);
      return null;
    }
    ret.setInfo(houseTypeInfo);
    Integer valueTier=(Integer)props.getProperty("House_ValueTier");
    // Price
    Float priceModifier=_priceModifiers.get(valueTier);
    int price=(int)(houseTypeInfo.getPrice().getInternalValue()*priceModifier.floatValue());
    ret.setPrice(Money.fromRawValue(price));
    // Upkeep
    Float upkeepModifier=_upkeepModifiers.get(valueTier);
    int upkeep=(int)(houseTypeInfo.getUpkeep().getInternalValue()*upkeepModifier.floatValue());
    ret.setUpkeep(Money.fromRawValue(upkeep));
    LOGGER.debug("Loaded house: {}",ret);
    return ret;
  }

  private void save()
  {
    HousingXMLWriter writer=new HousingXMLWriter();
    writer.writeHousingData(GeneratedFiles.HOUSING,_housingMgr);
    _i18n.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    MainDatHousingLoader loader=new MainDatHousingLoader(facade,placesLoader);
    loader.doIt();
    facade.dispose();
  }
}
