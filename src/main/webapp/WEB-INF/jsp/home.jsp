<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<html>
<head>
    <meta charset="UTF-8">
    <title>数据格式转换</title>
    <script src="https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.2.1.min.js"></script>
    <script src="https://cdn.bootcss.com/json2/20150503/json2.js"></script>
    <link rel="stylesheet" href="js/layui/css/layui.css" media="all">
    <script src="js/layui/layui.js"></script>
    <script src="js/home.js"></script>
</head>
<body>

<div>
    <span style="color:red">第1步：导入部门信息及项目信息：（导入一次后可以缓存三小时，不用每次转换都导入）</span>
</div>
<form id="form1" >
    <input style="height:38px;" id="file" type="file" name="file"></input>
    <button id="btn1"  type="button" class="btn">导入</button>
</form>

<form id="form2" target="_self" action="importExcel" enctype="multipart/form-data" method="post">

    <span>选择区域：</span>
    <select style="height:38px;"  name="areaId">
        <option value="120102">深圳</option>
        <option value="140101">北京</option>
    </select>
    <br/>
    <div>
        <span style="color:red">第2步：输入系统字段的起始值：</span>
    </div>
    <fieldset>
        <legend>固定值</legend>
        单据头(日期):
        <div class="layui-inline">
            <input style="width:180px;" value="${Fdate}" autocomplete="off" placeholder="请选择日期" type="text" class="layui-input" name="Fdate" id="date1">
        </div>

        单据体(摘要)：
        <div class="layui-inline">
            <input  type="text" value="${FEXPLANATION}" name="FEXPLANATION" placeholder="请输入"  autocomplete="off" class="layui-input">
        </div>
        <%--<input style="width:180px;height: 38px;" type="text" name="FEXPLANATION" value="项目支出调整">--%>
        <span>选择机构：</span>
        <select style="height:38px;" name="organization" value="${organization}">
            <option value="101">深圳国际公益学院</option>
            <option value="01">深圳市亚太国际公益教育基金会</option>
            <option value="102">北京善至教育咨询有限公司</option>
        </select>

    </fieldset>
    <!-- 起始值输入部分 -->
    <fieldset>
        <legend>起始值</legend>
        单据头(序号):
        <div class="layui-inline">
            <input id="FBillHeadNo" value="${FBillHeadNo}" onkeyup="value=value.replace(/[^\d]/g,'')" type="text" name="FBillHeadNo" placeholder="请输入" autocomplete="off" class="layui-input">
        </div>
        单据头(凭证号):
        <div class="layui-inline">
            <input id="FVOUCHERGROUPNO" value="${FVOUCHERGROUPNO}" onkeyup="value=value.replace(/[^\d]/g,'')" type="text" autocomplete="off" name="FVOUCHERGROUPNO" placeholder="请输入起始值" class="layui-input"/>
        </div>
        单据体(序号):
        <div class="layui-inline">
            <input id="FEntity" value="${FEntity}" type="text" name="FEntity" onkeyup="value=value.replace(/[^\d]/g,'')" autocomplete="off" placeholder="请输入起始值" class="layui-input" />
        </div>
    </fieldset>
    <div>
        <span style="color:red">第3步：导入原始数据表，点击开始转换按钮输出新的excel</span>
    </div>
    <br/>
    <input class="form-input" type="file" name="filename"></input>
    <button id="btn2" type="button" class="btn">开始转换</button>
    <div id="msgDiv" style="border: 1px solid red;width:500px;height:50px;display: none">
        <span>系统提示：${msg}</span>
        <input id="msg" type="hidden" value="${msg}"/>
    </div>

</form>

<script>
    layui.use('laydate', function(){
        var laydate = layui.laydate;

        //执行一个laydate实例
        laydate.render({
            elem: '#date1' //指定元素
        });
    });
    layui.use('layer', function(){
        var layer = layui.layer;
    });
</script>
</body>
</html>