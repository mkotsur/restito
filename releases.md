Restito releases
============================================

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
