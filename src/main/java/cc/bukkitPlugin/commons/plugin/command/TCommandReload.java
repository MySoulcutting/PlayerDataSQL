package cc.bukkitPlugin.commons.plugin.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.ILangModel;
import cc.commons.commentedyaml.CommentedYamlConfig;

public class TCommandReload<T extends ABukkitPlugin<T>,E extends TCommandExc<T>>extends TACommandBase<T,E> implements ILangModel{

    public TCommandReload(E exector){
        this(exector,1);
    }

    public TCommandReload(E pExector,int pMaxArgLength){
        super(pExector,"reload",pMaxArgLength);
        this.mPlugin.getLangManager().registerLangModel(this);

        this.mSubCmd.add("plugin");
        this.mSubCmd.add("config");
        this.mSubCmd.add("lang");
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(pArgs.length>1)
            return errorArgsNumber(pSender,pArgs.length);
        String cmdLabel=pArgs.length>=1?pArgs[0]:"plugin";

        if(!this.mSubCmd.contains(cmdLabel.toLowerCase()))
            return unknowChildCommand(pSender,pLabel,cmdLabel);
        if(!hasCmdPermission(pSender,cmdLabel))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(cmdLabel.equalsIgnoreCase("plugin")){
            this.mPlugin.reloadPlugin(pSender);
            return true;
        }else if(cmdLabel.equalsIgnoreCase("config")){
            this.mPlugin.reloadConfig(pSender);
            return true;
        }else if(cmdLabel.equalsIgnoreCase("help")){
            return help(pSender,pLabel);
        }else if(cmdLabel.equalsIgnoreCase("lang")){
            this.mPlugin.reloadLang(pSender);
            return true;
        }else return send(pSender,"&c未处理reload的子命令 "+cmdLabel+" ,请联系开发者完善代码");
    }

    @Override
    protected void postSubCmdHelpWrite(CommandSender pSender,String pLabel,List<String> pHelps,String pSubCmd){
        if(pSubCmd!=null&&pSubCmd.equalsIgnoreCase("plugin")&&pHelps.size()>=2){
            int index=pHelps.size()-2;
            String tUsage=pHelps.get(index);
            if(tUsage.equalsIgnoreCase("plugin")){
                tUsage=tUsage.substring(0,tUsage.length()-6)+"[plugin]";
                pHelps.set(index,tUsage);
            }
        }
    }

    @Override
    public void addDefaultLang(CommentedYamlConfig pConfig){
        pConfig.addDefault("HelpReloadPlugin","重载整个插件");
        pConfig.addDefault("HelpReloadConfig","重载插件主配置");
        pConfig.addDefault("HelpReloadLang","重载插件语言翻译");
    }

    @Override
    public void setLang(CommentedYamlConfig pConfig){}
}
