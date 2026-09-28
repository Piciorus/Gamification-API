```
script:pre-request {
  const axios = require('axios');
  const certsPath = bru.cwd() + '/../../../.certs/bruno';

  try {
    const cert = await axios.get('file://' + certsPath + '/x-client-cert.txt');
    const sig = await axios.get('file://' + certsPath + '/x-client-signature.txt');
    const nonce = await axios.get('file://' + certsPath + '/x-client-nonce.txt');

    bru.setVar("certificate", cert.data.trim());
    bru.setVar("clientsignature", sig.data.trim());
    bru.setVar("clientnonce", nonce.data.trim());
    console.log('SUCCESS');
  } catch (e) {
    console.error('ERROR:', e.message);
  }
}

```

```
script:pre-request {
  const got = require('got');
  const certsPath = bru.cwd() + '/../../../.certs/bruno';

  try {
    const cert = await got('file://' + certsPath + '/x-client-cert.txt');
    bru.setVar("certificate", cert.body.trim());
    console.log('SUCCESS');
  } catch (e) {
    console.error('ERROR:', e.message);
  }
}

```


```
script:pre-request {
  try {
    const certsPath = bru.cwd() + '/../../../.certs/bruno';
    const cert = await fetch('file://' + certsPath + '/x-client-cert.txt');
    const text = await cert.text();
    bru.setVar("certificate", text.trim());
    console.log('SUCCESS:', text.substring(0, 20));
  } catch (e) {
    console.error('ERROR:', e.message);
  }
}

```
