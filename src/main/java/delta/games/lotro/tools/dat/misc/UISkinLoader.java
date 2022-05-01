package delta.games.lotro.tools.dat.misc;

import delta.common.utils.collections.CollectionTools;
import delta.common.utils.files.FilesDeleter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.DIDMapper;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.ui.*;
import delta.games.lotro.dat.loaders.DataIdMapLoader;
import delta.games.lotro.dat.utils.DatIconsUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.*;

public class UISkinLoader {

    private final DataFacade facade;
    private final UILayoutLoader uiLoader;
    private final EnumMapper artAssetMapper;
    private final EnumMapper uiElementMapper;
    private final Map<Integer, UIElement> elementLookup;
    private final Map<Integer, UILayout> layoutLookup;
    private final Map<String, UIElement> skinPanels;
    private final Map<Integer, String> images;
    private final File outputFolder;
    private final JSONObject jsonImages;
    private final JSONObject jsonPanels;

    public UISkinLoader(DataFacade facade, File outputFolder) {
        this.facade = facade;
        this.uiLoader = new UILayoutLoader(facade);
        this.artAssetMapper = facade.getEnumsManager().getEnumMapper(587202796);
        this.uiElementMapper = facade.getEnumsManager().getEnumMapper(587202769);
        this.elementLookup = new HashMap<>();
        this.layoutLookup = new HashMap<>();
        this.skinPanels = new HashMap<>();
        this.images = new HashMap<>();
        this.outputFolder = outputFolder;
        this.jsonImages = new JSONObject();
        this.jsonPanels = new JSONObject();
    }

