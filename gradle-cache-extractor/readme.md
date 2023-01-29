# Gradle Cache Extractor
Extracts dependencies from your Gradle cache so you can provide them on your self-hosted Maven repository.  
Use `-s` and `-d` to specify the source (= Gradle cache) and target directory respectively.  
You can optionally provide a dependency identifier with `-a` so only that dependency will be extracted. It's also possible to just specify a group id or artifact id, just leave out the parts you're not interested in.

## Example Usage
```bash
./gce.clj -s "/home/schrofi/.gradle/caches/modules-2/files-2.1" -d "/home/schrofi/Projects/fallback-maven-repository/repository" -a "org.bitbucket.consentmanager:android-consentmanager:1.3.3"

./gce.clj -s "/home/schrofi/.gradle/caches/modules-2/files-2.1" -d "/home/schrofi/Projects/fallback-maven-repository/repository" -a "org.bitbucket.consentmanager:android-consentmanager"
```

## Hosting a Maven Repository on Github
An easy (and free) way to host your Maven repository is to create a public Git repository on Github. You can then use this repository as target directory for the extractor script. It's not necessary to put packages into the root folder of your repository, you can also create a subfolder to keep the repository tidy and maybe add a readme in the root folder.

Once you've got your dependencies pushed to the repository you can specify it in your `build.gradle`:

```groovy
allprojects {
    repositories {
        ...
        maven { 
            url 'https://github.com/USER/REPOSITORY/raw/BRANCH/SUBFOLDERS' 
        }
    }
}
```

Unfortunately I wasn't able to make this works with private repositories and a token on Github. If you got that to work, I'd be happy to hear about it! It would make that solution even nicer.  

On Bitbucket you can actually use app passwords and specify them for private repositories like this:
```groovy
maven {
    url 'https://api.bitbucket.org/2.0/repositories/USER/REPO/src/BRANCH/FOLDER'
    credentials  {
        username = bitbucketUser
        password = bitbucketPassword
    }
    authentication {
        basic(BasicAuthentication)
    }
}
```

Either way you can also just host it on any static webserver.

### Fish
To use the script from anywhere easily, you can create the following fish function: 

```
function gce
    PATH_TO_SCRIPT/gce.clj $argv
end
funcsave gce
```
