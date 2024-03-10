package cc.commons.commentedyaml.serialize.convert;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.SimpleBindings;

public class DataConvertMap extends DataConvert{

    public DataConvertMap(){
        this.mDefaultExtends.add(HashMap.class);
        this.mDefaultExtends.add(SimpleBindings.class);
        this.mDefaultExtends.add(ConcurrentHashMap.class);
        this.mDefaultExtends.add(TreeMap.class);
    }

    @Override
    public boolean skipExtends(){
        return false;
    }

    @Override
    public boolean accept(Class<?> pTarget){
        return Map.class.isAssignableFrom(pTarget);
    }

    @Override
    public Object conver(Object pData,Class<?> pTarget,Type pGenType) throws Exception{
        Type tKeyType=DataConvert.getType(pGenType,0);
        Class<?> tKeyClazz=DataConvert.getType(tKeyType);
        Type tValueType=DataConvert.getType(pGenType,1);
        Class<?> tValueClazz=DataConvert.getType(tValueType);
        Map<Object,Object> tActValue=(Map<Object,Object>)this.getDefaultExtends(pTarget).newInstance();

        for(Map.Entry<Object,Object> sEntry : ((Map<Object,Object>)pData).entrySet()){
            tActValue.put(
                    DataConvert.convertData(sEntry.getKey(),tKeyClazz,tKeyType),
                    DataConvert.convertData(sEntry.getValue(),tValueClazz,tValueType));
        }

        return tActValue;
    }

}
