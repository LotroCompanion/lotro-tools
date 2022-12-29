package delta.games.lotro.tools.updater;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import delta.updates.data.DirectoryDescription;
import delta.updates.data.SoftwareDescription;
import delta.updates.data.SoftwarePackageDescription;
import delta.updates.data.Version;
import delta.updates.tools.PackagesBuilder;
import delta.updates.tools.ToolsConfig;
import delta.updates.utils.DescriptionBuilder;

/**
 * Initializes the given directory so that it becomes manageable
 * by the updates manager.
 * @author DAM
 */
public class MainInitUpdatesSystemLoreDatabaseRu
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    // Constants
    //String baseURL="http://localhost:8080/delta-web-genea-1.1-SNAPSHOT/app16/";
    String baseURL="https://sourceforge.net/projects/lotrocompanion/files/loredb/ru/${file}/download";
    File to=new File("d:/tmp/loredb-ru-packages");
    ToolsConfig config=new ToolsConfig(baseURL,to); 
    File from=new File("D:/shared/damien/dev/lotrocompanion/releases/EoA-ru/LotRO Lore Database/app");

    // Build a description of the software directory
    DescriptionBuilder descriptionBuilder=new DescriptionBuilder();
    DirectoryDescription description=(DirectoryDescription)descriptionBuilder.build(from);
    description.removeEntry(".updates");
    description.setName("");

    // Build packages
    PackagesBuilder builder=new PackagesBuilder(from,config);
    List<SoftwarePackageDescription> packages=new ArrayList<SoftwarePackageDescription>();
    int packageID=0;
    // Data
    DirectoryDescription dataDir=(DirectoryDescription)description.getEntryByName("data");
    packages.add(builder.buildPackage(packageID,"data",dataDir));
    packageID++;
    // Lib
    DirectoryDescription libDir=(DirectoryDescription)description.getEntryByName("lib");
    packages.add(builder.buildPackage(packageID,"lib",libDir));
    packageID++;
    // Remaining
    packages.add(builder.buildPackage(packageID,"main",description));
    packageID++;

    // Build the software description
    SoftwareDescription software=new SoftwareDescription(0);
    software.setName("LotRO Lore Database");
    software.setVersion(new Version(1011,"SoA Book 11 (Russian) - 1.0.0"));
    software.setContentsDescription("LotRO Lore Database");
    software.setDate(new Date().getTime());
    builder.updateSoftware(software,packages);
  }
}
