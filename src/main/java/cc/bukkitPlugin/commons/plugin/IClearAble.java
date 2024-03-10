package cc.bukkitPlugin.commons.plugin;

/**
 * 可清除内存临时数据
 * 
 * @author 聪聪
 *
 */
public interface IClearAble{

    /**
     * 清理内存数据
     * 
     * @return 是否成功
     */
    public boolean clearStatus();

}
