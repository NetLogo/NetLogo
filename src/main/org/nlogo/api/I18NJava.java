package org.nlogo.api;

public class I18NJava {

  static public BundleKindWrapper gui(){
    return new BundleKindWrapper(I18N.gui());
  }

  static public BundleKindWrapper errors(){
    return new BundleKindWrapper(I18N.errors());
  }

  static public class BundleKindWrapper {
    private I18N.BundleKind bk;
    public BundleKindWrapper(I18N.BundleKind bk){
      this.bk = bk;
    }
    public String get(String key){
      return bk.get(key);
    }
    public String getN(String key, Object... args){
      return bk.getNJava(key, args);
    }
  }
}
