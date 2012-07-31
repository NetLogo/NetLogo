package org.nlogo.deltatick;


import org.nlogo.deltatick.dnd.AgentInput;
import org.nlogo.deltatick.dnd.EnergyInput;
import org.nlogo.deltatick.dnd.PrettyInput;

//swing is for GUI components -A. (sept 10)
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 1, 2010
 * Time: 4:24:07 PM
 * To change this template use File | Settings | File Templates.
 */

//abstract class cannot be instantiated, but can be subclassed (eg. polygon class to be subclassed with square, triangle
//etc) -A. (sept 10)
public abstract class CodeBlock
        extends javax.swing.JPanel
        implements Transferable {

    //flavors is an array initialized with codeBlockFlavor -A. (sept 10)
    DataFlavor[] flavors = new DataFlavor[]{codeBlockFlavor};
    JLabel nameLabel;
    String code;
    private JButton exitButton = new JButton();
    String ifCode;

    // the following are linked so that they're always in order
    Map<String, JTextField> inputs = new LinkedHashMap<String, JTextField>();
    Map<String, JTextField> energyInputs = new LinkedHashMap<String, JTextField>();
    Map<String, JTextField> agentInputs = new LinkedHashMap<String, JTextField>();
    List<CodeBlock> myBlocks = new LinkedList<CodeBlock>();

    //BoxLayout either stacks components on top of each other, or in a row -A. (sept 9)

    BoxLayout myLayout;

    //defining an object, myParent of class, CodeBlock -A. (sept 10)
    CodeBlock myParent;

    Color color;

    JPanel label = new JPanel();
    RemoveButton removeButton = new RemoveButton(this);
    //constructor of RemoveButton takes a CodeBlock as parameter hence "this" -A. (Sept 10)

    //BoxLayout.Y_AXIS is why blocks stack one below each other -A. (sept 9)
    public CodeBlock(String name, Color color) {
        myLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(myLayout);
        this.color = color;
        setBackground(color);
        this.setForeground(color);
        setBorder(org.nlogo.swing.Utils.createWidgetBorder());
        setName(name);
        makeLabel();
        add(label);
    }


    //this method overrides other default definition is out there to getMinimumSize -A. (sept 10)
    @Override
    public java.awt.Dimension getMinimumSize() {
        return new java.awt.Dimension(getPreferredWidth(), 35);
    }

    // same as above -A. (Sept 10)
    @Override
    public java.awt.Dimension getPreferredSize() {
        if (myLayout != null) {
            int x = myLayout.preferredLayoutSize(this).width;
            int y = myLayout.preferredLayoutSize(this).height;
            return new java.awt.Dimension(
                    getPreferredWidth(),
                    java.lang.Math.max(35, y)
            );
        }
        return super.getPreferredSize();
    }

    //this makeLabel works only for behavior blocks and quantity block -A. (sept9)
    //getName() gets the name of the component -A. (sept 10)
    public void makeLabel() {
        JLabel name = new JLabel(getName());
        java.awt.Font font = name.getFont();
        name.setFont(new java.awt.Font("Arial", font.getStyle(), 11));
         // for PC

        label.add(removeButton);
        removeButton.setVisible(false);
        label.setBackground(getBackground());
        label.add(name);
    }

    public class RemoveButton extends JButton {
        CodeBlock myParent;
        RemoveButton thisButton;

        public RemoveButton(CodeBlock myParent) {
            this.myParent = myParent;
            this.thisButton = this;
            setPreferredSize(new Dimension(10, 10));
            setAction(deleteAction);
            setBorder(null);
            setForeground(java.awt.Color.gray);
            setBorderPainted(false);
            setMargin(new java.awt.Insets(5, 5, 5, 5));
        }

        private final javax.swing.Action deleteAction =
                new javax.swing.AbstractAction("X") {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        //CodeBlock homePanel = (CodeBlock) myParent.getParent();
                        myParent.die();

                        /*
                        for (CodeBlock block : myBlocks) {
                            if ( block instanceof TraitBlock ) {
                                block.die();
                            }
                        }
                        */
                    }
                };
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        for (int i = 0; i < flavors.length; i++) {
            if (dataFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    // not sure what these unpack methods are doing here
    public String unPackAsCode() {
        return "This should be overwritten:" + code;
    }

    public String unPackAsCommand() {
        return "This should be overwritten (command):" + code;
    }

    public String unPackAsProcedure() {
        if ( ifCode != null ) {
        return "This should be overwritten (procedure):" + code + " " + ifCode;
    }
        else {
            return "This should be overwritten (procedure):" + code;
        }
    }

    public void highlight() {
        this.setBackground(this.getBackground().brighter());
    }

    public void unHighlight() {
        this.setBackground(this.getBackground().darker());
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setIfCode(String code) {
        this.ifCode = code;
    }

    public Object getTransferData(DataFlavor dataFlavor)
            throws UnsupportedFlavorException {
        unSelectInputs();
        if (isDataFlavorSupported(dataFlavor)) {
            if (dataFlavor.equals(behaviorBlockFlavor) ||
                    dataFlavor.equals(plotBlockFlavor) ||
                    dataFlavor.equals(breedBlockFlavor) ||
                    dataFlavor.equals(quantityBlockFlavor) ||
                    dataFlavor.equals(conditionBlockFlavor) ||
                    dataFlavor.equals(patchBlockFlavor) ||
                    dataFlavor.equals(codeBlockFlavor) ||
                    dataFlavor.equals(traitBlockFlavor)) {
                return this;
            }
            if (dataFlavor.equals(DataFlavor.stringFlavor)) {
                return unPackAsCode();
            }
        } else {
            return "Flavor Not Supported";
        }
        return null;
    }

    //declaring dataflavors for each type of block -a.
    // The dataflavors as static vars to use when needed
    public static final DataFlavor breedBlockFlavor =
            new DataFlavor(BreedBlock.class, "Breed Block");
    public static final DataFlavor plotBlockFlavor =
            new DataFlavor(PlotBlock.class, "Plot Block");
    public static final DataFlavor behaviorBlockFlavor =
            new DataFlavor(BehaviorBlock.class, "Behavior Block");
    public static final DataFlavor conditionBlockFlavor =
            new DataFlavor(ConditionBlock.class, "Condition Block");
    public static final DataFlavor quantityBlockFlavor =
            new DataFlavor(QuantityBlock.class, "Quantity Block");
    public static final DataFlavor codeBlockFlavor =
            new DataFlavor(CodeBlock.class, "Code Block");
    public static final DataFlavor patchBlockFlavor =
            new DataFlavor(PatchBlock.class, "Patch Block");
    public static final DataFlavor envtBlockFlavor =
            new DataFlavor(EnvtBlock.class, "Envt Block");
    public static final DataFlavor traitBlockFlavor =
            new DataFlavor(TraitBlock.class, "Trait Block");

    public static final DataFlavor getDataFlavorForThisClass(Class myClass) {
        if (myClass == BreedBlock.class) {
            return breedBlockFlavor;
        }
        if (myClass == PlotBlock.class) {
            return breedBlockFlavor;
        }
        if (myClass == BehaviorBlock.class) {
            return breedBlockFlavor;
        }
        if (myClass == ConditionBlock.class) {
            return breedBlockFlavor;
        }
        if (myClass == QuantityBlock.class) {
            return breedBlockFlavor;
        }
        if (myClass == CodeBlock.class) {
            return breedBlockFlavor;
        }
        if (myClass == TraitBlock.class) {
            return breedBlockFlavor;
        }
        System.out.println(" oops... class is " + myClass);
        return null;
    }

    public void addInput(String inputName, String defaultValue) {
        PrettyInput input = new PrettyInput(this);
        input.setName(inputName);
        input.setText(defaultValue);

        //inputs is a linked hashmap <String, JTextField> (march 2)
        inputs.put(inputName, input);
        label.add(input);
    }


    public void addInputEnergy(String inputName, String defaultValue) {
        EnergyInput energyInput = new EnergyInput(this);
        energyInput.setName(inputName);
        energyInput.setText(defaultValue);

        energyInputs.put(inputName, energyInput);
        label.add(energyInput);
    }


    public void addAgentInput(String inputName, String defaultValue) {
        AgentInput agentInput = new AgentInput(this);
        agentInput.setName(inputName);
        agentInput.setText(defaultValue);

        agentInputs.put(inputName, agentInput);
        label.add(agentInput);
    }

    public void addDistanceInput(String inputName, String defaultValue) {
        DistanceInput distanceInput = new DistanceInput(this);
        distanceInput.setName(inputName);
        distanceInput.setText(defaultValue);

        agentInputs.put(inputName, distanceInput);
        label.add(distanceInput);
    }


    public void setMyParent(CodeBlock block) {
        myParent = block;

    }


    public Component getMyParent() {
        if (myParent != null) {
            return myParent;
        }
        return null;

    }


    //perform all the methods defined later in this class to the CodeBlock block
    // List<CodeBlock> myBlocks = new LinkedList<CodeBlock>() [myBlocks is a linked list]
    //any block that goes into Breed, Plot or Envt block becomes part of this myBlocks -A. (sept 9)
    public void addBlock(CodeBlock block) {
        myBlocks.add(block);
        this.add(block);
        block.enableInputs();
        block.showRemoveButton();
        this.add(Box.createRigidArea(new Dimension(this.getWidth(), 4)));
        block.setMyParent(this);
        block.doLayout();
        block.validate();
        block.repaint();

        /*
        if (block instanceof TraitBlock) {
            ((TraitBlock) block).numberAgents();
        }
        */

        //block.validate();
        // pc
        //block.revalidate();
        //validate();
        // pc
        //revalidate();
        //repaint();

        doLayout();
        validate();
        repaint();

        this.getParent().doLayout();
        this.getParent().validate();
        this.getParent().repaint();
    }


    public void removeBlock(CodeBlock block) {
        myBlocks.remove(block);

    }


    // myBlocks is a linked list of all blocks in BuildPanel while behaviorBlocks is an array list of behaviors
    // Both are similar, though slightly different in terms of goal and implementation.
    // In a LinkedList, each element is linked to its previous and next element making it easier to delete or insert in
    // the middle of the list. A ArrayList is more as its name subjects used as an array.
    //   Performance is similar, though LinkedList is optimized when inserting elements before the end of the list,
    // where ArrayList is optimized when adding elements at the end. (bigmoose)


    //this method goes through all the blocks in the linked list,myBlocks to find behavior blocks
    // and create the arraylist, BehaviorBlock
    // never used -A. (aug 26)
    public ArrayList<BehaviorBlock> behaviorBlocks() {
        ArrayList<BehaviorBlock> behaviors = new ArrayList<BehaviorBlock>();

        for (CodeBlock block : myBlocks) {
            if (block instanceof BehaviorBlock) {
                behaviors.add((BehaviorBlock) block);
            }
        }

        return behaviors;
    }


    //this method goes through all the blocks in the linked list,myBlocks to find condition blocks
    // and create the arrayedlist, ConditionBlock  -a.
    // not sure what this ArrayList is used for
    //never used - A. (aug 26)
    public ArrayList<ConditionBlock> conditionBlocks() {
        ArrayList<ConditionBlock> conditions = new ArrayList<ConditionBlock>();

        for (CodeBlock block : myBlocks) {
            if (block instanceof ConditionBlock) {
                conditions.add((ConditionBlock) block);
            }
        }

        return conditions;
    }


    //Hashmap is like an array, but the index is a key, not necessarily a number -A.
    public HashMap<String, CodeBlock> children() {
        HashMap<String, CodeBlock> children = new HashMap<String, CodeBlock>();
        for (CodeBlock codeBlock : myBlocks) {
            children.put(codeBlock.getName(), codeBlock);
            if (children != null) {
                children.putAll(codeBlock.children());
            }
        }
        return children;
    }

    // don't understand what this is doing -A. (sept 9)
    public int getPreferredWidth() {
        // find out how many levels down I am
        if (getParent() instanceof BuildPanel) {
            return 250;
        }
        if (getParent() instanceof CodeBlock) {
            return ((CodeBlock) getParent()).getPreferredWidth() - 10;
        }

        return 240;
    }

    public Insets getInsets() {
        return new Insets(8, 8, 7, 7);
    }

    public Rectangle getBounds() {
        return new Rectangle(getLocation().x,
                getLocation().y,
                getPreferredSize().width,
                getPreferredSize().height);
    }

    public void unSelectInputs() {
        for (JTextField input : inputs.values()) {
            input.setSelectionStart(0);
            input.setSelectionEnd(0);
        }
    }

    public void disableInputs() {
        for (JTextField input : inputs.values()) {
            input.setEditable(false);
        }
    }

    public void enableInputs() {
        for (JTextField input : inputs.values()) {
            input.setEditable(true);
        }
    }

    public void showRemoveButton() {
       removeButton.setVisible(true);
    }

    // TODO: Remove trait from myTraits when it is deleted from LibraryHolder
    public void die() {
        Container parent = getParent();
        boolean checkParent;
        Container pParent = parent.getParent();
        //checkParent = false;


        if (parent instanceof BreedBlock) {
            checkParent = true;
            System.out.println(getParent());
            if (this instanceof TraitBlock) {

                ((BuildPanel) pParent).removeTrait((TraitBlock) this);
                ((BreedBlock) parent).removeTraitBlock((TraitBlock) this);

            }
        }
        if (parent instanceof JPanel) {
            if (this instanceof TraitBlock) {
                for (Component child : pParent.getComponents()) {
                    if (child.getClass() == BuildPanel.class) {
                        ((BuildPanel) child).removeTrait((TraitBlock) this);
                    }
                }
                for (Component child : parent.getComponents()) {
                    if (child.getClass() == LibraryHolder.class) {
                        ((LibraryHolder) child).removeTraitBlock((TraitBlock) this);
                    }
                }
            }
        }


        if (parent instanceof CodeBlock) {

            ((CodeBlock) parent).removeBlock(this);
        }

        if (parent instanceof BuildPanel) {
            //System.out.println("parent " + getParent() + " this " + this);
            if (this instanceof BreedBlock) {

                ((BuildPanel) parent).removeBreed((BreedBlock) this);
                for (Component child : getComponents()) {
                if (child instanceof TraitBlock) {
                ((BreedBlock) this).removeTraitBlock((TraitBlock) child);
                ((BuildPanel) parent).removeTrait((TraitBlock) child);
                }

                }
            }
            if (this instanceof PlotBlock) {
                ((BuildPanel) parent).removePlot((PlotBlock) this);
            }
            if (this instanceof EnvtBlock) {
                ((BuildPanel) parent).removeEnvt((EnvtBlock) this);
            }
        }


        // kill off any subblocks
        for (Component child : getComponents()) {
            if (child.getClass() == CodeBlock.class) {
                ((CodeBlock) child).die();
            }
        }
        parent.remove(this);
        parent.repaint();
    }



    }
