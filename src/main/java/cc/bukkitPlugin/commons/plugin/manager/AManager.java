package cc.bukkitPlugin.commons.plugin.manager;

import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;

public abstract class AManager<T extends ABukkitPlugin<T>>{

    protected final T mPlugin;

    protected AManager(T pPlugin){
        this.mPlugin=pPlugin;
    }

    public T getPlugin(){
        return this.mPlugin;
    }

}
