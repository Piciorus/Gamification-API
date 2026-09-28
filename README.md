```
script:pre-request {
  const certsPath = bru.cwd() + '/../../../.certs/bruno';

  try {
    bru.setVar("certificate", bru.readFile(certsPath + '/x-client-cert.txt').trim());
    bru.setVar("clientsignature", bru.readFile(certsPath + '/x-client-signature.txt').trim());
    bru.setVar("clientnonce", bru.readFile(certsPath + '/x-client-nonce.txt').trim());
  } catch (e) {
    console.error('ERROR:', e.message);
  }
}
```
