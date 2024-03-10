package cc.bukkitPlugin.commons.plugin;

public enum Status{

    /** 处理命令状态 */
    Process_Command,
    /** 重载整个插件状态 */
    Reload_Plugin,
    /** 重载插件主配置状态 */
    Reload_Config,
    /** 重载插件主语言文件状态 */
    Reload_Lang,

}
