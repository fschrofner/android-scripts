# Playstore APK Puller
A script that extracts the installed APKs for the provided bundle id from the currently connected device via adb.
APKs are downloaded to the current working dir when executing the script.
Optionally you can also directly install the downloaded APKs to another device (by disconnecting the device you've pulled the APKs from and launching an emulator, for example).

## Example Usage
```bash
pap.clj -b com.example.app
```

### Fish
You can create a fish function to easily use the script from any directory.

```
function pap
    PATH_TO_SCRIPT/pap.clj $argv
end

funcsave pap
```
