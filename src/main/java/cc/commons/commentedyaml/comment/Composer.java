package cc.commons.commentedyaml.comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import cc.commons.commentedyaml.CommentedValue;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.commentedyaml.serialize.SerializableYamlObject;

enum Mode{
    /** 将配置管理器中的注释导入到dump出来的字符串中 */
    DUMP,
    /** 从字符串中读取出注释 */
    LOAD
}

public class Composer{

    private final static String mLineSeparator=System.getProperty("line.separator","\r\n");
    /** Yaml节点边界符号 */
    private final static HashSet<Character> mWrapChars;
    /** Yaml源格式字符标识 */
    private final static HashSet<String> mRawMarks;
    /**
     * 已经处理完毕的文本的最后一行索引
     * <p>
     * 请勿直接调用该变量<br>
     * 使用{@link Composer#alreadyHandleLine()}来更改已经处理的当前行<br>
     * 使用{@link Composer#getNextUnhandleLine()}来获取下一行未处理的文本<br>
     * 使用{@link Composer#haveUnhandleLine()}来判断是否已经处理完所有行文本<br>
     * </p>
     */
    @Deprecated
    private int mLineIndex=-1;
    /**
     * 原文本的行
     * <p>
     * 在{@link Mode#DUMP}模式中,函数{@link #alreadyHandleLine()}会使用到此内容来写入到{@link #mContent}
     * </p>
     */
    private String[] mSourceLines;
    /**
     * 原文本的行
     * <p>
     * 内容可能会在程序处理过程中发生变化
     * </p>
     */
    private String[] mLines;
    /** 绑定的配置管理器 */
    private CommentedYamlConfig mConfig;
    /** 当前绑定的Yaml对象 */
    private SerializableYamlObject mObject;
    /** 与注释合并的文本 */
    private final ArrayList<String> mContent=new ArrayList<>();
    /** 从文本中提取出且未保存到配置管理器中的注释 */
    private ArrayList<String> mComment=new ArrayList<>();
    /** 最后一条缓存的注释所在的行数 */
    private int mCommentLineIndex=-1;
    /** 注释操作模式 */
    private Mode mMode;

    static{
        mWrapChars=new HashSet<>();
        mWrapChars.add('\'');
        mWrapChars.add('"');
        mRawMarks=new HashSet<>();
        mRawMarks.add("<");
        mRawMarks.add("|");
        mRawMarks.add("|-");
    }

    private Composer(CommentedYamlConfig pConfig,String pContent,Mode pMode){
        this.mMode=pMode;
        this.mConfig=pConfig;

        pContent=pContent.replace("\t","    ");
        this.mLines=pContent.split("\r?\n");
        if(this.mMode==Mode.DUMP){
            this.mSourceLines=Arrays.copyOf(this.mLines,this.mLines.length);
        }
    }

    private Composer(SerializableYamlObject pObject,String pContent,Mode pMode){
        this.mMode=pMode;
        this.mObject=pObject;
        this.mSourceLines=pContent.split("[\\r]?\n");
        this.mLines=Arrays.copyOf(this.mSourceLines,this.mSourceLines.length);
    }

    /**
     * 从给予的文本中搜索节点的注释,并导入到配置管理器中
     * <p>
     * 如果导入出错,导入将终止,但是已经导入的注释将会保持
     * </p>
     *
     * @param pConfig
     *            配置管理器
     * @param pContent
     *            文本
     * @return 是否导入成功
     */
    public static boolean loadComment(CommentedYamlConfig pConfig,String pContent){
        return Composer.convert(new Composer(pConfig,pContent,Mode.LOAD));
    }

    /**
     * 将配置管理器中的注释输出并嵌入到文本中
     * <p>
     * 如果合并出错,合并将终止,函数将返回未合并的初始文本
     * </p>
     * 
     * @param pConfig
     *            配置管理器
     * @param pContent
     *            文本
     * @return 合并后的文本
     */
    public static String dumpComment(CommentedYamlConfig pConfig,String pContent){
        Composer tComposer=new Composer(pConfig,pContent,Mode.DUMP);
        if(Composer.convert(tComposer)){
            StringBuilder tSBuilder=new StringBuilder();
            for(String sStr : tComposer.mContent){
                tSBuilder.append(sStr).append(Composer.mLineSeparator);
            }
            return tSBuilder.toString();
        }
        return pContent;
    }

