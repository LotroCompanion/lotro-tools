package delta.games.lotro.tools.dat.misc;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.common.enums.BillingGroup;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.billingGroups.BillingGroupDescription;
import delta.games.lotro.lore.billingGroups.io.xml.BillingGroupsXMLWriter;
import delta.games.lotro.lore.titles.TitleDescription;
import delta.games.lotro.lore.titles.TitlesManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.WeenieContentDirectory;

/**
 * Loader for billing groups descriptions.
 * @author DAM
 */
public class MainBillingGroupsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainBillingGroupsLoader.class);

  private DataFacade _facade;
  private Map<BillingGroup,BillingGroupDescription> _loadedData;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainBillingGroupsLoader(DataFacade facade)
  {
    _facade=facade;
    _loadedData=new HashMap<BillingGroup,BillingGroupDescription>();
  }

  /**
   * Load billing groups descriptions.
   */
  public void doIt()
  {
    initGroups();
    loadTitles();
    save();
  }

  private void loadTitles()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"VersionAccountTitleBestow");
    if (props==null)
    {
      return;
    }
    Object[] partnerDataList=(Object[])props.getProperty("Version_TitleBestow_PartnerDataList");
    for(Object partnerDataObj : partnerDataList)
    {
      PropertiesSet partnerDataProps=(PropertiesSet)partnerDataObj;
      Object[] entries=(Object[])partnerDataProps.getProperty("Version_TitleBestow_DataList");
      for(Object entry : entries)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        handleEntry(entryProps);
      }
    }
  }

  private void initGroups()
  {
    LotroEnum<BillingGroup> groupsEnum=LotroEnumsRegistry.getInstance().get(BillingGroup.class);
    for(BillingGroup group : groupsEnum.getAll())
    {
      getBillingGroupDescription(group);
    }
  }

  private void handleEntry(PropertiesSet entryProps)
  {
    /*
        Version_TitleBestow_BillingGroups: {40} (MorPr10)
        Version_TitleBestow_FreepTitleList: 
          #1: Version_TitleBestow_Title 1879117452
        Version_TitleBestow_CreepTitleList: 
          #1: Version_TitleBestow_Title 1879356048
        Version_TitleBestow_StampProperty: 268444584 (Version_Title_StampProperty_1)
     */
    List<TitleDescription> titles=new ArrayList<TitleDescription>();
    TitlesManager titlesMgr=TitlesManager.getInstance();
    Object[] freepTitlesArray=(Object[])entryProps.getProperty("Version_TitleBestow_FreepTitleList");
    if (freepTitlesArray!=null)
    {
      for(Object titleEntryObj : freepTitlesArray)
      {
        int titleID=((Integer)titleEntryObj).intValue();
        TitleDescription title=titlesMgr.getTitle(titleID);
        titles.add(title);
      }
    }
    Object[] creepTitlesArray=(Object[])entryProps.getProperty("Version_TitleBestow_CreepTitleList");
    if (creepTitlesArray!=null)
    {
      for(Object titleEntryObj : creepTitlesArray)
      {
        int titleID=((Integer)titleEntryObj).intValue();
        TitleDescription title=titlesMgr.getTitle(titleID);
        titles.add(title);
      }
    }
    if (titles.size()>0)
    {
      BitSet billingGroups=(BitSet)entryProps.getProperty("Version_TitleBestow_BillingGroups");
      LotroEnum<BillingGroup> billingGroupEnum=LotroEnumsRegistry.getInstance().get(BillingGroup.class);
      List<BillingGroup> groups=billingGroupEnum.getFromBitSet(billingGroups);
      for(BillingGroup group : groups)
      {
        BillingGroupDescription groupDescription=getBillingGroupDescription(group);
        for(TitleDescription title : titles)
        {
          groupDescription.addAccountTitle(title);
        }
      }
    }
  }

  private BillingGroupDescription getBillingGroupDescription(BillingGroup group)
  {
    BillingGroupDescription groupDescription=_loadedData.get(group);
    if (groupDescription==null)
    {
      groupDescription=new BillingGroupDescription(group);
      _loadedData.put(group,groupDescription);
    }
    return groupDescription;
  }

  private void save()
  {
    List<BillingGroupDescription> billingGroupsDescriptions=new ArrayList<BillingGroupDescription>();
    LotroEnum<BillingGroup> billingGroupEnum=LotroEnumsRegistry.getInstance().get(BillingGroup.class);
    for(BillingGroup group : billingGroupEnum.getAll())
    {
      BillingGroupDescription groupDescription=_loadedData.get(group);
      if (groupDescription!=null)
      {
        billingGroupsDescriptions.add(groupDescription);
        //showGroup(groupDescription);
      }
    }
    // Save groups
    int nbBillingGroups=billingGroupsDescriptions.size();
    LOGGER.info("Writing "+nbBillingGroups+" titles");
    boolean ok=new BillingGroupsXMLWriter().write(GeneratedFiles.BILLING_GROUPS,billingGroupsDescriptions,EncodingNames.UTF_8);
    if (ok)
    {
      System.out.println("Wrote billing groups file: "+GeneratedFiles.BILLING_GROUPS);
    }
  }

  void showGroup(BillingGroupDescription groupDescription)
  {
    List<TitleDescription> titles=groupDescription.getAccountTitles();
    if (titles.size()>0)
    {
      System.out.println("Group: "+groupDescription.getName());
      for(TitleDescription title : titles)
      {
        System.out.println("\t"+title.getIdentifier()+" - "+title.getName());
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
    new MainBillingGroupsLoader(facade).doIt();
    facade.dispose();
  }
}
