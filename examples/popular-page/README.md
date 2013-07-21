## Try it

```cd examples/popular-page```

### To run the app:

```mvn compile exec:java``` or ```gradle run```


### To run tests:

```mvn test``` or ```gradle -i test```


# What's this all about

Let's imagine that we have to write a REST client, which takes list of titles, fetches data from Wikipedia, and tells you which page was edited most recently.

First of all we can logically split our application in 2 parts:

* Code which will be directly responsible for communication with rest backend of Wikipedia and producing Java objects from JSON. It sits in [WikiClient](https://github.com/mkotsur/restito/blob/master/examples/popular-page/src/main/java/com/xebialabs/restito/examples/WikiClient.java);
* Code which will consume domain objects from _WikiClient_ and do something with them. Let's call it [App](https://github.com/mkotsur/restito/blob/master/examples/popular-page/src/main/java/com/xebialabs/restito/examples/App.java).

Thanks to various [mocking frameworks](http://stackoverflow.com/questions/22697/whats-the-best-mock-framework-for-java), there is no problem to test the latter part: you just mock all points of interaction between [App](https://github.com/mkotsur/restito/blob/master/examples/popular-page/src/main/java/com/xebialabs/restito/examples/App.java) and [WikiClient](https://github.com/mkotsur/restito/blob/master/examples/popular-page/src/main/java/com/xebialabs/restito/examples/WikiClient.java) and you are good to write expectations within controlled environment.

Different story when you want to test [WikiClient](https://github.com/mkotsur/restito/blob/master/examples/popular-page/src/main/java/com/xebialabs/restito/examples/WikiClient.java) itself. You are dependent on:

* Random behavior of real world people, who edit articles: your test may pass when you and assert that _Article A_ was edited, but later it would fail when another article is edited;
* Internet connection - means your tests tend to be slow and brittle;
* API calls limits, which you quickly exceed if make real calls from tests.

Restito helps you here: it allows you to check that your code can handle different responses returned by backend and makes requests it should make.

Now take a look at the code: [com.xebialabs.restito.examples.WikiClient](https://github.com/mkotsur/restito/blob/master/examples/popular-page/src/main/java/com/xebialabs/restito/examples/WikiClient.java) is the part, which we were talking about. It does 3 things:

 * Fetches data from Wikipedia;
 * Transforms this data into java objects of type [PageRevision](https://github.com/mkotsur/restito/blob/master/examples/popular-page/src/main/java/com/xebialabs/restito/examples/PageRevision.java) with help of _Gson_ library;
 * Orders all revisions and returns the latest one.

It is arguable if you want to keep all these concerns in a single class, but for the sake of argument, let's agree that this is our layer which has deal with backend and we want to test it. In ideal world this layer might be thiner, but it will always exist.

The only pre-requisite for testing this code with _Restito_ is that your [WikiClient](https://github.com/mkotsur/restito/blob/master/examples/popular-page/src/main/java/com/xebialabs/restito/examples/WikiClient.java) can be parametrized with the entry point. In this way, you can feed it with ```http://localhost:XXX``` instead of ```http://en.wikipedia.org```.

That's it. Then our happy-flow test-case will look like [WikiClientTest](https://github.com/mkotsur/restito/blob/master/examples/popular-page/src/test/java/com/xebialabs/restito/examples/WikiClientTest.java). Simple, eh?

To have more insight into Restito DSL, check [Developer's Guide](https://github.com/mkotsur/restito/blob/master/guide.md)

The next step probably would be to test some not so happy scenarios, when server returns ```404 Not Found```, or ```500 Server Error``` and make sure that your client behaves in the decent way in this cases. Or may be extend the business logic and cover more functional cases.

Feel free to play with it.


