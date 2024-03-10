package cc.bukkitPlugin.commons.plugin.manager.fileManager;

import org.bukkit.command.CommandSender;

import cc.commons.commentedyaml.CommentedYamlConfig;

public interface IConfigModel{

    /**
     * 添加默认配置
     * 
     * @param pConfig
     *            配置管理器
     */
    public void addDefaults(CommentedYamlConfig pConfig);

    /**
     * 重新从主配置文件读取配置
     * <p>
     * 确保在重载了配置文件之后重载<br>
     * 最好只在此函数中做和配置变动相关的操作
     * </p>
     * 
     * @param pSender
     *            请求发起者,可能为null
     * @param pConfig
     */
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig);

}
