package mcri.mf

import arc.mf.client.RemoteServer
import arc.mf.client.RemoteServer.Connection as MfConnection
import arc.mf.client.ServerClient
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

@Slf4j
@Singleton(lazy = true)
class MfClient {

    public static final String MF_APP = "archie";

    final String mfHost = System.getenv("MF_HOST") ?: "mediaflux.mcri.edu.au"
    final int mfPort = Integer.parseInt(System.getenv("MF_PORT") ?: '443')
    final String mfDomain = System.getenv("MF_DOMAIN") ?: 'MCRI'
    final String mfUsername = System.getenv("MF_USERNAME") ?: 'username'
    final String mfPassword = System.getenv("MF_PASSWORD") ?: 'secret'

    private final MfConnection conn
    private final String token

    static {
        ServerClient.setSessionPooling(true)
    }

    {
        RemoteServer server = new RemoteServer(mfHost, mfPort, true, true)
        token = authConn(server)
        conn = (MfConnection) server.open()
        conn.connectWithToken(MF_APP, token)
    }

    private String authConn(RemoteServer server) {
        MfConnection authConn = (MfConnection) server.open()
        authConn.connect(MF_APP, mfDomain, mfUsername, mfPassword)
        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.app(MF_APP)
        xml.'destroy-on-service-call'('server.version')
        xml.perm {
            access('ACCESS')
            resource(type: 'service', 'server.version')
        }
        def r = authConn.execute("secure.identity.token.create", writer.toString())
        String token = r.value("token")
        return token
    }

    Collection<String> systemServiceList() {
        def r = conn.execute("system.service.list")
        Collection<String> services = r.values("service");
        return services
    }

    void showAsset(Long id) {
        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.app(MF_APP)
        xml.id(id)
        def ele = conn.execute('asset.get', writer.toString())
        println ele
    }
}
