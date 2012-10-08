# Developer's guide

* [Motivation](#motivation)
* [Starting and stopping stub server](#starting_and_stopping_stub_server)
    * [Specific vs random port](#specific_vs_random_port)
* [Stubbing server behavior](#stubbing_server_behavior)
    * [Stub conditions](#stub_conditions)
    * [Own condition](#own_condition)
    * [Stub actions](#stub_actions)
    * [Own action](#own_action)
    * [Automatic content type](#automatic_content_type)
    * [Expected stubs](#expected_stubs) <sup style="color: orange">Experimental!</sup>
    * [Autodiscovery of stubs content](#autodiscovery_of_stubs_content) <sup style="color: orange">Experimental!</sup>
* [Verifying calls to server](#mimic_server_behavior)
    * [Simeple verifications](#simple_verifications)
    * [Limiting number of calls](#limiting_number_of_calls)
    * [Sequenced verifications](#sequenced_verification)

<a name="motivation"/>
# Motivation
Let's imagine you have an application or library which uses a REST interface. At some point you would like to make sure that it works exactly as expected and mocking low-level HTTP client is not always the best option. That's where <b>Restito</b> comes to play: it gives you a DSL to test your application with mocked server just like you would do it with any mocking framework.

<a name="starting_and_stopping_stub_server"/>
# Starting and stopping stub server

```java

	@Before
	public void start() {
		server = new StubServer().run();
	}
```

Method [StubServer.stop()](http://mkotsur.github.com/restito/javadoc/3.7.1-SNAPSHOT/com/xebialabs/restito/server/StubServer.html#stop\(\)) is exposed, but that's not necessary to call it. JVM will close ports when tests are over.

<a name="specific_vs_random_port" />
## Selecting a random free port

By default, [StubServer.DEFAULT_PORT](http://mkotsur.github.com/restito/javadoc/3.7.1-SNAPSHOT/com/xebialabs/restito/server/StubServer.html#DEFAULT_PORT) is used, but if it's busy, then next available will be taken.

```java
	@Test
	public void shouldStartServerOnRandomPortWhenDefaultPortIsBusy() {
		StubServer server1 = new StubServer().run();
		StubServer server2 = new StubServer().run();
		assertTrue(server2.getPort() > server1.getPort());
	}
```
If you want to specify port explicitly, then you can do something like that:

```java
    @Test
    public void shouldUseSpecificPort() {
        StubServer server = new StubServer(8888).run();
        assertEquals(8888, server.getPort());
    }
```

<a name="stubbing_server_behavior" />

Stubbing is a way to teach server to behave as you wish.