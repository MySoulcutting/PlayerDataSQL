package cc.commons.commentedyaml.serialize.convert;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;

import org.yaml.snakeyaml.error.YAMLException;

public abstract class DataConvert{

    /** 注册的数据转换类 */
    protected static LinkedHashSet<DataConvert> mDataConvert=new LinkedHashSet<>();

    static{
        mDataConvert.add(new DataConvertMap());
        mDataConvert.add(new DataConvertCollection());
        mDataConvert.add(new DataConvertArray());
        mDataConvert.add(new DataConvertBase());
    }

    /**
     * 获取注册的数据转换类
     * <p>
     * 数据转换类注册的顺序十分重要
     * </p>
     * 
     * @return 数据转换类
     */
    public static LinkedHashSet<DataConvert> getDataConvertClass(){
        return DataConvert.mDataConvert;
    }

    /**
     * 转换数据到指定类型
     * 
     * @param pData
     *            当前数据
     * @param pTarget
     *            目标类型
     * @param pGenType
     *            模板参数
     * @return 转换后的数据
     */
    public static Object convertData(Object pData,Class<?> pTarget,Type pGenType) throws Exception{
        // 即使继承也不能返回,因为Map等集合内的数据类型可能存在不一致的情况
        for(DataConvert sConvert : DataConvert.mDataConvert){
            if(sConvert.accept(pTarget)){
                return (pTarget.isInstance(pData)&&sConvert.skipExtends())?pData:sConvert.conver(pData,pTarget,pGenType);
            }
        }

        return pData;
    }

    /**
     * 获取指定位置的实际参数类型
     * 
     * @param genType
     *            参数化数据
     * @param pIndex
     *            位置
     * @return 实际类型或Object.class;
     */
    public static Type getType(Type genType,int pIndex){
        if(genType instanceof ParameterizedType){
            ParameterizedType parameterizedType=(ParameterizedType)genType;
            Type[] actualTypeArguments=parameterizedType.getActualTypeArguments();
            return pIndex<actualTypeArguments.length?actualTypeArguments[pIndex]:Object.class;
        }
        return Object.class;
    }

    public static Class<?> getType(Type pGenType){
        if(pGenType instanceof Class){
            return (Class<?>)pGenType;
        }else if(pGenType instanceof ParameterizedType){
            return (Class<?>)((ParameterizedType)pGenType).getRawType();
        }else if(pGenType instanceof GenericArrayType){
            return (Class<?>)((GenericArrayType)pGenType).getGenericComponentType();
        }
        throw new YAMLException("Unknow Type: "+pGenType.getTypeName());

    }

    /**
     * 此类是否可以实例化
     * 
     * @param pClazz
     *            类
     * @return 能否实例化
     */
    public static boolean canInstance(Class<?> pClazz){
        return !(pClazz.isInterface()||Modifier.isAbstract(pClazz.getModifiers()));
    }

    /** 如果目标类无法实例化,默认的用于替换的实例 */
    protected LinkedHashSet<Class<?>> mDefaultExtends=new LinkedHashSet<>();

    public Class<?> getDefaultExtends(Class<?> pTarget){
        if(DataConvert.canInstance(pTarget))
            return pTarget;

        for(Class<?> sEntend : this.mDefaultExtends){
            if(pTarget.isAssignableFrom(sEntend)){
                return sEntend;
            }
        }

        throw new YAMLException("Can't instance "+pTarget.getName());
    }

    /**
     * 是否跳过继承自目标数据类型的数据
     * <p>
     * 此项配置的目的是为了防止集合类的内部类型与实际类型不匹配而设计的
     * </p>
     * 
     * @return
     */
    public boolean skipExtends(){
        return true;
    }

    /**
     * 是否接受此类型的转换
     * 
     * @param pTarget
     *            转换成的目标
     * @return 是否接受
     */
    public abstract boolean accept(Class<?> pTarget);

    /**
     * 转换数据为指定的类型
     * 
     * @param pData
     *            当前数据
     * @param pTarget
     *            目标类型
     * @param pTypes
     *            模板参数
     * @return 转换后的数据
     */
    public abstract Object conver(Object pData,Class<?> pTarget,Type pGenType) throws Exception;

}
