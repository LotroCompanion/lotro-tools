package delta.games.lotro.tools.dat.misc;

import delta.common.utils.collections.CollectionTools;
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
    private final EnumMapper uiElementTypeMapper;
    private final Map<Integer, UIElement> elementLookup;
    private final Map<Integer, UILayout> layoutLookup;
    private final Map<String, UIElement> skinPanels;
    private final Map<Integer, String> images;
    private final File outputFolder;
    private final JSONObject jsonImages;
    private final JSONObject jsonPanels;
    private final  boolean dumpAssocations;

    public UISkinLoader(DataFacade facade, File outputFolder, boolean dumpAssocations) {
        this.facade = facade;
        this.uiLoader = new UILayoutLoader(facade);
        this.artAssetMapper = facade.getEnumsManager().getEnumMapper(587202796);
        this.uiElementMapper = facade.getEnumsManager().getEnumMapper(587202769);
        this.uiElementTypeMapper = facade.getEnumsManager().getEnumMapper(587202763);
        this.elementLookup = new HashMap<>();
        this.layoutLookup = new HashMap<>();
        this.skinPanels = new HashMap<>();
        this.images = new HashMap<>();
        this.outputFolder = outputFolder;
        this.dumpAssocations = dumpAssocations;
        this.jsonImages = new JSONObject();
        this.jsonPanels = new JSONObject();
    }

    public void doIt() {

//        FilesDeleter deleter=new FilesDeleter(outputFolder,null,true);
//        deleter.doIt();
//        outputFolder.mkdirs();
        buildImages();
        buildUILayout();

        if (this.dumpAssocations) {
            try (PrintWriter fw = new PrintWriter(new FileWriter(new File(outputFolder, "SkinData.json")))) {
                JSONObject data = new JSONObject();
                data.put("panels", this.jsonPanels);
                data.put("assets", this.jsonImages);
                data.write(fw);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

            // these images aren't in skinning but are useful for plugins
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
            rec.put("w", image.getWidth());
            rec.put("h", image.getHeight());
            to.getAbsoluteFile().getParentFile().mkdirs();
            boolean ret= ImageIO.write(image,"tga",to);
            if (!ret) {
                System.out.println(to + " failed to save.");
            }
        }
        String imageIdHex = String.format("0x%08X", dataId);
        //rec.put("id", imageIdHex);
        rec.put("n", assetName);
        if (this.dumpAssocations) jsonImages.put(imageIdHex, rec);
        return to;
    }

    private String capitalize(String phrase, boolean lowerFirst) {
        String[] words = phrase.split("[\\W_]+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0 && lowerFirst) {
                word = word.isEmpty() ? word : word.toLowerCase();
            } else {
                word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            }
            builder.append(word);
        }
        return builder.toString();
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
                /*
                // map & theater are unexposed skins.  They aren't fully plumbed into skin system.
                //  worth exploring
                if (label.matches("^(map|theater)")) {
                    skinPanels.put("ID_UISkin_" + capitalize(label, false), layout.getChildElements());
                    buildLookup(layout);
                }
                */
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
            //for (Map.Entry<String, UIElement> panel : Collections.singletonMap("ID_UISkin_AccomplishmentPanel", skinPanels.get("ID_UISkin_AccomplishmentPanel")).entrySet()) {
            for (Map.Entry<String, UIElement> panel : skinPanels.entrySet()) {
                JSONObject rec = new JSONObject();
                fw.println(String.format("<PanelFile ID=\"%s\">", panel.getKey()));
                printChildren(fw, panel.getValue(), rec, "\t");
                fw.println("</PanelFile>");
                if (this.dumpAssocations) this.jsonPanels.put(panel.getKey(), rec);
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
        String name = uiElementMapper.getLabel(id);
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
        Rectangle r = ui.getRelativeBounds();
        // {UICore_Element_ObjectMode=UICore_Element_ObjectMode: 1 (Stretch)} <-- Investigate
        //UICore_Element_ObjectMode -> {PropertiesSet$PropertyValue@1120} "UICore_Element_ObjectMode: 3 (CanvasResize)"
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
                    if (!r.contains(baseEl.getRelativeBounds()) && !baseEl.getChildElements().isEmpty()) {
                        //System.out.println("Parent: " + name + ", Base: " + uiElementMapper.getString(base) + " (" + basetype + "), children # : " + baseEl.getChildElements().size());
                        //String basetype = uiElementTypeMapper.getLabel(baseEl.getTypeID());
                        //System.out.println("Base: " + uiElementMapper.getString(base) + " (" + basetype + "), children # : " + baseEl.getChildElements().size());
                        baseEl = this.constrainElementTo(baseEl, r);
                    }
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

        String openingTag = String.format(prefix + "<Element ID=\"%s\" X=\"%s\" Y=\"%s\" Width=\"%s\" Height=\"%s\">", name, (int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());

        rec.put("id", name);
        if (this.dumpAssocations && !assets.isEmpty()) {
            // I want keys and values to be in same order
            // so can't use keys & values functions
            /*
            List<String> keys = new LinkedList<>();
            List<String> values = new LinkedList<>();
            for (Map.Entry<String, String> e : assets.entrySet()) {
                keys.add(e.getKey());
                values.add(e.getValue());
            }
            String artAssetID = CollectionTools.stringListAsString(keys, ",", null, null);
            String imageID = CollectionTools.stringListAsString(values, ",", null, null);
            openingTag = String.format(prefix + "<Element ID=\"%s\" ArtAssetID=\"%s\" ImageID=\"%s\" X=\"%s\" Y=\"%s\" Width=\"%s\" Height=\"%s\">", name, artAssetID, imageID, (int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
            */
            rec.put("assets", new JSONArray(new LinkedList(assets.values())));
        }
        JSONObject bounds = new JSONObject();
        bounds.put("w", (int) r.getWidth());
        bounds.put("h", (int) r.getHeight());
        bounds.put("x", (int) r.getX());
        bounds.put("y", (int) r.getY());
        rec.put("b", bounds);

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
        rec.put("c", jsonChildren);
        if (notFound != null) {
            pw.println(prefix + "\t<!-- NOT FOUND: " + notFound + " -->");
        }
        pw.println(prefix + "</Element>");
    }

    private UIElement shallowCopyEl(UIElement el) {
        UIElement newEl = new UIElement(el.getIdentifier());
        newEl.setTypeID(el.getTypeID());
        newEl.setMargins(el.getMargins().clone());
        newEl.setBaseLayoutDID(el.getBaseLayoutDID());
        newEl.setBaseElementId(el.getBaseElementId());
        PropertiesSet oldPs = el.getProperties();
        PropertiesSet ps = newEl.getProperties();
        for (String name : oldPs.getPropertyNames()) {
            ps.setProperty(oldPs.getPropertyValueByName(name));
        }
        for (UIData d : el.getData()) newEl.addData(d);
        newEl.setRelativeBounds((Rectangle) el.getRelativeBounds().clone());
        return newEl;
    }

    private Map<String, UIElement> shallowCloneChildrenToLabelMap(UIElement el, String stripLabelPrefix) {
        Map<String, UIElement> children = new LinkedHashMap<>();
        for (UIElement c : el.getChildElements()) {
            String name = uiElementMapper.getLabel(c.getIdentifier()).replaceAll(stripLabelPrefix, "");
            children.put(name, shallowCopyEl(c));
        }
        return children;
    }

    private UIElement constrainElementTo(UIElement el, Rectangle r) {
        UIElement newEl = shallowCopyEl(el);
        newEl.setRelativeBounds((Rectangle) r.clone());

        String name = uiElementMapper.getLabel(el.getIdentifier());
        switch (name) {
            case "Tab_Tier1_Left": {
                UIElement oldTab = el.getChildElements().get(0);
                UIElement newTab = shallowCopyEl(oldTab);
                int tabWidth = (int) (r.getWidth() - 1);
                newTab.getRelativeBounds().setSize(tabWidth, (int) r.getHeight());
                newEl.getChildElements().add(newTab);
                Map<String, UIElement> children = shallowCloneChildrenToLabelMap(oldTab, "^tab_front_");
                Rectangle nRec = children.get("n").getRelativeBounds();
                Rectangle wRec = children.get("w").getRelativeBounds();
                Rectangle eRec = children.get("e").getRelativeBounds();
                nRec.setSize((int) (tabWidth - wRec.getWidth() - eRec.getWidth() + 3), (int) nRec.getHeight());
                eRec.setLocation((int) (tabWidth - eRec.getWidth() + 1), (int) eRec.getY());
                newTab.getChildElements().addAll(children.values());
                break;
            }
            case "Box_Silver": {
                int recHeight = (int) r.getHeight();
                int recWidth = (int) r.getWidth();
                Map<String, UIElement> children = shallowCloneChildrenToLabelMap(el, "^Base_Box_Silver_");
                Rectangle mRec = children.get("TopMid").getRelativeBounds();
                Rectangle lRec = children.get("TopLeft").getRelativeBounds();
                Rectangle rRec = children.get("TopRight").getRelativeBounds();
                int middleWidth = (int) (recWidth - lRec.getWidth() - rRec.getWidth());
                int rightOffset = (int) (recWidth - rRec.getWidth());
                mRec.setSize(middleWidth, (int) mRec.getHeight());
                rRec.setLocation(rightOffset, (int) rRec.getY());
                int sideHeight = (int) lRec.getHeight();
                int midHeight = (int) mRec.getHeight();

                mRec = children.get("Background").getRelativeBounds();
                lRec = children.get("MidLeft").getRelativeBounds();
                rRec = children.get("MidRight").getRelativeBounds();
                mRec.setSize((int) (recWidth - lRec.getWidth() - rRec.getWidth()), (int) (recHeight - (midHeight * 2)));
                rRec.setBounds((int) (recWidth - rRec.getWidth()), (int) rRec.getY(), (int) rRec.getWidth(), (int) (recHeight - (sideHeight * 2)));
                lRec.setSize((int) lRec.getWidth(), (int) rRec.getHeight());

                mRec = children.get("BottomMid").getRelativeBounds();
                lRec = children.get("BottomLeft").getRelativeBounds();
                rRec = children.get("BottomRight").getRelativeBounds();
                mRec.setBounds((int) mRec.getX(), (int) (recHeight - mRec.getHeight()), middleWidth, (int) mRec.getHeight());
                rRec.setLocation(rightOffset, (int) (recHeight - rRec.getHeight()));
                lRec.setLocation((int) lRec.getX(), (int) (recHeight - rRec.getHeight()));

                newEl.getChildElements().addAll(children.values());
                break;
            }
            case "Base_Box_Titlebar": {
                Map<String, UIElement> children = shallowCloneChildrenToLabelMap(el, "^Base_Box_Titlebar_Top");
                Rectangle mRec = children.get("Mid").getRelativeBounds();
                Rectangle lRec = children.get("Left").getRelativeBounds();
                Rectangle rRec = children.get("Right").getRelativeBounds();
                mRec.setSize((int) (r.getWidth() - lRec.getWidth() - rRec.getWidth()), (int) mRec.getHeight());
                rRec.setLocation((int) (r.getWidth() - rRec.getWidth()), (int) rRec.getY());
                newEl.getChildElements().addAll(children.values());
                // TODO: The label needs sized
                break;
            }
            default:
                List<String> children = new LinkedList<>();
                for (UIElement c : el.getChildElements()) {
                    Rectangle cr = c.getRelativeBounds();
                    children.add(String.format("%s (x:%s, y:%s, w:%s, h: %s, c:[%s])", uiElementMapper.getLabel(c.getIdentifier()), cr.getX(), cr.getY(), cr.getWidth(), cr.getHeight(), c.getChildElements().size()));
                }
                //System.out.println("Base: " + String.format("%s (x:%s, y:%s, w:%s, h: %s)", uiElementMapper.getLabel(el.getIdentifier()), r.getX(), r.getY(), r.getWidth(), r.getHeight()) + " => " + CollectionTools.stringListAsString(children, null, ", "));
                System.out.println("Base: " + uiElementMapper.getLabel(el.getIdentifier()) + " => " + CollectionTools.stringListAsString(children, null, ", "));
                return el;
        }

         /*
        Base: AccomplishmentsPage_Base => , AccomplishmentsPage_FilterMenuContainer (x:-6.0, y:48.0, w:177.0, h: 32.0, c:[2]) , AccomplishmentExamination_Container (x:269.0, y:0.0, w:276.0, h: 460.0, c:[1]) , AccomplishmentsPage_ShowCompletedField (x:165.0, y:41.0, w:95.0, h: 20.0, c:[2]) , AccomplishmentsPage_Title (x:1.0, y:12.0, w:268.0, h: 30.0, c:[0]) , AccomplishmentsList_Container (x:6.0, y:8.0, w:262.0, h: 457.0, c:[2]) , AccomplishmentsPage_ShowSetRewardsField (x:165.0, y:59.0, w:105.0, h: 20.0, c:[2]) , AccomplishmentTrackerButton (x:403.0, y:422.0, w:128.0, h: 20.0, c:[0]) , WebStoreAccelerateDeedButton (x:280.0, y:408.0, w:103.0, h: 42.0, c:[1])
        Base: Base_Box_Titlebar => , Base_Box_Titlebar_TopRight (x:856.0, y:0.0, w:35.0, h: 42.0, c:[0]) , Base_Box_Titlebar_TopLeft (x:0.0, y:0.0, w:35.0, h: 42.0, c:[0]) , Base_Box_Titlebar_TopMid (x:35.0, y:0.0, w:821.0, h: 42.0, c:[0])
        Base: Box_Silver => , Base_Box_Silver_BottomLeft (x:0.0, y:476.0, w:36.0, h: 36.0, c:[0]) , Base_Box_Silver_BottomMid (x:36.0, y:476.0, w:440.0, h: 36.0, c:[0]) , Base_Box_Silver_BottomRight (x:476.0, y:476.0, w:36.0, h: 36.0, c:[0]) , Base_Box_Silver_MidRight (x:476.0, y:36.0, w:36.0, h: 440.0, c:[0]) , Base_Box_Silver_Background (x:36.0, y:36.0, w:440.0, h: 440.0, c:[0]) , Base_Box_Silver_TopRight (x:476.0, y:0.0, w:36.0, h: 36.0, c:[0]) , Base_Box_Silver_TopLeft (x:0.0, y:0.0, w:36.0, h: 36.0, c:[0]) , Base_Box_Silver_TopMid (x:36.0, y:0.0, w:440.0, h: 36.0, c:[0]) , Base_Box_Silver_MidLeft (x:0.0, y:36.0, w:36.0, h: 440.0, c:[0])
        Base: Reputation_Tab => , Reputation_Tab_Icon (x:0.0, y:0.0, w:59.0, h: 47.0, c:[0])
        Base: Tab_Tier1_Left => , tab_tier1_left_innards (x:0.0, y:0.0, w:200.0, h: 29.0, c:[3])
         */
        return newEl;
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

        if (args.length < 1 || args.length > 2) {
            System.out.println("USAGE:\n\t" + UISkinLoader.class.getName() + " <out folder> [<dump associations> (true|false)]\n");
            System.exit(0);
        }
        File outputFolder = new File(args[0]);
        if (!outputFolder.exists()) outputFolder.mkdirs();
        DataFacade facade = new DataFacade();
        UISkinLoader skinning = new UISkinLoader(facade, outputFolder, args.length > 1 && "true".equals(args[1]));
        skinning.doIt();
        facade.dispose();
    }

}
