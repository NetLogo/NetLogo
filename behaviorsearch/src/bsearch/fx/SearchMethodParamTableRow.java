package bsearch.fx;

import javafx.beans.property.SimpleStringProperty;

public class SearchMethodParamTableRow {
    private final SimpleStringProperty param;
    private SimpleStringProperty value;

    public SearchMethodParamTableRow(String param, String value) {
        this.param = new SimpleStringProperty(param);
        this.value = new SimpleStringProperty(value);
    }

    public String getParam() {
        return param.get();
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }


    @Override
    public String toString() {
        return param + " " + value;

    }

}