    public void doIt() {

        FilesDeleter deleter=new FilesDeleter(outputFolder,null,true);
        deleter.doIt();
        outputFolder.mkdirs();
        buildImages();
        buildUILayout();

        try (PrintWriter fw = new PrintWriter(new FileWriter(new File(outputFolder, "SkinImages.json")))) {
            JSONObject data = new JSONObject();
            data.put("panels", this.jsonPanels);
            data.put("assets", this.jsonImages);
            data.write(fw);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void buildImages() {
        try {

            PropertiesSet props = facade.loadProperties(0x78000011);
            Object[] array = (Object[]) props.getProperty("UISkin_MappingArray");

            PrintWriter fw = new PrintWriter(new FileWriter(new File(outputFolder, "SkinDefinitions.xml")));
            fw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            fw.println("<opt>");
            fw.println("\t<SkinName Name=\"UIArtPackTemplate\"></SkinName>");
            List<String> mappings = new LinkedList<>();
            for (Object entry : array) {
                PropertiesSet entryProps = (PropertiesSet) entry;
                int dataId = (Integer) entryProps.getProperty("UISkin_DataID");
                int assetID = (Integer) entryProps.getProperty("UISkin_UIArtAssetID");
                String assetName = artAssetMapper.getLabel(assetID);
                File to = writeImageToDisk(dataId, assetName);
                mappings.add(String.format("\t<Mapping ArtAssetID=\"%s\" FileName=\"%s\"></Mapping>", assetName, to.getName()));
            }
            Collections.sort(mappings);
            for (String mapping : mappings) {
                fw.println(mapping);
            }
            fw.println("</opt>");
            fw.close();

            Map<Integer, String> cursors = new HashMap<Integer, String>(){{
                put(0x410000DD, "cursor_move");
                put(0x41007DC7, "cursor_pointer");
                put(0x41007E20, "cursor_resize_diagonal_backward");
                put(0x410081BF, "cursor_resize_horizontal");
                put(0x410081C0, "cursor_resize_vertical");
                put(0x410081C5, "cursor_busy");
                put(0x4101973F, "cursor_resize_diagonal_forward");;
            }};
            for (Map.Entry<Integer, String> e : cursors.entrySet()) {
                writeImageToDisk(e.getKey(), e.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File writeImageToDisk(int dataId, String assetName) throws JSONException, IOException {
        JSONObject rec = new JSONObject();
        File to = new File(outputFolder, assetName + ".tga");
        this.images.put(dataId, assetName);
        if (!to.exists()) {
            // skins save images in tga format
            //boolean ret = DatIconsUtils.buildImageFile(facade, dataId, "tga", to);
            BufferedImage image = DatIconsUtils.loadImage(facade, dataId);
            rec.put("width", image.getWidth());
            rec.put("height", image.getHeight());
            to.getAbsoluteFile().getParentFile().mkdirs();
            boolean ret= ImageIO.write(image,"tga",to);
            if (!ret) {
                System.out.println(to + " failed to save.");
            }
        }
        String imageIdHex = String.format("0x%08X", dataId);
        rec.put("id", imageIdHex);
        rec.put("name", assetName);
        jsonImages.put(imageIdHex, rec);
        return to;
    }

    private void buildUILayout() {

        byte[] data = facade.loadData(0x28000000);
        int uiId = DataIdMapLoader.decodeDataIdMap(data).getDataIdForLabel("UILAYOUT");
        DIDMapper map = DataIdMapLoader.decodeDataIdMap(facade.loadData(uiId));
        for (String label : map.getLabels()) {
            int dataId = map.getDataIdForLabel(label);
            if (dataId == 0) continue;
            try {
                UILayout layout = uiLoader.loadUiLayout(dataId);
                if (layout == null) continue;
                buildLookup(layout);
            } catch (Exception e) {
            }
        }
        // These two Elements are missing from the UI Layout tables.  I had to
        // Hand map these with their child elements.. :(
        /*
        268436807 ->
         "ProgressUIElementTest_Label", "Progress_BaseBox", "ProgressUIElement_Meter", "BaseBox_Empty"
         */
        UIElement el = new UIElement(268436807);
        el.addChild(elementLookup.get(268442554));
        el.addChild(elementLookup.get(268436808));
        el.addChild(elementLookup.get(268436809));
        el.addChild(elementLookup.get(268436810));
        elementLookup.put(268436807, el);

        /*
        268441701 ->
        "BaseBox_Empty_mounted","Progress_BaseBox","ProgressUIElement_Meter","ProgressUIElement_Label"
        */
        el = new UIElement(268441701);
        el.addChild(elementLookup.get(268441703));
        el.addChild(elementLookup.get(268436808));
        el.addChild(elementLookup.get(268436809));
        el.addChild(elementLookup.get(268436811));
        elementLookup.put(268441701, el);

        /*
        // Tried scanning ranges to find layouts that were not located
        // in the DID Mappings.. this ended up not being needed
        // if you followed the "base Layout DID" of UIElements
        for (int i = 0x20000000; i < 0x2FFFFFFF; i++) {
            try {
                if (layoutLookup.containsKey(i)) continue;

                UILayout layout = uiLoader.loadUiLayout(i);
                if (layout != null && !layout.getChildElements().isEmpty()) { // && uiElements.getLabel(layout.getChildElements().get(0).getIdentifier()) != null) {
                    System.out.println("Found: " + i);
                }
            } catch (Exception e) {}
        }
        */

        try {
            // print it all out
            PrintWriter fw = new PrintWriter(new FileWriter(new File(outputFolder, "SkinDictionary.txt")));
            for (Map.Entry<String, UIElement> panel : skinPanels.entrySet()) {
                JSONObject rec = new JSONObject();
                fw.println(String.format("<PanelFile ID=\"%s\">", panel.getKey()));
                printChildren(fw, panel.getValue(), rec, "\t");
                fw.println("</PanelFile>");
                this.jsonPanels.put(panel.getKey(), rec);
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void buildLookup(UILayout l) {
        layoutLookup.put(l.getIdentifier(), l);
        for (UIElement child : l.getChildElements()) {
            buildLookup(child);
        }
    }

    private void buildLookup(UIElement ui) {
        int id = ui.getIdentifier();
        elementLookup.put(id, ui);
        int bld = ui.getBaseLayoutDID();
        if (bld > 0 && !layoutLookup.containsKey(bld)) {
            try {
                buildLookup(uiLoader.loadUiLayout(bld));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String skin = (String) ui.getProperties().getProperty("UISkin_UserElementID");
        if (skin != null) {
            skinPanels.put(skin, ui);
        }
        for (UIElement child : ui.getChildElements()) {
            buildLookup(child);
        }
    }

    private void printChildren(PrintWriter pw, UIElement ui, JSONObject rec, String prefix) throws JSONException {

        int id = ui.getIdentifier();
        String name = uiElementMapper.getString(id);

        // See which UIElements are tied to skinning images
        Map<String, String> assets = buildElementAssetMap(name, ui);

        List<UIElement> children = new LinkedList<>();
        int base = ui.getBaseElementId();
        int baseLayout = ui.getBaseLayoutDID();
        String notFound = null;
        if (base > 0) {
            try {
                UIElement baseEl = elementLookup.get(base);
                if (baseEl != null) {
                    Object templates = baseEl.getProperties().getProperty("UICore_ListBox_entry_templates");
                    if (templates != null) {
                        for (Object cur : (Object[]) templates) {
                            PropertiesSet ps = (PropertiesSet) cur;
                            int childId = (Integer) ps.getProperty("UICore_ListBox_entry_template_element");
                            UIElement child = elementLookup.get(childId);
                            if (child != null) {
                                children.add(child);
                            } else {
                                System.out.println("Couldn't find child! : " + childId);
                            }
                        }
                    }
                    if (!baseEl.getChildElements().isEmpty()) {
                        children.addAll(baseEl.getChildElements());
                    }
                } else {
                    // THIS SHOULD NOT HAPPEN!
                    // This is where the two elements manually defined
                    // above would reach. :(
                    String baseName = uiElementMapper.getLabel(base);
                    notFound = String.format("%s (%s)", baseName, base);
                    if (baseLayout > 0) {
                        // the layouts should have already been found at this point. no reason to have to look it up
                        UILayout layout = uiLoader.loadUiLayout(baseLayout);
                        baseEl = getUIElementById(uiElementMapper, base, layout.getChildElements(), "");
                        System.out.println(baseEl);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        children.addAll(ui.getChildElements());

        Rectangle r = ui.getRelativeBounds();
        //ui.getProperties().getPropertyNames().forEach(n -> System.out.println(String.format("%s => %s", n, ui.getProperties().getProperty(n))));
        String openingTag = String.format(prefix + "<Element ID=\"%s\" X=\"%s\" Y=\"%s\" Width=\"%s\" Height=\"%s\">", name, (int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());

        rec.put("id", name);
        if (!assets.isEmpty()) {
            // I want keys and values to be in same order
            // so can't use keys & values functions
            JSONArray jsonAssets = new JSONArray();
            List<String> keys = new LinkedList<>();
            List<String> values = new LinkedList<>();
            for (Map.Entry<String, String> e : assets.entrySet()) {
                keys.add(e.getKey());
                values.add(e.getValue());
                JSONObject a = new JSONObject();
                a.put("assetID", e.getKey());
                a.put("imageID", e.getValue());
                jsonAssets.put(a);
            }
            rec.put("assets", jsonAssets);
            /*
            String artAssetID = CollectionTools.stringListAsString(keys, ",", null, null);
            String imageID = CollectionTools.stringListAsString(values, ",", null, null);
            openingTag = String.format(prefix + "<Element ID=\"%s\" ArtAssetID=\"%s\" ImageID=\"%s\" X=\"%s\" Y=\"%s\" Width=\"%s\" Height=\"%s\">", name, artAssetID, imageID, (int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
            */
        }
        JSONObject bounds = new JSONObject();
        bounds.put("width", (int) r.getWidth());
        bounds.put("height", (int) r.getHeight());
        bounds.put("x", (int) r.getX());
        bounds.put("y", (int) r.getY());
        rec.put("bounds", bounds);
        rec.put("margins", new JSONArray(ui.getMargins()));

        if (children.isEmpty() && notFound == null) {
            pw.println(openingTag + " </Element>");
            return;
        }
        pw.println(openingTag);
        JSONArray jsonChildren = new JSONArray();
        for (UIElement child : children) {
            JSONObject cRec = new JSONObject();
            printChildren(pw, child, cRec,prefix + "\t");
            jsonChildren.put(cRec);
        }
        rec.put("children", jsonChildren);
        if (notFound != null) {
            pw.println(prefix + "\t<!-- NOT FOUND: " + notFound + " -->");
        }
        pw.println(prefix + "</Element>");
    }

    private Map<String, String> buildElementAssetMap(String elementName, UIElement ui) {
        Map<String, String> assets = new TreeMap<>();
        for (UIData d : ui.getData()) {
            UIImage img = null;
            if (d instanceof UIImage) {
                img = (UIImage) d;
            } else if (d instanceof  UIStateImage) {
                img = ((UIStateImage) d)._image;
            }
            if (img != null) {
                int imageId = img._imageDID;
                if (imageId > 0) {
                    String imageID = String.format("0x%08X", imageId);
                    String artAssetID = images.get(imageId);
                    if (artAssetID == null) {
                        artAssetID = elementName.toLowerCase();
                        System.out.println("Image not found: " + imageID + " for " + elementName);
                        File to = new File(outputFolder, artAssetID + ".tga");
                        this.images.put(imageId, artAssetID);
                        if (!to.exists()) {
                            // skins save images in tga format
                            DatIconsUtils.buildImageFile(facade, imageId, "tga", to);
                        }
                    }
                    assets.put(artAssetID, imageID);
                }
            }
        }
        return assets;
    }

    private UIElement getUIElementById(EnumMapper uiElements, int id, List<UIElement> elements, String prefix) {
        for (UIElement ui : elements) {
            int uiElementId = ui.getIdentifier();
            String name = uiElements.getString(uiElementId);
            System.out.printf("%s%s (%s) <==> %s%n", prefix, name, uiElementId, id);
            if (uiElementId == id) {
                return ui;
            }
            UIElement foundElement = getUIElementById(uiElements, id, ui.getChildElements(), prefix + "\t");
            if (foundElement != null) {
                return foundElement;
            }
        }
        return null;
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("USAGE:\n\t" + UISkinLoader.class.getName() + " <out folder>\n");
            System.exit(0);
        }
        File outputFolder = new File(args[0]);
        if (!outputFolder.exists()) outputFolder.mkdirs();
        DataFacade facade = new DataFacade();
        UISkinLoader skinning = new UISkinLoader(facade, outputFolder);
        skinning.doIt();
        facade.dispose();
    }

}
