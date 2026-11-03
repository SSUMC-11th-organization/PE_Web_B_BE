// 외부 패키지 없이 실습 화면만 제공하는 로컬 정적 서버입니다.
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const html = fs.readFileSync(path.join(__dirname, 'index.html'));
const server = http.createServer((req, res) => {
  if (req.url === '/favicon.ico') { res.writeHead(204); return res.end(); }
  if (req.url !== '/' && req.url !== '/index.html') { res.writeHead(404); return res.end('Not found'); }
  res.writeHead(200, {'Content-Type': 'text/html; charset=utf-8'});
  res.end(html);
});
server.listen(Number(process.env.PORT || 5173), '127.0.0.1', () => console.log('화면: http://localhost:' + server.address().port));
