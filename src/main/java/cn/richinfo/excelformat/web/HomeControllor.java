package cn.richinfo.excelformat.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import cn.richinfo.excelformat.service.ChangeExcelService;
import cn.richinfo.excelformat.util.ExcelImportUtils;

/**
 *@desc
 *@author create by chenlin
 *@date 2018年7月18日--上午10:16:36
 */

@Controller
public class HomeControllor {
	@Autowired
	private ChangeExcelService changeExcelService;
	
	 @RequestMapping(value = "/home", method = RequestMethod.GET)
	   public String tohomepage(ModelMap model) {
	      model.addAttribute("message", "");
	      return "home";
	   }
	 
	 @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	    public String importExcel(@RequestParam(value="filename") MultipartFile file,
	                              HttpServletRequest request,HttpServletResponse response,ModelMap model
	                              ) throws IOException {

	        //判断文件是否为空
	        if(file==null){
	        	model.addAttribute("message","文件不能为空！");
	            return "home";
	        }

	        //获取文件名
	        String fileName=file.getOriginalFilename();
	        String beginId = request.getParameter("beginId");
	        //验证文件名是否合格
	        if(!ExcelImportUtils.validateExcel(fileName)){
	        	model.addAttribute("message","文件必须是excel格式！");
	            return "home";
	        }

	        //进一步判断文件内容是否为空（即判断其大小是否为0或其名称是否为null）
	        long size=file.getSize();
	        if(StringUtils.isEmpty(fileName) || size==0){
	        	model.addAttribute("message","文件不能为空！");
	            return "home";
	        }
	        /*读取excel内容做转换*/
	        String message ="转换成功";
	        message = changeExcelService.ImportToChange(fileName, file,request,response);
	        model.addAttribute("message",message);
	        return "home";
	    }
	 
	 
}
