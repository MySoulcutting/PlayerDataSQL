package cc.commons.util.reflect;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import cc.commons.util.StringUtil;
import cc.commons.util.interfaces.IFilter;

public class ClassUtil{

    private static final String REFLACT_OP_ERROR="反射操作异常";

    private static LinkedHashMap<Integer,String> mModifers=new LinkedHashMap<>();

    public static final int M_BRIDGE=0x00000040;
    public static final int M_VARARGS=0x00000080;
    public static final int M_SYNTHETIC=0x00001000;
    public static final int M_ANNOTATION=0x00002000;
    public static final int M_ENUM=0x00004000;

    static{
        mModifers.put(Modifier.PUBLIC,"PUBLIC");
        mModifers.put(Modifier.PRIVATE,"PRIVATE");
        mModifers.put(Modifier.PROTECTED,"PROTECTED");
        mModifers.put(Modifier.STATIC,"STATIC");
        mModifers.put(Modifier.FINAL,"FINAL");
        mModifers.put(Modifier.SYNCHRONIZED,"SYNCHRONIZED");
        mModifers.put(Modifier.VOLATILE,"VOLATILE");
        mModifers.put(Modifier.TRANSIENT,"TRANSIENT");
        mModifers.put(Modifier.NATIVE,"NATIVE");
        mModifers.put(Modifier.INTERFACE,"INTERFACE");
        mModifers.put(Modifier.ABSTRACT,"ABSTRACT");
        mModifers.put(Modifier.STRICT,"STRICT");
        mModifers.put(0x00000040,"BRIDGE");
        mModifers.put(0x00000080,"VARARGS");
        mModifers.put(0x00001000,"SYNTHETIC");
        mModifers.put(0x00002000,"ANNOTATION");
        mModifers.put(0x00004000,"ENUM");
    }

    /**
     * 后者指定的访问限定符值域是否存在在前者中或者相同
     * <p>
     * pWhich<=0,表示总是存在
     * </p>
     * 
     * @param pCol
     *            访问限定符集合
     * @param pWhich
     *            需要存在的访问限定符集合
     * @return 是否存在
     * @see java.lang.reflect.Modifier
     */
    public static boolean includedModifier(int pCol,int pWhich){

        if(pWhich<=0||pCol==pWhich)
            return true;
        return (pCol&pWhich)!=0;
    }

    /**
     * 访问限定符由数字映射到字符串
     * <p>
     * pWhich<=0,表示总是存在
     * </p>
     * 
     * @param pModifers
     *            访问限定符数值
     * @return 包含的访问限定符
     */
    public static ArrayList<String> getModiferName(int pModifers){
        ArrayList<String> tStrs=new ArrayList<>();
        for(Map.Entry<Integer,String> sEntry : ClassUtil.mModifers.entrySet()){
            if((sEntry.getKey().intValue()&pModifers)!=0){
                tStrs.add(sEntry.getValue());
            }
        }
        return tStrs;
    }

    /**
     * 访问限定符由数字映射到字符串,并使用逗号连接每个访问限定符
     * <p>
     * pWhich<=0,表示总是存在
     * </p>
     * 
     * @param pModifers
     *            访问限定符数值
     * @return 包含的访问限定符
     */
    public static String getModiferNameStr(int pModifers){
        return StringUtil.toString(ClassUtil.getModiferName(pModifers)," ,");
    }

    /**
     * 获取类全名的前段字符串
     * 
     * @param pClassName
     *            类全名
     * @return 类路径,如果不为空末端将包含点
     */
    public static String getClassPacket(String pClassName){
        String packetPath="";
        int pos=pClassName.lastIndexOf(".");
        if(pos!=-1)
            packetPath=pClassName.substring(0,pos+1);
        return packetPath;
    }

    /**
     * 查看指定类是否加载,不会报错
     * 
     * @param pName
     *            类完整名字
     * @return
     */
    public static boolean isClassLoaded(String pName){
        try{
            Class.forName(pName);
            return true;
        }catch(Throwable exp){
            return false;
        }
    }

    /**
     * 获取类
     * 
     * @param pClazz
     *            类全限定名
     * @return 类
     */
    public static Class<?> getClass(String pClazz){
        try{
            return Class.forName(pClazz);
        }catch(ClassNotFoundException exp){
            throw new IllegalStateException(REFLACT_OP_ERROR,exp);
        }
    }

    /**
     * 获取同一包下的类
     * 
     * @param pSiblingClazz
     *            其中一个兄弟类
     * @param pShortName
     *            另一个兄弟的类短名
     * @return 另一个兄弟类实例
     */
    public static Class<?> getSiblingClass(Class<?> pSiblingClazz,String pShortName){
        return ClassUtil.getClass(ClassUtil.getClassPacket(pSiblingClazz.getName())+pShortName);
    }

