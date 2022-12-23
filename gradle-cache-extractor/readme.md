# Gradle Cache Extractor
Extracts dependencies from your Gradle cache so you can provide them on your self-hosted Maven repository.
You can optionally provide a dependency with `-a` so only that dependency will be extracted. It's also possible to just specify a group id or artifact id, just leave out the parts you're not interested in.

## Example Usage
```bash
./gce.clj -s "/home/schrofi/.gradle/caches/modules-2/files-2.1" -d "/home/schrofi/Documents/maven_repo" -a "org.bitbucket.consentmanager:android-consentmanager:1.3.3"

./gce.clj -s "/home/schrofi/.gradle/caches/modules-2/files-2.1" -d "/home/schrofi/Documents/maven_repo" -a "org.bitbucket.consentmanager:android-consentmanager"
