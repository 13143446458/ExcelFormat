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
<form id="form1" action="importExcel" enctype="multipart/form-data" method="post">
    <input class="form-input" type="file" name="filename"></input>
    <button type="submit" class="btn">导入</button>
</form>

<form id="form2" action="importExcel" enctype="multipart/form-data" method="post">
    
    <span>选择区域：</span>
    <select>
    	<option>深圳</option>
    	<option>北京</option>
    </select>
    <br/>
    <div>
	<span style="color:red">第2步：输入系统字段的起始值：</span>
	</div>
    <fieldset>
    <legend>起始值</legend>
    (单据体)部门#编码: <input type="text" name="beginId" value="">
             单据体(序号): <input type="text" name="FEntity" />
  </fieldset>
  	<div>
	<span style="color:red">第3步：导入原始数据表，点击开始转换按钮输出新的excel</span>
	</div>
    <input class="form-input" type="file" name="filename"></input>
    <button type="submit" class="btn">开始转换</button>
</form>
<span>${message}</span>
</body>
</html>