    public static <T> Constructor<T> getConstrouctor(Class<T> pClazz,Class<?>...pParamTypes){
        try{
            return pClazz.getDeclaredConstructor(pParamTypes);
        }catch(NoSuchMethodException|SecurityException exp){
            throw new IllegalStateException(REFLACT_OP_ERROR,exp);
        }
    }
    
    /**
     * 使用无参构造函数实例化类
     * 
     * @param pClazz
     *            类
     * @return 实例
     */
    public static <T> T newInstance(Class<? extends T> pClazz){
        return ClassUtil.newInstance(pClazz,new Class<?>[]{},new Object[]{});
    }

    /**
     * 使用单参构造函数实例化类
     * 
     * @param pClazz
     *            类
     * @param pParamType
     *            参数类型
     * @param pParam
     *            参数
     * @return 实例
     */
    public static <T> T newInstance(Class<? extends T> pClazz,Class<?> pParamType,Object pParam){
        return ClassUtil.newInstance(pClazz,new Class<?>[]{pParamType},new Object[]{pParam});
    }

    /**
     * 实例化类
     * 
     * @param pClazz
     *            类
     * @param pParamTypes
     *            参数类型
     * @param pParams
     *            参数
     * @return 实例
     */
    public static <T> T newInstance(Class<? extends T> pClazz,Class<?>[] pParamTypes,Object[] pParams){
        try{
            Constructor<? extends T> tcons;
            if(pParamTypes==null||pParamTypes.length==0)
                tcons=pClazz.getDeclaredConstructor();
            else tcons=pClazz.getDeclaredConstructor(pParamTypes);
            tcons.setAccessible(true);
            return tcons.newInstance(pParams);
        }catch(NoSuchMethodException|SecurityException|InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException exp){
            throw new IllegalStateException(REFLACT_OP_ERROR,exp);
        }
    }

    public static <T> T newInstance(Constructor<T> pCons,Object...pParams){
        try{
            pCons.setAccessible(true);
            return pCons.newInstance(pParams);
        }catch(SecurityException|InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException exp){
            throw new IllegalStateException(REFLACT_OP_ERROR,exp);
        }
    }

    /**
     * 使用无参构造函数实例化类
     * 
     * @param pClazz
     *            类全限定名
     * @return 实例
     */
    public static Object newInstance(String pClazz){
        return ClassUtil.newInstance(pClazz,new Class<?>[]{},new Object[]{});
    }

    /**
     * 使用单参构造函数实例化类
     * 
     * @param pClazz
     *            类全限定名
     * @param pParamClazz
     *            参数类型
     * @param pParam
     *            参数
     * @return 实例
     */
    public static Object newInstance(String pClazz,Class<?> pParamClazz,Object pParam){
        return ClassUtil.newInstance(pClazz,new Class<?>[]{pParamClazz},new Object[]{pParam});
    }

    /**
     * 实例化类
     * 
     * @param pClazz
     *            类全限定名
     * @param pParamTypes
     *            参数类型
     * @param pParams
     *            参数
     * @return 实例
     */
    public static Object newInstance(String pClazz,Class<?>[] pParamTypes,Object[] pParams){
        return ClassUtil.newInstance(ClassUtil.getClass(pClazz),pParamTypes,pParams);
    }

    /**
     * 获取包下的所有类
     * 
     * @param pPackage
     *            包名,会替换路径中的点为反斜杠
     * @param pRecursive
     *            是否递归获取
     * @return 包下所有的类
     * @throws IOException
     *             读写文件时发生异常
     */
    public static List<Class<?>> getPackageClasses(String pPackage,boolean pRecursive) throws IOException{
        return ClassUtil.getPackageClasses(pPackage,pRecursive,(IFilter<Class<?>>)null);
    }

