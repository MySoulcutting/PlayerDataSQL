package cc.commons.util.interfaces;

/***
 * 
 * 过滤器
 * 
 * @param <T>
 *            过滤类的类型
 */
public interface IFilter<T>{

    /**
     * 是否接受该实例
     * 
     * @param pObj
     *            过滤的实例
     * @return 是否接受
     */
    public boolean accept(T pObj);

}
