package delta.games.lotro.tools.extraction.global;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.config.servers.ServerDescription;
import delta.games.lotro.config.servers.io.xml.ServerXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;

/**
 * Builder for the servers file.
 * @author DAM
 */
public class MainServersBuilder
{
  /**
   * European servers.
   */
  private static final String EU="(EU)";
  /**
   * US servers.
   */
  private static final String US="(U.S)";
  private static final Logger LOGGER=LoggerFactory.getLogger(MainServersBuilder.class);

  private ServerDescription buildServer(String name, String ip, String location)
  {
    InetAddress address=null;
    try
    {
      address=InetAddress.getByName(ip);
    }
    catch(IOException ioe)
    {
      LOGGER.error("Cannot build server address", ioe);
    }
    ServerDescription ret=new ServerDescription();
    ret.setName(name);
    ret.setAddress(address);
    ret.setLocation(location);
    return ret;
  }

  private List<ServerDescription> buildServers()
  {
    List<ServerDescription> ret=new ArrayList<ServerDescription>();
    // US
    ret.add(buildServer("Arkenstone","198.252.160.98",US)); // NOSONAR
    ret.add(buildServer("Brandywine","198.252.160.99",US)); // NOSONAR
    ret.add(buildServer("Crickhollow","198.252.160.100",US)); // NOSONAR
    ret.add(buildServer("Gladden","198.252.160.101",US)); // NOSONAR
    ret.add(buildServer("Landroval","198.252.160.102",US)); // NOSONAR
    // EU
    ret.add(buildServer("Belegaer","198.252.160.103",EU)); // NOSONAR
    ret.add(buildServer("Evernight","198.252.160.104",EU)); // NOSONAR
    ret.add(buildServer("Gwaihir","198.252.160.105",EU)); // NOSONAR
    ret.add(buildServer("Laurelin","198.252.160.106",EU)); // NOSONAR
    ret.add(buildServer("Sirannon","198.252.160.107",EU)); // NOSONAR
    return ret;
  }

  /**
   * Build servers file.
   */
  public void doIt()
  {
    List<ServerDescription> servers=buildServers();
    File toFile=GeneratedFiles.SERVERS;
    boolean ok=ServerXMLWriter.writeServersFile(toFile,servers);
    if (ok)
    {
      LOGGER.info("Wrote file: "+toFile);
    }
    else
    {
      LOGGER.error("Failed to build servers file: "+toFile);
    }
  }


  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    MainServersBuilder builder=new MainServersBuilder();
    builder.doIt();
  }
}
