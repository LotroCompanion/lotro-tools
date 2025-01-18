package delta.games.lotro.tools.extraction.housing;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.enums.HouseType;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.money.Money;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.housing.HouseDefinition;
import delta.games.lotro.lore.housing.HouseTypeInfo;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.skills.MainSkillDataLoader;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * @author dm
 */
public class MainDatHousingLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainSkillDataLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  private PlacesLoader _placesLoader;
  private Map<Integer,Float> _priceModifiers;
  private Map<Integer,Float> _upkeepModifiers;
  private LotroEnum<HouseType> _houseTypeEnum;
  private Map<HouseType,HouseTypeInfo> _houseInfos;
  private Map<Integer,HouseDefinition> _houses;

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
    _houseInfos=new HashMap<HouseType,HouseTypeInfo>();
    _houses=new HashMap<Integer,HouseDefinition>();
  }

  private void doIt()
  {
    loadGlobalHousingData();
    loadNeighborhoods();
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
      int icon16ID=((Integer)typeInfoProps.getProperty("HousingControl_Icon16")).intValue();
      typeInfo.setIcon16ID(icon16ID);
      int icon32ID=((Integer)typeInfoProps.getProperty("HousingControl_Icon32")).intValue();
      typeInfo.setIcon32ID(icon32ID);
      int iconLargeID=((Integer)typeInfoProps.getProperty("HousingControl_Icon_Large")).intValue();
      typeInfo.setIconLargeID(iconLargeID);
      int iconPanoramaID=((Integer)typeInfoProps.getProperty("HousingControl_Icon_Panorama")).intValue();
      typeInfo.setIconPanoramaID(iconPanoramaID);
      _houseInfos.put(type,typeInfo);
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
      handleNeighborhood(neighborhoodID);
    }
  }

  private void handleNeighborhood(int neighborhoodID)
  {
    /*
    Neighborhood_Name: Test Estate 11
    Neighborhood_NeighborhoodTemplate: 1879415308
    */
    PropertiesSet props=_facade.loadProperties(neighborhoodID+DATConstants.DBPROPERTIES_OFFSET);
    String name=_i18n.getNameStringProperty(props,"Neighborhood_Name",neighborhoodID,0);
    int template=((Integer)props.getProperty("Neighborhood_NeighborhoodTemplate")).intValue();
    handleNeighborhoodTemplate(template);
  }

  private void handleNeighborhoodTemplate(int neighborhoodTemplateID)
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
    PropertiesSet props=_facade.loadProperties(neighborhoodTemplateID+DATConstants.DBPROPERTIES_OFFSET);
    String name=_i18n.getNameStringProperty(props,"NeighborhoodTemplate_Name",neighborhoodTemplateID,0);
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
      handleHouse(houseID);
    }
  }

  private HouseDefinition handleHouse(int houseID)
  {
    Integer key=Integer.valueOf(houseID);
    HouseDefinition house=_houses.get(key);
    if (house==null)
    {
      house=loadHouse(houseID);
      _houses.put(key,house);
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
    HouseDefinition ret=new HouseDefinition();
    // Address
    String address=_i18n.getNameStringProperty(props,"House_Address",houseID,0);
    ret.setAddress(address);
    // Description
    String description=_i18n.getStringProperty(props,"House_Description");
    ret.setDescription(description);
    // Neighborhood
    int neighborhoodTemplateID=((Integer)props.getProperty("House_NeighborhoodTemplate")).intValue();
    Object[] characteristicsArray=(Object[])props.getProperty("House_CharacteristicArray");
    if (characteristicsArray!=null)
    {
      for(Object characteristicObj : characteristicsArray)
      {
        int traitID=((Integer)characteristicObj).intValue();
      }
    }
    // Price & upkeep
    int typeCode=((Integer)props.getProperty("House_Type")).intValue();
    HouseType type=_houseTypeEnum.getEntry(typeCode);
    HouseTypeInfo houseTypeInfo=_houseInfos.get(type);
    if (houseTypeInfo==null)
    {
      LOGGER.warn("Ignored house: "+address+" of type "+type);
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
    LOGGER.debug("Loaded house: name="+ret.getAddress()+", price="+ret.getPrice()+", upkeep="+ret.getUpkeep());
    return ret;
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
