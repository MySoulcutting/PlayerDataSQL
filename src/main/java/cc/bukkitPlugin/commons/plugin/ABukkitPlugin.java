package cc.bukkitPlugin.commons.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.java.JavaPlugin;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.Log.Level;
import cc.bukkitPlugin.commons.extra.YamlLogImpl;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.TConfigManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.TLangManager;
import cc.bukkitPlugin.commons.util.Statistics;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.StringUtil;

public abstract class ABukkitPlugin<T extends ABukkitPlugin<T>>extends JavaPlugin{

    /** 插件实例 */
    private final static HashMap<Class<? extends ABukkitPlugin<?>>,ABukkitPlugin<?>> mInstances=new HashMap<>();
    /** 插件状态栈 */
    public static final ArrayList<Status> mStatusStack=new ArrayList<>();
    /** 可重置状态模块实例列表 */
    protected ArrayList<IClearAble> mClearModel=new ArrayList<>();
    /** 需要在插件关闭时进行处理的模块列表,此列表模块将在插件关闭后调用 */
    protected ArrayList<INeedClose> mCloseModels=new ArrayList<>();
    /** 需要重载的模块列表,此列表模块将在配置文件重载后调用重载方法 */
    protected ArrayList<INeedReload> mReloadModels=new ArrayList<>();
    /** 所有管理器模块,请不要直接访问该列表 */
    protected final LinkedHashMap<Class<?>,Object> mManager=new LinkedHashMap<>();
    /** 配置管理器,需要初始化 */
    private TConfigManager<T> mConfigManager;
    /** 语言管理器,需要初始化 */
    private TLangManager<T> mLangManager;
    /** 插件使用情况收集实例 */
    protected final Statistics mStatistics;
    // 缓存
    private AManager<?> mCachedMan=null;
    private boolean mForceNoStatistics=false;

    /**
     * 新建一个插件实例
     * <p>
     * 同时会设置静态mInstance变量的值和插件消息前缀<br />
     * 实例化该对象后,请注意同时实例化语言文件和配置文件
     * </p>
     */
    public ABukkitPlugin(){
        ABukkitPlugin.mInstances.put((Class<? extends ABukkitPlugin<?>>)this.getClass(),this);
        /** 实例化统计类 */
        this.mStatistics=new Statistics(this);
        if(this.getDescription().getVersion().split("\\.").length>3) this.mForceNoStatistics=true;
        /** 设置Log默认前缀 */
        Log.setMsgPrefix("§7[§a"+getName()+"§7]§3");
        /** 设置CommentedYaml的日志器 */
        CommentedYamlConfig.setLogger(YamlLogImpl.getInstance());
    }

    /**
     * 获取插件实例<br>
     * 不建议在static模块中初始化的时候调用此方法
     */
    @SuppressWarnings("unchecked")
    protected static <T extends ABukkitPlugin<T>> T getInstance(Class<T> pClazz){
        try{
            T tPlugin=(T)ABukkitPlugin.mInstances.get(pClazz);
            if(tPlugin==null)
                throw new IllegalStateException("Plugin not instantiated");
            return tPlugin;
        }catch(ClassCastException exp){
            return null;
        }
    }

    /**
     * 获取插件实例<br>
     * 不建议在static模块中初始化的时候调用此方法
     */
    public static ABukkitPlugin<?> getInstance(){
        for(ABukkitPlugin<?> sInstance : ABukkitPlugin.mInstances.values()){
            return sInstance;
        }
        return null;
    }

    /** 获取语言翻译 */
    public String C(String pNode){
        return this.getLangManager().getNode(pNode);
    }

    /** 获取语言翻译 */
    public String C(String pNode,String[] pPlaceHolders,Object...pParams){
        return this.getLangManager().getNode(pNode,pPlaceHolders,pParams);
    }

    /** 获取语言翻译 */
    public String C(String pNode,String pPlaceHolder,Object pParam){
        return this.getLangManager().getNode(pNode,new String[]{pPlaceHolder},pParam);
    }

    /**
     * 注册Bukkit时间监听器
     * 
     * @param pListener
     *            监听器
     * @see org.bukkit.plugin.PluginManager
     */
    public void registerEvents(Listener pListener){
        Bukkit.getPluginManager().registerEvents(pListener,this);
    }

