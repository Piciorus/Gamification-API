```
script:pre-request {
  try {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', 'file://' + bru.cwd() + '/../../../.certs/bruno/x-client-cert.txt', false);
    xhr.send();
    bru.setVar("certificate", xhr.responseText.trim());
    console.log('SUCCESS cert:', xhr.responseText.substring(0, 20));
  } catch (e) {
    console.error('ERROR:', e.message);
  }
}

```
