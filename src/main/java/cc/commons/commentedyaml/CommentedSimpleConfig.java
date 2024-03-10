package cc.commons.commentedyaml;

import java.util.Map;

/**
 * 支持注释的Yaml文件配置管理器<br>
 * 此配置管理器专门针对配置文件只有一层的文件
 * 
 * @author 聪聪
 *
 */
public class CommentedSimpleConfig extends CommentedYamlConfig{

    public CommentedSimpleConfig(){
        super();
        this.options().enabelComment(false);
    }

    protected void convertMapsToSections(Map<?,?> pInput,CommentedSection pSection){
        if(pInput==null)
            return;
        for(Map.Entry<?,?> entry : pInput.entrySet()){
            String key=entry.getKey().toString();
            Object value=entry.getValue();
            pSection.set(key,value);
        }
    }

    protected void getChildValues(Map<String,Object> pValues,boolean pDeep){
        super.getChildValues(pValues,false);
    }

    @Override
    protected CommentedSection getParentSection(String pPath,WarpKey pWarpKey){
        return this;
    }

    @Override
    protected CommentedSection getOrCreateParentSection(String pPath,WarpKey pWarpKey){
        return this;
    }

    @Override
    public String getCurrentPath(){
        return "";
    }

    @Override
    public Object get(String pPath,Object pDefValue){
        CommentedValue tValue=this.mChild.get(pPath);
        return tValue==null?pDefValue:tValue.getValue();
    }

}
