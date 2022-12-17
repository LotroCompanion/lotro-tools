package delta.games.lotro.tools.dat.combat;

import java.util.Objects;

import org.apache.log4j.Logger;

import delta.games.lotro.character.stats.ratings.ProgressionRatingCurveImpl;
import delta.games.lotro.character.stats.ratings.RatingCurve;
import delta.games.lotro.character.stats.ratings.RatingCurveId;
import delta.games.lotro.common.global.CombatData;
import delta.games.lotro.common.global.io.xml.CombatDataXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
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
      Progression hardCapProg=DatStatUtils.getProgression(_facade,hardCapId);
      // Rating
      int ratingId=((Integer)calcControlProps.getProperty("Combat_Control_Rating")).intValue();
      Progression ratingProg=DatStatUtils.getProgression(_facade,ratingId);
      // Target cap
      int targetCapId=((Integer)calcControlProps.getProperty("Combat_Control_Target_Cap")).intValue();
      Progression targetCapProg=DatStatUtils.getProgression(_facade,targetCapId);

      ProgressionRatingCurveImpl curve=new ProgressionRatingCurveImpl(hardCapProg,ratingProg,targetCapProg);
      _data.getRatingsMgr().setCurve(id,curve);
      /*
      RatingsMgr mgr=new RatingsMgr();
      RatingsInitializerUpdate21.init(mgr);
      RatingCurve oldCurve=mgr.getCurve(id);
      compareCurves(id,curve,oldCurve);
      */
    }
    else
    {
      LOGGER.warn("Unmanaged curve type: "+calcType+" => "+_calcType.getString(calcType));
      //System.out.println("Hard cap: "+hardCapProg);
      //System.out.println("Rating: "+ratingProg);
      //System.out.println("Target cap: "+targetCapProg);
    }
  }

  void compareCurves(RatingCurveId id, RatingCurve curve, RatingCurve oldCurve)
  {
    for(int level=1;level<=120;level++)
    {
      Double r1=curve.getRatingForCap(level);
      Double r2=oldCurve.getRatingForCap(level);
      if (!Objects.equals(r1,r2))
      {
        System.out.println(id+": level="+level+": cap ratings diff: r1="+r1+", r2="+r2);
      }
      for(int rating=0;rating<5000000;rating+=10000)
      {
        Double p1=curve.getPercentage(rating,level);
        Double p2=oldCurve.getPercentage(rating,level);
        if (p1==null)
        {
          if (p2!=null)
          {
            System.out.println("(1) "+id+": Bad point: level="+level+", rating="+rating);
          }
        }
        else
        {
          if (p2==null)
          {
            System.out.println("(2) "+id+": Bad point: level="+level+", rating="+rating);
          }
          double delta=Math.abs(p1.doubleValue()-p2.doubleValue());
          if (delta>0.01)
          {
            System.out.println("(3) "+id+": Bad point: level="+level+", rating="+rating+", delta="+delta);
          }
          else
          {
            // Good point!
          }
        }
      }
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
    DatStatUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_COMBAT);
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
