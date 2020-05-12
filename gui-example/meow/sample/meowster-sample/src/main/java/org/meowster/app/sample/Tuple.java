package org.meowster.app.sample;

public class Tuple<A, B>  {
    private A val0;
    private B val1;

    public Tuple(A value0, B value1) {
        this.val0 = value0;
        this.val1 = value1;
    }

    public A getValue0() {
        return this.val0;
    }

    public B getValue1() {
        return this.val1;
    }

    public void setAt0(A value) {
        this.val0 = value;
    }

    public void setAt1(B value) {
        this.val1 = value;
    }
}