    /**
     * 导入或导出注释
     * <p>
     * 如果是Yaml节点格式分析失败,会将剩余未分析的内容写入到文本中,并且结果返回true
     * </p>
     * 
     * @param pComposer
     *            注释管理器
     * @return 是否无错误发生
     */
    public static boolean convert(Composer pComposer){
        YamlNode tRootNode=new YamlNode();
        tRootNode.setParent(tRootNode);
        try{
            while(pComposer.haveUnhandleLine()){
                pComposer.convertNode(tRootNode,-1);
            }
        }catch(IllegalStateException exp){
            CommentedYamlConfig.getLogger().severe(exp.getMessage());
            while(pComposer.haveUnhandleLine()){
                pComposer.alreadyHandleLine();
            }
            return true;
        }catch(Throwable exp){
            CommentedYamlConfig.getLogger().severe("导入导出配置文件注释时发生了错误",exp);
            return false;
        }
        return true;
    }

    /**
     * 转换节点
     * <p>
     * 此函数并未检查数组节点中的值类型是否一直<br>
     * 因为此类型的错误会在Yaml中就被检出
     * </p>
     *
     * @param pParent
     *            当前父节点
     * @param pParentSpaceLevel
     *            父节点的缩进等级,root节点为-1
     */
    private void convertNode(YamlNode pParent,int pParentSpaceLevel){
        YamlNode tLastChild=null; // 用于转换List节点时使用 
        ArrayList<String> tFullPath;
        int tLastIndent=0;
        while(this.haveUnhandleLine()){
            String tNowLine=this.getNextUnhandleLineNoComment();
            if(tNowLine==null){
                return;
            }
            int tNowSpaceLevel=this.getSpaceCount(tNowLine);
            if(tNowSpaceLevel>pParentSpaceLevel){ // 子节点或List节点的值
                String tLine=Composer.trimLeftSide(tNowLine);
                if(tLine.startsWith("- ")){
                    //提前做转换,兼容 不规则缩进的数组值
                    this.replaceCharAndSetBack(tNowLine,tNowSpaceLevel,' ');
                    this.convertNode(tLastChild==null?pParent:tLastChild,tNowSpaceLevel);
                    this.mComment.clear();
                    continue;
                }
                if(tLastIndent==0){
                    tLastIndent=tNowSpaceLevel-pParentSpaceLevel;
                }else{
                    int tIndent=tNowSpaceLevel-pParentSpaceLevel;
                    if(tIndent>tLastIndent){
                        this.convertNode(tLastChild,pParentSpaceLevel+tLastIndent);
                        continue;
                    }else if(tIndent<tLastIndent){
                        this.log("错误的缩进");// 此错误会在Yaml中检出
                        continue;
                    }
                }
                if(tLine.startsWith("? ")){
                    // Name节点非String类型
                    this.replaceCharAndSetBack(tNowLine,tNowSpaceLevel,' ');
                    tNowLine=this.getNextUnhandleLine();
                    tLine=Composer.trimLeftSide(tNowLine);
                    if(tLine.startsWith("!!")){
                        this.alreadyHandleLine();
                    }
                    this.convertNode(null,tNowSpaceLevel);
                    this.mComment.clear();
                    continue;
                }
                YamlNode tConvertNode=this.getLineType(pParent,tNowLine);
                switch(tConvertNode.mType){
                    case List:
                    case Comment:
                        // 不可能为数组或注释,已在上面进行处理
                        throw new IllegalStateException("请报告此问题给作者,此情况不应该发生");
                    case Node_Valued:
                        this.getOrSetComment(tConvertNode,tNowSpaceLevel);
                        // 读取剩余部分的值
                        Character tWarpChar=null;
                        boolean tWarp=!this.isCloseLine(tConvertNode.mValueStr);
                        if(tWarp){
                            tWarpChar=tConvertNode.mValueStr.charAt(0);
                        }
                        boolean tMulLine=tWarp;
                        while(this.haveUnhandleLine()){
                            String tNextLine=this.getNextUnhandleLine();
                            if(tWarp){
                                tConvertNode.mValueStr+=tNextLine;
                                this.alreadyHandleLine();
                                Character tCloseMark=this.getCloseMark(tNextLine,false);
                                if(tCloseMark!=null&&tCloseMark.equals(tWarpChar)){
                                    break;
                                }
                            }else{
                                int tNextSpace=this.getSpaceCount(tNextLine);
                                if(tNextSpace>tNowSpaceLevel){
                                    tConvertNode.mValueStr+=" "+tNextLine.trim();
                                    this.alreadyHandleLine();
                                    tMulLine=true;
                                }else break;
                            }
                        }
                        if(!tMulLine){
                            if(this.mMode==Mode.LOAD){
                                int tIndex=tConvertNode.mValueStr.indexOf('#');
                                if(tIndex!=-1&&(tFullPath=tConvertNode.getPathList())!=null){
                                    CommentedValue tValue=this.mConfig.getCommentedValue(tFullPath);
                                    if(tValue!=null){
                                        tValue.addComments(tConvertNode.mValueStr.substring(tIndex));
                                    }else{
                                        this.mComment.add(tConvertNode.mValueStr.substring(tIndex));
                                        this.mConfig.setCommentsNoReplace(tFullPath,this.mComment);
                                        this.mComment.clear();
                                    }
                                    this.mComment.clear();
                                }
                            }
                            if(Composer.mRawMarks.contains(tConvertNode.mValueStr)){
                                tConvertNode.mValueStr=this.readRawContent(tNowSpaceLevel);
                            }
                            tLastChild=tConvertNode;
                        }
                        break;
                    case Node_Empty:
                        this.getOrSetComment(tConvertNode,tNowSpaceLevel);
                        tLastChild=tConvertNode;
                        break;
                    case String:
                        this.alreadyHandleLine();
                        if(this.isCloseLine(tConvertNode.mValueStr)){
                            // this.log("此处不应该有"+tNowLine+",是否缺少冒号?");
                        }else{
                            if(!this.haveUnhandleLine())
                                return;
                            this.setNextUnhandleLine(tNowLine+(this.getNextUnhandleLine().trim()));
                        }
                        break;
                }
            }else{ // 与父节点同级或更上级节点,交由上一层处理
                return;
            }
        }
        return;
    }

