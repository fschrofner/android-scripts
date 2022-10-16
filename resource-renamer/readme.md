# Resource Renamer
A tool that enables you to quickly rename a single Android resource that is already split up into multiple folders, e.g. `drawable-xhdpi`, `drawable-xxhdpi`, ...
It will reuse the file extension of the files that are already present inside the directory.

## Example Usage
```bash
./rr.clj -d "/home/schrofi/Downloads/random_asset" -n "ic_photo"
```

## 
### Fish
To more conveniently use the tool from the commandline, you can define a fish function like this:
```
function rr 
    PATH_TO_SCRIPT/rr.clj -d $(pwd) -n $argv[1]
end

funcsave rr
```

And then just use the tool by opening a terminal in the resource folder you want to rename and execute `rr TARGET_NAME`.
