package org.nlogo.deltatick.xml;

import org.nlogo.deltatick.BreedBlock;
import org.nlogo.deltatick.EnvtBlock;
import org.nlogo.deltatick.PlotBlock;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/16/12
 * Time: 6:23 PM
 * To change this template use File | Settings | File Templates.
 */

// this one is for EnvtBlocks

public class ModelBackgroundInfo2 {
    //ArrayList<Breed> breeds = new ArrayList<Breed>(); //list of breeds available in XML
    ArrayList<Global> globals = new ArrayList<Global>();
    ArrayList<Envt> envts = new ArrayList<Envt>();
    String setup;
    String go;
    String library;
    String version;

    public ModelBackgroundInfo2() {
    }

    public void clear() {
        //breeds.clear();
        globals.clear();
        envts.clear();
        setup = null;
        go = null;
        library = null;
        version = null;
    }

    public void populate(NodeList globalNodes, NodeList envtNodes, NodeList setup, NodeList go, NodeList library) throws Exception {
        try {
            if (setup.getLength() > 0) {
                this.setup = setup.item(0).getTextContent();
            }
            if (go.getLength() > 0) {
                this.go = go.item(0).getTextContent();
            }

            // populating class variable, breeds with the given breedNodes nodelist -A. (oct 17)

            for (int i = 0; i < envtNodes.getLength(); i++) {
                Node envtNode = envtNodes.item(i);
                envts.add(new Envt(envtNode));
            }

            for (int i = 0; i < globalNodes.getLength(); i++) {
                Node globalNode = globalNodes.item(i);
                globals.add(new Global(globalNode));
            }

            //for (int i = 0 ; i < patchNodes.getLength(); i++) {
            //  Node patchNode = patchNodes.item(i);
            //patches.add( new Patch( patchNode ));
            //}

            this.library = library.item(0).getAttributes().getNamedItem("name").getTextContent();
            this.version = library.item(0).getAttributes().getNamedItem("version").getTextContent();
        } catch (Exception e) {
            throw new Exception("Malformed XML file!" + e);
        }
    }

    // Where this stuff is being translated into NetLogo code in the code window -A.

    //these methods are activated and implemented when library is loaded itself -A. (sept 13)
    public String declareGlobals() {
        String code = "";
        if (globals.size() > 0) {
            code += "globals [\n";
            for (Global global : globals) {
                code += "  " + global.name + "\n";
            }
            code += "]\n";
        }
        return code;
    }

    //will have to insert setup code for patches here as well -A. (sept 13)
    public String setupBlock(List<EnvtBlock> usedEnvts, List<PlotBlock> myPlots) {
        String code = "to setup\n";
        code += "  clear-all\n";
        if (setup != null) {
            code += setup;
        }

        for (Global global : globals) {
            code += global.setup();
        }
        for (EnvtBlock envtBlock : usedEnvts) {
            //code += "to set";
            code += envtBlock.setup();
        }
        //insert setup code for patches here
        code += "  reset-ticks\n";
        if (myPlots.size() > 0) {
            code += "  do-plotting\n";
        }
        code += "end\n";

        return code;
    }

    public String updateBlock(List<EnvtBlock> usedEnvts) {
        String code = "";
        if (go != null) {
            code += go;
        }

        for (Global global : globals) {
            code += global.update();
        }
        for (EnvtBlock envtBlock : usedEnvts) {
            code += envtBlock.update();
        }

        return code;
    }


    public String[] getEnvtTypes() {
        String[] envtTypes = new String[envts.size()];
        int i = 0;
        for (Envt envt : envts) {
            envtTypes[i] = envt.nameEnvt;
            i++;
        }
        return envtTypes;

    }

    public ArrayList<Envt> getEnvts() {
        return envts;
    }

    class Global {
        String name;
        String setupReporter;
        String updateReporter;

        public Global(Node globalNode) {
            name = globalNode.getAttributes().getNamedItem("name").getTextContent();

            NodeList info = globalNode.getChildNodes();
            for (int i = 0; i < info.getLength(); i++) {
                if (info.item(i).getNodeName() == "setupReporter") {
                    setupReporter = info.item(i).getTextContent();
                }

                if (info.item(i).getNodeName() == "updateReporter") {
                    updateReporter = info.item(i).getTextContent();
                }
            }
        }

        public String setup() {
            String code = "";
            code += "set " + name + " " + setupReporter + "\n";
            return code;
        }

        public String update() {
            String code = "";
            code += "set " + name + " " + updateReporter + "\n";
            return code;
        }
    }

}
