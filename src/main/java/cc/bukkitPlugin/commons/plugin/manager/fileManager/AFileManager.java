package cc.bukkitPlugin.commons.plugin.manager.fileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.FileUtil;
import cc.commons.util.IOUtil;

public abstract class AFileManager<T extends ABukkitPlugin<T>>extends AManager<T>{

    // 静态全局变量
    /**
     * 配置文件版本节点
     */
    public static final String SEC_CFG_VERSION="version";
    // 普通变量
    /**
     * 管理器依赖的文件名
     */
    protected String mFileName;
    /**
     * 管理器依赖的文件
     */
    protected File mFile;
    protected final CommentedYamlConfig mConfig;
    /**
     * 配置文件版本
     */
    protected String mVersion="1.0";

    /**
     * 构造一个基于文件的管理器
     * 
     * @param pPlugin
     *            主插件
     * @param pFileName
     *            数据文件的相对路径
     */
    public AFileManager(T pPlugin,String pFileName,String pVersion){
        super(pPlugin);
        this.mVersion=pVersion;
        this.mConfig=new CommentedYamlConfig();
        this.updateConfigFile(pFileName);
    }

    protected void updateConfigFile(String pFileName){
        this.mFileName=pFileName;
        this.mFile=new File(mPlugin.getDataFolder().getAbsolutePath(),mFileName);
    }

    /**
     * 重载配置文件
     * <p>
     * 如果文件不存在,自动创建文件<br>
     * 所有可能发生的错误都会被捕捉并显示在控制台<br>
     * 载入过程的消息将会发送给请求发送者<br>
     * </p>
     * 
     * @param pSender
     *            请求发起者,可能为null
     * @return 是否载入成功
     */
    public boolean reloadConfig(CommandSender pSender){
        if(!this.mFile.isFile()){ // 尝试从压缩包中加载
            InputStream tIPStream=null;
            OutputStream tOPStream=null;
            try{
                tIPStream=this.mPlugin.getResource(this.mFile.getName());
                if(tIPStream!=null){
                    FileUtil.createNewFile(this.mFile,false);
                    tOPStream=new FileOutputStream(this.mFile);
                    IOUtil.copy(tIPStream,tOPStream);
                }
            }catch(Throwable exp){
                Log.severe(pSender,null,exp);
            }finally{
                IOUtil.closeStream(tIPStream,tOPStream);
            }
        }
        if(!this.mConfig.loadFromFile(this.mFile)){
            String tLang=null;
            if(this instanceof TLangManager)
                tLang=TLangManager.staticGetNode("MsgLoadFileFail",null);
            else tLang=this.mPlugin.C("MsgLoadFileFail");
            Log.severe(pSender,tLang.replace("%file%",this.mFileName));
            return false;
        }
        return true;
    }

    /**
     * 保存config中的数据到到文件
     * <p>
     * 如果存在注释头部,将会同时保存注释头部
     * </p>
     */
    public boolean saveConfig(final CommandSender pSender){
        Thread tThread=new Thread(new Runnable(){

            @Override
            public void run(){
                if(!AFileManager.this.mConfig.saveToFile(mFile)){
                    String tLang=null;
                    if(AFileManager.this instanceof TLangManager)
                        tLang=TLangManager.staticGetNode("MsgSaveFileFail",null);
                    else tLang=AFileManager.this.mPlugin.C("MsgSaveFileFail");
                    Log.severe(pSender,tLang.replace("%file%",AFileManager.this.mFileName));
                }
            }
        });
        tThread.start();
        if(!this.mPlugin.isEnabled()){
            try{
                tThread.join();
            }catch(InterruptedException e){
                // DO Noting
            }
        }
        return true;
    }

    /**
     * 添加配置文件的默认值
     * <p>
     * 默认添加了版本<br>
     * 不应该在该函数中保存配置
     * </p>
     */
    protected void addDefaults(){
        this.mConfig.addDefault(AFileManager.SEC_CFG_VERSION,"1.0","配置文件版本,重要,请勿修改","插件升级配置的时候需要用到");
    }

    /**
     * 检查配置文件版本是否需要更新,如果是则返回true
     * <p>
     * 请在实现类中的该方法中对文件进行更新操作
     * </p>
     */
    protected boolean checkUpdate(){
        String tVersion=this.mConfig.getString(SEC_CFG_VERSION,"1.0");
        if(this.mVersion.compareToIgnoreCase(tVersion)<=0)
            return false;
        return true;
    }

    /**
     * 获取语言节点
     * 
     * @param pNode
     *            节点名
     */
    protected String C(String pNode){
        return this.mPlugin.getLangManager().getNode(pNode);
    }

    /**
     * 获取该文件管理器的配置器
     */
    public CommentedYamlConfig getConfig(){
        return this.mConfig;
    }

    /**
     * 获取该文件管理器的文件名
     * 
     * @return
     */
    public String getConfigFilename(){
        return this.mFileName;

    }

    /** 获取文件版本,如果文件中未记录版本,将返回1.0 */
    public String getVersion(){
        return this.mConfig.getString(SEC_CFG_VERSION,"1.0");
    }

    /**
     * 更新配置管理器中文件的版本
     * <p>
     * 注意: 函数内不会调用{@link #saveConfig(CommandSender)}函数去保存配置
     * </p>
     */
    public void updateVersion(String pVersion){
        this.mConfig.set(SEC_CFG_VERSION,pVersion);
    }

}
