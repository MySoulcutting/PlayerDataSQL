package cc.bukkitPlugin.commons.nmsutil.nbt.exception;

public class NBTSerializeException extends Exception{

    public NBTSerializeException(String pErrorMsg){
        super(pErrorMsg);
    }

    public NBTSerializeException(Throwable pCause){
        super(pCause);
    }

    public NBTSerializeException(String pErrorMsg,Throwable pCause){
        super(pErrorMsg,pCause);
    }

}
