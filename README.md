```
script:pre-request {
  const { readFileSync } = require('node:fs');
  const { resolve } = require('node:path');

  const certsPath = resolve(bru.cwd(), '../../../.certs/bruno');

  try {
    bru.setVar("certificate", readFileSync(certsPath + '/x-client-cert.txt', 'utf8').trim());
    bru.setVar("clientsignature", readFileSync(certsPath + '/x-client-signature.txt', 'utf8').trim());
    bru.setVar("clientnonce", readFileSync(certsPath + '/x-client-nonce.txt', 'utf8').trim());
  } catch (e) {
    console.error('ERROR:', e.message);
  }
}
```
