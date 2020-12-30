package delta.games.lotro.tools.lore.deeds.geo.migration;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.NumericTools;
import delta.common.utils.files.TextFileReader;
import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.TextUtils;
import delta.common.utils.url.URLTools;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.quests.geo.AchievableGeoPoint;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectiveCondition;

/**
 * Migration of old marker data into the deeds.
 * @author DAM
 */
public class OldMarkersMigration
{
  private List<OldMarkerSpec> loadOldMarkers()
  {
    URL url=URLTools.getFromClassPath("oldMarkers.txt",this);
    TextFileReader r=new TextFileReader(url,EncodingNames.UTF_8);
    List<String> lines=TextUtils.readAsLines(r);
    r.terminate();
    List<OldMarkerSpec> specs=new ArrayList<OldMarkerSpec>();
    for(String line : lines)
    {
      OldMarkerSpec spec=buildSpec(line);
      fixSpec(spec);
      specs.add(spec);
    }
    return specs;
  }

  private Map<Integer,List<OldMarkerSpec>> sortSpecs(List<OldMarkerSpec> specs)
  {
    Map<Integer,List<OldMarkerSpec>> ret=new HashMap<Integer,List<OldMarkerSpec>>();
    for(OldMarkerSpec spec : specs)
    {
      Integer key=Integer.valueOf(spec.getDeedId());
      List<OldMarkerSpec> list=ret.get(key);
      if (list==null)
      {
        list=new ArrayList<OldMarkerSpec>();
        ret.put(key,list);
      }
      list.add(spec);
    }
    return ret;
  }

  private OldMarkerSpec buildSpec(String line)
  {
    String[] values=line.split("\t");
    if (values.length!=5) return null;
    int deedId=NumericTools.parseInt(values[0],-1);
    if (deedId==-1) return null;
    int pointId=NumericTools.parseInt(values[1],-1);
    if (pointId==-1) return null;
    String mapKey=values[2];
    Float latitude=NumericTools.parseFloat(values[3]);
    if (latitude==null) return null;
    Float longitude=NumericTools.parseFloat(values[4]);
    if (longitude==null) return null;
    OldMarkerSpec spec=new OldMarkerSpec(deedId,pointId,mapKey,latitude.floatValue(),longitude.floatValue());
    return spec;
  }

  private void fixSpec(OldMarkerSpec spec)
  {
    int pointId=spec.getPointId();
    if (pointId==9632) spec.setPosition(54.032597f,-4.0633106f);
    else if (pointId==9934) spec.setPosition(10.7f,-64.2f);
  }

  private void handleDeed(int deedId, List<OldMarkerSpec> specs)
  {
    DeedsManager deedsMgr=DeedsManager.getInstance();
    DeedDescription deed=deedsMgr.getDeed(deedId);
    if (deed==null)
    {
      return;
    }
    for(Objective objective : deed.getObjectives().getObjectives())
    {
      for(ObjectiveCondition condition : objective.getConditions())
      {
        for(AchievableGeoPoint point : condition.getPoints())
        {
          OldMarkerSpec spec=handlePoint(point,specs);
          if (spec!=null)
          {
            point.setOldMarkerId(Integer.valueOf(spec.getPointId()));
          }
        }
      }
    }
  }

  private OldMarkerSpec handlePoint(AchievableGeoPoint point, List<OldMarkerSpec> specs)
  {
    OldMarkerSpec selected=null;
    Float minDistance=null;
    for(OldMarkerSpec spec : specs)
    {
      float distance=distance(point,spec);
      if ((minDistance==null) || (minDistance.floatValue()>distance))
      {
        minDistance=Float.valueOf(distance);
        selected=spec;
      }
    }
    if (minDistance.floatValue()<0.5)
    {
      return selected;
      //System.out.println("Spec: "+spec+" => "+selected.getLonLat()+" => min distance="+minDistance);
    }
    System.out.println("Failure: Point: "+point+" => "+selected.getLatitude()+"/"+selected.getLongitude()+" => min distance="+minDistance);
    return null;

  }

  private float distance(AchievableGeoPoint point, OldMarkerSpec spec)
  {
    float lonPoint=point.getLonLat().x;
    float latPoint=point.getLonLat().y;
    float lonSpec=spec.getLongitude();
    float latSpec=spec.getLatitude();

    float dx=(lonPoint-lonSpec);
    float dy=(latPoint-latSpec);
    return (float)Math.sqrt(dx*dx+dy*dy);
  }

  /**
   * Perform migration.
   */
  public void doIt()
  {
    List<OldMarkerSpec> specs=loadOldMarkers();
    Map<Integer,List<OldMarkerSpec>> sortedSpecs=sortSpecs(specs);
    for(Integer deedId : sortedSpecs.keySet())
    {
      handleDeed(deedId.intValue(),sortedSpecs.get(deedId));
    }
  }
}
