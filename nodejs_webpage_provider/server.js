if(process.argv.length != 5){
	console.log('please provide following infomation in arguments:');
	console.log('nodejs server.js webpage_listening_port daemon_address daemon_port');
	process.exit(1);
}

webpage_listening_port = process.argv[2];
daemon_address = process.argv[3];
daemon_port = process.argv[4];

https = require('https');
fs = require('fs');
net = require('net');

var options = {
	key: fs.readFileSync('ssl.key'),
	cert: fs.readFileSync('1_li-veterans.org_bundle.crt'),
	passphrase:"101SShooter"
};

https.createServer(options,responder).listen(webpage_listening_port);

function responder(request, response){
	var startTime = (new Date()).getTime();
	var url = require('url').parse(request.url,true);
	console.log('Received request: '+url.pathname);
	if(url.pathname == '/request'){
		var serverConnection = new net.Socket();
		response.writeHead(200, {'Content-Type': 'application/json'});
		serverConnection.setTimeout(1500,function(){
			serverConnection.destroy();
			response.write('{"errorCode":100}');//error code for daemon not respond
			response.end();
		});
		serverConnection.connect(daemon_port,daemon_address,function(){
			console.log('Connected');
			url.query.sourceIP = request.connection.remoteAddress;
			var jsonToBeSent = JSON.stringify(url.query)+'\n';
			console.log('jsonToBeSent:'+jsonToBeSent);
			console.log('finished sending to command daemon: '+serverConnection.write(jsonToBeSent));
		});
		serverConnection.on('data', function(result){
			console.log('Received from daemon: <'+result+'>');
			response.write(result+'');
			response.end();
		});
		serverConnection.on('error', function(ex) {
			console.log(ex);
			response.write('{"errorCode":101}');//error code for daemon respond error
			response.end();
		});
		serverConnection.on('close', function() {
			console.log('Connection to daemon closed');
			response.end();
			console.log('time used:'+((new Date()).getTime() - startTime));
		});
	}else{
		response.writeHead(404);
		response.end('404 Not Found\n');
	}
}

process.on('uncaughtException', function (err) {
    console.log(err);
});