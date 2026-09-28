```
script:pre-request {
  const certsPath = bru.cwd() + '/../../../.certs/bruno';

  try {
    bru.setEnvVar("certificate", bru.readFile(certsPath + '/x-client-cert.txt').trim());
    bru.setEnvVar("clientsignature", bru.readFile(certsPath + '/x-client-signature.txt').trim());
    bru.setEnvVar("clientnonce", bru.readFile(certsPath + '/x-client-nonce.txt').trim());
  } catch (e) {
    console.warn('Could not read cert files:', e.message);
  }
}
```
