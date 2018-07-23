<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<html>
  <head>
    <meta charset="UTF-8">
    <title>数据格式转换</title>
</head>
<body>

<div>
	<span style="color:red">第1步：导入部门信息及项目信息：</span>
</div>
<form id="form1" action="importDeptInfo" enctype="multipart/form-data" method="post">
    <input class="form-input" type="file" name="filename"></input>
    <button type="submit" class="btn">导入</button>
</form>

<form id="form2" action="importExcel" enctype="multipart/form-data" method="post">
    
    <span>选择区域：</span>
    <select>
    	<option value="120102">深圳</option>
    	<option value="140101">北京</option>
    </select>
    <br/>
    <div>
	<span style="color:red">第2步：输入系统字段的起始值：</span>
	</div>
	 <fieldset>
    <legend>固定值</legend>
             单据头(日期): <input type="text" name="Fdate" />
             单据头(序号): <input type="text" name="FBillHeadNo" />
             单据体(摘要)：<input type="text" name="FEXPLANATION" value="项目支出调整">
  </fieldset>
	<!-- 固定值输入部分 -->
    <fieldset>
    <legend>起始值</legend>
             单据头(凭证号): <input type="text" name="FVOUCHERGROUPNO" />
             单据体(序号): <input type="text" name="FEntity" />
  </fieldset>
  	<div>
	<span style="color:red">第3步：导入原始数据表，点击开始转换按钮输出新的excel</span>
	</div>
    <input class="form-input" type="file" name="filename"></input>
    <button type="submit" class="btn">开始转换</button>
</form>
<div style="border: 1px solid red;width:300px;height:50px;">
	<span>系统提示：${message}</span>
</div>

</body>
</html>