package org.nlogo.deltatick;

import org.nlogo.agent.TreeAgentSet;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 1/25/13
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpeciesInspector {
    String breedName;
    String startLifeSpan;
    String endLifeSpan;
    String lowestEnergy;
    String highestEnergy;
    ArrayList<Trait> traitsList = new ArrayList<Trait>();
    ArrayList<Trait> selectedTraitsList = new ArrayList<Trait>();
    HashMap<Trait, Variation> selectedVariationHashMap = new HashMap<Trait, Variation>();
    HashMap<String, String> selectedTraitValues = new HashMap<String, String>();
    HashMap<String, String> selectedTraitVariations = new HashMap<String, String>();

    //TODO send values from SpeciesInspectorPanel to this object to separate GUI from data

    public SpeciesInspector() {

        }

    public ArrayList<Trait> getSelectedTraitsList() {
        return selectedTraitsList;
    }

    public void addToSelectedTraitsList(Trait trait) {
        selectedTraitsList.add(trait);
    }

    public void removeTrait(String traitName) {
        Iterator it = selectedTraitsList.iterator();
        while(it.hasNext()) {
            Trait t = (Trait) it.next();
            if (traitName.equalsIgnoreCase(t.getNameTrait())) {
                it.remove();
            }
        }
    }

    public void addtoSelectedVariations(Trait string, Variation variation) {
        selectedVariationHashMap.put(string, variation);
    }


}
