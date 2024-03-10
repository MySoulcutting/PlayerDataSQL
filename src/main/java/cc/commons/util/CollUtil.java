package cc.commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CollUtil{

    /**
     * 是否为null或者长度为0
     * 
     * @param pObjs
     *            检查的对象
     * @return
     */
    public static boolean isEmpty(Object[] pObjs){
        return pObjs==null||pObjs.length==0;
    }

    /**
     * 检查是否不为null,同时长度不为0
     * 
     * @param pObjs
     *            检查的对象
     * @return
     */
    public static boolean isNotEmpty(Object[] pObjs){
        return !isEmpty(pObjs);
    }

    /**
     * 是否为null或者集合无元素
     * 
     * @param pCol
     *            检查的对象
     * @return
     */
    public static <T> boolean isEmpty(Collection<T> pCol){
        return pCol==null||pCol.isEmpty();
    }

    /**
     * 检查是否不为null,且集合有元素
     * 
     * @param pCol
     *            检查的对象
     * @return
     */
    public static <T> boolean isNotEmpty(Collection<T> pCol){
        return !isEmpty(pCol);
    }

    /**
     * 是否为null或者Map无元素
     * 
     * @param pMap
     *            检查的对象
     * @return
     */
    public static <K,V> boolean isEmpty(Map<K,V> pMap){
        return pMap==null||pMap.isEmpty();
    }

    /**
     * 是否不为null且Map有元素
     * 
     * @param pMap
     *            检查的对象
     * @return
     */
    public static <K,V> boolean isNotEmpty(Map<K,V> pMap){
        return !isEmpty(pMap);
    }

    /**
     * 获取集合对象实例
     * <p>
     * 集合类必须有一个无参构造函数
     * </p>
     * 
     * @param pColClazz
     *            集合类
     * @return 实例
     */
    public static <T extends Collection> T getInstance(Class<T> pColClazz){
        try{
            return pColClazz.newInstance();
        }catch(InstantiationException|IllegalAccessException e){
            throw new IllegalStateException(e);
        }
    }

    /**
     * 复制数组内容到新的集合
     * <p>
     * 集合类必须有一个无参构造函数
     * </p>
     * 
     * @param pArrays
     *            数组
     * @param pColClazz
     *            集合类
     * @return 包含数组内容的集合
     */
    public static <T extends Collection<E>,E> T asCollection(E[] pArrays,Class<T> pColClazz){
        T tCol=CollUtil.getInstance(pColClazz);
        if(pArrays==null||pArrays.length==0){
            return tCol;
        }
        for(E sT : pArrays){
            tCol.add(sT);
        }
        return tCol;
    }

    /**
     * 将数组转化成List
     * 
     * @param pArrays
     *            要转换的数组,如果数组为null,将返回空List
     * @return 转换后的List,非null
     */
    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> asList(T[] pArrays){
        return CollUtil.asCollection(pArrays,ArrayList.class);
    }

    /**
     * 将数组元素添加到集合中
     * 
     * @param pCol
     *            集合
     * @param pEles
     *            数组元素
     * @return 添加了元素后的集合
     */
    public static <T,E extends Collection<T>> E addEles(E pCol,T[] pEles){
        for(int i=pEles.length-1;i>=0;i--){
            if(pEles[i]==null) continue;
            pCol.add(pEles[i]);
        }
        return pCol;
    }

    /**
     * 将数组元素从集合中移除
     * 
     * @param pCol
     *            集合
     * @param pEles
     *            数组元素
     * @return 移除了元素后的集合
     */
    public static <T,E extends Collection<T>> E removeEles(E pCol,T[] pEles){
        for(int i=pEles.length-1;i>=0;i--){
            pCol.remove(pEles[i]);
        }
        return pCol;
    }
}
