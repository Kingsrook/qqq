The `test-only-key` / `test-only-key.pub` key pair in this directory was generated via:

```shell
ssh-keygen -t rsa -b 4096 -m PEM -f test-only-key
openssl pkcs8 -topk8 -inform PEM -in test-only-key -outform PEM -nocrypt -out test-only-key-kpcs8.pem
cp test-only-key-kpcs8.pem .../src/test/resources/test-only-key
```

It is NOT meant to be used as a secure key in ANY environment.

It is included in this repo ONLY to be used for basic unit testing. 