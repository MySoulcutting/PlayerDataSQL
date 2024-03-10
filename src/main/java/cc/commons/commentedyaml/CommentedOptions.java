package cc.commons.commentedyaml;

import org.yaml.snakeyaml.DumperOptions;

public class CommentedOptions extends DumperOptions{

    private char mPathSeparator='.';
    private boolean mSaveComment=true;
    private boolean mBackupOnFormatError=true;

    protected CommentedOptions(){}

    public char pathSeparator(){
        return this.mPathSeparator;
    }

    public void pathSeparator(char value){
        this.mPathSeparator=value;
    }

    public boolean isEnableComment(){
        return this.mSaveComment;
    }

    /**
     * 启用或停用配置文件的注释
     * 
     * @return 之前的注释启用状态
     */
    public boolean enabelComment(boolean pEnable){
        boolean oldStatus=this.mSaveComment;
        this.mSaveComment=pEnable;
        return oldStatus;
    }

    public boolean isBackupOnFormatError(){
        return this.mSaveComment;
    }
    
    /**
     * 启用或禁用在文件格式错误时的备份
     * 
     * @param pEnable
     *            启用或禁用
     * @return 之前的状态
     */
    public boolean backupOnFormatError(boolean pEnable){
        boolean oldStatus=this.mBackupOnFormatError;
        this.mBackupOnFormatError=pEnable;
        return oldStatus;
    }

}
