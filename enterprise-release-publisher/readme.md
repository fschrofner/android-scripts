# Enterprise Release Publisher
A script that reads the configuration file in your Android project, builds the defined module/variant combination and then uploads it to the specified server using the defined file name and also updates the JSON file on the remote with the same file name.

## Configuration
To use the script for your project, you have to add a configuration file called `enterprise-releases.json` in the root of your project directory.
This file can contain multiple release configurations and needs to specify a couple of key values:
```json
{
    "DEV": {
        "module": "app",
        "variant": "debug",
        "server": "sftp://127.0.0.1",
        "path": "/var/www/apps/projects/test",
        "fileName": "Enterprise-Test"
    },
    "INT": {
        "module": "app",
        "variant": "release",
        "server": "sftp://127.0.0.1",
        "path": "/var/www/apps/projects/test",
        "fileName": "Enterprise-Test"
    }
}
```
- `module`: specifies the module built with Gradle
- `variant`: the variant that should be built. it's combined with the module from above to create the proper Gradle command, e.g. `:app:assembleDebug`
- `server`: the server (including protocol) to upload the files to
- `path`: the path on the remote server where the files should be placed
- `fileName`: the name to be used for the apk & json files on the remote

If there's only one release configuration defined, it will automatically pick that one. In case there are multiple configurations you have to add the configuration name you want to build & upload as argument (see examples below).

## Example Usage
Simply execute the script in the root directory of your project, optionally specifying the configuration to use if there are multiple.
```bash
erp.clj
erp.clj "INT"
```

##
### SSH Configuration
I'm assuming that you've properly defined your credentials in `~/.ssh/config`, so you don't have to specify any credentials in your configuration files. You can assign specific credentials to different hosts there.

```
Host SERVER
     User USER
     IdentityFile PATH_TO_KEY
```

### Fish
If you don't want to add the script itself to your path you can also create a fish function to use it from any directory.

```
function erp
    PATH_TO_SCRIPT/erp.clj $argv[1]
end

funcsave erp
```
