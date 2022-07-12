Restito releases
============================================

## 1.x (Java 11+)

* 1.0.0
    * Gradle 7 and  Java 11 (#82) 

## 0.9.x (Java 8+)

* 0.9.5
  * (security) Update mina-core dependency. See #79
* 0.9.4
  * (fix) Critical racing condition on Stub Server while registering the calls. See #74 
* 0.9.3
    * #68: Implement mulitply headers matching in expecting request (#69)
    * (fix) Severe encoding bug in Action.stringContent(String content). See #66

* 0.9.2
    * Added registerCalls flag. Closes #62
    * Possibility to assign labels to stubs. Closes #45

* 0.9.1
    * Fix large content order test (#54)

* 0.9.0
    * Fixed bunch of deprecations, IDE warnings, etc...
    * Java8fication Closes #52
    * Sequenced stub actions, as a first class citizen! + Java 8 (#50)

**0.8.2 is the last feature-release which works with Java 7**

In order to add new features more productively I'd like to switch to Java 8. Few users were asked and indicated that this wouldn't pose a problem for them.

* 0.8.2
    * Some documentation/links fixes
    * Added the ability to specify keystore and truststore through the standard java system properties. #47
    * Added delay() action. Closes #51
* 0.8.1
    * Fixed expired SSL certificate.
* 0.8
    * Clearing StubServer state
    * Using new json-path implementation
    * JUnit 4 rule to start stub server
    * Allow to match for other types then String in Condition.withPostBodyContainingJsonPath() method
* 0.7
    * Now possible to specify port range. Closes #35
    * Returning copies of stubs and calls lists. Closes #33
* 0.6
    * Supporting post body validation using JSON path.
    * Not allowing external modifications of calls and stubs lists.
* 0.5.1
    * Get rid of guava dependency.
    * Removed deprecated methods. #9
* 0.5
    * Fixed synchronization with quick requests
    * #24 removed dependency on javax.ws.rs
    * Improved charset and content-type handling. Closes #24
* 0.4
    * Added support for http basic authentication
    * Updated libs
    * Added verification for at least number of times
* 0.4-beta-3
    * Fixed "/" URI stubbing. Closes #16
    * Fixed groovy testCompile version
    * Added some information regarding logging
    * Added support for matching the whole URL (like http://google.com)
    * Updated slf4j to 1.7.5 and removed confusing logback.xml from the repo
* 0.4-beta-2
    * Added PATCH support
* 0.4-beta-1
    * Added support for unicode characters
    * Deprecated methods exposed to the problem from #7. To be removed at 1.0
    * Added ok() and noContent() actions
    * Used long enough without any problems to call it beta
* 0.4-alpha-2: Fixed some meta information build.gradle
* 0.4-alpha-1: First public release
