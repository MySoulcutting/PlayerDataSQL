package cc.commons.commentedyaml.serialize.convert;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

public class DataConvertBase extends DataConvert{

    private static HashMap<Class<?>,BaseType> mBaseTypes=new HashMap();
    private static HashMap<Class<?>,Class<?>> mTypeMap=new HashMap();

    static{
        mBaseTypes.put(byte.class,new BaseType(byte.class,Byte.class,"byteValue","parseByte"));
        mTypeMap.put(Byte.class,byte.class);
        mBaseTypes.put(short.class,new BaseType(short.class,Short.class,"shortValue","parseShort"));
        mTypeMap.put(Short.class,short.class);
        mBaseTypes.put(char.class,new BaseType(char.class,Character.class,"shortValue","parseShort"));
        mTypeMap.put(Character.class,char.class);
        mBaseTypes.put(int.class,new BaseType(int.class,Integer.class,"intValue","parseInt"));
        mTypeMap.put(Integer.class,int.class);
        mBaseTypes.put(long.class,new BaseType(long.class,Long.class,"longValue","parseLong"));
        mTypeMap.put(Long.class,long.class);
        mBaseTypes.put(float.class,new BaseType(float.class,Float.class,"floatValue","parseFloat"));
        mTypeMap.put(Float.class,float.class);
        mBaseTypes.put(double.class,new BaseType(double.class,Double.class,"doubleValue","parseDouble"));
        mTypeMap.put(Double.class,double.class);
    }

    private static class BaseType{

        public final Class<?> mBaseType;
        public final Class<?> mObjType;
        public final Method mValueMethod;
        public final Method mPaserMethod;

        public BaseType(Class<?> pBaseType,Class<?> pObjType,String pValueMethod,String pPaserMethod){
            this.mBaseType=pBaseType;
            this.mObjType=pObjType;
            try{
                this.mValueMethod=Number.class.getMethod(pValueMethod);
                if(pBaseType!=char.class){
                    this.mPaserMethod=pObjType.getMethod(pPaserMethod,String.class);
                }else{
                    this.mPaserMethod=Short.class.getMethod(pPaserMethod,String.class);
                }
            }catch(NoSuchMethodException|SecurityException exp){
                throw new IllegalStateException(exp.getMessage(),exp);
            }
        }
    }

    @Override
    public boolean accept(Class<?> pTarget){
        return Number.class.isAssignableFrom(pTarget)||DataConvertBase.mBaseTypes.containsKey(pTarget);
    }

    @Override
    public Object conver(Object pData,Class<?> pTarget,Type pGenType) throws Exception{
        Class<?> tAliseTarget=pTarget;
        if(Number.class.isAssignableFrom(pTarget)){
            tAliseTarget=mTypeMap.get(tAliseTarget);
        }

        if(pData instanceof Number){
            return mBaseTypes.get(tAliseTarget).mValueMethod.invoke(pData);
        }else{
            String tStr=String.valueOf(pData);
            if(tAliseTarget==char.class){
                return tStr.length()>0?tStr.charAt(0):'0';
            }else{
                return mBaseTypes.get(tAliseTarget).mPaserMethod.invoke(null,tStr);
            }
        }

    }

}
