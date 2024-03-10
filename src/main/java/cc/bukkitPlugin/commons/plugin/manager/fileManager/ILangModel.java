package cc.bukkitPlugin.commons.plugin.manager.fileManager;

import cc.commons.commentedyaml.CommentedYamlConfig;

/**
 * 用于实现模块化组件的语言翻译的注册
 */
public interface ILangModel{

    /**
     * 添加语言
     * 
     * @param pConfig
     *            配置管理器
     */
    public void addDefaultLang(CommentedYamlConfig pConfig);

    /**
     * 设置实例的翻译
     * 
     * @param 语言翻译管理器
     */
    public void setLang(CommentedYamlConfig pConfig);

}
