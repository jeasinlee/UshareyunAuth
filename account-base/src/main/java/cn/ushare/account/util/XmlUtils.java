package cn.ushare.account.util;

import java.io.Writer;
import java.lang.reflect.Field;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.apache.commons.lang3.StringUtils;

/**
 * XML工具类
 */
public class XmlUtils {

	/**
	 * 创建XStream
	 */
	public static XStream createXstream() {
		return new XStream(new XppDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new PrettyPrintWriter(out) {
                    boolean cdata = false;
                    Class<?> targetClass = null;
                    @Override
                    public void startNode(String name,
                            @SuppressWarnings("rawtypes") Class clazz) {
                        super.startNode(name, clazz);
                        //业务处理，对于用XStreamCDATA标记的Field，需要加上CDATA标签
                        if(!name.equals("xml")){//代表当前处理节点是class，用XstreamAlias把class的别名改成xml，下面的代码片段有提到
                            cdata = needCDATA(targetClass, name);
                        }else{
                            targetClass = clazz;
                        }
                    }

                    @Override
                    protected void writeText(QuickWriter writer, String text) {
                        if (cdata) {
                            writer.write(cDATA(text));
                        } else {
                            writer.write(text);
                        }
                    }

                    private String cDATA(String text) {
    					return "<![CDATA[" + text + "]]>";
    				}
                };
            }
        });
    }

	private static boolean existsCDATA(Class<?> clazz, String fieldAlias){
        //scan fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            //1. exists XStreamCDATA
            if(field.getAnnotation(XStreamCDATA.class) != null ){
                XStreamAlias xStreamAlias = field.getAnnotation(XStreamAlias.class);
                //2. exists XStreamAlias
                if(null != xStreamAlias){
                    if(fieldAlias.equals(xStreamAlias.value()))//matched
                        return true;
                }else{// not exists XStreamAlias
                    if(fieldAlias.equals(field.getName()))
                        return true;
                }
            }
        }
        return false;
    }

	private static boolean needCDATA(Class<?> targetClass, String fieldAlias){
	    boolean cdata = false;
	    //first, scan self
	    cdata = existsCDATA(targetClass, fieldAlias);
	    if(cdata) return cdata;
	    //if cdata is false, scan supperClass until java.lang.Object
	    Class<?> superClass = targetClass.getSuperclass();
	    while(!superClass.equals(Object.class)){
	        cdata = existsCDATA(superClass, fieldAlias);
	        if(cdata) return cdata;
	        superClass = superClass.getClass().getSuperclass();
	    }
	    return false;
	}

	/**
	 * 支持注解转化XML
	 */
	public static String toXML(Object obj, Class<?> cls) {
		if (obj == null) {
			return null;
		}
		XStream xstream = createXstream();
		xstream.processAnnotations(cls);
		return getDefaultXMLHeader() + xstream.toXML(obj);
	}

	/**
	 * Object 转化 XML
	 */
	public static String toXML(Object obj) {
		if (obj == null) {
			return null;
		}
		XStream xstream = createXstream();
		return getDefaultXMLHeader() + xstream.toXML(obj);
	}

	/**
	 * XML转化为JAVA对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T xml2Obj(String xml, Class<?> cls) {
		if (StringUtils.isBlank(xml)) {
			return null;
		}
		XStream xstream = createXstream();
		if (cls != null) {
			xstream.processAnnotations(cls);
		}
		return (T) xstream.fromXML(xml);
	}

	/**
	 * XML转化为JAVA对象
	 */
	public static <T> T xml2Obj(String xml) {
		return xml2Obj(xml, null);
	}

	private static String getDefaultXMLHeader() {
		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	}

	/**
	 *
	 * @description XppDriver
	 *
	 * @author lixining
	 * @version $Id: XmlUtils.java, v 0.1 2015年8月18日 上午9:46:57 lixining Exp $
	 */
	public static class MyXppDriver extends XppDriver {
		boolean useCDATA = false;

		MyXppDriver(boolean useCDATA) {
			super(new XmlFriendlyNameCoder("__", "_"));
			this.useCDATA = useCDATA;
		}

		@Override
		public HierarchicalStreamWriter createWriter(Writer out) {
			if (!useCDATA) {
				return super.createWriter(out);
			}
			return new PrettyPrintWriter(out) {
				boolean cdata = true;

				@Override
				public void startNode(String name, @SuppressWarnings("rawtypes") Class clazz) {
					super.startNode(name, clazz);
				}

				@Override
				protected void writeText(QuickWriter writer, String text) {
					if (cdata) {
						writer.write(cDATA(text));
					} else {
						writer.write(text);
					}
				}

				private String cDATA(String text) {
					return "<![CDATA[" + text + "]]>";
				}
			};
		}
	}
}


