package org.nlogo.deltatick.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 4/1/13
 * Time: 8:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeepCopyStream {

    private DeepCopyStream() {}

    static public Object deepClone(Object source) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        Object o = new Object();
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);

            oos.writeObject(source);
            oos.flush();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ois = new ObjectInputStream(bais);

            o = ois.readObject();


        } catch (Exception e) {
            System.out.println("Error in deepClone. " + e.getMessage());
        } finally {
            try {
            oos.close();
            ois.close();
            } catch (Exception e) {
                System.out.println("Error in closing streams. " + e.getMessage());
            }
        }

        return o;

    }
}
