package cc.commons.commentedyaml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import cc.commons.commentedyaml.comment.Composer;
import cc.commons.commentedyaml.serialize.SerializableYamlObject;
import cc.commons.commentedyaml.serialize.convert.SerializableYamlUtils;

/**
 * 支持注释的Yaml文件配置管理器
 * 
 * @author 聪聪
 */
public class CommentedYamlConfig extends CommentedSection{

    public static class ErrorLog{

        private Logger mLogger=Logger.getGlobal();

        public void severe(String pErrorMsg){
            this.mLogger.log(Level.SEVERE,pErrorMsg);
        }

        public void severe(Throwable pExp){
            this.mLogger.log(Level.SEVERE,pExp.getLocalizedMessage(),pExp);
        }

        public void severe(String pErrorMsg,Throwable pExp){
            this.mLogger.log(Level.SEVERE,pErrorMsg,pExp);
        }

    }

    protected static final String BLANK_CONFIG="{}\n";
    /** 错误日记记录器 */
    protected static ErrorLog mErrorLog=new ErrorLog();
    /** 解析类 */
    protected static Class<? extends CommentedRepresenter> mRepresenterClazz=CommentedRepresenter.class;
    /** 构造类 */
    protected static Class<? extends CommentedConstructor> mConstructorClazz=CommentedConstructor.class;

    /**
     * 设置错误日志记录器
     * <p>
     * 自定义日志器,只要自定义以下方法即可<br>
     * {@link ErrorLog#severe(String, Throwable)}
     * </p>
     * 
     * @param pLogger
     *            日志记录器
     */
    public static void setLogger(ErrorLog pLogger){
        if(pLogger!=null){
            CommentedYamlConfig.mErrorLog=pLogger;
        }
    }

    /**
     * 获取日志记录器
     * 
     * @return 日志记录器
     */
    public static ErrorLog getLogger(){
        return CommentedYamlConfig.mErrorLog;
    }

    /**
     * 设置Yaml解析类
     * 
     * @param pClazz
     *            解析类
     */
    public static void setRepresenter(Class<? extends CommentedRepresenter> pClazz){
        CommentedYamlConfig.mRepresenterClazz=pClazz;
    }

    /**
     * 实例化一个Yaml解析器
     * 
     * @return 解析器
     */
    public static Representer newRepresenter(){
        try{
            return CommentedYamlConfig.mRepresenterClazz.newInstance();
        }catch(Throwable exp){
            CommentedYamlConfig.getLogger().severe(exp);
            return new CommentedRepresenter();
        }
    }

    /**
     * 设置Yaml构造类
     * 
     * @param pClazz
     *            构造类
     */
    public static void setConstructor(Class<? extends CommentedConstructor> pClazz){
        CommentedYamlConfig.mConstructorClazz=pClazz;
    }

    /**
     * 实例化一个Yaml构造器
     * 
     * @return 构造器
     */
    public static Constructor newConstructor(){
        try{
            return CommentedYamlConfig.mConstructorClazz.newInstance();
        }catch(Throwable exp){
            CommentedYamlConfig.getLogger().severe(exp);
            return new CommentedConstructor();
        }
    }

    private static String listToPath(Collection<String> pPathParts){
        if(pPathParts==null||pPathParts.isEmpty())
            return "";

        StringBuilder tSBuilder=new StringBuilder();
        for(String sPath : pPathParts){
            tSBuilder.append(sPath).append('.');
        }
        return tSBuilder.deleteCharAt(tSBuilder.length()-1).toString();
    }

    /**
     * 载入配置文件
     * 
     * @param pFile
     *            要载入的配置文件
     * @return 载入的配置管理器
     */
    public static CommentedYamlConfig loadFromFileS(File pFile){
        return CommentedYamlConfig.loadFromFileS(pFile,true);
    }

    /**
     * 载入配置文件
     * 
     * @param pFile
     *            要载入的配置文件
     * @param pEnableComment
     *            是否解析配置文件注释
     * @return 载入的配置管理器
     */
    public static CommentedYamlConfig loadFromFileS(File pFile,boolean pEnableComment){
        CommentedYamlConfig tConfig=new CommentedYamlConfig();
        tConfig.options().enabelComment(pEnableComment);
        tConfig.loadFromFile(pFile);
        return tConfig;
    }