    private void getOrSetComment(YamlNode pConvertNode,int pNowSpaceLevel){
        ArrayList<String> tFullPath;
        if(this.mMode==Mode.LOAD){
            this.alreadyHandleLine();
            if(pConvertNode.mType==LineType.Node_Empty&&pConvertNode.mValueStr!=null&&!pConvertNode.mValueStr.isEmpty()){
                this.addCacheComment(pConvertNode.mValueStr);
            }
            if(!this.mComment.isEmpty()&&(tFullPath=pConvertNode.getPathList())!=null){
                this.mConfig.setCommentsNoReplace(tFullPath,this.mComment);
                this.mComment.clear();
            }
        }else{
            if((tFullPath=pConvertNode.getPathList())!=null){
                this.addCommentToContent(pNowSpaceLevel,this.mConfig.getComments(tFullPath));
            }
            this.alreadyHandleLine();
        }
    }

    /**
     * 将字符串指定位置的点设置为指定的字符后,并设置会待处理内容队列
     * 
     * @param pLine
     *            内容
     * @param pIndex
     *            设置的位置
     * @param pChar
     *            设置成的字符
     */
    private void replaceCharAndSetBack(String pLine,int pIndex,char pChar){
        char[] tArrs=pLine.toCharArray();
        tArrs[pIndex]=pChar;
        this.setNextUnhandleLine(new String(tArrs));
    }

    /**
     * 读取从当前行开始的raw内容
     * 
     * @param pParentSpace
     *            父节点的空格数量
     * @return 读取的内容
     */
    private String readRawContent(int pParentSpace){
        int tMinSpaceCount=pParentSpace+1;
        StringBuilder tRawContent=new StringBuilder();
        while(this.haveUnhandleLine()){
            String tLineContent=this.getNextUnhandleLine();
            int tSpaceCount=this.getSpaceCount(tLineContent);
            if(tSpaceCount<tMinSpaceCount)
                break;

            tRawContent.append(tLineContent.substring(tMinSpaceCount)).append('\n');

            this.alreadyHandleLine();
        }
        if(tRawContent.length()!=0){
            tRawContent.setLength(tRawContent.length()-1);
        }
        return tRawContent.toString();
    }

    /**
     * 是否还有未处理的行
     */
    private boolean haveUnhandleLine(){
        return this.mLineIndex+1<this.mLines.length;
    }

    /**
     * 获取下一行非注释的未处理的行文本,请注意使用情况
     * <p>
     * 如果没有下一行了,将返回null
     * </p>
     * 
     * @return 文本或null
     */
    private String getNextUnhandleLineNoComment(){
        String tNowLine;
        while(this.haveUnhandleLine()){
            tNowLine=this.getNextUnhandleLine();
            if(this.getSpaceCount(tNowLine)==-1){
                this.alreadyHandleLine();
                this.addCacheComment(tNowLine);
            }else return tNowLine;
        }
        return null;
    }

