package cn.ushare.account.util;

/** 
 *    
 * 描述：根据某个cidr IP段，查找相应起始结束ip
 *  
 */  
public class CIDRIpUtil {  
    
    /* 
     * 初始化CIDR IP范围 
     * @param cidrIp 例如：x.x.x.x/n 
     */  
    public static String[] initCidrIpBlock(String cidrIp){  
        if(cidrIp==null||"".equals(cidrIp.trim())){  
            throw new RuntimeException("["+cidrIp+"]参数格式不正确，CIDR地址部分为空");  
        }  
        String[] ipIds = cidrIp.split("\\/");  
        if(ipIds==null||ipIds.length!=2){  
            throw new RuntimeException("["+cidrIp+"]参数格式不正确，CIDR地址格式不正确，正确格式为x.x.x.x/n");  
        }  
        int num = Integer.parseInt(ipIds[1]);  
        if(num>32||num<4){  
            throw new RuntimeException("["+cidrIp+"]参数格式不正确，CIDR地址格式不正确，网络ID值必须在（4,32）范围内");  
        }  
         
        String networkId =  getNetworkId(cidrIp);  
        String maxIpAddres =  getMaxIpAddres(networkId,  getMaskRevert(num));  
        String[] ipArray =   new String[2]  ;
        ipArray[0] = networkId;
        ipArray[1] = maxIpAddres;
        return  ipArray;
    }  
    /* 
     * 获取网络ID，即也是CIDR表示的最小IP 
     * @param ipCidr CIDR法表示的IP，例如：172.16.0.0/12 
     * @return 网络ID，即也是CIDR表示的最小IP 
     */  
    private static String getNetworkId(String ipCidr){  
        String[] ipMaskLen = ipCidr.split("\\/");  
        String mask =  getMask(Integer.parseInt(ipMaskLen[1]));  
        String[] ips = ipMaskLen[0].split("\\.");  
        String[] masks = mask.split("\\.");  
        StringBuffer sb = new StringBuffer();  
        for(int i=0;i<4;i++){  
            sb.append(Integer.parseInt(ips[i])&Integer.parseInt(masks[i]));  
            if(i!=3){  
                sb.append(".");  
            }  
        }  
        return sb.toString();  
    }  
    /* 
     * 获取掩码 
     * @param maskLength 网络ID位数 
     * @return 
     */  
    private static String getMask(int maskLength){  
        int binaryMask = 0xFFFFFFFF << (32 - maskLength);  
        StringBuffer sb = new StringBuffer();  
        for(int shift=24;shift>0;shift-=8){  
            sb.append(Integer.toString((binaryMask>>>shift)&0xFF));  
            sb.append(".");  
        }  
        sb.append(Integer.toString(binaryMask&0xFF));  
        return sb.toString();  
    }  
    /* 
     * 获取IP最大值 
     * @param netId 网络ID 
     * @param maskReverse 掩码反码 
     * @return 
     */  
    private static String getMaxIpAddres(String netId,String maskReverse){  
        String[] netIdArray = netId.split("\\.");  
        String[] maskRevertArray = maskReverse.split("\\.");  
        StringBuffer sb = new StringBuffer();  
        for(int i=0,len=netIdArray.length;i<len;i++){  
            sb.append(Integer.parseInt(netIdArray[i])+Integer.parseInt(maskRevertArray[i]));  
            if(i!=len-1){  
                sb.append(".");  
            }  
        }  
        return sb.toString();  
    }  
     
    /* 
     * 获取掩码的反码  
     * @param maskLength 网络ID位数 
     * @return 
     */  
    private static String getMaskRevert(int maskLength){  
        int binaryMask = 0xFFFFFFFF << (32 - maskLength);  
        binaryMask = binaryMask ^ 0xFFFFFFFF;  
        StringBuffer sb = new StringBuffer(15);  
        for(int shift=24;shift>0;shift-=8){  
            sb.append(Integer.toString((binaryMask>>>shift)&0xFF));  
            sb.append(".");  
        }  
        sb.append(Integer.toString(binaryMask&0xFF));  
        return sb.toString();  
    }  
       
    /** 
     * @param args 
     */  
   public static void main(String[] args) {  
        // TODO Auto-generated method stub  
        String ipConfig = "220.170.51.31/32";  
        System.out.println(CIDRIpUtil.initCidrIpBlock(ipConfig)[0]);

        System.out.println(CIDRIpUtil.initCidrIpBlock(ipConfig)[1]);
    }  
  
}  