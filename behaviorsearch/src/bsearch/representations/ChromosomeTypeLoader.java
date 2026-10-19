package bsearch.representations;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import bsearch.app.BehaviorSearchException;
import bsearch.util.GeneralUtils;

public class ChromosomeTypeLoader {


    public static ChromosomeFactory createFromName(String chromosomeName) throws BehaviorSearchException
    {
        String chromosomeFactoryClassName = chromosomeName;
        if (!chromosomeFactoryClassName.contains("."))
        {
            // A bit of Java voodoo to find an inner class, so we can dynamically instantiate a factory...
            // (To be honest, we probably shouldn't be using inner classes, since this seems like a bit of a hack...)
            //  ~Forrest (10/1/2009)
            chromosomeFactoryClassName = "bsearch.representations." + chromosomeFactoryClassName + "$Factory";
        }
        ChromosomeFactory factory;
        try {
            factory = (ChromosomeFactory) Class.forName(chromosomeFactoryClassName).getDeclaredConstructor().newInstance();
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
            throw new BehaviorSearchException("Failed to find/load ChromosomeFactory from Java class: " + chromosomeFactoryClassName );
        }
        return factory;
    }

    public static List<String> getAllChromosomeTypes() throws BehaviorSearchException
    {
        Scanner scanner;
        try {
            scanner = new Scanner(GeneralUtils.getResource("ChromosomeTypeList.txt"));
        } catch (FileNotFoundException e) {
            throw new BehaviorSearchException("Error loading list of Chromosome class names: File 'ChromosomeTypeList.txt' couldn't be found.");
        }

        LinkedList<String> list = new LinkedList<String>();

        while ( scanner.hasNextLine() )
        {
            String s = scanner.nextLine();
            if (s.trim().length() > 0)
            {
                list.add(s);
            }
        }
        scanner.close();
        return list;
    }
}
