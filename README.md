# Pos_app_plugin

## Installation

## Step 1
```sh
#Create a github.properties file in the root folder of android. Path should be like below: E.g. "myReactApp/android/github.properties"

# Add the following in github.properties:
USERNAME_GITHUB=MyUsernameOnGithub
TOKEN_GITHUB=TokenFromGithubClassicToken
```

## Step 2: build.gradle (app)
```sh


def githubPropertiesFile = rootProject.file("github.properties")
def githubProperties = new Properties()
githubProperties.load(new FileInputStream(githubPropertiesFile))

android{
  ...
}

repositories {
  maven {
    url 'https://gitlab.com/api/v4/projects/4128550/packages/maven'
  }
  maven {
    name "GitHubPackages"
    url 'https://maven.pkg.github.com/Blusalt-FS/PosAppPlugin'

    credentials {
      username githubProperties['USERNAME_GITHUB']
      password githubProperties['TOKEN_GITHUB']
    }
  }
}

dependencies{
  implementation 'net.blusalt:posplugin:1.0-1'
}
```