    /** Yaml加载器,非线程安全 */
    protected final Yaml mYaml;
    /** 配置管理器,Yaml Dump选项 */
    protected final CommentedOptions mOptions;
    /** 仅用于加载,非线程安全 */
    protected final Representer mConfigRepresenter;
    /** 缓存的注释,为那些路径被阻断的注释节点保存的注释 */
    protected final HashMap<String,ArrayList<String>> mCachedComment=new HashMap<>();

    /**
     * 使用指定的消息前缀实例化配置管理器
     */
    public CommentedYamlConfig(){
        this.mOptions=new CommentedOptions();
        this.mOptions.setIndent(2);
        this.mOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.mOptions.setAllowUnicode(true);
        if(!this.mOptions.isAllowUnicode()){
            Class<DumperOptions> clazz=DumperOptions.class;
            try{
                Field field=clazz.getDeclaredField("allowUnicode");
                field.setAccessible(true);
                field.setBoolean(mOptions,true);
            }catch(Exception exp){
                this.log("错误,无法设置文件存储为unicode编码",exp);
            }
        }
        this.mConfigRepresenter=CommentedYamlConfig.newRepresenter();
        this.mConfigRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.mYaml=new Yaml(CommentedYamlConfig.newConstructor(),this.mConfigRepresenter,mOptions);
    }

    /**
     * 在发生异常时用于输出调试信息,或输出警告信息到控制台
     * 
     * @param pMsg
     *            错误的提示消息
     * @param pExp
     *            要抛出的异常
     */
    private void log(String pMsg,Throwable pExp){
        CommentedYamlConfig.mErrorLog.severe(pMsg,pExp);
    }

    /**
     * 将从字符串生成的配置节点数据格式化复制到本实例中
     * 
     * @param input
     *            格式化后的数据
     * @param section
     *            本实例,用于递归
     */
    protected void convertMapsToSections(Map<?,?> input,CommentedSection section){
        if(input==null)
            return;
        for(Map.Entry<?,?> entry : input.entrySet()){
            String key=entry.getKey().toString();
            Object value=entry.getValue();
            if((value instanceof Map))
                convertMapsToSections((Map<?,?>)value,section.createSection(key));
            else section.set(key,value);
        }
    }

    /**
     * 保存当前配置内存中的数据为字符串
     */
    public String saveToString(){
        Map<String,Object> tValues=this.getValues(false);
        Representer tRepresenter=CommentedYamlConfig.newRepresenter();
        tRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String tDumpValue=new Yaml(CommentedYamlConfig.newConstructor(),tRepresenter,mOptions).dump(tValues);
        if(tDumpValue.equals(BLANK_CONFIG))
            return "";

        if(this.options().isEnableComment()){
            return Composer.dumpComment(this,tDumpValue);
        }else return tDumpValue;
    }

    /**
     * 保存指定的数据到指定的文件,如果文件不存在将会自动创建
     * <p>
     * 保存数据过程中的任何错误都会被记录到控制台然后忽视
     * </p>
     * 
     * @param pFile
     *            指定的文件
     * @return 是否保存成功
     */
    public boolean saveToFile(File pFile){
        FileOutputStream tOutput=null;
        try{
            byte[] tContent=this.saveToString().getBytes("UTF-8");
            synchronized(this){
                this.tryCreateFile(pFile);
                tOutput=new FileOutputStream(pFile,false);
                tOutput.write(tContent);
            }
        }catch(FileNotFoundException ex){
            this.log("未找到文件["+pFile+"]",ex);
            return false;
        }catch(IOException ex){
            this.log("无法保存文件["+pFile+"]",ex);
            return false;
        }finally{
            if(tOutput!=null)
                try{
                tOutput.close();
                }catch(IOException exp){
                }
        }
        return true;
    }

    /**
     * 反序列化指定的数据到指定的类型,如果类型不存在你就会狗带
     * <p>
     * 保存数据过程中的任何错误都会被记录到控制台然后忽视
     * </p>
     *
     * @param pClass
     *            指定的类型
     * @return T
     */
    public <T extends SerializableYamlObject> T saveToObject(Class<T> pClass) throws YAMLException{
        return SerializableYamlUtils.saveToObject(this,null,pClass);
    }

