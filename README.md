# packagemap-java-parser

<img src="/cover.png" alt="PackageMap cover logo" title="PackageMap cover logo">

packagemap-java-parser provides a parser to build maps of your java code. 

Check out the site at [https://packagemap.co](https://packagemap.co)

Code review just got a lot easier! Visualise your code. 

## Getting started - github action

Use the github action:
```yaml
- name: PackageMap java parser Github action
  uses: packagemap/packagemap-java-parser@v0.0.2
  with:
    src_dir: 'src/main/java'
    base: 'com.mycompany'
    api_key: ${{ secrets.PACKAGEMAP_KEY }}
```

1. Add the github action config
2. Add a PACKAGEMAP_KEY secret to your repo in the format `user_id:secret_key`

## Getting started - local

1. Sign up at [packagemap.co](http://packagemap.co) to get an API Access key, and API Secret key.
2. Download the jar from the release page and run it

```bash
java -jar packagemap-java-parser-1.0.0-SNAPSHOT-all.jar \
    --key <access-key>:<secret-key> \
    --base com.mycompany \
    [list of java source directories separated by spaces]
```

You can also pass the api key using `--key` flag or using the `PACKAGEMAP_KEY` environment variable.

For example:

```bash
java -jar packagemap-java-parser-1.0.0-SNAPSHOT-all.jar \
    --key ABCDEFGHIJKLMNOP:183b9d197ed48a67c6e479a51 \
    --base com.mycompany \
    ./src/main/java
```

1. The parser will print out the URL of the map, navigate to that URL to analyse and filter. 
2. Try out the `--git=` flag with a branch name `--git=origin/main` or a commit hash to map only the files that have changed.

## Docker image

Instead of running the jar directly, you can use the docker image `packagemap/packagemap-java-parser`:
```bash
docker run \
  -v $(pwd):/project \
  packagemap/packagemap-java-parser
  --key <access-key>:<secret-key> \
  --base com.mycompany \
  /project/src/main/java
```

## Command line flags

| flag | description |
| --- | --- |
| -b, --base | The base package that all our source code shares. This helps the parser ignore classes imported from your dependencies |
| -k, --key | Your access key and secret key. Maps will be added to your account on [packagemap.co](https://packagemap.co). Alternatively you can pass the `PACKAGEMAP_KEY` environment variable. Both flag and env var use the colon separated format `user_id:secret_key` |
| -g, --git | Builds a map of only the files that have changed vs. the target commit hash or head of the target branch.  |
| [dirs] | A list of directories to parse the source files from. The directory should target just above the source code package hierarchy.<br/> e.g. for class `src/main/java/co/packagemap/Main.java` we should use the directory `src/main/java`. |

## Using the PackageMap UI

The UI renders the graphs of the source code. It allows filtering and excluding classes. 

- Filter by wildcard with `*`. e.g. `*MyClass` to match all types ending in `MyClass`.
- Filter by package prefix. e.g. `com.mypackage.data` to match all types in the `data` package.
