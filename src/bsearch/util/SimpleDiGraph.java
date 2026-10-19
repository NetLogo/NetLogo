package bsearch.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/* TODO: This class needs more functionality, and needs tested.
 */
public class SimpleDiGraph<T> {
    List<T> vertices;
    HashMap<T,Set<T>> edges;

    private SimpleDiGraph()
    {
        vertices = new LinkedList<T>();
        edges = new HashMap<T,Set<T>>();
    }

    public Set<T> neighbors(T node)
    {
        if (edges.containsKey(node))
        {
            return edges.get(node);
        }
        else
        {
            throw new IllegalStateException("node key not found");
        }
    }


    public static SimpleDiGraph<String> readEdgeListFromFile(String filename) throws FileNotFoundException
    {
        SimpleDiGraph<String> G = new SimpleDiGraph<String>();

        Scanner scanner = new Scanner(new File(filename));

        while (scanner.hasNext())
        {
            String line = scanner.nextLine();
            String[] lineItems = line.trim().split("\\W");
            G.vertices.add(lineItems[0]);
            G.vertices.add(lineItems[1]);
            if (!G.edges.containsKey(lineItems[0]))
            {
                G.edges.put(lineItems[0], new LinkedHashSet<String>());
            }
            if (!G.edges.containsKey(lineItems[1]))
            {
                G.edges.put(lineItems[1], new LinkedHashSet<String>());
            }
            G.edges.get(lineItems[0]).add(lineItems[1]);
        }

        return G;
    }


}
