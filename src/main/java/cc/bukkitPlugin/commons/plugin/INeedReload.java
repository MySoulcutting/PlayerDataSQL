package cc.bukkitPlugin.commons.plugin;

import org.bukkit.command.CommandSender;

/**
 * 如果实例需要在插件重载的时候重新加载除配置以外的内容可以实现该接口
 * 
 * @author 聪聪
 *
 */
public interface INeedReload{

    public boolean reloadConfig(CommandSender pSender);

}
