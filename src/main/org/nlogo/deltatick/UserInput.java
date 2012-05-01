package org.nlogo.deltatick;

import java.lang.reflect.Array;
import java.util.ArrayList;
import org.nlogo.deltatick.xml.Breed;
import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/19/12
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */

//TODO: When breed removed from BuildPanel, remove from ui_breeds

public class UserInput
         {
    // ArrayList of breeds
    ArrayList<ui_Breed> ui_breeds = new ArrayList<ui_Breed>();

    public UserInput () {

    }

    public void addBreed ( String breed ) {
        ui_Breed tmp = new ui_Breed();
        tmp.setName(breed);
        ui_breeds.add(tmp);
    }

    // this is working (March 23)
    public void addTraitAndVariations( String bName, String tName, ArrayList<String> vNames ) {
        addTrait( bName, tName );
        for ( String variation : vNames ) {
            addVariation( bName, tName, variation );
            //System.out.println( "userInput" + bName + " " + tName + " " + variation);
        }

    }

    public void addTrait ( String bname, String tname ) {
        for ( ui_Breed breed : ui_breeds ) {
            if (breed.getName().equals(bname)) {
                breed.addTrait(tname);
            }
        }
    }

    public void addVariation ( String bname, String tname, String vname) {
        for ( ui_Breed breed : ui_breeds ) {
            if (breed.getName().equals(bname)) {
                breed.addVariation(tname, vname);
            }
        }
    }

    public String[] getBreeds() {
        ArrayList<String> bnames = new ArrayList<String>();
        for ( ui_Breed ui_breed : ui_breeds ) {
            bnames.add(ui_breed.getName());
        }
        String[] breeds = new String[ui_breeds.size()];
        bnames.toArray(breeds);
        return breeds;
    }

    public String[] getTraits (String bname) {
        ArrayList<String> newList = new ArrayList<String>();
        for ( ui_Breed ui_breed : ui_breeds ) {
            if (ui_breed.getName().equals(bname)) {
                newList = ui_breed.getTraits();
            }
        }
        String[] traits = new String[newList.size()];
        newList.toArray(traits);
        return traits;

    }

    public ArrayList<String> getVariations (String bname, String tname) {
        ArrayList<String> vnames = new ArrayList<String>();
        for ( ui_Breed ui_breed : ui_breeds ) {
            if ( ui_breed.getName().equals(bname) ) {
                vnames = ui_breed.getVariations(tname);
            }
        }
        return vnames;
    }

    public String[] getBreedTraitVariation () {
        ArrayList<String> newList = new ArrayList<String>();
        for ( ui_Breed breed : ui_breeds ) {
            System.out.println(breed.ui_traits.size());
                for ( ui_Trait trait : breed.ui_traits ) {
                    for ( String variation : trait.ui_variations ) {
                        String tmp = new String ( breed.name + " " + trait.name + " " + variation );
                        newList.add(tmp);
                }
            }
        }
        String[] listAll = new String[newList.size()];
        newList.toArray(listAll);
        return listAll;
    }

    public String [] getTraitVariation ( String bName ) {
        ArrayList<String> newList = new ArrayList<String>();
        for ( ui_Breed breed : ui_breeds ) {
            if ( breed.getName().equals(bName) ) {
                ArrayList<String> traits = new ArrayList<String>(breed.getTraits());
                for ( String trait : traits ) {
                    ArrayList<String> variations = new ArrayList<String>(getVariations( bName, trait ));
                    for ( String variation : variations ) {
                        String tmp = new String (trait + " " + variation);
                        newList.add(tmp);
                    }
                }
            }
        }
        String[] traitsVariations = new String[newList.size()];
        newList.toArray(traitsVariations);
        return traitsVariations;

    }


    public class ui_Variation {
        String name;



        public void setName( String input ) {
            name = input;
        }

        public String getName() {
            return name;
        }

    }

    public class ui_Trait {
        String name;
        ArrayList<String> ui_variations = new ArrayList<String>();


        public void setName ( String input ) {
            name = input;
        }

        public String getName () {
            return name;
        }

        public void addVariation ( String v ) {
            ui_variations.add(v);
        }

        public ArrayList<String> getVariationList () {
            return ui_variations;
        }

    }

    public class ui_Breed {
        String name;
        ArrayList<ui_Trait> ui_traits = new ArrayList<ui_Trait>();

        public void setName ( String input ) {
            name = input;
        }

        public String getName () {
            return name;
        }

        public void addTrait ( String tname ) {
            ui_Trait tmp = new ui_Trait();
            tmp.setName( tname );
            ui_traits.add(tmp);
        }

        public void addVariation ( String tname, String vname ) {
            for ( ui_Trait trait : ui_traits ) {
                if (trait.getName() == tname) {
                    trait.addVariation( vname );
                }
            }
        }

        public ArrayList<String> getTraits () {
            ArrayList<String> tmp = new ArrayList<String>();
            for ( ui_Trait trait : ui_traits ) {
                tmp.add(trait.getName());
            }
            return tmp;
        }

        public ArrayList<String> getVariations (String tname) {
            ArrayList<String> tmp = new ArrayList<String>();
            for ( ui_Trait trait : ui_traits ) {
                if (trait.getName() == tname) {
                    tmp = trait.getVariationList();
                }
            }
            return tmp;

        }
}
}