    /**
     * 获取下一行未处理的行文本
     * <p>
     * 如果没有下一行了,将返回null
     * </p>
     * 
     * @return 文本或null
     */
    private String getNextUnhandleLine(){
        if(!this.haveUnhandleLine())
            return null;
        return this.mLines[this.mLineIndex+1];
    }

    /**
     * 设置下一行未处理文本的内容
     * 
     * @param pNewLine
     *            新的文本
     * @return 是否设置成功
     */
    private boolean setNextUnhandleLine(String pNewLine){
        if(!this.haveUnhandleLine())
            return false;
        this.mLines[this.mLineIndex+1]=pNewLine;
        return true;
    }

    /**
     * 指示已处理行号加1,并将已经处理行的源文本放入{@link Composer#mContent}中
     */
    private void alreadyHandleLine(){
        this.mLineIndex++;
        if(this.mMode==Mode.DUMP){
            this.mContent.add(this.mSourceLines[this.mLineIndex]);
        }
    }

    /**
     * 添加注释内容{@link Composer#mContent}中
     * 
     * @param pParentSpace
     *            注释行的权重
     * @param pComments
     *            注释文本
     */
    private void addCommentToContent(int pSpaceLevel,ArrayList<String> pComments){
        if(pComments==null||pComments.isEmpty())
            return;

        StringBuilder tSBuilder=new StringBuilder();
        while(pSpaceLevel-->0){
            tSBuilder.append(' ');
        }
        String tBlank=tSBuilder.append("# ").toString();

        for(String sComment : pComments){
            if(sComment.isEmpty()){
                this.mContent.add(sComment);
            }else this.mContent.add(tBlank+sComment);
        }
    }

    /**
     * 添加注释缓存到{@link Composer#mComment}中,并设置{@link Composer#mLineIndex}为该注释的所在的行数<br>
     * 当识别到Node时,再将注释缓存设置到配置管理器中
     * <p>
     * 注意此方法只在{@link Mode#LOAD}模式下调用
     * </p>
     * 
     * @param pLine
     *            行
     */
    private void addCacheComment(String pLine){
        if(this.mCommentLineIndex+1!=this.mLineIndex){
            this.mComment.clear();
        }

        String tLine=Composer.trimLeftSide(pLine);
        int tIndex=0;
        if(tLine.length()>0&&tLine.charAt(0)=='#'){
            tIndex++;
            if(tLine.length()>1&&tLine.charAt(1)==' ')
                tIndex++;
        }

        this.mComment.add(tLine.substring(tIndex));
        this.mCommentLineIndex=this.mLineIndex;
    }

    /** 缓存的空格数量 */
    private int mCachedSpaceCount=-1;
    /** 缓存的计算空格数量的字符串 */
    private String mCachedSpaceCountStr=null;

    /**
     * 获取空格数量
     * <p>
     * 如果返回-1,说明该行是注释行或者为空行
     * </p>
     * 
     * @param pLine
     *            行
     * @return 该行空格数量
     */
    private int getSpaceCount(String pLine){
        if(pLine==null||pLine.isEmpty())
            return -1;

        if(this.mCachedSpaceCountStr!=pLine){
            this.mCachedSpaceCountStr=pLine;
            int i=-1,tLen=pLine.length(),tSpaceLevel=0;
            char tChar;
            while(++i<tLen){
                tChar=pLine.charAt(i);
                if(tChar==' '){
                    tSpaceLevel++;
                }else if(tChar=='\t'){
                    tSpaceLevel+=4;
                }else if(tChar=='#'){
                    tSpaceLevel=-1;
                    break;
                }else break;
            }
            this.mCachedSpaceCount=i<tLen?tSpaceLevel:-1;
        }

        return this.mCachedSpaceCount;
    }