    /**
     * 请使用{@link ABukkitPlugin#getConfigManager()#getConfig()}<br>
     * 调用此方法将抛出异常
     */
    @Deprecated
    public FileConfiguration getConfig(){
        throw new IllegalAccessError("请不要调用此方法来获取配置文件管理器");
    }

    /** 获取插件主配置管理器 */
    public TConfigManager<T> getConfigManager(){
        if(this.mConfigManager==null)
            throw new IllegalStateException("未实例化配置管理器");
        return this.mConfigManager;
    }

    /** 设置插件的配置管理器,会同时将配置管理器注册到重载模块上 */
    protected void setConfigManager(TConfigManager<T> pConfigMan){
        this.mConfigManager=pConfigMan;
        this.registerReloadModel(pConfigMan);
    }

    /** 获取插件语言管理器 */
    public TLangManager<T> getLangManager(){
        if(this.mLangManager==null){
            this.mLangManager=new TLangManager<>((T)this,"lang.yml","1.0");
        }
        return this.mLangManager;
    }

    /** 设置插件的语言管理器,会同时将语言管理器注册到重载模块上 */
    protected void setLangManager(TLangManager<T> plangMan){
        this.mLangManager=plangMan;
        this.registerReloadModel(plangMan);
    }

    /**
     * 重载插件
     * <p>
     * 先重载主配置管理器和语言列表<br>
     * 然后重载INeedConfig模块<br>
     * 最后重载INeedReload模块
     * </p>
     * <p>
     * 如果重载过程中发生错误,会通知重载的用户,成功则不会通知成功消息
     * </p>
     */
    public void reloadPlugin(CommandSender pSender){
        ABukkitPlugin.mStatusStack.add(Status.Reload_Plugin);
        try{
            // 调用模块的reloadConfig()
            for(INeedReload sReload : this.mReloadModels){
                if(sReload==this.mLangManager){// 重载语言文件
                    this.reloadLang(pSender);
                }else if(sReload==this.mConfigManager){// 重载配置和配置模块
                    this.reloadConfig(pSender);
                }else{
                    sReload.reloadConfig(pSender);
                }
            }
            Log.info(pSender,this.C("MsgPluginReloaded"));
        }finally{
            ABukkitPlugin.mStatusStack.remove(Status.Reload_Plugin);
        }
    }

    /**
     * 重载插件主配置
     * <p>
     * 先重载配置管理器再重载INeedConfig模块<br>
     * 如果重载过程中发生错误,会通知重载的用户,成功则不会通知成功消息
     * </p>
     * 
     * @param pSender
     *            请求发起者
     * @return 是否重载成功
     */
    public boolean reloadConfig(CommandSender pSender){
        ABukkitPlugin.mStatusStack.add(Status.Reload_Config);
        try{
            return this.getConfigManager().reloadConfig(pSender);
        }finally{
            ABukkitPlugin.mStatusStack.remove(Status.Reload_Config);
        }
    }

    /**
     * 重载语言文件
     * 
     * @param pSender
     *            请求发起者
     * @return
     */
    public boolean reloadLang(CommandSender pSender){
        ABukkitPlugin.mStatusStack.add(Status.Reload_Lang);
        try{
            boolean tResult=this.getLangManager().reloadConfig(pSender);
            if(tResult){
                // 设置Log的翻译
                for(Level sLevel : Level.values()){
                    if(StringUtil.isNotEmpty(sLevel.mNameKey)){
                        sLevel.setLang(TLangManager.staticGetNode(sLevel.mNameKey,sLevel.getLang()));
                    }
                }
            }
            return tResult;
        }finally{
            ABukkitPlugin.mStatusStack.remove(Status.Reload_Lang);
        }
    }

    public void setStatistics(boolean pEnable){
        try{
            this.mStatistics.setEnable(this.mForceNoStatistics?false:pEnable);
        }catch(Throwable ignore){
        }
    }

    /**
     * 清理插件可清理模块列表
     * 
     * @return 清理的模块数量
     */
    public int clearModelStatus(){
        int tCount=0;
        for(IClearAble sClearModel : this.mClearModel){
            if(sClearModel.clearStatus())
                tCount++;
        }
        return tCount;
    }

