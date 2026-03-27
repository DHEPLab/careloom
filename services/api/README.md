# Healthy Future Backend

## Local Development

```
$ cp src/main/resources/application-local.sample.yml src/main/resources/application-local.yml
```

Configure MySQL connection info in the src/main/resources/application-local.yml file.

Create the database healthy --character_set_server=utf8mb4 --collation-server=utf8mb4_bin

```
$ SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

IDEA config:
* Run -> Edit Configurations -> Gradle -> bootRun -> VM Options: -Dspring.profiles.active=local
* Change the Java format indentation to 2 spaces which is same as spotless config
  * Settings => Editor -> Code Style -> Java -> Tabs and Indents -> Indent: 2
  * Settings => Editor -> Code Style -> Java -> Tabs and Indents -> Tab: 2
  * Settings => Editor -> Code Style -> Java -> Tabs and Indents -> Continuation indent: 4

Enable the git hooks:
We use the [lefthook](https://github.com/evilmartians/lefthook) as our git hook manager.
You can use any tool to install the lefthook:
```shell
brew install lefthook
go install github.com/evilmartians/lefthook@latest
npm install -g lefthook
```
After you install the lefthook, you can run command `lefthook install` to init the hooks

or you can run command `lefthook run pre-commit` and `lefthook run pre-push` to test the hooks running


Default user credentials:
* Username: admin
* Password: admin123

* Username: super_admin
* Password: 123456

## External Dependencies

* AWS S3, used for storing course images and videos.

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/gradle-plugin/reference/html/)
* [Checkstyle](https://checkstyle.sourceforge.io/)
    * [google-java-format](https://github.com/google/google-java-format)
    * [google-java-format IntelliJ plugin](https://github.com/google/google-java-format#intellij-android-studio-and-other-jetbrains-ides)
* [Spotbugs](https://spotbugs.github.io/)
