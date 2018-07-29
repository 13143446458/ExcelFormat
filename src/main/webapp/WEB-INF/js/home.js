$(document).ready(function () {
    $("#btn1").on("click", function(){
        var formData = new FormData($("#form1")[0]);
        formData.append("file", $("#file")[0].files);
        $.ajax({
            url: "/importDeptInfo",
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function (returndata) {
                console.log(JSON.stringify(returndata));
                layer.msg(returndata.responseText);
            },
            error: function (returndata) {
                console.log(JSON.stringify(returndata));
                layer.msg(returndata.responseText);
            }
        });
    });

    //  按钮2事件
    $("#btn2").on("click", function(){
        var fdate = $("#date1").val();
        var FBillHeadNo = $("#FBillHeadNo").val();
        var FVOUCHERGROUPNO = $("#FVOUCHERGROUPNO").val();
        var FEntity = $("#FEntity").val();
        if(fdate ==""){
            layer.msg("日期不能为空！");
            return;
        }else if (FBillHeadNo ==""){
            layer.msg("单据头(序号)不能为空！");
            return;
        } else if (FVOUCHERGROUPNO ==""){
            layer.msg("单据头(凭证号)不能为空！");
            return;
        } else if(FEntity ==""){
            layer.msg("单据体(序号)不能为空！");
            return;
        }
        var form = $("#form2")[0];
        form.submit();
    });

    var message = $("#msg").val();
    if(message !=""){
        //$("#msgDiv").css("display","");
        alert(message);
    }

});