    /**
     * 注册可清理状态模块列表
     */
    public void registerClearModel(IClearAble pClearModel){
        if(pClearModel==null)
            return;
        if(this.mClearModel.contains(pClearModel))
            return;
        this.mClearModel.add(pClearModel);
    }

    /**
     * 注册需要重载配置的模块列表,此列表只用于非重要实例使用
     */
    public void registerReloadModel(INeedReload pNeedReload){
        if(pNeedReload==null)
            return;
        if(this.mReloadModels.contains(pNeedReload))
            return;
        this.mReloadModels.add(pNeedReload);
    }

    /**
     * 注册需要重载配置的模块列表,此列表只用于非重要实例使用
     */
    public void registerCloseModel(INeedClose pNeedClose){
        if(pNeedClose==null)
            return;
        if(this.mCloseModels.contains(pNeedClose))
            return;
        this.mCloseModels.add(pNeedClose);
    }

    public void registerListener(Listener pListener){
        Bukkit.getPluginManager().registerEvents(pListener,this);
    }

    /**
     * 移除可清理状态模块列表
     * 
     * @return 如果存在并移除返回true,不存在返回false
     */
    public boolean unregisterClearModel(IClearAble pClearModel){
        return this.mClearModel.remove(pClearModel);
    }

    /**
     * 移除可重载模块列表
     * 
     * @return 如果存在并移除返回true,不存在返回false
     */
    public boolean unregisterReloadModel(INeedReload pReloadModel){
        return this.mReloadModels.remove(pReloadModel);
    }

    /**
     * 注册管理模块
     * 
     * @param pManager
     *            管理模块实例
     */
    protected <E extends AManager<?>> void registerManager(E pManager){
        if(pManager==null)
            return;
        Object oldMan=this.mManager.put(pManager.getClass(),pManager);
        if(oldMan!=null){
            Log.severe(pManager.getClass().getSimpleName()+"管理器重复注册");
        }
    }

    /**
     * 获取注册的管理模块实例
     * 
     * @param pClazz
     *            模块类
     * @return 模块实例,非null
     */
    public <E extends AManager<T>> E getManager(Class<E> pClazz){
        if(pClazz==null)
            return null;
        try{
            if(this.mCachedMan!=null&&pClazz==this.mCachedMan.getClass())
                return (E)this.mCachedMan;

            E tFindMan=(E)this.mManager.get(pClazz);
            if(tFindMan==null)
                throw new IllegalStateException(this.C("MsgUnInstanceManager").replace("%class%",pClazz.getSimpleName()));

            this.mCachedMan=tFindMan;
            return tFindMan;
        }catch(Throwable exp){
            Log.severe("在获取管理器并转换类型时发生了错误",exp);
            return null;
        }
    }

    /** 函数中会调用需要在插件关闭时进行处理的模块 */
    @Override
    public void onDisable(){

        // 模块关闭
        for(INeedClose sCloseModel : this.mCloseModels){
            sCloseModel.disable();
        }
        // 停止信息收集
        this.mStatistics.stop();
    }

    /**
     * 禁用此方法,请使用{@link ABukkitPlugin#reloadConfig(CommandSender)}方法重载配置
     */
    @Deprecated
    @Override
    public void reloadConfig(){
        throw new IllegalAccessError("请使用reloadConfig(CommandSender)方法代替本方法");
    }

    /**
     * 检查玩家是否有以插件名字开头的权限
     * 
     * @param pPlayer
     *            玩家,允许为null
     * @param pCmdTails
     *            子权限
     * @return 是否
     */
    public boolean hasCmdPermission(Permissible pPlayer,String...pCmdTails){
        if(pPlayer==null)
            return false;
        StringBuilder tPermitBuilder=new StringBuilder(this.getName());
        if(pCmdTails!=null&&pCmdTails.length>0){
            for(String sCmdTail : pCmdTails){
                tPermitBuilder.append('.').append(sCmdTail);
            }
        }
        return pPlayer.hasPermission(tPermitBuilder.toString());
    }

}
