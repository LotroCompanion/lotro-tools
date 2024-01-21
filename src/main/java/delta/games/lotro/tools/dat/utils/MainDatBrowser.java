package delta.games.lotro.tools.dat.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.dat.archive.DATArchive;
import delta.games.lotro.dat.archive.DirectoryEntry;
import delta.games.lotro.dat.archive.FileEntry;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.KPM;

/**
 * @author DAM
 */
public class MainDatBrowser
{
  private DataFacade _facade;
  private Map<Integer,IntegerHolder> _countByType;
  private byte[][] _toSearch;

  private byte[] intToByteArray(int value)
  {
    byte[] ret=new byte[4];
    ret[0]=(byte)(value&0xff);
    value>>=8;
    ret[1]=(byte)(value&0xff);
    value>>=8;
    ret[2]=(byte)(value&0xff);
    value>>=8;
    ret[3]=(byte)(value&0xff);
    return ret;
  }

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatBrowser(DataFacade facade)
  {
    _facade=facade;
    _countByType=new HashMap<Integer,IntegerHolder>();
  }

  /*
  private static int[] IDS= {1879049908,1879059895,1879060264,1879084796,1879084798,1879084800,1879085263,1879085275,1879085288,1879085289,1879102770,
      1879105669,1879105702,1879105719,1879105751,1879105764,1879105766,1879105808,1879105875,1879105891,1879105894,1879105901,1879105921,1879106009,
      1879106037,1879106052,1879106072,1879106073,1879106093,1879106104,1879106124,1879106127,1879106147,1879106213,1879106249,1879119370,1879136185,
      1879151960,1879156819,1879157300,1879189153,1879189154,1879189155,1879189156,1879189157,1879192023,1879211576,1879211633,1879212135,1879220100,
      1879222091,1879222092,1879224296,1879232939,1879239108,1879267320,1879325961,1879381487};
      */

  private List<Long> ids=new ArrayList<Long>();

  private void handleEntry(long id)
  {
    if (id%10000000==0) System.out.println("ID: "+id);
    byte[] data=_facade.loadData(id);
    if ((data!=null) && (data.length>=8))
    {
      boolean found=true;
      // Select
      for(byte[] toSearch : _toSearch)
      {
        int index=findBuffer(data,toSearch);
        if (index==-1)
        {
          found=false;
          break;
        }
      }
      if (found)
      {
        handleBuffer(id,data);
      }
    }
  }

  private void handleBuffer(long id, byte[] data)
  {
    int type=getType(data);
    Integer typeKey=Integer.valueOf(type);
    // Count by type
    IntegerHolder holder=_countByType.get(typeKey);
    if (holder==null)
    {
      holder=new IntegerHolder();
      _countByType.put(typeKey,holder);
    }
    holder.increment();
    System.out.println("Found "+id);
    long propsId=(id<0x78FFFFFF)?id+0x9000000:id;
    PropertiesSet props=_facade.loadProperties(propsId);
    System.out.println("*********** entry "+id+"******************");
    if (props!=null)
    {
      System.out.println(props.dump());
    }
    else
    {
      System.out.println("props is null");
    }
    ids.add(Long.valueOf(id));
  }

  private int findBuffer(byte[] buffer, byte[] toFind)
  {
    return KPM.indexOf(buffer,toFind);
  }

  private int getType(byte[] data)
  {
    int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
    return classDefIndex;
  }

  private void searchUtf16String(String s)
  {
    byte[] buffer=s.getBytes(StandardCharsets.UTF_16);
    int length=s.length();
    // Swap all words
    for(int i=0;i<length;i++)
    {
      byte tmp=buffer[i*2+1];
      buffer[i*2+1]=buffer[i*2];
      buffer[i*2]=tmp;
    }
    _toSearch=new byte[1][];
    _toSearch[0]=buffer;
    search();
  }

  private void searchId(int[] idToSearch)
  {
    _toSearch=new byte[idToSearch.length][];
    for(int i=0;i<idToSearch.length;i++)
    {
      _toSearch[i]=intToByteArray(idToSearch[i]);
      int revert=BufferUtils.readUInt32(new ByteArrayInputStream(_toSearch[i]));
      if (revert!=idToSearch[i])
      {
        System.out.println("Bad int->byte[] conversion!");
      }
    }
    System.out.println("************** searching ids=" + Arrays.toString(idToSearch) + "******************");
    search();
  }

  private void search()
  {
    // Iterate on keys
    //for(int id=0x70000000;id<0x7FFFFFFF;id++)
    for(long id=0x00000000L;id<0x9FFFFFFFL;id++)
    {
      handleEntry(id);
    }
    /*
    DATArchive archive=_facade.getDatFilesManager().getArchive(DATFilesConstants.HIGHRES);
    DirectoryEntry rootEntry=archive.getRootEntry();
    handleDirectory(archive,rootEntry);
    */
  }

  void handleDirectory(DATArchive archive, DirectoryEntry dir)
  {
    try
    {
      archive.ensureLoaded(dir);
      //System.out.println("Directory: "+dir);
      List<FileEntry> entries=dir.getFiles();
      for(FileEntry entry : entries)
      {
        long id=entry.getFileId();
        handleEntry(id);
      }
      List<DirectoryEntry> dirEntries=dir.getDirectories();
      for(DirectoryEntry dirEntry : dirEntries)
      {
        handleDirectory(archive,dirEntry);
      }
    }
    catch(IOException ioe)
    {
      ioe.printStackTrace();
    }
  }

  private void doIt()
  {
    searchId(new int[] {0x10000053, 0x73C4A39});
    //searchUtf16String("skill_hunter_recall_thorinshall");
    System.out.println(_countByType);
    System.out.println(ids);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatBrowser(facade).doIt();
    facade.dispose();
  }
}
