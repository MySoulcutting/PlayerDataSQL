package cc.bukkitPlugin.commons.plugin.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.commons.plugin.Status;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.StringUtil;

public class TCommandExc<T extends ABukkitPlugin<T>> implements CommandExecutor,TabCompleter,IConfigModel{

    /** 注册的命令,标签必须小写 */
    protected final HashMap<String,TACommandBase<T,? extends TCommandExc<T>>> mCommands=new HashMap<>();
    /** 注册的命令的标签,此列表有序 */
    protected final TreeSet<String> mCommandLabels=new TreeSet<>();
    /** 主命令名,用于构造权限 */
    protected String mMainLabel;
    /** 该执行器的主命令标签,包含斜杠 */
    protected String mMainCmdLabel;
    protected T mPlugin;

    protected String mCmdUsagePrefix="§b§l";
    protected String mCmdDescPrefix="    §2";

    public TCommandExc(T pPlugin,boolean pRegister){
        this(pPlugin,null,null,pRegister);
    }

    /**
     * 构造一个命令执行器
     * 
     * @param pPlugin
     *            插件
     * @param pMainCommand
     *            执行器主命令,不包含斜杠 ,null时使用插件名
     * @param pRegister
     *            是否绑定插件命令到该命令执行器
     */
    public TCommandExc(T pPlugin,String pMainCommand,boolean pRegister){
        this(pPlugin,null,pMainCommand,pRegister);
    }

    /**
     * 构造一个命令执行器
     * 
     * @param pPlugin
     *            插件
     * @param pMainLabel
     *            主权限头,用于构造权限,null时使用插件名
     * @param pMainCommand
     *            执行器主命令,不包含斜杠 ,null时使用插件名
     * @param pRegister
     *            是否绑定插件命令到该命令执行器
     */
    public TCommandExc(T pPlugin,String pMainLabel,String pMainCommand,boolean pRegister){
        this.mPlugin=pPlugin;
        if(pRegister){
            // 绑定执行器
            PluginCommand tCmd=null;
            for(String sMainCmd : this.mPlugin.getDescription().getCommands().keySet()){
                if((tCmd=this.mPlugin.getCommand(sMainCmd))!=null){
                    tCmd.setExecutor(this);
                    tCmd.setTabCompleter(this);
                    if(pMainCommand==null)
                        pMainCommand=tCmd.getName();
                }
            }
        }
        // 主命令名字
        if(pMainCommand!=null){
            this.mMainCmdLabel="/"+pMainCommand;
        }else{
            this.mMainCmdLabel="/"+this.mPlugin.getName();
        }
        // 主权限名
        if(pMainLabel==null){
            this.mMainLabel=pPlugin.getName();
        }else{
            this.mMainLabel=pMainLabel;
        }

        this.mPlugin.getConfigManager().registerConfigModel(this);
    }

    protected void registerSubCommand(){
        this.register(new TCommandHelp<T,TCommandExc<T>>(this));
        this.register(new TCommandReload<T,TCommandExc<T>>(this));
    }

    /** 获取主插件实例 */
    public T getPlugin(){
        return this.mPlugin;
    }

    /** 获取主命令标签,包括斜杠 */
    public String getCmdLabel(){
        return this.mMainCmdLabel;
    }

    /** 获取此执行器的主标签,主要用于构造权限 */
    public String getMainLabel(){
        return this.mMainLabel;
    }

    public String getCmdUsagePrefix(){
        return this.mCmdUsagePrefix;
    }

    public String getCmdDescPrefix(){
        return this.mCmdDescPrefix;
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        this.mCmdUsagePrefix=pConfig.getString("Prefix.CmdUsagePrefix",this.mCmdUsagePrefix);
        this.mCmdDescPrefix=pConfig.getString("Prefix.CmdDescPrefix",this.mCmdDescPrefix);
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){}

    /** 获取命令标签Set集合的拷贝 */
    public TreeSet<String> keySet(){
        TreeSet<String> cmdLabels=new TreeSet<>();
        cmdLabels.addAll(this.mCommandLabels);
        return cmdLabels;
    }

    /**
     * 根据标签名获取子命令,忽略标签大小写
     */
    public TACommandBase<T,? extends TCommandExc<T>> getCommand(String pLabel){
        for(Entry<String,TACommandBase<T,? extends TCommandExc<T>>> sEntry : this.mCommands.entrySet()){
            if(sEntry.getKey().equalsIgnoreCase(pLabel))
                return sEntry.getValue();
        }
        return null;
    }

    /**
     * 根据标类获取子命令
     */
    public TACommandBase<T,? extends TCommandExc<T>> getCommand(Class<? extends TACommandBase<T,? extends TCommandExc<T>>> pClazz){
        for(TACommandBase<T,? extends TCommandExc<T>> scmd : this.mCommands.values()){
            if(scmd.getClass()==pClazz)
                return scmd;
        }
        return null;
    }