    /**
     * 反序列化指定的数据到指定的类型,如果类型不存在你就会狗带
     * <p>
     * 保存数据过程中的任何错误都会被记录到控制台然后忽视
     * </p>
     *
     * @param pClass
     *            指定的类型
     * @return T
     */
    public <T extends SerializableYamlObject> T saveToObject(Class<T> pClass,T pObj) throws YAMLException{
        return SerializableYamlUtils.saveToObject(this,pObj,pClass);
    }

    /**
     * 序列化指定的数据到指定的类型,如果类型不存在你就会狗带
     * <p>
     * 保存数据过程中的任何错误都会被记录到控制台然后忽视
     * </p>
     *
     * @param pObj
     *            指定的对象
     * @param pClass
     *            指定的类型
     * @return CommentedYamlConfig 返回自己
     */
    public <T extends SerializableYamlObject> CommentedYamlConfig loadObject(T pObj,Class<T> pClass) throws YAMLException{
        super.loadObject(pObj,pClass);
        return this;
    }

    /**
     * 从字符串中载入配置
     */
    public void loadFromString(String pContents) throws YAMLException{
        Map<?,?> input=null;
        try{
            input=(Map<?,?>)this.mYaml.load(pContents);
        }catch(YAMLException e){
            throw e;
        }catch(ClassCastException e){
            throw new YAMLException("配置文件顶级节点不是Map");
        }
        this.clear();
        this.convertMapsToSections(input,this);
        if(this.options().isEnableComment()){
            Composer.loadComment(this,pContents);
        }
    }

    /**
     * 从给定的文件路径中载入数据
     * <p>
     * 如果文件不存在将会自动创建<br />
     * 载入配置文件过程中的任何错误都会被记录到控制台然后忽视<br />
     * 如果载入失败,配置管理器内容将不变<br />
     * 编码默认使用 UTF-8
     * <p>
     *
     * @return 是否载入成功
     * @param pFilename
     *            输入的文件路径
     * @throws NullPointerException
     *             如果文件名为空
     */
    public boolean loadFromFile(String pFilename){
        return this.loadFromFile(new File(pFilename));
    }

    /**
     * 从给定的文件中载入数据
     * <p>
     * 如果文件不存在将会自动创建<br />
     * 载入配置文件过程中的任何错误都会被记录到控制台然后忽视<br />
     * 如果载入失败,配置管理器内容将不变<br />
     * 编码默认使用 UTF-8
     * <p>
     *
     * @param pFile
     *            输入文件
     * @return 是否成功加载
     * @throws IllegalArgumentException
     *             如果文件为空
     */
    public boolean loadFromFile(File pFile){
        InputStreamReader tInput=null;
        boolean tBackup=false,tResult=true;
        try{
            if(!pFile.isFile()) this.tryCreateFile(pFile);

            int tReadLen=-1;
            char[] tBuff=new char[2048];
            InputStreamReader tReader=(tInput=new InputStreamReader(new FileInputStream(pFile),"UTF-8"));
            StringBuilder tSBuilder=new StringBuilder();
            while((tReadLen=tReader.read(tBuff))!=-1){
                tSBuilder.append(tBuff,0,tReadLen);
            }

            this.loadFromString(tSBuilder.toString());
        }catch(FileNotFoundException ex){
            this.log("无法找到文件["+pFile+"]",ex);
            return false;
        }catch(IOException ex){
            this.log("无法加载文件["+pFile+"]",ex);
            return false;
        }catch(YAMLException ex){
            this.log("无法加载文件["+pFile+"],配置文件格式错误",ex);
            tBackup=this.options().isBackupOnFormatError();
            tResult=false;
        }finally{
            if(tInput!=null) try{
                tInput.close();
            }catch(IOException ignore){
            }
        }

        if(tBackup){
            String tFileName=pFile.getName(),tSuffix="";
            int tIndex=tFileName.lastIndexOf('.');
            if(tIndex!=-1){
                tSuffix=tFileName.substring(tIndex+1);
                tFileName=tFileName.substring(0,tIndex);
            }
            tFileName=tFileName+(tSuffix.isEmpty()?"":'.'+tSuffix)+"."+new SimpleDateFormat("MMddHHmmssSSSS").format(new Date());
            pFile.renameTo(new File(pFile.getAbsoluteFile().getParentFile(),tFileName));
        }

        return tResult;
    }

