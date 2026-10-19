package bsearch.algorithms;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import bsearch.app.BehaviorSearchException;
import bsearch.util.GeneralUtils;

public class SearchMethodLoader {

    public static SearchMethod createFromName(String searchMethodName) throws BehaviorSearchException
    {
        String searchMethodClassName = searchMethodName;
        if (!searchMethodClassName.contains("."))
        {
            searchMethodClassName = "bsearch.algorithms." + searchMethodClassName;
        }
        SearchMethod searcher;
        try {
            searcher = (SearchMethod) Class.forName(searchMethodClassName).getDeclaredConstructor().newInstance();
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
            throw new BehaviorSearchException("Failed to find/load SearchMethod from Java class: " + searchMethodClassName );
        }
        return searcher;
    }

    public static List<String> getAllSearchMethodNames() throws BehaviorSearchException
    {
        Scanner scanner;
        try {
            scanner = new Scanner(GeneralUtils.getResource("SearchMethodList.txt"));
        } catch (FileNotFoundException e) {
            throw new BehaviorSearchException("Error loading list of SearchMethod names: File 'SearchMethodList.txt' couldn't be found.");
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
        return list;
    }
}
