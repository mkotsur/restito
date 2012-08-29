package com.xebialabs.restito.behavior;

import com.xebialabs.restito.stubs.Stub;
import com.xebialabs.restito.stubs.StubBuilder;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeploitStandardBehavior implements Behavior {

	@Override
	public List<Stub> getStubs() {
		return new ArrayList<Stub>(Arrays.asList(
				new StubBuilder().
						withUri("/deployit/server/info").
						forXmlResourceContent("com/xebialabs/restito/stubs/deployit.server.info.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/metadata/type").
						forXmlResourceContent("com/xebialabs/restito/stubs/deployit.metadata.type.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/package/upload/importDarMojoPomTest-1.0.dar").
						forXmlResourceContent("com/xebialabs/restito/stubs/deployit.package.upload.importDarMojoPomTest-1.0.dar.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/package/import").
						forXmlResourceContent("com/xebialabs/restito/stubs/deployit.package.upload.import.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/repository/ci/Environments/myEnv").
						forXmlResourceContent("com/xebialabs/restito/stubs/deployit.repository.ci.Environments.myEnv.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/repository/exists/Environments/myEnv/importDarMojoPomTest").
						forStringContent("<boolean>false</boolean>").
						build(),
				new StubBuilder().
						withUri("/deployit/repository/ci/Environments/myEnvironment/importDarMojoPomTest").
						forStatus(HttpStatus.NOT_FOUND_404).
						build(),
				new StubBuilder().
						withUri("/deployit/deployment/prepare/initial").
						withParameter("environment", "Environments/myEnv").
						withParameter("version", "Applications/importDarMojoPomTest/1.0").
						forXmlResourceContent("com/xebialabs/restito/stubs/deployit.deployment.prepare.initial.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/deployment/prepare/update").
						withParameter("deployedApplication", "Environments/myEnv/importDarMojoPomTest").
						withParameter("version", "Applications/importDarMojoPomTest/1.5").
						forXmlResourceContent("com/xebialabs/restito/stubs/deployit.deployment.prepare.update.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/deployment/generate/all").
						withMethod(Method.POST).
						forXmlResourceContent("com/xebialabs/restito/stubs/deployit.deployment.generate.all.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/deployment/validate").
						withMethod(Method.POST).
						forXmlResourceContent("com/xebialabs/restito/stubs/deployit.deployment.validate.xml").
						build(),
				new StubBuilder().
						withMethod(Method.POST).
						withUri("/deployit/deployment").
						forStringContent("2eb8bbee-f462-4bf5-9e2b-d61bb5f22a6a").
						build(),
				new StubBuilder().
						withMethod(Method.POST).
						withUri("/deployit/task/2eb8bbee-f462-4bf5-9e2b-d61bb5f22a6a/start").
						forSuccess().
						build(),
				new StubBuilder().
						withMethod(Method.GET).
						withUri("/deployit/deployment/prepare/undeploy").
						withParameter("deployedApplication", "Environments/myEnv/importDarMojoPomTest").
						forXmlResourceContent("com/xebialabs/restito/stubs/deployit.deployment.prepare.undeploy.xml").
						build()
		));
	}
}
