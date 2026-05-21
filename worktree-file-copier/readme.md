# Worktree File Copier
Copies files from the original Git repository to the worktree in the current working directory. This is useful for carrying over files that are not checked into Git, but required to build your project.  
All paths must be relative to the repository root.

If you run the script outside a linked worktree (or inside the main checkout), an error will be thrown.

## Example Usage
```bash
./wfc.clj local.properties app/build.gradle
```

### Fish
You can simplify the usage by creating a fish function for files that you constantly copy:

```fish
# shorthand for copy local.properties
function cpl
    PATH_TO_SCRIPT/cpl.clj local.properties
end

funcsave cpl
```

You could also take it one step further and use that function inside another function which initializes other things you might need:
```fish
# shorthand for worktree init
function wti
    cpl
    git submodule update --init
end
```
