# Release workflow

Pushing a tag matching `v*` creates a GitHub Release and uploads a signed APK.

Required repository secrets:

- `ANDROID_SIGNING_KEY_BASE64`: base64 encoded Android signing keystore.
- `ANDROID_KEY_ALIAS`: signing key alias.
- `ANDROID_KEYSTORE_PASSWORD`: keystore password.
- `ANDROID_KEY_PASSWORD`: key password.

Example keystore encoding:

```bash
base64 -i release.jks | pbcopy
```

Example release:

```bash
git tag v2.1.2
git push origin v2.1.2
```

The uploaded APK name is `ReadropsForLumina-<tag>.apk`.
