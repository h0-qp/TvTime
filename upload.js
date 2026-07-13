const fs = require('fs');
const https = require('https');
const FormData = require('form-data');

const form = new FormData();
form.append('reqtype', 'fileupload');
form.append('userhash', '');
form.append('fileToUpload', fs.createReadStream('app/build/outputs/apk/debug/app-debug.apk'));

const request = https.request({
  method: 'POST',
  host: 'litterbox.catbox.moe',
  path: '/api',
  headers: form.getHeaders()
}, (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => { console.log(data); });
});

form.pipe(request);
