package cn.richinfo.excelformat.util;

import javax.servlet.http.HttpServletRequest;

/**
 *@desc
 *@author create by chenlin
 *@date 2018年7月18日--下午2:41:25
 */

public class Tools {
	 /**
	  * 不同浏览器的文件下载名字符集编码处理
	  * @param request
	  * @param fileNames
	  * @return
	  */
	 public static String processFileName(HttpServletRequest request, String fileNames) {
	        String codedfilename = null;
	        try {
	            String agent = request.getHeader("USER-AGENT");
	            if (null != agent && -1 != agent.indexOf("MSIE") || null != agent
	                    && -1 != agent.indexOf("Trident")) {// ie

	                String name = java.net.URLEncoder.encode(fileNames, "UTF8");

	                codedfilename = name;
	            } else if (null != agent && -1 != agent.indexOf("Mozilla")) {// 火狐,chrome等


	                codedfilename = new String(fileNames.getBytes("UTF-8"), "iso-8859-1");
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return codedfilename;
	    }
}
