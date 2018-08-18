package cn.richinfo.excelformat.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.richinfo.excelformat.util.ArithUtil;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import cn.richinfo.excelformat.util.ExcelImportUtils;
import cn.richinfo.excelformat.util.PropertyUtil;
import cn.richinfo.excelformat.util.Tools;

/**
 * @author create by chenlin
 * @desc
 * @date 2018年7月17日--下午2:21:10
 */
@Service
public class ChangeExcelService {

    private static Logger logger = Logger.getLogger(ChangeExcelService.class.getClass());

    /**
     * 上传excel文件到临时目录后并开始解析
     *
     * @param fileName
     * @param mfile
     * @param request
     * @param response
     * @return
     */
    public String ImportToChange(String fileName, MultipartFile mfile, HttpServletRequest request, HttpServletResponse response) {
        String filePath = request.getRealPath("") + "/fileDir";
        logger.info("filePath:" + filePath);
        File uploadDir = new File(filePath);
        //创建一个目录 （它的路径名由当前 File 对象指定，包括任一必须的父路径。）
        if (!uploadDir.exists()) uploadDir.mkdirs();
        //新建一个文件
        File tempFile = new File(filePath + new Date().getTime() + ".xlsx");
        //初始化输入流
        InputStream is = null;
        try {
            //将上传的文件写入新建的文件中
            mfile.transferTo(tempFile);

            //根据新建的文件实例化输入流
            is = new FileInputStream(tempFile);

            //根据版本选择创建Workbook的方式
            Workbook wb = null;
            //根据文件名判断文件是2003版本还是2007版本
            if (ExcelImportUtils.isExcel2007(fileName)) {
                wb = new XSSFWorkbook(is);
            } else {
                wb = new HSSFWorkbook(is);
            }

            //根据excel里面的内容读取信息
            return readExcelValue(wb, tempFile, request, response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();//关闭流
                } catch (IOException e) {
                    is = null;
                    e.printStackTrace();
                }
            }
        }
        return "导入出错！请检查数据格式！";
    }


    /**
     * 解析Excel里面的数据
     *
     * @param wb
     * @param tempFile
     * @param request
     * @param response
     * @return
     */
    private String readExcelValue(Workbook wb, File tempFile, HttpServletRequest request, HttpServletResponse response) {

        Map<String, String> deptInfoMap = (Map<String, String>) request.getSession().getAttribute("deptInfoMap");
        Map<String, String> projectInfoMap = (Map<String, String>) request.getSession().getAttribute("projectInfoMap");
        if (deptInfoMap == null || projectInfoMap == null) {
            return "部门信息或者项目信息为空！";
        }
        //错误信息接收器
        String errorMsg = "";
        String br = "<br/>";
        String areaId = request.getParameter("areaId");
        Map<String, String> areaMaps = new HashMap<String, String>();
        areaMaps.put("120102", "深圳");
        areaMaps.put("140101", "北京");
        String FBillHeadNo = request.getParameter("FBillHeadNo");//单据头序号，从页面接收
        int FBillHeadNoINT = Integer.parseInt(FBillHeadNo);
        String Fdate = request.getParameter("Fdate");//日期
        String FVOUCHERGROUPNO = request.getParameter("FVOUCHERGROUPNO");//单据头（凭证号）
        String FEntity = request.getParameter("FEntity");
        String FEXPLANATION = request.getParameter("FEXPLANATION");//摘要
        String organization = request.getParameter("organization");
        int entityNum = Integer.parseInt(FEntity);
        //组织机构名称对应ID的数据
        Map<String, String> organizationMaps = new HashMap<String, String>();
        organizationMaps.put("101", "深圳国际公益学院");
        organizationMaps.put("01", "深圳市亚太国际公益教育基金会");
        organizationMaps.put("102", "北京善至教育咨询有限公司");
        //账簿名称对应ID的数据
        Map<String, String> accountBookIdMaps = new HashMap<String, String>();
        accountBookIdMaps.put("101", "001");
        accountBookIdMaps.put("01", "002");
        accountBookIdMaps.put("102", "003");

        String deptName = null;//组织名称
        String deptNo = null;//组织编码
        String pzzbm = PropertyUtil.getProperty("conf.field.pzzbm", "PRE001");//默认字段：凭证字编码
        String pzzmc = PropertyUtil.getProperty("conf.field.pzzmc", "记");//默认字段：凭证字名称
        String isWb = PropertyUtil.getProperty("conf.field.wb", "False");//默认字段：外币
        String bwbbm = PropertyUtil.getProperty("conf.field.bwbbm", "PRE001");//默认字段：本位币编码
        String bwbmc = PropertyUtil.getProperty("conf.field.bwbmc", "人民币");//默认字段：本位币名称
        String cashierRecheck = PropertyUtil.getProperty("conf.field.CashierRecheck", "False");//默认字段：出纳复核操作
        int sheetNum = wb.getNumberOfSheets();//sheet页的数量
        int FVOUCHERGROUPNOINT = Integer.parseInt(FVOUCHERGROUPNO);
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        for (int s = 0; s < sheetNum; s++) {
            List<String> progectNameList = new ArrayList<String>();
            //得到第一个shell
            Sheet sheet = wb.getSheetAt(s);
            //得到Excel的行数
            int totalRows = sheet.getPhysicalNumberOfRows();
            //总列数
            int totalCells = 0;
            //得到Excel的列数(前提是有行数)，从第二行算起
            if (totalRows > 7 && sheet.getRow(7) != null) {
                totalCells = sheet.getRow(7).getPhysicalNumberOfCells();
            }
            for (int r = 7; r < totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    errorMsg += br + "第" + (r + 1) + "行数据有问题，请仔细检查！";
                    continue;
                }

                /*凭证数据部分*/
                String subjectCode = "";
                double rowTatal = 0;
                double sumCell = 0;
                //循环Excel的列
                for (int c = 0; c < totalCells; c++) {
                    Cell cell = row.getCell(c);
                    //第三列为空列，跳过
                    if (c == 3) {
                        continue;
                    }
                    if (null != cell) {
                        if (c == 0) {
                            subjectCode = cell.getStringCellValue();//科目编码
                            if ("总计".equals(subjectCode)) {
                                break;//中断内层循环
                            }
                        } else if (c >= 2) {
                            double moneyNum = cell.getNumericCellValue();//单行合计数值
                            //logger.info("原始值,行："+(r+1)+"列："+(c+1)+"值"+moneyNum);
                            moneyNum = ArithUtil.round(moneyNum,2);//保留2位
                            logger.info("保留两位后，行："+(r+1)+"列："+(c+1)+"值"+moneyNum);
                            if (c == 2) {//本行所有项目合计
                                rowTatal = moneyNum;//单行合计
                            } else {//项目列
                                sumCell = ArithUtil.add(sumCell,moneyNum);//求和
                                //最后一列的时候
                                double diffValue = 0;//差值
                                if (c + 1 == totalCells) {
                                    diffValue = ArithUtil.sub(rowTatal,sumCell);
                                    diffValue = ArithUtil.round(diffValue,2);
                                    //logger.info("补差：sheet="+(s+1)+",row="+(r+1)+",cell="+(c+1)+",diffValue="+diffValue);
                                    if (diffValue != 0) {//差值不为0
                                        for (int k = totalCells - 1; k > 3; k--) {
                                            Cell cellObj = row.getCell(k);
                                            double oldValue = cellObj.getNumericCellValue();
                                            if (oldValue > 0 || oldValue < 0) {
                                                double newValue = ArithUtil.add(oldValue,diffValue);//将差值补到不为0的单元格中
                                                cellObj.setCellValue(newValue);
                                                logger.info("补差：sheet="+(s+1)+",row="+(r+1)+",cell="+(k+1)+",diffValue="+diffValue);
                                                break;//中断循环
                                            }
                                        }

                                    }
                                }
                            }

                        }
                    }
                }
            }

            //-----------开始读取分割线----------------
            //循环Excel行数,从第二行开始。标题不入库
            for (int r = 3; r < totalRows; r++) {
                String rowMessage = "";
                Row row = sheet.getRow(r);
                if (row == null) {
                    errorMsg += br + "第" + (r + 1) + "行数据有问题，请仔细检查！";
                    continue;
                }
                /*获取部门中心及项目名称*/
                if (r == 3) {
                    deptName = row.getCell(1).getStringCellValue();
                    deptNo = deptInfoMap.get(deptName);
                    /*从第四列开始读取项目名称*/
                    for (int i = 4; i < totalCells; i++) {
                        Cell cell = row.getCell(i);
                        String progectName = cell.getStringCellValue();
                        progectNameList.add(progectName);
                    }
                }
                /*凭证数据部分*/
                if (r > 6) {
                    String subjectCode = "";
                    String subjectName = "";
                    //循环Excel的列
                    for (int c = 0; c < totalCells; c++) {
                        Cell cell = row.getCell(c);
                        //第三列为空列，跳过
                        if (c == 3) {
                            continue;
                        }
                        if (null != cell) {
                            if (c == 0) {
                                subjectCode = cell.getStringCellValue();//科目编码
                                if ("总计".equals(subjectCode)) {
                                    break;//中断内层循环
                                }
                            } else if (c == 1) {
                                subjectName = cell.getStringCellValue();//科目名称
                            } else if (c >= 2) {
                                Map<String, Object> data = new HashMap<String, Object>();
                                String projectName = "";
                                String projectNo = "";
                                double moneyNum = cell.getNumericCellValue();//单行合计数值

                                if (c == 2) {//本行所有项目合计
                                    if ("家族传承中心".equals(deptName)) {
                                        projectName = "家族慈善传承中心其他";
                                    } else {
                                        projectName = deptName + "其他";//合计行项目名称
                                    }
                                    moneyNum = moneyNum * (-1);//取反
                                } else {//项目列
                                    projectName = progectNameList.get(c - 4);//通过列索引取得对应项目名
                                }
                                //金额为0不生成凭证
                                if (moneyNum == 0) {
                                    continue;
                                }
                                projectNo = projectInfoMap.get(projectName);//获取项目编码
                                /*公共部分*/
                                data.put("FBillHead(GL_VOUCHER)", FBillHeadNoINT);//单据头序号
                                data.put("FAccountBookID", accountBookIdMaps.get(organization));//账簿编码
                                data.put("FAccountBookID#Name", organizationMaps.get(organization));//账簿名称
                                data.put("FDate", Fdate);//日期
                                data.put("FVOUCHERGROUPID", pzzbm);//凭证字编码
                                data.put("FVOUCHERGROUPID#Name", pzzmc);//凭证字名称
                                data.put("FVOUCHERGROUPNO", FVOUCHERGROUPNOINT);//*(单据头)凭证号
                                data.put("FISFOREIGNCUR", isWb);//外币
                                data.put("FBASECURRENCYID", bwbbm);//本位币编码
                                data.put("FBASECURRENCYID#Name", bwbmc);//本位币名称
                                data.put("FCashierRecheck", cashierRecheck);//出纳复核操作
                                data.put("FCreateDate", Fdate);//创建日期
                                data.put("FCancleRecheck", "False");//取消复核操作
                                data.put("FACCBOOKORGID", organization);//(单据头)核算组织#编码
                                data.put("FACCBOOKORGID#Name", organizationMaps.get(organization));//(单据头)核算组织#名称
                                data.put("FIsQty", "False");//(单据头)数量金额核算
                                data.put("FEntity", entityNum);//单据体序号
                                data.put("FEXPLANATION", FEXPLANATION);//(单据体)摘要
                                data.put("FACCOUNTID", subjectCode);//科目编码
                                data.put("FACCOUNTID#Name", subjectName);//科目名称
                                data.put("FDetailID#FF100002", projectNo);//项目编码
                                data.put("FDetailID#FF100002#Name", projectName);//项目名称
                                data.put("FDetailID#FF100003", areaId);//区域编码
                                data.put("FDetailID#FF100003#Name", areaMaps.get(areaId));//区域名称
                                data.put("FDetailID#FFlex5", deptNo);//部门编码
                                data.put("FDetailID#FFlex5#Name", deptName);//部门名称
                                data.put("FCURRENCYID", "PRE001");//*(单据体)币别#编码
                                data.put("FCURRENCYID#Name", "人民币");//(单据体)币别#名称
                                data.put("FEXCHANGERATETYPE", "HLTX01_SYS");//*(单据体)汇率类型#编码
                                data.put("FEXCHANGERATETYPE#Name", "固定汇率");//(单据体)汇率类型#名称
                                data.put("FEXCHANGERATE", "1");//(单据体)汇率
                                data.put("FAMOUNTFOR", moneyNum);//(单据体)原币金额
                                data.put("FDEBIT", moneyNum);//(单据体)借方金额
                                dataList.add(data);
                                entityNum++;
                            }
                        } else {
                            rowMessage += "第" + (c + 1) + "列数据有问题，请仔细检查；";
                        }
                    }

                    //拼接每行的错误提示
                    if (!StringUtils.isEmpty(rowMessage)) {
                        errorMsg += br + "第" + (r + 1) + "行，" + rowMessage;
                    } else {

                    }

                }

            }
            FVOUCHERGROUPNOINT++;
            FBillHeadNoINT++;
        }


        /* 输出excel格式错误信息*/
        if (!StringUtils.isEmpty(errorMsg)) {
            return errorMsg;
        }

        //输出excel文件名
        String newFileName = "模板表--凭证.xls";
        HSSFWorkbook book = new HSSFWorkbook();// 创建Excel文件
        HSSFSheet newSheet = book.createSheet("凭证#单据头(FBillHead)"); // 创建一个工作薄
        newSheet.createFreezePane(2, 2, 3, 3);
        //设置样式-颜色
        HSSFCellStyle style = book.createCellStyle();
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        HSSFRow row1 = newSheet.createRow(0);
        //row1.setHeight((short) 500);// 设置行高
        String[] row1Array = {"FBillHead(GL_VOUCHER)",
                "FAccountBookID",
                "FAccountBookID#Name",
                "FDate",
                "FVOUCHERGROUPID",
                "FVOUCHERGROUPID#Name",
                "FVOUCHERGROUPNO",
                "FISFOREIGNCUR",
                "FBASECURRENCYID",
                "FBASECURRENCYID#Name",
                "FCashierRecheck",
                "FCreateDate",
                "FIsSplit",
                "FCancleRecheck",
                "FACCBOOKORGID",
                "FACCBOOKORGID#Name",
                "FAuditDate",
                "FIsQty",
                "FModifierId",
                "FModifierId#Name",
                "FModifyDate",
                "*Split*1",
                "FEntity",
                "FEXPLANATION",
                "FACCOUNTID",
                "FACCOUNTID#Name",
                "FDetailID#FF100002",
                "FDetailID#FF100002#Name",
                "FDetailID#FFLEX11",
                "FDetailID#FFLEX11#Name",
                "FDetailID#FFlex10",
                "FDetailID#FFlex10#Name",
                "FDetailID#FF100006",
                "FDetailID#FF100006#Name",
                "FDetailID#FF100004",
                "FDetailID#FF100004#Name",
                "FDetailID#FF100003",
                "FDetailID#FF100003#Name",
                "FDetailID#FFLEX9",
                "FDetailID#FFLEX9#Name",
                "FDetailID#FFlex5",
                "FDetailID#FFlex5#Name",
                "FDetailID#FFlex4",
                "FDetailID#FFlex4#Name",
                "FDetailID#FFlex8",
                "FDetailID#FFlex8#Name",
                "FDetailID#FFlex7",
                "FDetailID#FFlex7#Name",
                "FDetailID#FFlex6",
                "FDetailID#FFlex6#Name",
                "FCURRENCYID",
                "FCURRENCYID#Name",
                "FEXCHANGERATETYPE",
                "FEXCHANGERATETYPE#Name",
                "FEXCHANGERATE",
                "FUnitId",
                "FUnitId#Name",
                "FPrice",
                "FQty",
                "FAMOUNTFOR",
                "FDEBIT",
                "FCREDIT",
                "FISMULTICOLLECT",
                "FOldEntryId"};
        for (int i = 0; i < row1Array.length; i++) {
            newSheet.setColumnWidth(i, 20 * 256);
            HSSFCell cell = row1.createCell(i);
            cell.setCellStyle(style);
            cell.setCellValue(row1Array[i]);
        }
        HSSFRow row2 = newSheet.createRow(1);
        //row2.setHeight((short) 500);// 设置行高
        String[] row2Array = {"*单据头(序号)",
                "*(单据头)账簿#编码	",
                "(单据头)账簿#名称",
                "*(单据头)日期",
                "*(单据头)凭证字#编码",
                "(单据头)凭证字#名称",
                "*(单据头)凭证号",
                "(单据头)外币",
                "(单据头)本位币(辅助)#编码",
                "(单据头)本位币(辅助)#名称",
                "(单据头)出纳复核操作(辅助)",
                "(单据头)创建日期",
                "(单据头)是否拆分",
                "(单据头)取消复核操(作辅助)",
                "(单据头)核算组织#编码",
                "(单据头)核算组织#名称",
                "(单据头)审核日期",
                "(单据头)数量金额核算",
                "(单据头)修改人#编码",
                "(单据头)修改人#名称",
                "(单据头)修改日期",
                "间隔列",
                "*单据体(序号)",
                "(单据体)摘要",
                "*(单据体)科目编码#编码",
                "(单据体)科目编码#名称",
                "(单据体)项目段#编码",
                "(单据体)项目段#名称(Null)",
                "(单据体)组织机构#编码",
                "(单据体)组织机构#名称(Null)",
                "(单据体)资产类别#编码",
                "(单据体)资产类别#名称(Null)",
                "(单据体)其他往来单位#编码",
                "(单据体)其他往来单位#名称(Null)",
                "(单据体)捐赠方段#编码",
                "(单据体)捐赠方段#名称(Null)",
                "(单据体)区域#编码",
                "(单据体)区域#名称(Null)",
                "(单据体)费用项目#编码",
                "(单据体)费用项目#名称(Null)",
                "(单据体)部门#编码",
                "(单据体)部门#名称(Null)",
                "(单据体)供应商#编码",
                "(单据体)供应商#名称(Null)",
                "(单据体)物料#编码",
                "(单据体)物料#名称(Null)",
                "(单据体)员工#编码",
                "(单据体)员工#名称(Null)",
                "(单据体)客户#编码",
                "(单据体)客户#名称(Null)",
                "*(单据体)币别#编码",
                "(单据体)币别#名称",
                "*(单据体)汇率类型#编码",
                "(单据体)汇率类型#名称",
                "(单据体)汇率",
                "(单据体)单位#编码",
                "(单据体)单位#名称",
                "(单据体)单价",
                "(单据体)数量",
                "(单据体)原币金额",
                "(单据体)借方金额",
                "(单据体)贷方金额",
                "(单据体)是否参与多栏账汇总",
                "(单据体)上移下移之前的分录内码"};
        for (int i = 0; i < row2Array.length; i++) {
            HSSFCell cell = row2.createCell(i);
            cell.setCellStyle(style);
            cell.setCellValue(row2Array[i]);
        }
        HSSFCellStyle cellStyle1 = book.createCellStyle();
        cellStyle1.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
        HSSFCellStyle cellStyle2 = book.createCellStyle();
        HSSFDataFormat df = book.createDataFormat();
        cellStyle2.setDataFormat(df.getFormat("#,##0.0"));
        for (int i = 0; i < dataList.size(); i++) {
            Row row = newSheet.createRow(i + 2);
            for (int j = 0; j < row1Array.length; j++) {
                Cell cell = row.createCell(j);
                String cellValue = "" + dataList.get(i).get(row1Array[j]);
                if ("null".equals(cellValue)) {
                    cellValue = "";
                }

                if ("FAMOUNTFOR".equals(row1Array[j])) {
                    cell.setCellStyle(cellStyle1);
                    cell.setCellValue(Double.parseDouble(cellValue));

                } else if ("FDEBIT".equals(row1Array[j])) {
                    cell.setCellStyle(cellStyle1);
                    cell.setCellValue(Double.parseDouble(cellValue));
                } else {
                    cell.setCellValue(cellValue);
                }

            }

        }

        newFileName = Tools.processFileName(request, newFileName);// 不同浏览器文件名乱码解决
        try {
            OutputStream os = response.getOutputStream();// 取得输出流
            response.reset();// 清空输出流
            response.setHeader("Connection", "close");
            response.setHeader("Content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + newFileName);
            book.write(os);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //删除上传的临时文件
        if (tempFile.exists()) {
            tempFile.delete();
        }
        return errorMsg;
    }

    /**
     * 导入部门及项目信息表数据
     *
     * @param fileName
     * @param mfile
     * @param request
     * @return
     */
    public String ImportDeptAndProjectData(String fileName, MultipartFile mfile, HttpServletRequest request) {

        String filePath = request.getRealPath("") + "/fileDir";
        logger.info("filePath:" + filePath);
        File uploadDir = new File(filePath);
        //创建一个目录 （它的路径名由当前 File 对象指定，包括任一必须的父路径。）
        if (!uploadDir.exists()) uploadDir.mkdirs();
        //新建一个文件
        File tempFile = new File(filePath + new Date().getTime() + ".xls");
        //初始化输入流
        InputStream is = null;
        try {
            //将上传的文件写入新建的文件中
            mfile.transferTo(tempFile);
            //根据新建的文件实例化输入流
            is = new FileInputStream(tempFile);

            //根据版本选择创建Workbook的方式
            Workbook wb = null;
            //根据文件名判断文件是2003版本还是2007版本
            if (ExcelImportUtils.isExcel2007(fileName)) {
                wb = new XSSFWorkbook(is);
            } else {
                wb = new HSSFWorkbook(is);
            }
            //根据excel里面的内容读取知识库信息
            //得到第一个shell
            Sheet sheet1 = wb.getSheetAt(0);//部门信息sheet
            String sheetName = sheet1.getSheetName();
            if (!"部门信息".equals(sheetName)) {
                return "第一个sheet页名称必须为部门信息";
            }
            int totalRows = sheet1.getPhysicalNumberOfRows();
            Map<String, String> deptInfoMap = new HashMap<String, String>();
            for (int i = 1; i < totalRows; i++) {
                Row row = sheet1.getRow(i);
                String deptNo = row.getCell(0).getStringCellValue();//部门编码
                String deptName = row.getCell(2).getStringCellValue();//部门名称
                deptInfoMap.put(deptName, deptNo);//放入到map中
            }
            //得到第二个shell
            Sheet sheet2 = wb.getSheetAt(1);//项目信息sheet
            sheetName = sheet2.getSheetName();
            if (!"项目信息".equals(sheetName)) {
                return "第二个sheet页名称必须为项目信息";
            }
            int totalRows2 = sheet2.getPhysicalNumberOfRows();
            Map<String, String> projectInfoMap = new HashMap<String, String>();
            for (int i = 1; i < totalRows2; i++) {
                Row row = sheet2.getRow(i);
                String projectNo = row.getCell(0).getStringCellValue();//项目编码
                String projectName = row.getCell(1).getStringCellValue();//项目名称
                projectInfoMap.put(projectName, projectNo);//放入到map中
            }
            HttpSession session = request.getSession();
            session.setAttribute("deptInfoMap", deptInfoMap);
            session.setAttribute("projectInfoMap", projectInfoMap);
            return "部门信息导入成功";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //删除上传的临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    is = null;
                    e.printStackTrace();
                }
            }
        }
        return "导入出错！请检查数据格式！";
    }

}
