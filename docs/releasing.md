# Releasing

On PRs to main / On pushes to main: test and build

On valid tag pushing: test, build and release

To publish, increase `versionCode`, update `versionName`
(in `app/build.gradle.kts`), and merge into `main`. Push
a `vX.Y.Z` tag matching `versionName` at that commit. For
example, for version `0.1.1`:

> `versionCode` is an increasing integer. Just +1 after each release

```sh
git tag v0.1.1
git push origin v0.1.1
```

CI checks that the tag matches the version and references a commit
on `main`. The release asset is `stop-and-breathe-vX.Y.Z.apk`.
