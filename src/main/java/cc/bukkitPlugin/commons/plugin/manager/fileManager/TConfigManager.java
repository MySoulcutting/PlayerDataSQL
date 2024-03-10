package cc.bukkitPlugin.commons.plugin.manager.fileManager;

import java.util.LinkedHashMap;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.Log.Level;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.commons.plugin.INeedReload;
import cc.commons.util.StringUtil;

public class TConfigManager<T extends ABukkitPlugin<T>>extends AFileManager<T> implements INeedReload{

    /** 是否启用开发者模式 */
    private static boolean mDevelopMode=true;
    /** 注册的可选配置模块 */
    private final LinkedHashMap<Class<? extends IConfigModel>,IConfigModel> mConfigModels=new LinkedHashMap<>();
    /** 是否启用信息收集 */
    private boolean mEnableMetrics=true;
    /** 是否启用线程安全检查 */
    private boolean mIsThreadSafe=true;

    public TConfigManager(T pPlugin,String pVersion){
        super(pPlugin,"config.yml",pVersion);
    }

    /**
     * 此函数中会载入并设置插件消息前缀<br>
     * 读取是否开启调试模式<br>
     * 读取是否开启插件信息收集
     */
    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender)){
            if(this.getClass()==TConfigManager.class) // 如果没有进行继承
                Log.severe(pSender,C("MsgErrorHappendWhenReloadConfig"));
            return false;
        }
        if(this.getClass()==TConfigManager.class){ // 如果没有进行继承,则本函数添加默认
            this.addDefaults();
        }
        // 设置消息等级
        Log.setLogLevel(this.mConfig.getString("LogLevel",Level.DEBUG.name()));
        Log.logStackTrace(this.mConfig.getBoolean("LogStackTrace",true));
        // 读入消息前缀
        String tPrefix=this.mConfig.getString("MsgPrefix");
        if(StringUtil.isNotEmpty(tPrefix)){
            Log.setMsgPrefix(Log.color(tPrefix));
        }
        // 读取信息收集配置
        this.mEnableMetrics=this.mConfig.getBoolean("EnableMetrics",this.mEnableMetrics);
        this.mPlugin.setStatistics(this.mEnableMetrics);
        // 线程安全设置
        this.mIsThreadSafe=this.mConfig.getBoolean("IsThreadSafe",this.mIsThreadSafe);
        if(this.getClass()==TConfigManager.class){ // 如果没有进行继承,则本函数添加默认
            this.reloadModles(pSender);
            this.saveConfig(null);
            Log.info(pSender,C("MsgConfigReloaded"));
        }
        return true;
    }

    protected void reloadModles(CommandSender pSender){
        for(IConfigModel sModel : this.mConfigModels.values()){
            sModel.setConfig(pSender,this.mConfig);
        }
    }

    @Override
    public void addDefaults(){
        super.addDefaults();
        this.mConfig.addDefault("LogLevel",Log.Level.SEVERE.name(),"设置消息输出等级","INFO<WARN<SEVERE<DEBUG<DEVELOP");
        this.mConfig.addDefault("LogStackTrace",true,"是否记录错误堆栈");
        this.mConfig.addDefault("MsgPrefix",Log.getMsgPrefix(),"插件大部分消息前缀");
        this.mConfig.addDefault("EnableMetrics",true,"是否开启服务器信息收集,只是用于插件使用情况统计","收集的信息: 使用版本 服务器ID 服务器IP 服务器端口 服务器版本 Java版本 系统版本");
        this.mConfig.addDefault("IsThreadSafe",true,"是否启用线程安全检查","当前的安全检查有[命令是否异步运行]");
        // 获取注册模块的配置
        for(IConfigModel sModel : this.mConfigModels.values()){
            sModel.addDefaults(this.mConfig);
        }
    }

    /**
     * 注册配置模块到配置配置器
     * <p>
     * 一个类只能注册一个实例
     * </p>
     * 
     * @param pConfigModel
     *            配置模块
     * @return 被替换的模块
     */
    public IConfigModel registerConfigModel(IConfigModel pConfigModel){
        if(pConfigModel==null)
            return null;
        return this.mConfigModels.put(pConfigModel.getClass(),pConfigModel);
    }

    /**
     * 从配置管理器中移除配置模块
     * 
     * @param pClazz
     *            配置模块类
     * @return 被移除的模块
     */
    public IConfigModel unregisterLangModel(Class<? extends IConfigModel> pClazz){
        if(pClazz==null)
            return null;
        return this.mConfigModels.remove(pClazz);
    }

    /**
     * 是否启用调试模式
     * <p>
     * 调试模式的判定为{@link Log#getLogLevel()}的序号大于等级{@link Level#DEBUG}
     * </p>
     */
    public static boolean isDebug(){
        return Log.isDebug();
    }

    /** 是否启用信息收集 */
    public boolean enableMetrics(){
        return this.mEnableMetrics;
    }

    /** 是否启用线程安全检查 */
    public boolean isThreadSafe(){
        return this.mIsThreadSafe;
    }

}
