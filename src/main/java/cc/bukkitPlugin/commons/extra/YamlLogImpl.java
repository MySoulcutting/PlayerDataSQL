package cc.bukkitPlugin.commons.extra;

import cc.bukkitPlugin.commons.Log;
import cc.commons.commentedyaml.CommentedYamlConfig.ErrorLog;

/**
 * Commented Yaml 错误日记继承
 */
public class YamlLogImpl extends ErrorLog{

    private final static YamlLogImpl mInstance=new YamlLogImpl();

    public static YamlLogImpl getInstance(){
        return YamlLogImpl.mInstance;
    }

    private YamlLogImpl(){}

    @Override
    public void severe(String pErrorMsg){
        Log.severe(pErrorMsg);
    }

    @Override
    public void severe(Throwable pExp){
        Log.severe(pExp);
    }

    @Override
    public void severe(String pErrorMsg,Throwable pExp){
        Log.severe(pErrorMsg,pExp);
    }

}
