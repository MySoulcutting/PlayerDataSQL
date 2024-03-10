package cc.commons.util.tools;

import java.util.Iterator;
import java.util.function.Consumer;

public class LimitedIterator implements Iterator<Integer> {

    public static LimitedIterator c(int pCount) {
        return new LimitedIterator(pCount);
    }

    public final int originCount;
    protected int mCurrentIndex = -1;

    private LimitedIterator(int pCount) {
        this.originCount = pCount;
    }

    @Override
    public boolean hasNext() {
        return this.mCurrentIndex + 1 < originCount;
    }

    @Override
    public Integer next() {
        this.mCurrentIndex++;
        return this.mCurrentIndex;
    }

    public void reset() {
        this.mCurrentIndex = -1;
    }

    public int getIndex() {
        return this.mCurrentIndex;
    }

    public void forEach(Consumer<? super Integer> action) {
        this.reset();
        this.forEachRemaining(action);
    }
}
