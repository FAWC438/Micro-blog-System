$(document).on("keyup", "#weiboText-div",
    function () {
        $("#weiboText").val($(this).text());
    });

$("#welcome-title").click(function () {
    window.location.href = '/selfWeibo'
});

$("#img-icon").click(function () {
    console.log("click!!!");
    document.getElementById("upload-img").click();
});
$("#login-btn").click(function () {
    window.location.href = '/login'
});

$("#logout-btn").click(function () {
    $.ajax({
        url: "/logout",
        type: "get",
        success: function () {
            alert("注销成功！")
            location.reload();
        },
        error: function () {
            alert("ajax错误！")
        }
    })
});

$("#reset-password-btn").click(function () {
    $.ajax({
        url: "/resetMailSend",
        type: "get",
        success: function (data) {
            alert(data)
        },
        error: function () {
            alert("ajax错误！")
        }
    })
});

const at_reg = /@[\u4e00-\u9fa5_a-zA-Z0-9]+\s/g;

$('#weiboText-div').keypress(function (event) {
    const keycode = (event.keyCode ? event.keyCode : event.which);
    if (keycode === '13') {
        $(this).append("<br><br>")
    }
});
let reg_num = 0
$("#weiboText-div").focus(function () {
    $(this).bind("input propertychange", function () {
        const input_content = $(this).text();
        // $(function () {
        //     $.each(content, function (index, value) {
        //         alert(index + ': ' + value);
        //     });
        // });
        if (input_content.match(at_reg)) {
            const target = $("#at-targets")
            const target_arr = input_content.match(at_reg)
            target.val("");
            for (let i = 0; i < target_arr.length; i++) {
                target.val(target.val() + String.fromCharCode(32) + target_arr[i]);
            }
            $.ajax({
                type: 'POST',
                url: "/patternAt",
                data: {
                    content: input_content,
                    // authorName: $("#login-userName").text(),
                    // weiboId: $("#weibo-id").text(),
                    // targetUser: input_content.match(at_reg)
                },
                success: function (data) {
                    console.log(data);
                    // 保证光标在最后
                    if (reg_num < input_content.match(at_reg).length) {
                        reg_num++;
                        $("#weiboText-div").html(data);
                        console.log(reg_num)
                        document.execCommand('selectAll', false, null);
                        document.getSelection().collapseToEnd();
                    }
                },
                error: function () {
                    alert("ajax错误！")
                }
            });

        }

    })
})
// .blur(function () {
//
// });

function like_add(id) {
    console.log(id)
    $.ajax({
        url: "/likeAdd",
        type: "get",
        data: {"weibo_id": id},
        success: function (data) {
            $("#likeNum" + id).html(data)
        },
        error: function () {
            alert("点赞出错");
            // alert("状态码：");
            // alert("请求状态：" + textStatus);
            // alert(errorThrown);
        }
    })
    $("#like-area").removeAttr('onclick');
}

function comment_show(id) {
    $.ajax({
        url: "/commentShow",
        type: "get",
        data: {"weibo_id": id},
        success: function (data) {
            console.log(data)
            const jsonObj = $.parseJSON(data)
            console.log(jsonObj)
            if ($("#commentTable" + id).html() === "") {
                $.each(jsonObj, function (name, value) {
                    $("#commentTable" + id).prepend("<p class='single-comment'>" + "<span style='font-weight: bold'>" + value.name.toString() + "</span>" + " :  " + value.content.toString() + "</p><hr/>");
                });
            } else {
                $("#commentTable" + id).html("");
            }
        },
        error: function () {
            alert("评论出错");
        }
    })
}

function passWeiboId(id) {
    console.log(id)
    let commentText = $("#commentText" + id).val()
    console.log(commentText)
    $.ajax({
        url: "/commentAdd",
        type: "get",
        data: {"weibo_id": id, "commentText": commentText},
        success: function (data) {
            $("#commentNum" + id).html(data)
            $("#commentText" + id).val("")
        },
        error: function () {
            alert("发表失败");
        }
    })
    setTimeout("comment_show(" + id + ")", 500)
}

function keepLastIndex(tObj, sPos) {
    if (tObj.setSelectionRange) {
        setTimeout(function () {
            tObj.setSelectionRange(sPos, sPos);
            tObj.focus();
        }, 0);
    } else if (tObj.createTextRange) {
        const rng = tObj.createTextRange();
        rng.move('character', sPos);
        rng.select();
    }

}