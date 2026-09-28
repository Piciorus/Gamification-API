```
script:pre-request {
  const certsPath = bru.cwd() + '/../../../.certs/bruno';

  console.log('cwd:', bru.cwd());
  console.log('certsPath:', certsPath);

  try {
    const cert = bru.readFile(certsPath + '/x-client-cert.txt');
    console.log('cert value:', cert);
    bru.setEnvVar("certificate", cert.trim());
    bru.setEnvVar("clientsignature", bru.readFile(certsPath + '/x-client-signature.txt').trim());
    bru.setEnvVar("clientnonce", bru.readFile(certsPath + '/x-client-nonce.txt').trim());
    console.log('env vars set successfully');
  } catch (e) {
    console.error('ERROR:', e.message);
  }
}
```
