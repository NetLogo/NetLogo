package org.nlogo.deltatick.xml;

import org.nlogo.deltatick.BreedBlock;
import org.nlogo.deltatick.EnvtBlock;
import org.nlogo.deltatick.PlotBlock;
import org.nlogo.deltatick.TraitBlock;
import org.nlogo.prim._patches;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 8, 2010
 * Time: 4:55:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelBackgroundInfo {
    ArrayList<Breed> breeds = new ArrayList<Breed>(); //list of breeds available in XML
    ArrayList<Global> globals = new ArrayList<Global>();
    ArrayList<Envt> envts = new ArrayList<Envt>();
    ArrayList<Trait> traits = new ArrayList<Trait>();
    String setup;
    String go;
    String library;
    String version;
    String draw;

    public ModelBackgroundInfo() {
    }

    public void clear() {
        breeds.clear();
        globals.clear();
        envts.clear();
        setup = null;
        go = null;
        library = null;
        version = null;
        draw = null;
        traits.clear();
    }

    public void populate(NodeList breedNodes, NodeList traitNodes, NodeList globalNodes, NodeList envtNodes, NodeList setup, NodeList go, NodeList library, NodeList draw, NodeList behavior) throws Exception {
        try {
            if (setup.getLength() > 0) {
                this.setup = setup.item(0).getTextContent();
            }
            if (go.getLength() > 0) {
                this.go = go.item(0).getTextContent();
            }

            if (draw.getLength() > 0) {
                this.draw = draw.item(0).getTextContent();
            }

            for (int i = 0; i < traitNodes.getLength(); i++) {
                Node traitNode = traitNodes.item(i);
                traits.add(new Trait(traitNode));
            }

            // populating class variable, breeds with the given breedNodes nodelist -A. (oct 17)
            for (int i = 0; i < breedNodes.getLength(); i++) {
                Node breedNode = breedNodes.item(i);
                breeds.add(new Breed(breedNode));
            }

            for (Breed breed : breeds) {
                breed.setTraitsArrayList(traits);
            }


            for (int i = 0; i < envtNodes.getLength(); i++) {
                Node envtNode = envtNodes.item(i);
                envts.add(new Envt(envtNode));
            }

            for (int i = 0; i < globalNodes.getLength(); i++) {
                Node globalNode = globalNodes.item(i);
                globals.add(new Global(globalNode));
            }


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
    public String setupBlock(List<BreedBlock> usedBreeds, List<TraitBlock> usedTraits, List<EnvtBlock> usedEnvts, List<PlotBlock> myPlots) {
        String code = "to setup\n";
        code += "  clear-all\n" + "ask patches [set pcolor white]\n";
        if (setup != null) {
            code += setup;
        }
        //this shows up when BreedBlock is dragged into BuildPanel -A. (sept 13)
        for (BreedBlock breedBlock : usedBreeds) {
            //code += breedBlock.setBreedShape();
            code += breedBlock.setup();
        }
         /*
        for (TraitBlock traitBlock : usedTraits) {
            code += traitBlock.setup();
        } */
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
            //code += "  do-plotting\n";
        }
        code += "end\n";

        return code;
    }

    public String updateBlock(List<BreedBlock> usedBreeds, List<EnvtBlock> usedEnvts) {
        String code = "";
        if (go != null) {
            code += go;
        }

        for (BreedBlock breedBlock : usedBreeds) {
            code += breedBlock.update();
        }
        for (Global global : globals) {
            code += global.update();
        }
        for (EnvtBlock envtBlock : usedEnvts) {
            code += envtBlock.update();
        }

        return code;
    }

    public String drawCode() {
        String code = "";
        if (draw != null) {
            code += draw;
        }
        return code;
    }

    // return only names of breeds -A. (Oct 17)
    public String[] getBreedTypes() {
        // breedTypes is an array of size n of breeds -A. (oct 5)
        String[] breedTypes = new String[breeds.size()];
        int i = 0;
        for (Breed breed : breeds) {
            breedTypes[i] = breed.plural;
            i++;
        }
        //System.out.println(breedTypes);
        return breedTypes;
    }

    // If I give you a breed name, give me other information about that breed -A. (oct 17)
    public Breed getBreed(String name) throws Exception {
        for (Breed breed : breeds) {
            if (breed.plural() == name) {
                return breed;
            }
        }
        throw new Exception();
    }

    //get entire ArrayList -A. (oct 17)
    public ArrayList<Breed> getBreeds() {
        return breeds;
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

    public String[] getTraitTypes() {
        String[] traitTypes = new String[traits.size()];
        int i = 0;
        for (Trait trait : traits) {
            traitTypes[i] = trait.getNameTrait();
            i++;
        }
        return traitTypes;
    }

    public ArrayList<Trait> getTraits() {
        return traits;
    }

    public String[] getVariationTypes(String traitName) {
        String [] variations = null;
        for (Trait trait : traits) {
            if (trait.getNameTrait().equals(traitName)) {
                variations = new String[trait.getVariationsList().size()];
                trait.getVariationsList().toArray(variations);
            }
        }
        return variations;
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

    public String getLibrary() {
        return library;
        }
}