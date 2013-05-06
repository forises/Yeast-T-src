package org.ystsrv.util;

import java.lang.ref.SoftReference;
import org.ystsrv.manager.Config;

public abstract class InMemoryCachedReference {

  public abstract Object get();

  public static InMemoryCachedReference newInstance(Object refered) {
    if (Config.USE_SOFT_REFS)
      return new SoftInMemoryCachedReference(refered);
    else
      return new HardInMemoryCachedReference(refered);
  }

  public static class SoftInMemoryCachedReference extends InMemoryCachedReference {
    SoftReference refered;
    public SoftInMemoryCachedReference(Object refered) {
      this.refered = new SoftReference(refered);
    }

    public Object get() {
      return this.refered.get();
    }
  }

  public static class HardInMemoryCachedReference extends InMemoryCachedReference {
    Object refered;
    public HardInMemoryCachedReference(Object refered) {
      this.refered = refered;
    }

    public Object get() {
      return this.refered;
    }
  }

//  public static class TestHeavyObject {
//    java.util.HashMap t = new java.util.HashMap();
//    TestHeavyObject() {
//      for (int i = 0;i<1000;i++)
//        t.put(i, java.util.Calendar.getInstance());
//    }
//  }
//
//  public static void main(String[] args) {
//    InMemoryCachedReference [] refs = new InMemoryCachedReference[100];
//    for (int i = 0;i<100;i++) {
//      Object o = new TestHeavyObject();
//      refs[i] = InMemoryCachedReference.newInstance(o);
//    }
//    for (int i = 0;i<100;i++)
//      if (refs[i].get() == null) System.out.println("Ref debil "+i+" perdida");
//  }
}
