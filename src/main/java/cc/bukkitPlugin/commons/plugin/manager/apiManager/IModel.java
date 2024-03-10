package cc.bukkitPlugin.commons.plugin.manager.apiManager;

import org.bukkit.plugin.Plugin;

public interface IModel{

    /**
     * 初始化一个模块
     * 
     * @return 是否初始化成功
     */
    boolean init();

    /**
     * 获取模块的名字<br>
     * 此名字可能用于模块的标志以及做为配置节点使用
     */
    String getName();

    /**
     * 获取模块的描述<br>
     * 此描述可能用于模块加载提示,配置注释
     */
    String getDescription();

    /** 获取模块所属插件 */
    Plugin getPlugin();

}
