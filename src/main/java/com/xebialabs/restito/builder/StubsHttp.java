package com.xebialabs.restito.builder;

import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.semantics.Stub;
import com.xebialabs.restito.server.StubServer;
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

		public Then condition(Condition a) {
			return new Then(server, a);
		}

	}

	public static class Then {

		private StubServer server;

		private Condition cond;

		public Then then(){
			return this;
		}

		public Then with(){
			return this;
		}

		protected Then(StubServer server, Condition condition) {
			this.server = server;
			this.cond = condition;
		}

		public void xmlResourceContent(String xmlResource) {
			server.addStub(new Stub(cond, Action.forXmlResourceContent(xmlResource)));
		}


		public void stringContent(String s) {
			server.addStub(new Stub(cond, Action.forStringContent(s)));
		}

		public void status(HttpStatus s) {
			server.addStub(new Stub(cond, Action.forStatus(s)));
		}
	}
}
