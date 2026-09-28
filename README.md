```
script:pre-request {
  const certFilePath = bru.cwd() + '/.certs/bruno/x-client-cert.txt';
  const signatureFilePath = bru.cwd() + '/.certs/bruno/x-client-signature.txt';
  const nonceFilePath = bru.cwd() + '/.certs/bruno/x-client-nonce.txt';

  try {
    bru.setVar("certificate", bru.readFile(certFilePath).trim());
    bru.setVar("clientsignature", bru.readFile(signatureFilePath).trim());
    bru.setVar("clientnonce", bru.readFile(nonceFilePath).trim());
  } catch (e) {
    console.warn('Could not read cert files:', e.message);
  }
}

```
