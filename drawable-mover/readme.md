# Drawable Mover
Quickly moves drawables from one module to another. It will reuse the density and file extension of the source file and simply move those drawables to the specified target module.
Just specify the source directory `-s` (the resource directory of the source module), the destination `-d` and the name of the resource to move `-n` *without* file extension.

## Example Usage
```bash
./dm.clj -s /home/schrofi/Projects/multi-module-project/app-module/src/main/res/ -d /home/schrofi/Projects/multi-module-project/shared-module/src/main/res -n "drawable-to-move"
```

### Fish
You can make it a bit more convenient to use by creating a fish function to at least fill in the source directory for you based on the current working directory.

```
function dm
   PATH_TO_SCRIPT/dm.clj -s $(pwd) -n $argv[1] -d $argv[2]
end

funcsave dm
```

Then you can simply open a terminal in the source resource directory and execute the command like `dm DRAWABLE_NAME DESTINATION_DIRECTORY`.
