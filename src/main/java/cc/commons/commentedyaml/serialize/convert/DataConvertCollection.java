package cc.commons.commentedyaml.serialize.convert;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class DataConvertCollection extends DataConvert{

    public DataConvertCollection(){
        this.mDefaultExtends.add(ArrayList.class);
        this.mDefaultExtends.add(HashSet.class);
        this.mDefaultExtends.add(TreeSet.class);
        this.mDefaultExtends.add(LinkedBlockingDeque.class);
        this.mDefaultExtends.add(LinkedBlockingQueue.class);
    }

    @Override
    public boolean skipExtends(){
        return false;
    }

    @Override
    public boolean accept(Class<?> pTarget){
        return Collection.class.isAssignableFrom(pTarget);
    }

    @Override
    public Object conver(Object pData,Class<?> pTarget,Type pGenType) throws Exception{
        Type tValueType=DataConvert.getType(pGenType,0);
        Class<?> tValueClazz=DataConvert.getType(tValueType);
        Collection<Object> tActValue=(Collection<Object>)this.getDefaultExtends(pTarget).newInstance();

        for(Object sObj : (Collection<Object>)pData){
            tActValue.add(DataConvert.convertData(sObj,tValueClazz,tValueType));
        }

        return tActValue;
    }
}
