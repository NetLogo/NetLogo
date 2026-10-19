package bsearch.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * A class containing a {@link TableCell} implementation that draws a
 * {@link TextField} node inside the cell. If the TextField is
 * left, the value is commited.
 *
 * credit: http://stackoverflow.com/questions/23632884/how-to-commit-when-clicking-outside-an-editable-tableview-cell-in-javafx
 * @author Hans Huckebein
 *
 */

public class AcceptOnExitTableCell<S,T> extends TableCell<S,T> {

/***************************************************************************
 *                                                                         *
 * Static cell factories                                                   *
 *                                                                         *
 **************************************************************************/

/**
 * Provides a {@link TextField} that allows editing of the cell content when
 * the cell is double-clicked, or when
 * {@link TableView#edit(int, javafx.scene.control.TableColumn)} is called.
 * This method will only  work on {@link TableColumn} instances which are of
 * type String.
 *
 * @return A {@link Callback} that can be inserted into the
 *      {@link TableColumn#cellFactoryProperty() cell factory property} of a
 *      TableColumn, that enables textual editing of the content.
 */
public static <S> Callback<TableColumn<S,String>, TableCell<S,String>> forTableColumn() {
    return forTableColumn(new DefaultStringConverter());
}

/**
 * Provides a {@link TextField} that allows editing of the cell content when
 * the cell is double-clicked, or when
 * {@link TableView#edit(int, javafx.scene.control.TableColumn) } is called.
 * This method will work  on any {@link TableColumn} instance, regardless of
 * its generic type. However, to enable this, a {@link StringConverter} must
 * be provided that will convert the given String (from what the user typed
 * in) into an instance of type T. This item will then be passed along to the
 * {@link TableColumn#onEditCommitProperty()} callback.
 *
 * @param converter A {@link StringConverter} that can convert the given String
 *      (from what the user typed in) into an instance of type T.
 * @return A {@link Callback} that can be inserted into the
 *      {@link TableColumn#cellFactoryProperty() cell factory property} of a
 *      TableColumn, that enables textual editing of the content.
 */
public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
        final StringConverter<T> converter) {
    return list -> new AcceptOnExitTableCell<S,T>(converter);
}


/***************************************************************************
 *                                                                         *
 * Fields                                                                  *
 *                                                                         *
 **************************************************************************/

private TextField textField;
private boolean escapePressed=false;
private TablePosition<S, ?> tablePos=null;


/***************************************************************************
 *                                                                         *
 * Constructors                                                            *
 *                                                                         *
 **************************************************************************/

/**
 * Creates a default TextFieldTableCell with a null converter. Without a
 * {@link StringConverter} specified, this cell will not be able to accept
 * input from the TextField (as it will not know how to convert this back
 * to the domain object). It is therefore strongly encouraged to not use
 * this constructor unless you intend to set the converter separately.
 */
public AcceptOnExitTableCell() {
    this(null);
}

/**
 * Creates a TextFieldTableCell that provides a {@link TextField} when put
 * into editing mode that allows editing of the cell content. This method
 * will work on any TableColumn instance, regardless of its generic type.
 * However, to enable this, a {@link StringConverter} must be provided that
 * will convert the given String (from what the user typed in) into an
 * instance of type T. This item will then be passed along to the
 * {@link TableColumn#onEditCommitProperty()} callback.
 *
 * @param converter A {@link StringConverter converter} that can convert
 *      the given String (from what the user typed in) into an instance of
 *      type T.
 */
public AcceptOnExitTableCell(StringConverter<T> converter) {
    this.getStyleClass().add("text-field-table-cell");
    setConverter(converter);
}



/***************************************************************************
 *                                                                         *
 * Properties                                                              *
 *                                                                         *
 **************************************************************************/

// --- converter
private ObjectProperty<StringConverter<T>> converter =
        new SimpleObjectProperty<StringConverter<T>>(this, "converter");

/**
 * The {@link StringConverter} property.
 */
public final ObjectProperty<StringConverter<T>> converterProperty() {
    return converter;
}

/**
 * Sets the {@link StringConverter} to be used in this cell.
 */
public final void setConverter(StringConverter<T> value) {
    converterProperty().set(value);
}

/**
 * Returns the {@link StringConverter} used in this cell.
 */
