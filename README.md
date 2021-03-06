# Bunkr

There are 2 ways of working with Bunkr archives: using the GUI, and through the CLI. Both interfaces attempt to expose
the same functionality but the GUI is more usable from a user standpoint and the CLI allows other programs to easily
manipulate and read from archives.

See [Cli Demo](CLI_DEMO.md) for usage examples of the CLI.

See [Screenshots](/screenshots/bunkr-gui-0.10.0-screenshots.md) for images of the GUI.

See [Overview](/docs/00_overview.md) for documentation of the file format and security.

## Development

The project has been organised into 3 parts:

- ```bunkr-core``` the core functionality behind the archive file format
- ```bunkr-cli``` a command line interface for creating and managing archives
- ```bunkr-gui``` a graphical interface for browsing and managing content of archives (this is the focus of this project once the core and cli are stable).

This project uses Apache Buildr for managing compilation and packaging. Each part can be compiled, tested, and packaged individually.

### Interested in auditing the code?

If you're interested in looking over the crypto, first look over the documentation at [/docs/00_overview.md](/docs/00_overview.md).
I'd suggest focusing on the `bunkr-core` library. It controls all of the manipulations of the base file format. It has
some test coverage, not enough, but enough to validate that it still behaves as it should.

If you pick up problems, please file an issue with the details and I will see what it takes to rework things.

#### Examples

```
# build
$ bundle exec buildr bunkr:bunkr-core:compile

# test
$ bundle exec buildr bunkr:bunkr-core:test

# run unit tests with coverage analyser
$ bundle exec buildr bunkr:bunkr-core:test jacoco:report

# package into a jar
$ bundle exec buildr bunkr:bunkr-core:package
```

#### For setting up your IntelliJ environment

```
$ bundle exec buildr idea
$ bundle exec buildr bunkr:pull_libs
```

If you get a 'No java install found' error, make sure you have `$JAVA_HOME` set.

```
export JAVA_HOME=$(/usr/libexec/java_home)
```
