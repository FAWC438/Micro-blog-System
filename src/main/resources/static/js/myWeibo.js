function attention(curId, id) {
    $.ajax({
        url: "/checkAttention",
        type: "get",
        data: {"curId": curId, "id": id},
        success: function (data) {
            const btn = $("#attention-btn");
            if (data === "True") {
                btn.css("background-color", "#ff8200")
                btn.text("已关注")
            } else if (data === "False") {
                btn.css("background-color", "#fff")
                btn.text("关注")
            } else {
                alert("关注服务不可用")
            }
            location.reload()
        },
        error: function () {
            alert("ajax出错");
        }
    })
}