public final StringConverter<T> getConverter() {
    return converterProperty().get();
}



/***************************************************************************
 *                                                                         *
 * Public API                                                              *
 *                                                                         *
 **************************************************************************/

/** {@inheritDoc} */
@Override public void startEdit() {
    if (! isEditable()
            || ! getTableView().isEditable()
            || ! getTableColumn().isEditable()) {
        return;
    }
    super.startEdit();

    if (isEditing()) {
        if (textField == null) {
            textField = getTextField();
        }
        escapePressed=false;
        startEdit(textField);
        final TableView<S> table = getTableView();
        tablePos=table.getEditingCell();
    }
}

/** {@inheritDoc} */
@Override public void commitEdit(T newValue) {
    if (! isEditing())
        return;

    final TableView<S> table = getTableView();
    if (table != null) {
       @SuppressWarnings("unchecked")
        // Inform the TableView of the edit being ready to be committed.
        CellEditEvent<S,T> editEvent = new CellEditEvent(
            table,
            tablePos,
            TableColumn.editCommitEvent(),
            newValue
        );

        Event.fireEvent(getTableColumn(), editEvent);
    }

    // we need to setEditing(false):
   super.cancelEdit(); // this fires an invalid EditCancelEvent.

    // update the item within this cell, so that it represents the new value
    updateItem(newValue, false);

    if (table != null) {
        // reset the editing cell on the TableView
        table.edit(-1, null);

        // request focus back onto the table, only if the current focus
        // owner has the table as a parent (otherwise the user might have
        // clicked out of the table entirely and given focus to something else.
        // It would be rude of us to request it back again.
       // requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(table);
    }
}


/** {@inheritDoc} */
@Override public void cancelEdit() {
    if(escapePressed) {
        // this is a cancel event after escape key
        super.cancelEdit();
        setText(getItemText()); // restore the original text in the view
    }
    else {
        // this is not a cancel event after escape key
        // we interpret it as commit.
        String newText=textField.getText(); // get the new text from the view
        //this.startEdit();
        //System.out.println(newText);
        this.commitEdit(getConverter().fromString(newText)); // commit the new text to the model
        //this.commitEdit((T)newText);
    }
    setGraphic(null); // stop editing with TextField

}

/** {@inheritDoc} */
@Override public void updateItem(T item, boolean empty) {
    super.updateItem(item, empty);
    updateItem();
}

/***************************************************************************
 *                                                                         *
 *  // djw code taken and adapted from package protected CellUtils.        *
 *                                                                         *
 **************************************************************************/

private TextField getTextField() {

    final TextField textField = new TextField(getItemText());

    // Use onAction here rather than onKeyReleased (with check for Enter),
    // as otherwise we encounter RT-34685
    textField.setOnAction(event -> {
        if (converter == null) {
            throw new IllegalStateException(
                    "Attempting to convert text input into Object, but provided "
                            + "StringConverter is null. Be sure to set a StringConverter "
                            + "in your cell factory.");
        }
        this.commitEdit(getConverter().fromString(textField.getText()));

        event.consume();
    });
    textField.setOnKeyPressed(t -> { if (t.getCode() == KeyCode.ESCAPE) escapePressed = true; else escapePressed = false; });
    textField.setOnKeyReleased(t -> {
        if (t.getCode() == KeyCode.ESCAPE) {
            // djw the code may depend on java version / expose incompatibilities:
            throw new IllegalArgumentException("did not expect esc key releases here.");
        }
    });
    return textField;
}

private String getItemText() {
    return getConverter() == null ?
            getItem() == null ? "" : getItem().toString() :
                getConverter().toString(getItem());
}

private void updateItem() {
    if (isEmpty()) {
        setText(null);
        setGraphic(null);
    } else {
        if (isEditing()) {
            if (textField != null) {
                textField.setText(getItemText());
            }
            setText(null);
            setGraphic(textField);
        } else {
            setText(getItemText());
            setGraphic(null);
        }
    }
}

private void startEdit(final TextField textField) {
    if (textField != null) {
        textField.setText(getItemText());
    }
    setText(null);
    setGraphic(textField);
    textField.selectAll();

    // requesting focus so that key input can immediately go into the
    // TextField (see RT-28132)
    textField.requestFocus();
 }
}
