package cc.commons.util;

public class ByteUtil{

    private static final char[] mHexChars={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    /**
     * 转换字节到Hex字符串
     * 
     * @param pByteData
     *            要转换的字节数据
     * @return 转换后的Hex字符串
     */
    public static String byteToHex(byte[] pByteData){
        StringBuilder tSB=new StringBuilder();
        for(byte sB : pByteData){
            tSB.append(ByteUtil.mHexChars[(sB>>4)&0x0F]);
            tSB.append(ByteUtil.mHexChars[sB&0x0F]);
        }
        return tSB.toString();
    }

    /**
     * 还原Hex字符串为byte字节数据
     * 
     * @param pHexData
     *            Hex字符串
     * @return 还原后的字节数据
     * @throws IllegalArgumentException
     *             输入的Hex字符串数据长度不是偶数
     */
    public static byte[] hexToByte(String pHexData){
        if(pHexData==null||pHexData.length()==0)
            return new byte[0];
        if(pHexData.length()%2!=0)
            throw new IllegalArgumentException("length must be even");
        byte[] tByteData=new byte[pHexData.length()/2];
        char[] tChars=pHexData.toCharArray();
        int[] tSigleByte=new int[2];
        for(int i=0,c=0;i<tChars.length;i+=2,c++){
            for(int j=0;j<2;j++){
                if(tChars[i+j]>='0'&&tChars[i+j]<='9'){
                    tSigleByte[j]=(tChars[i+j]-'0');
                }else if(tChars[i+j]>='A'&&tChars[i+j]<='F'){
                    tSigleByte[j]=(tChars[i+j]-'A'+10);
                }else if(tChars[i+j]>='a'&&tChars[i+j]<='f'){
                    tSigleByte[j]=(tChars[i+j]-'a'+10);
                }
            }
            tSigleByte[0]=(tSigleByte[0]&0x0f)<<4;
            tSigleByte[1]=(tSigleByte[1]&0x0f);
            tByteData[c]=(byte)(tSigleByte[0]|tSigleByte[1]);
        }
        return tByteData;
    }

    private static char[] m6ByteToChar=new char[64];
    static{
        int i=0;
        for(char c='A';c<='Z';c++)
            m6ByteToChar[i++]=c;
        for(char c='a';c<='z';c++)
            m6ByteToChar[i++]=c;
        for(char c='0';c<='9';c++)
            m6ByteToChar[i++]=c;
        m6ByteToChar[i++]='+';
        m6ByteToChar[i++]='/';
    }

    private static byte[] mCharTo6Byte=new byte[128];
    static{
        for(int i=0;i<mCharTo6Byte.length;i++)
            mCharTo6Byte[i]=-1;
        for(int i=0;i<64;i++)
            mCharTo6Byte[m6ByteToChar[i]]=(byte)i;
    }

    public static String byteToBase64(byte[] pByteData){
        return byteToBase64(pByteData,0,pByteData.length);
    }

    public static String byteToBase64(byte[] pByteData,int pEncodeLen){
        return byteToBase64(pByteData,0,pEncodeLen);
    }

    public static String byteToBase64(byte[] pByteData,int pOffset,int pEncodeLen){
        int oDataLen=(pEncodeLen*4+2)/3;
        StringBuilder tSB=new StringBuilder();
        int tEndIndex=pOffset+pEncodeLen;
        int tFillIndex=0;
        while(pOffset<tEndIndex){
            int i0=pByteData[pOffset++]&0xFF;
            int i1=pOffset<tEndIndex?pByteData[pOffset++]&0xFF:0;
            int i2=pOffset<tEndIndex?pByteData[pOffset++]&0xFF:0;
            tSB.append(m6ByteToChar[i0>>>2]);
            tSB.append(m6ByteToChar[((i0&3)<<4)|(i1>>>4)]);
            tFillIndex+=2;
            tSB.append(tFillIndex<oDataLen?m6ByteToChar[((i1&0xf)<<2)|(i2>>>6)]:'=');
            tFillIndex++;
            tSB.append(tFillIndex<oDataLen?m6ByteToChar[i2&0x3F]:'=');
            tFillIndex++;
        }
        return tSB.toString();
    }

    public static byte[] base64ToByte(String s){
        char[] tDecodeChars=s.toCharArray();
        int tDecodeLen=tDecodeChars.length;
        if(tDecodeLen%4!=0){
            throw new IllegalArgumentException("Length of Base64 string is not a multiple of 4.");
        }

        while(tDecodeLen>0&&tDecodeChars[tDecodeLen-1]=='=')
            tDecodeLen--;
        int tOriginLen=(tDecodeLen*3)/4;
        byte[] tOriginByte=new byte[tOriginLen];
        int tOpIndex=0;
        int tFillIndex=0;
        while(tOpIndex<tDecodeLen){
            int i0=tDecodeChars[tOpIndex++];
            int i1=tDecodeChars[tOpIndex++];
            int i2=tOpIndex<tDecodeLen?tDecodeChars[tOpIndex++]:'A';
            int i3=tOpIndex<tDecodeLen?tDecodeChars[tOpIndex++]:'A';
            if(i0>127||i1>127||i2>127||i3>127)
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            int b0=mCharTo6Byte[i0];
            int b1=mCharTo6Byte[i1];
            int b2=mCharTo6Byte[i2];
            int b3=mCharTo6Byte[i3];
            if(b0<0||b1<0||b2<0||b3<0){
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            tOriginByte[tFillIndex++]=(byte)((b0<<2)|(b1>>>4));
            if(tFillIndex<tOriginLen){
                tOriginByte[tFillIndex++]=(byte)(((b1&0xf)<<4)|(b2>>>2));
            }
            if(tFillIndex<tOriginLen){
                tOriginByte[tFillIndex++]=(byte)(((b2&3)<<6)|b3);
            }
        }
        return tOriginByte;
    }

}