    /**
     * 从给定的流中载入数据,流不会自动关闭
     * <p>
     * 如果文件不存在将会自动创建<br />
     * 载入配置文件过程中的任何错误都会被记录到控制台然后忽视<br />
     * 如果载入失败,配置管理器内容将不变<br />
     * 编码默认使用 UTF-8
     * <p>
     *
     * @param stream
     *            输入的数据流
     * @return 是否载入成功
     * @throws IllegalArgumentException
     *             如果输入流为空
     */
    public boolean loadFromStream(InputStream stream){
        try{
            ByteArrayOutputStream tBAOStream=new ByteArrayOutputStream();
            byte[] tBuffer=new byte[4096];
            int tRead=-1;
            while((tRead=stream.read(tBuffer))!=-1){
                tBAOStream.write(tBuffer,0,tRead);
            }

            this.loadFromString(new String(tBAOStream.toByteArray(),"UTF-8"));
        }catch(IOException ex){
            this.log("无法从输入流加载配置",ex);
            return false;
        }catch(YAMLException ex){
            this.log("无法从输入流加载配置,配置文件格式错误",ex);
            return false;
        }
        return true;
    }

    /**
     * 创建一个新文件<br/>
     * 如果文件已经存在,将什么都不干
     * 
     * @param pFile
     *            文件
     * @throws IOException
     *             创建文件时发生错误
     */
    protected void tryCreateFile(File pFile) throws IOException{
        if(pFile.exists()&&pFile.isFile())
            return;
        pFile=pFile.getAbsoluteFile();
        if(!pFile.getParentFile().isDirectory()){
            pFile.getParentFile().mkdirs();
        }
        pFile.createNewFile();
    }

    /**
     * 获取管理器的读取,Dump配置
     * 
     * @return 配置器
     */
    public CommentedOptions options(){
        return this.mOptions;
    }

    public void addCacheComment(List<String> pPath,Collection<String> pComments){
        this.addCacheComment(listToPath(pPath),pComments);
    }

    public void addCacheComment(String pPath,Collection<String> pComments){
        if(!pComments.isEmpty()){
            this.mCachedComment.put(pPath,new ArrayList<String>(pComments));
        }
    }

    @Override
    public boolean setCommentsNoReplace(List<String> pPath,Collection<String> pComments){
        if(super.setCommentsNoReplace(pPath,pComments))
            return true;

        this.mCachedComment.put(listToPath(pPath),new ArrayList<String>(pComments));
        return true;
    }

    @Override
    public ArrayList<String> getComments(List<String> pPath){
        ArrayList<String> tComments=super.getComments(pPath);
        if(tComments==null||tComments.isEmpty()){
            tComments=this.mCachedComment.get(listToPath(pPath));
        }
        return tComments;
    }

    @Override
    public Object remove(String pPath){
        Object tValue=super.remove(pPath);
        if(!this.mCachedComment.isEmpty()){
            Iterator<Map.Entry<String,ArrayList<String>>> tEntryIt=this.mCachedComment.entrySet().iterator();
            while(tEntryIt.hasNext()){
                Entry<String,ArrayList<String>> tEntry=tEntryIt.next();
                String tKey=tEntry.getKey();
                if(tKey.startsWith(pPath)){
                    if(tKey.length()>pPath.length()&&tKey.charAt(pPath.length())!='.')
                        continue;
                    tEntryIt.remove();
                }
            }
        }
        return tValue;
    }

    @Override
    public void set(String pPath,Object pValue,String...pComments){
        ArrayList<String> tComments=this.mCachedComment.remove(pPath);
        if(pComments.length==0&&tComments!=null&&!tComments.isEmpty()){
            pComments=tComments.toArray(new String[tComments.size()]);
        }
        super.set(pPath,pValue,pComments);
    }

    @Override
    public Map<String,CommentedValue> clear(){
        Map<String,CommentedValue> tValues=super.clear();
        this.mCachedComment.clear();
        return tValues;
    }

}
