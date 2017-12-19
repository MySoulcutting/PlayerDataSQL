package cc.bukkitPlugin.pds.dmodel;

import java.io.File;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.api.IDataModel;

public abstract class ADataModel implements IDataModel{

    protected PlayerDataSQL mPlugin;
    /** 服务器文件夹 */
    protected File mServerDir;
    /** 服务器玩家文件夹 */
    protected File mPlayerDataDir;
    /** 是否已经成功初始化,null指示未初始化 */
    protected Boolean mInitSuccess=null;

    public ADataModel(PlayerDataSQL pPlugin){
        this.mPlugin=pPlugin;

        this.mServerDir=pPlugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile();
        this.mPlayerDataDir=new File(this.mServerDir,"world"+File.separator+"playerdata");
    }

    @Override
    public Plugin getPlugin(){
        return this.mPlugin;
    }

    /**
     * 获取以UUID或者玩家名字为模式命令的数据文件
     * 
     * @param pPlayer
     *            玩家
     * @param pDir
     *            数据文件夹
     * @param pNameParam
     *            文件名模式,%name%为被替换的UUID或Name参数
     * @return 玩家数据文件
     */
    public File getUUIDOrNameFile(OfflinePlayer pPlayer,File pDir,String pNameParam){
        File tDataFile=new File(pDir,pNameParam.replace("%name%",pPlayer.getUniqueId().toString()));
        if(!tDataFile.isFile()){
            tDataFile=new File(pDir,pNameParam.replace("%name%",pPlayer.getName().toString()));
        }
        return tDataFile;
    }

    @Override
    public final boolean init(){
        if(this.mInitSuccess!=null)
            return this.mInitSuccess.booleanValue();

        try{
            return this.mInitSuccess=this.initOnce();
        }catch(Exception exp){
            if(exp instanceof ClassNotFoundException){
            }else{
                Throwable tSource=exp;
                if(exp instanceof IllegalStateException&&exp.getCause()!=null){
                    tSource=exp.getCause();
                }

                if(tSource instanceof NoSuchMethodException||tSource instanceof NoSuchFieldException){
                    Log.severe("模块 "+this.getDesc()+" 可能不支持你当前MOD版本",tSource);
                }else{
                    Log.severe("模块 "+this.getDesc()+" 初始化时发生了错误",exp);
                }
            }
            return (this.mInitSuccess=false);
        }
    }

    /**
     * 模块进行一次初始化,初始化成功则启用模块
     * <p>
     * 注意,此函数只会被调用一次
     * </p>
     * 
     * @return 是否初始化成功
     */
    protected abstract boolean initOnce() throws Exception;

}
