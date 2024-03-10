package cc.bukkitPlugin.commons.nmsutil.nbt.exception;

public class NBTDeserializeException extends Exception{

    public NBTDeserializeException(String pErrorMsg){
        super(pErrorMsg);
    }

    public NBTDeserializeException(Throwable pCause){
        super(pCause);
    }

    public NBTDeserializeException(String pErrorMsg,Throwable pCause){
        super(pErrorMsg,pCause);
    }

}
