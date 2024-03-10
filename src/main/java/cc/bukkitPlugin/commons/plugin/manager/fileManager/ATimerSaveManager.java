package cc.bukkitPlugin.commons.plugin.manager.fileManager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.commons.plugin.INeedClose;

public class ATimerSaveManager<T extends ABukkitPlugin<T>>extends AFileManager<T> implements Runnable,INeedClose{

    /** 配置文件是否变更 */
    protected boolean mChanged=false;
    /** 当前运行的保存任务,可能为null */
    protected BukkitTask mTask;

    public ATimerSaveManager(T pPlugin,String pFileName,String pVersion){
        this(pPlugin,pFileName,pVersion,true);
    }

    public ATimerSaveManager(T pPlugin,String pFileName,String pVersion,boolean pRegisterTask){
        super(pPlugin,pFileName,pVersion);

        this.mPlugin.registerCloseModel(this);
        if(pRegisterTask){
            this.mTask=Bukkit.getScheduler().runTaskTimer(this.mPlugin,this,200,200);
        }
    }

    @Override
    public void run(){
        if(!this.mChanged)
            return;

        try{
            this.saveConfig(null);
        }catch(Throwable exp){
            Log.severe(exp);
        }
    }

    @Override
    public void disable(){
        if(this.mTask!=null){
            this.mTask.cancel();
        }
        if(this.mChanged){
            this.saveConfig(null);
        }
    }

    /** 标记配置的状态为已经变动,循环任务在检查到配置变更时,会保存文件 */
    public void markChanged(){
        this.mChanged=true;
    }

    public boolean isChanged(){
        return this.mChanged;
    }

    @Override
    public boolean saveConfig(CommandSender pSender){
        boolean tResult=super.saveConfig(pSender);
        if(tResult){
            this.mChanged=false;
        }
        return tResult;
    }

}
