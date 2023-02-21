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

    static {
        ServerClient.setSessionPooling(true)
    }

    {
        RemoteServer server = new RemoteServer(mfHost, mfPort, true, true)
        conn = (MfConnection) server.open()
        conn.connect(MF_APP, mfDomain, mfUsername, mfPassword)
    }

    Collection<String> systemServiceList() {
        def r = conn.execute("system.service.list")
        Collection<String> services = r.values("service")
        return services
    }

    void actorSelfDescribe() {
        def resp = conn.execute("actor.self.describe")
        log.info("actor.self.describe: ${resp}")
    }

    void assetExists(def assetId) {
        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.service(name: 'asset.exists') {
            id(assetId)
        }

        def resp = conn.execute('service.execute', writer.toString())
        log.info("asset.exists: ${resp}")
    }

    void assetGet(def assetId) {
        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.service(name: 'asset.get') {
            id(assetId)
//            xpath(ename: 'path', 'path')
        }

        def resp = conn.execute('service.execute', writer.toString())
        log.info("actor.self.describe: ${resp}")
    }

    void assetShareableList() {
        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.service(name: 'asset.shareable.list')

        def resp = conn.execute('service.execute', writer.toString())
        log.info("asset.shareable.list: ${resp}")
    }

    void recall(String pathId) {
//        java -jar /hpc/scripts/online-recall.jar \
//          -host datastore.mcri.edu.au -port 443 -ssl true -mode start \
//          -mount BIOI1 \
//          -path tommyl/9d6deb86a0cc2b861bd93813eabb97fca43f9c34/results/site_199704_00002-60adf7d_family_NA24385-WGS-THREEGENES-PROBAND.ped \
//          -store bioi1_01_prim \
//          -destination-store bioi1_01_gpfs \
//          -notification-email tommy.li@mcri.edu.au

        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.id(pathId)

        def re = conn.execute("user.self.describe");
        def email = re.stringValue("user/e-mail");
        xml.'notification-email-address'(email)

        xml.'send-notification-email'(true)
        xml.'migrate'(store: 'bioi1_01_prim', 'online')
        xml.'asset-service'(name: 'asset.content.copy.create') {
            store('bioi1_01_gpfs')
        }
        xml.'where'('csize>0')

        re = conn.execute("asset.preparation.request.create", writer.toString())
        int reqId = re.intValue("id")
        log.info("migrate-preparation-id: " + reqId)

        Writer writerStatus = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writerStatus)
        mb.id(reqId)
        def statusRe = conn.execute("asset.preparation.request.describe", writerStatus.toString())
        Thread.sleep(5000)
        boolean executing = statusRe.booleanValue("request/activity/executing", false);
        assert executing  // non-deterministic, sometimes it's false, especially for small files
    }

    void archive(String pathId) {
        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.id(pathId)

        def resp = conn.execute("user.self.describe");
        def email = resp.stringValue("user/e-mail");
        xml.'notification-email-address'(email)

        xml.'send-notification-email'(true)
        xml.'migrate'(store: 'bioi1_01_gpfs', 'offline')
        xml.'asset-service'(name: 'asset.content.migrate') {
            store('bioi1_01_prim')
        }
        xml.'where'('csize>0')

        resp = conn.execute("asset.preparation.request.create", writer.toString())
        int reqId = resp.intValue("id")
        println("migrate-preparation-id: " + reqId);

        Writer writerStatus = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writerStatus)
        mb.id(reqId)
        def statusRe = conn.execute("asset.preparation.request.describe", writerStatus.toString())
        boolean executing = statusRe.booleanValue("request/activity/executing", true);
        assert executing
    }
}
