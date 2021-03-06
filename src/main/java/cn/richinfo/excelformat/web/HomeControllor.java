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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.richinfo.excelformat.service.ChangeExcelService;
import cn.richinfo.excelformat.util.ExcelImportUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *@desc
 *@author create by chenlin
 *@date 2018年7月18日--上午10:16:36
 */

@Controller
public class HomeControllor {
	@Autowired
	private ChangeExcelService changeExcelService;
	/**
	 * 首页页面跳转
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String tohomepage(ModelMap model) {
		model.addAttribute("FEXPLANATION", "项目支出调整");
		return "home";
	}
	
	/**
	 * 导入数据汇总表进行转换格式
	 * @param file
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public String importExcel(
			@RequestParam(value = "filename") MultipartFile file,
			HttpServletRequest request, HttpServletResponse response,
            RedirectAttributes attributes) throws IOException {

        String FBillHeadNo = request.getParameter("FBillHeadNo");//单据头序号，从页面接收
        String Fdate = request.getParameter("Fdate");//日期
        String FVOUCHERGROUPNO = request.getParameter("FVOUCHERGROUPNO");//单据头（凭证号）
        String FEntity = request.getParameter("FEntity");
        String FEXPLANATION = request.getParameter("FEXPLANATION");//摘要
        String organization = request.getParameter("organization");

        attributes.addFlashAttribute("Fdate", Fdate);
        attributes.addFlashAttribute("FBillHeadNo", FBillHeadNo);
        attributes.addFlashAttribute("FVOUCHERGROUPNO", FVOUCHERGROUPNO);
        attributes.addFlashAttribute("FEntity", FEntity);
        attributes.addFlashAttribute("FEXPLANATION", FEXPLANATION);
        attributes.addFlashAttribute("organization", organization);
		// 判断文件是否为空
		if (file == null) {
            attributes.addFlashAttribute("msg", "文件不能为空！");
			return "redirect:/home";
		}

		// 获取文件名
		String fileName = file.getOriginalFilename();
		String beginId = request.getParameter("beginId");
		// 验证文件名是否合格
		if (!ExcelImportUtils.validateExcel(fileName)) {
            attributes.addFlashAttribute("msg", "文件必须是excel格式！");
			return "redirect:/home";
		}

		// 进一步判断文件内容是否为空（即判断其大小是否为0或其名称是否为null）
		long size = file.getSize();
		if (StringUtils.isEmpty(fileName) || size == 0) {
            attributes.addFlashAttribute("msg", "文件不能为空！");
			return "redirect:/home";
		}
		/* 读取excel内容做转换 */
		String message = "转换成功";
		message = changeExcelService.ImportToChange(fileName, file, request, response);
        attributes.addFlashAttribute("msg", message);
        System.out.print(message);
		return "redirect:/home";
	}

	/**
	 * 导入部门及项目信息表
	 * 
	 * @param file
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/importDeptInfo", method = RequestMethod.POST)
	@ResponseBody
	public String importDeptExcel(
			@RequestParam(value = "file") MultipartFile file,
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
		// 判断文件是否为空
		if (file == null) {
			return "文件不能为空！";
		}
		// 获取文件名
		String fileName = file.getOriginalFilename();
		// 验证文件名是否合格
		if (!ExcelImportUtils.validateExcel(fileName)) {
			return "文件必须是excel格式！";
		}
		// 进一步判断文件内容是否为空（即判断其大小是否为0或其名称是否为null）
		long size = file.getSize();
		if (StringUtils.isEmpty(fileName) || size == 0) {
			return "文件不能为空！";
		}
		/* 读取excel内容做转换 */
		String message = changeExcelService.ImportDeptAndProjectData(fileName, file, request);
		return message;
	}
	 
}