    /**
     * 注册命令到命令列表中,将同时注册命令的映射标签
     */
    public void register(TACommandBase<T,? extends TCommandExc<T>> pCmd){
        if(pCmd==null)
            return;
        HashSet<String> tCmdLabels=pCmd.getCommandLabelAlias();
        if(tCmdLabels==null)
            tCmdLabels=new HashSet<>(1);
        tCmdLabels.add(pCmd.getCommandLabel().toLowerCase());
        for(String sCmdLabel : tCmdLabels){
            sCmdLabel=sCmdLabel.toLowerCase();
            TACommandBase<T,? extends TCommandExc<T>> oldCmd=this.mCommands.put(sCmdLabel,pCmd);
            if(oldCmd!=null&&oldCmd!=pCmd){
                if(oldCmd.getClass()==pCmd.getClass())
                    Log.severe("command"+oldCmd.getClass().getSimpleName()+"重复注册");
                else Log.severe("command"+oldCmd.getClass().getSimpleName()+"与命令"+pCmd.getClass().getSimpleName()+"使用了相同的命令标签");
            }else{ // 标签未重复时添加命令标签到标签列表
                this.mCommandLabels.add(sCmdLabel);
            }
        }
    }

    /**
     * 更改命令模块的标签名
     * 
     * @param pCmdClazz
     *            命令模块类
     * @param pNewLabel
     *            新的名字
     * @return 是否成功
     */
    @Deprecated
    public boolean replaceLabel(Class<? extends TACommandBase<T,? extends TCommandExc<T>>> pCmdClazz,String pNewLabel){
        if(StringUtil.isBlank(pNewLabel))
            return false;
        pNewLabel=pNewLabel.toLowerCase();
        if(this.mCommands.get(pNewLabel)!=null)
            return false;
        for(Map.Entry<String,TACommandBase<T,? extends TCommandExc<T>>> tCmdEntry : this.mCommands.entrySet()){
            if(pCmdClazz.isInstance(tCmdEntry.getValue())){
                this.mCommands.put(pNewLabel,this.mCommands.remove(tCmdEntry.getKey()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender pSender,Command command,String pLabel,String[] pArgs){
        if(this.mPlugin.getConfigManager().isThreadSafe()&&!Bukkit.isPrimaryThread())
            throw new IllegalStateException(this.mPlugin.C("MsgAsyncCommandNotAllow"));
        ABukkitPlugin.mStatusStack.add(Status.Process_Command);
        try{
            String cmdLabel="help";
            if(pArgs.length>0)
                cmdLabel=pArgs[0];
            TACommandBase<T,? extends TCommandExc<T>> cmd=this.getCommand(cmdLabel);
            if(cmd!=null){
                if(pArgs.length>0)
                    pArgs=Arrays.copyOfRange(pArgs,1,pArgs.length);
                return cmd.execute(pSender,cmdLabel,pArgs);
            }else return Log.send(pSender,this.mPlugin.C("MsgUnknowCommand"));
        }catch(Throwable exp){
            Log.severe(this.mPlugin.C("MsgErrorHappedWhenHandlerCmd"),exp);
            Log.send(pSender,this.mPlugin.C("MsgErrorHappendPleaseContanctAdmin"));
        }finally{
            ABukkitPlugin.mStatusStack.remove(Status.Process_Command);
        }
        return true;
    }

    /**
     * Tab命令自动补全
     * <p>
     * 会首先检查 插件名.cmdcomplete权限,如果不存在直接返回</br>
     * </p>
     * 
     * @return
     */
    public List<String> onTabComplete(CommandSender pSender,Command pCmd,String pStr,String[] pArgs){
        if(!pSender.hasPermission(this.mPlugin.getName()+".cmdcomplete"))
            return null;
        ArrayList<String> list=new ArrayList<>();
        if(pArgs.length==0){
            list.addAll(this.mCommands.keySet());
        }else if(pArgs.length==1){
            Collection<String> samePrefixTabs=StringUtil.getSamePrefixIgnoreCase(this.mCommands.keySet(),pArgs[0],true);
            if(samePrefixTabs.size()==0){
                String findTab=StringUtil.getIgnoreCase(this.mCommands.keySet(),pArgs[0]);
                if(findTab!=null){
                    if(this.getCommand(findTab).getMaxArgumentLength(findTab)>0)
                        list.add(findTab+" ");
                    else list.add(findTab);
                }
            }else if(samePrefixTabs.size()==1){
                String findTab=samePrefixTabs.toArray()[0].toString();
                if(this.getCommand(findTab).getMaxArgumentLength(findTab)>0)
                    list.add(findTab+" ");
                else list.add(findTab);
            }else list.addAll(samePrefixTabs);
        }else{
            TACommandBase<T,? extends TCommandExc<T>> tCmd=this.getCommand(pArgs[0]);
            if(tCmd!=null&&tCmd.getMaxArgumentLength(pArgs[0])>0)
                list.addAll(tCmd.onTabComplete(pSender,Arrays.copyOfRange(pArgs,1,pArgs.length)));
        }
        // ABukkitPlugin.send(pSender,"Tab输入:"+Function.asList(pArgs));
        // ABukkitPlugin.send(pSender,"Tab返回:"+list);
        return list;
    }

    public void reload(){}
}