    /**
     * 获取包下的所有类
     * 
     * @param pPackage
     *            包名,会替换路径中的点为反斜杠
     * @param pRecursive
     *            是否递归获取
     * @param pClassFilter
     *            类过滤器,可以为null
     * @return 包下所有的类
     * @throws IOException
     *             读写文件时发生异常
     */
    public static List<Class<?>> getPackageClasses(String pPackage,boolean pRecursive,IFilter<Class<?>> pClassFilter) throws IOException{
        ArrayList<Class<?>> tClasses=new ArrayList<>();
        String tPackageDir=pPackage.replace('.','/');
        String tFixPackageDir=tPackageDir.endsWith("/")?tPackageDir:tPackageDir+'/';
        pPackage=pPackage.replace('/','.');
        String tFixPackageName=StringUtil.isEmpty(pPackage)?"":pPackage.endsWith(".")?pPackage:pPackage+'.';
        Enumeration<URL> tURLs=Thread.currentThread().getContextClassLoader().getResources(tPackageDir);
        if(tURLs==null||!tURLs.hasMoreElements()) tURLs=ClassUtil.class.getClassLoader().getResources(tPackageDir);

        while(tURLs!=null&&tURLs.hasMoreElements()){
            URL tURL=tURLs.nextElement();
            String tProtocol=tURL.getProtocol();

            if("file".equalsIgnoreCase(tProtocol)){
                String tFilePath=URLDecoder.decode(tURL.getFile(),"UTF-8");
                tClasses.addAll(ClassUtil.getDirClasses(pPackage,tFilePath,pRecursive));
            }else if("jar".equalsIgnoreCase(tProtocol)){
                JarFile tJarFile=((JarURLConnection)tURL.openConnection()).getJarFile();
                Enumeration<JarEntry> tEntries=tJarFile.entries();
                while(tEntries.hasMoreElements()){
                    JarEntry tEntry=tEntries.nextElement();
                    String tName=tEntry.getName();

                    if(!tName.toLowerCase().endsWith(".class")) continue;
                    tName=tName.charAt(0)=='/'?tName.substring(1):tName;
                    if(!tName.startsWith(tFixPackageDir)) continue;

                    String tSubName=tName.substring(tFixPackageDir.length(),tName.length()-6);
                    int tLastPackagesIndex=tSubName.indexOf('/');
                    String tClassName=null;
                    if(tLastPackagesIndex!=-1){
                        if(pRecursive){
                            tClassName=tFixPackageName+(tSubName.replace('/','.'));
                        }
                    }else{
                        tClassName=tFixPackageName+tSubName;
                    }

                    if(tClassName!=null){
                        try{
                            Class<?> tClazz=Class.forName(tClassName,false,ClassUtil.class.getClassLoader());
                            if(pClassFilter==null||pClassFilter.accept(tClazz)){
                                tClasses.add(tClazz);
                            }
                        }catch(ClassNotFoundException|NoClassDefFoundError ignore){
                        }catch(LinkageError exp){
                            exp.printStackTrace();
                        }
                    }

                }
            }
        }
        return tClasses;
    }

    /**
     * 获取路径下的所有类
     * 
     * @param pPackage
     *            类的包名
     * @param pFilePath
     *            路径
     * @param pRecursive
     *            是否递归
     * @return 获取的类
     */
    public static List<Class<?>> getDirClasses(String pPackage,String pFilePath,boolean pRecursive){
        return ClassUtil.getDirClasses(pPackage,pFilePath,pRecursive,(IFilter<Class<?>>)null);
    }

    /**
     * 获取路径下的所有类
     * 
     * @param pPackage
     *            类的包名
     * @param pFilePath
     *            路径
     * @param pRecursive
     *            是否递归
     * @param pClassFilter
     *            类过滤器
     * @return 获取的类
     */
    public static List<Class<?>> getDirClasses(String pPackage,String pFilePath,boolean pRecursive,IFilter<Class<?>> pClassFilter){
        ArrayList<Class<?>> tClasses=new ArrayList<>();
        File tDir=new File(pFilePath);

        if(!tDir.isDirectory()) return tClasses;
        File[] tSubFiles=tDir.listFiles();
        if(tSubFiles==null||tSubFiles.length==0) return tClasses;
        pPackage=pPackage.replace('/','.');
        String tFixPackageName=StringUtil.isEmpty(pPackage)?"":pPackage.endsWith(".")?pPackage:pPackage+'.';

        for(File sSubFile : tSubFiles){
            if(sSubFile.isDirectory()){
                if(pRecursive){
                    tClasses.addAll(ClassUtil.getDirClasses(tFixPackageName+sSubFile.getName(),sSubFile.getAbsolutePath(),pRecursive,pClassFilter));
                }
            }else{
                String tClassName=sSubFile.getName().substring(0,sSubFile.getName().length()-6);
                try{
                    Class<?> tClazz=Class.forName(tFixPackageName+tClassName,false,ClassUtil.class.getClassLoader());
                    if(pClassFilter==null||pClassFilter.accept(tClazz)){
                        tClasses.add(tClazz);
                    }
                }catch(ClassNotFoundException|NoClassDefFoundError ignore){
                }catch(LinkageError exp){
                    exp.printStackTrace();
                }
            }
        }

        return tClasses;
    }

}
