package cc.commons.commentedyaml.serialize.convert;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

public class DataConvertArray extends DataConvert{

    @Override
    public boolean accept(Class<?> pTarget){
        return pTarget.isArray();
    }

    @Override
    public Object conver(Object pData,Class<?> pTarget,Type pGenType) throws Exception{
        Collection<?> tData=(Collection<?>)pData;
        Class<?> tComponentType=pTarget.getComponentType();
        Object tActData=Array.newInstance(tComponentType,tData.size());
        int tIndex=0;
        for(Object sObj : tData){
            Array.set(tActData,tIndex++,DataConvert.convertData(sObj,tComponentType,Object.class));
        }
        return tActData;
    }

}
