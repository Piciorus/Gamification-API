```
script:pre-request {
  const fs = require('fs');
  const path = require('path');

  const certsPath = path.resolve(__dirname, '../../../.certs/bruno');

  try {
    bru.setVar("certificate", fs.readFileSync(path.join(certsPath, 'x-client-cert.txt'), 'utf8').trim());
    bru.setVar("clientsignature", fs.readFileSync(path.join(certsPath, 'x-client-signature.txt'), 'utf8').trim());
    bru.setVar("clientnonce", fs.readFileSync(path.join(certsPath, 'x-client-nonce.txt'), 'utf8').trim());
  } catch (e) {
    console.warn('Could not read cert files from .certs/bruno/:', e.message);
  }
}

```
