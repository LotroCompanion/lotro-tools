package delta.games.lotro.tools.dat.combat;

import org.apache.log4j.Logger;

import delta.games.lotro.character.stats.ratings.ProgressionRatingCurveImpl;
import delta.games.lotro.character.stats.ratings.RatingCurveId;
import delta.games.lotro.common.global.CombatData;
import delta.games.lotro.common.global.io.xml.CombatDataXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.ProgressionUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loader for combat data.
 * @author DAM
 */
public class MainDatCombatLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatCombatLoader.class);

  private DataFacade _facade;
  private EnumMapper _calcType;
  private CombatData _data;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatCombatLoader(DataFacade facade)
  {
    _facade=facade;
    _calcType=_facade.getEnumsManager().getEnumMapper(587203377);
    _data=new CombatData();
  }

  /**
   * Load combat data.
   */
  public void doIt()
  {
    // CombatControl
    PropertiesSet props=_facade.loadProperties(1879048757+DATConstants.DBPROPERTIES_OFFSET);
    Object[] calcControlInfoArray=(Object[])props.getProperty("Combat_Control_CalcControlInfoArray");
    if (calcControlInfoArray!=null)
    {
      for(Object calcControlInfoObj : calcControlInfoArray)
      {
        PropertiesSet calcControlProps=(PropertiesSet)calcControlInfoObj;
        handleOneStat(calcControlProps);
      }
    }
    save();
  }

  private void handleOneStat(PropertiesSet calcControlProps)
  {
    int calcType=((Integer)calcControlProps.getProperty("Combat_Control_CalculationType")).intValue();
    RatingCurveId id=getRatingCurveId(calcType);
    if (id!=null)
    {
      // Hard cap
      int hardCapId=((Integer)calcControlProps.getProperty("Combat_Control_Hard_Cap")).intValue();
      Progression hardCapProg=ProgressionUtils.getProgression(_facade,hardCapId);
      // Rating
      int ratingId=((Integer)calcControlProps.getProperty("Combat_Control_Rating")).intValue();
      Progression ratingProg=ProgressionUtils.getProgression(_facade,ratingId);
      // Target cap
      int targetCapId=((Integer)calcControlProps.getProperty("Combat_Control_Target_Cap")).intValue();
      Progression targetCapProg=ProgressionUtils.getProgression(_facade,targetCapId);

      ProgressionRatingCurveImpl curve=new ProgressionRatingCurveImpl(hardCapProg,ratingProg,targetCapProg);
      _data.getRatingsMgr().setCurve(id,curve);
    }
    else
    {
      LOGGER.warn("Unmanaged curve type: "+calcType+" => "+_calcType.getString(calcType));
    }
  }

  private RatingCurveId getRatingCurveId(int calcType)
  {
    // Block
    if (calcType==1) return RatingCurveId.AVOIDANCE;
    if (calcType==4) return RatingCurveId.PARTIAL_AVOIDANCE;
    if (calcType==15) return RatingCurveId.PARTIAL_MITIGATION;
    // Parry
    if (calcType==2) return RatingCurveId.AVOIDANCE;
    if (calcType==5) return RatingCurveId.PARTIAL_AVOIDANCE;
    if (calcType==16) return RatingCurveId.PARTIAL_MITIGATION;
    // Evade
    if (calcType==3) return RatingCurveId.AVOIDANCE;
    if (calcType==6) return RatingCurveId.PARTIAL_AVOIDANCE;
    if (calcType==17) return RatingCurveId.PARTIAL_MITIGATION;
    // Crits & devastates
    if (calcType==7) return RatingCurveId.CRITICAL_HIT;
    if (calcType==23) return RatingCurveId.CRIT_DEVASTATE_MAGNITUDE;
    if (calcType==8) return RatingCurveId.DEVASTATE_HIT;
    // Defence
    if (calcType==24) return RatingCurveId.CRITICAL_DEFENCE;
    if (calcType==9) return RatingCurveId.RESISTANCE;
    // Armor mitigations
    if (calcType==12) return RatingCurveId.LIGHT_MITIGATION;
    if (calcType==13) return RatingCurveId.MEDIUM_MITIGATION;
    if (calcType==14) return RatingCurveId.HEAVY_MITIGATION;
    // Healing
    if (calcType==18) return RatingCurveId.INCOMING_HEALING;
    if (calcType==19) return RatingCurveId.HEALING;
    // Finesse
    if (calcType==20) return RatingCurveId.FINESSE;
    // Damage
    if (calcType==11) return RatingCurveId.DAMAGE;
    // Unused:
    //Combat_Control_CalculationType: 10 (VulnerabilityDefense)
    //Combat_Control_CalculationType: 21 (MomentumConversion)
    //Combat_Control_CalculationType: 22 (PVPDefense)
    return null;
  }

  private void save()
  {
    CombatDataXMLWriter.write(GeneratedFiles.COMBAT_DATA,_data);
    // Save progressions
    ProgressionUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_COMBAT);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatCombatLoader(facade).doIt();
    facade.dispose();
  }
}
