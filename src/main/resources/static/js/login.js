const reg = document.getElementById("register");
const hl = document.getElementById("has-login");
reg.onclick = function () {
    console.log("click!!!");
    window.location.href = '/register'
};
hl.onclick = function () {
    console.log("click!!!");
    window.location.href = '/mainPage'
}