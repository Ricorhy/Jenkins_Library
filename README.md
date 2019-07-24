# WORK IN PROGRESS!

Please see https://jenkins.io/doc/book/pipeline/shared-libraries/

## Creating a Declarative method

How to write a nice parameterized and configurable method based on [Defining a more structured DSL](https://jenkins.io/doc/book/pipeline/shared-libraries/#defining-a-more-structured-dsl)

Let's say we have a `helloMethod` that prints and `message1` and `message2`.   
```groovy
// helloMethod.groovy
def helloMethod(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    println "${config.message1}"
    println "${config.message2}"
}
```
To use it on a pipeline script.  
```groovy
node {
    helloMethod {
        message1 = "hi"
        message2 = "hello"
    }
}

// prints hi and hello
```

## Methods Usage Documentation

### getAemEnvDetails

Get Adobe Experience Manager environment details. Accepts `environment`.  

```groovy
getAemEnvDetails {
    environment = 'dev'
}
```

### getReleaseVersion

If current workspace is a git project, fetch the tags, remove the prefix (if there is any) and then return them in decimal major.minor.patch format in a List. This is used in `getNextVersion`.  

```groovy
def previousVersions = getReleaseTagVersions tagsPrefix: 'release-'
```

### getNextVersion

Get the snapshot version of a maven project. It must be a valid snapshot version format!  
Calculate and return the `version` in major.minor.@buildNumber format.  

```groovy
def version = getNextVersion {
    snapshotVersion = readMavenPom file: 'pom.xml'
}
```

### buildProject

Wraps the tasks that is required to build different strategies (`review`, `snapshot` and `release`).  

```groovy
buildProject {
    strategy = 'review'
}
```