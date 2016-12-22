var express = require("express");
var app = express();
var server = require("http").createServer(app);
var io = require("socket.io").listen(server);
var fs = require("fs");
server.listen(process.env.PORT || 3000);

var mangUsername = [];

// app.get("/", function(req, res) {
//     res.sendFile(__dirname + "/index.html");
// });

io.sockets.on('connection', function(socket) {

    console.log("Co nguoi connect "+socket.id);

    socket.on("client-gui-username", function(data) {
        var ketqua = false;
        if (mangUsername.indexOf(data) > -1) {

            ketqua = false;
        } else {
            mangUsername.push(data);
            socket.un=data;
            ketqua = true;
            io.sockets.emit('server-gui-username',{danhsach:mangUsername});

        }
        socket.emit("ketquaDangKyUn", {
            noidung: ketqua
        });


    });

});
