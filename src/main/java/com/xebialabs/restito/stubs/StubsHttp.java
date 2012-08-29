package com.xebialabs.restito.stubs;

import com.xebialabs.restito.StubServer;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;

public class StubsHttp {

	public static When whenHttp(StubServer server) {
		return new When(server);
	}

	public static class When {

		private StubServer server;

		private When(StubServer server) {
			this.server = server;
		}

		public Then get(String uri) {
			return method(Method.GET, uri);
		}

		public Then delete(String uri) {
			return method(Method.DELETE, uri);
		}

		public Then post(String uri) {
			return method(Method.POST, uri);
		}

		public Then method(Method method, String uri) {
			return new Then(server, new StubBuilder().withMethod(method).withUri(uri));
		}
	}

	public static class Then {

		private StubServer server;

		private StubBuilder builder;

		public Then then(){
			return this;
		}

		public Then with(){
			return this;
		}

		protected Then(StubServer server, StubBuilder builder) {
			this.server = server;
			this.builder = builder;
		}

		public void xmlResourceContent(String xmlResource) {
			builder.forXmlResourceContent(xmlResource);
			server.addStub(builder.build());
		}


		public void stringContent(String s) {
			builder.forStringContent(s);
			server.addStub(builder.build());
		}

		public void status(HttpStatus s) {
			builder.forStatus(s);
			server.addStub(builder.build());
		}
	}
}
