package cc.commons.util.extra;

import java.util.ArrayList;
import java.util.Collection;

public class CList<E>extends ArrayList<E>{

    public CList(){
        super();
    }

    public CList(int initialCapacity){
        super(initialCapacity);
    }

    public CList(Collection<? extends E> c){
        super(c);
    }

    public E first(){
        return this.get(0);
    }

    public E last(){
        return this.get(this.size()-1);
    }

    /**
     * 判断集合中是否只有一个元素
     * 
     * @return
     */
    public boolean onlyOne(){
        return this.size()==1;
    }

    /**
     * 在集合只有一个元素的时候,调用此函数获取该元素
     * <p>
     * 如果集合元素数量不为1,会抛出异常
     * </p>
     * 
     * @return 集合中的唯一元素
     */
    public E oneGet(){
        if(this.size()!=1) throw new IllegalStateException("Not Only One Element");
        return this.get(0);
    }

}
