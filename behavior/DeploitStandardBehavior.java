package com.xebialabs.restito.behavior;

import com.xebialabs.restito.stubs.Stub;
import com.xebialabs.restito.stubs.StubBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeploitStandardBehavior implements Behavior {

	@Override
	public List<Stub> getStubs() {
		return new ArrayList<Stub>(Arrays.asList(
				new StubBuilder().
						withUri("/deployit/server/info").
						withXmlResourceContent("com/xebialabs/restito/stubs/deployit.server.info.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/metadata/type").
						withXmlResourceContent("com/xebialabs/restito/stubs/deployit.metadata.type.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/package/upload/importDarMojoPomTest-1.0.dar").
						withXmlResourceContent("com/xebialabs/restito/stubs/deployit.package.upload.importDarMojoPomTest-1.0.dar.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/package/import").
						withXmlResourceContent("com/xebialabs/restito/stubs/deployit.package.upload.import.xml").
						build(),
				new StubBuilder().
						withUri("/deployit/repository/ci/Environments/myEnv").
						withXmlResourceContent("com/xebialabs/restito/stubs/deployit.repository.ci.Environments.myEnv.xml").
						build()
		));
	}
}