    private YamlNode getLineType(YamlNode pParent,String pLine){
        YamlNode tNode=new YamlNode();
        tNode.setParent(pParent);
        pLine=pLine.trim();

        if(pLine.isEmpty()||pLine.charAt(0)=='#'){
            tNode.mType=LineType.Comment;
            tNode.mValueStr=pLine;
        }else if(pLine.startsWith("- ")){
            tNode.mType=LineType.List;
        }else{
            char[] tArrs=pLine.toCharArray();
            int tIndex=0;
            char tWarpChar=tArrs[0];
            boolean tWarp=Composer.mWrapChars.contains(tWarpChar);
            boolean tNameWarp=tWarp;
            tIndex+=tWarp?1:0;
            while(tIndex<tArrs.length){
                char c=tArrs[tIndex++];
                if(tWarp){
                    if(c==tWarpChar){
                        if(tIndex>=tArrs.length)
                            break; // 没字符了,作为字符串处理
                        if(tWarpChar=='\''){
                            if(tArrs[tIndex]!=tWarpChar){
                                tWarp=false; // '后面不是单引号,warp到尾部
                            }else tIndex++; // 如果是两个单引号,跳过这个字符的检查
                        }else{ // 检查是否进行\转义
                            if(tIndex>1&&tArrs[tIndex-1]=='\\')
                                ; // \转义了
                            else tWarp=false;
                        }
                    }
                }else{
                    if(c==':'){
                        if(tIndex<tArrs.length&&tArrs[tIndex]!=' ')
                            continue;

                        int tStartIndex=tNameWarp?1:0;
                        tNode.mName=new String(tArrs,tStartIndex,Math.max(0,(tNameWarp?tIndex:tIndex-1)-tStartIndex));

                        tNode.mType=LineType.Node_Empty;
                        if(tIndex>=tArrs.length)
                            return tNode;

                        tNode.mValueStr=Composer.trimLeftSide(new String(tArrs,tIndex+1,tArrs.length-tIndex-1));
                        if(tNode.mValueStr.startsWith("#")||tNode.mValueStr.startsWith("!!")){
                            int tCharIndex=tNode.mValueStr.indexOf('#');
                            if(tCharIndex!=-1){
                                tNode.mValueStr=tNode.mValueStr.substring(tCharIndex);
                            }else{
                                tNode.mValueStr="";
                            }
                            return tNode;
                        }

                        tNode.mType=LineType.Node_Valued;
                        return tNode;
                    }
                }
            }
            tNode.mType=LineType.String;
            tNode.mValueStr=pLine;
        }
        return tNode;
    }

    private void log(String pMsg){
        String showMsg="第"+(this.mLineIndex+2)+"行配置错误,"+pMsg+",无法导入注释\n"
                +"请提供你的配置文件给作者以供分析格式\n"
                +"错误内容(不包括引号) \""+this.getNextUnhandleLine()+"\"";
        if(this.mMode==Mode.DUMP){
            while(haveUnhandleLine()){
                this.alreadyHandleLine();
            }
        }
        throw new IllegalStateException(showMsg);
    }

    private boolean isCloseLine(String pLine){
        if((pLine=pLine.trim()).isEmpty())
            return true;

        char tFirstChar=pLine.charAt(0),tLastChar=pLine.charAt(pLine.length()-1);
        if(Composer.mWrapChars.contains(tFirstChar)){
            if(pLine.length()==1)
                return false;
            if(pLine.length()==2)
                return true;
            if(tFirstChar=='\''){
                // 在内部的单引号一定是成对出现的
                pLine=pLine.substring(1,pLine.length()).replace("''","");
                return pLine.charAt(pLine.length()-1)=='\'';
            }else{
                if(pLine.charAt(pLine.length()-2)!='\\'&&tLastChar==tFirstChar){ //是一对边界符,且最后一个字符没有被转义
                    return true;
                }else{
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取关闭标识字符,如果不存在,返回null
     * 
     * @param pLine
     *            行
     * @param pFullLine
     *            是否为一个完整的行
     */
    private Character getCloseMark(String pLine,boolean pFullLine){
        if((pLine=pLine.trim()).isEmpty())
            return null;

        Character tc=pLine.charAt(pLine.length()-1);
        if(Composer.mWrapChars.contains(tc)){
            if(tc=='\''){
                int tNoBorderIndex=pLine.length()-1,tIndex;
                while(tNoBorderIndex>0&&pLine.charAt(tIndex=tNoBorderIndex-1)=='\''){
                    tNoBorderIndex=tIndex;
                }

                int tBorderAmount=pLine.length()-tNoBorderIndex;
                if(pFullLine&&tBorderAmount==0&&pLine.length()>1){
                    tBorderAmount--;
                }
                return tBorderAmount%2==0?null:tc;
            }
            return tc;
        }else return null;
    }

    /**
     * 去除左边的空格
     * 
     * @param pStr
     *            字符串
     * @return 去除左边空格后的字符串
     */
    public static String trimLeftSide(String pStr){
        int tIndex=-1,tLen=pStr.length();
        while(++tIndex<tLen&&Character.isWhitespace(pStr.charAt(tIndex)));
        return pStr.substring(tIndex);
    }

}
