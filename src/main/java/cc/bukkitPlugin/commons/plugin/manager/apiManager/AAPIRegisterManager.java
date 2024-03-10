package cc.bukkitPlugin.commons.plugin.manager.apiManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.commons.util.StringUtil;

public abstract class AAPIRegisterManager<T extends ABukkitPlugin<T>,E extends IModel>extends AManager<T> implements Listener{

    protected LinkedHashMap<String,E> mAllModels=new LinkedHashMap<>();

    protected AAPIRegisterManager(T pPlugin){
        super(pPlugin);
        this.mPlugin.getServer().getPluginManager().registerEvents(this,this.mPlugin);
    }

    /**
     * 注册一个模块<br>
     * 函数会调用参数pModel的{@link IModel#init()}方法,检查模块是否初始化成功
     * 
     * @param pPlugin
     *            模块来源
     * @param pModel
     *            模块
     * @return 是否注册成功
     */
    public boolean register(E pModel){
        try{
            if(pModel==null||!pModel.init()||StringUtil.isEmpty(pModel.getName()))
                return false;
        }catch(Throwable exp){
            return false;
        }
        Plugin tPlugin=pModel.getPlugin();
        if(tPlugin==null||!tPlugin.isEnabled())
            return false;

        synchronized(this){
            E replaceModel=this.mAllModels.put((tPlugin.getName()+"|"+pModel.getName()).toLowerCase(),pModel);
            if(replaceModel!=null&&replaceModel!=pModel){
                this.onSameNameModelRegister(replaceModel,pModel);
            }
            this.onModelSuccessRegister(pModel);
        }
        return true;
    }

    /**
     * 注销一个插件的所有模块
     * 
     * @param pPlugin
     *            插件
     * @return 被注销的模块
     */
    public Collection<E> unregisterPluginModels(Plugin pPlugin){
        if(pPlugin==null)
            return null;

        ArrayList<E> tRemoveModels=new ArrayList<>();
        synchronized(this){
            Iterator<Map.Entry<String,E>> tModelIt=this.mAllModels.entrySet().iterator();
            while(tModelIt.hasNext()){
                E sModel=tModelIt.next().getValue();
                if(sModel.getPlugin()==pPlugin){
                    tModelIt.remove();
                    tRemoveModels.add(sModel);
                }
            }
        }
        return tRemoveModels;
    }

    /**
     * 注销一个模块
     * 
     * @param pPlugin
     *            模块所属插件
     * @param pModelName
     *            模块名字
     * @return 被注销的模块
     */
    public E unregister(Plugin pPlugin,String pModelName){
        if(pPlugin==null||StringUtil.isEmpty(pModelName))
            return null;

        synchronized(this){
            return this.mAllModels.remove((pPlugin.getName()+"|"+pModelName).toLowerCase());
        }
    }

    /**
     * 在注册了相同名字不同模块时调用
     * 
     * @param pReplacedModel
     *            被替换的模块
     * @param pNewModel
     *            注册的新模块
     */
    protected void onSameNameModelRegister(E pReplacedModel,E pNewModel){}

    /**
     * 在模块成功注册时调用
     * 
     * @param pModel
     *            成功注册的模块
     */
    protected void onModelSuccessRegister(E pModel){
        Log.info(this.getAPIName()+"注册了模块"+pModel.getName());
    }

    /**
     * 根据名字获取模块
     * 
     * @param pModelName
     *            <code>("插件名字"+"|"+"模块名字").toLowerCase()</code>
     * @return 获取的模块
     */
    public E getModel(String pModelName){
        return this.mAllModels.get(pModelName);
    }

    /**
     * 获取全部注册的模块
     */
    public Collection<E> getAllModels(){
        Collection<E> tCopy=new HashSet<>();
        synchronized(this){
            tCopy.addAll(this.mAllModels.values());
        }
        return tCopy;
    }

    /**
     * 在插件被停用是调用,用于注销被停用的插件的模块
     * <p>
     * 如果是插件本身停用,会清空{@link AAPIRegisterManager#mAllModels}
     * </p>
     * 
     * @param pEvent
     *            插件停用事件
     */
    @EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
    public void onPluginDisable(PluginDisableEvent pEvent){
        if(pEvent.getPlugin()==this.mPlugin){
            this.mAllModels.clear();
        }else{
            this.unregisterPluginModels(pEvent.getPlugin());
        }
    }

    public abstract String getAPIName();

}
