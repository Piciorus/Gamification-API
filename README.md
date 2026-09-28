```
script:pre-request {
  try {
    const fs = require('fs');
    const certsPath = bru.cwd() + '/../../../.certs/bruno';
    
    bru.setVar("certificate", fs.readFileSync(certsPath + '/x-client-cert.txt', 'utf8').trim());
    bru.setVar("clientsignature", fs.readFileSync(certsPath + '/x-client-signature.txt', 'utf8').trim());
    bru.setVar("clientnonce", fs.readFileSync(certsPath + '/x-client-nonce.txt', 'utf8').trim());
    console.log('SUCCESS');
  } catch (e) {
    console.error('ERROR:', e.message);
  }
}
```
```
script:pre-request {
  try {
    const fs = require('graceful-fs');
    // același cod ca mai sus
  } catch (e) {
    console.error('ERROR:', e.message);
  }
}

```
