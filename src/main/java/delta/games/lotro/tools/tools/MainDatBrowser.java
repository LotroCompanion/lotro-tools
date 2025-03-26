package delta.games.lotro.tools.tools;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.KPM;

/**
 * Tool to search byte patterns in the DAT files.
 * @author DAM
 */
public class MainDatBrowser
{
  private static final long MIN_ID=0x70000000L; // 0x00000000L
  private static final long MAX_ID=0x7FFFFFFFL; // 0x9FFFFFFFL

  private DataFacade _facade;
  private List<Long> _ids;
  private Map<Integer,IntegerHolder> _countByType;
  private byte[][] _toSearch;
  private PrintStream _out;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatBrowser(DataFacade facade)
  {
    _facade=facade;
    _countByType=new HashMap<Integer,IntegerHolder>();
    _ids=new ArrayList<Long>();
    _out=System.out; // NOSONAR
  }

  private void handleEntry(long id)
  {
    if (id%10000000==0)
    {
      _out.println("ID: "+id);
    }
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
    _out.println("Found "+id);
    long propsId=(id<0x78FFFFFF)?id+0x9000000:id;
    PropertiesSet props=_facade.loadProperties(propsId);
    _out.println("*********** entry "+id+"******************");
    if (props!=null)
    {
      _out.println(props.dump());
    }
    else
    {
      _out.println("props is null");
    }
    _ids.add(Long.valueOf(id));
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

  void searchUtf16String(String s)
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

  void searchId(int[] idToSearch)
  {
    _toSearch=new byte[idToSearch.length][];
    for(int i=0;i<idToSearch.length;i++)
    {
      _toSearch[i]=intToByteArray(idToSearch[i]);
      int revert=BufferUtils.readUInt32(new ByteArrayInputStream(_toSearch[i]));
      if (revert!=idToSearch[i])
      {
        _out.println("Bad int->byte[] conversion!");
      }
    }
    _out.println("************** searching ids=" + Arrays.toString(idToSearch) + "******************");
    search();
  }

  private static byte[] intToByteArray(int value)
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

  private void search()
  {
    for(long id=MIN_ID;id<MAX_ID;id++)
    {
      handleEntry(id);
    }
  }

  private void doIt()
  {
    searchId(new int[] {0x10000053, 0x73C4A39});
    //searchUtf16String("skill_hunter_recall_thorinshall")
    _out.println(_countByType);
    _out.println(_ids);
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
