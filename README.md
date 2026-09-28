```
vars {
  trauth-url: ...
  certsFolder: d0
}

```

```
vars {
  trauth-url: ...
  certsFolder: x0
}

```


```
script:pre-request {
  const fs = require('fs');
  const folder = bru.getEnvVar('certsFolder');

  if (!folder) {
    console.warn('certsFolder not set for this environment — skipping cert load');
    return;
  }

  const certsPath = bru.cwd() + '/../../../.certs/bruno/' + folder;

  try {
    bru.setVar("certificate", fs.readFileSync(certsPath + '/x-client-cert.txt', 'utf8').trim());
    bru.setVar("clientsignature", fs.readFileSync(certsPath + '/x-client-signature.txt', 'utf8').trim());
    bru.setVar("clientnonce", fs.readFileSync(certsPath + '/x-client-nonce.txt', 'utf8').trim());
    console.log('SUCCESS - loaded certs from:', folder);
  } catch (e) {
    console.error('ERROR reading certs:', e.message);
  }
}

```
