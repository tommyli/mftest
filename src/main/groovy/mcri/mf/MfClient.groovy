package mcri.mf

import arc.mf.client.RemoteServer
import arc.mf.client.RemoteServer.Connection as MfConnection
import arc.mf.client.ServerClient
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder
import groovy.xml.XmlSlurper

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
        conn = (MfConnection) server.open()
        conn.connect(MF_APP, mfDomain, mfUsername, mfPassword)
    }

    Collection<String> systemServiceList() {
        def r = conn.execute("system.service.list")
        Collection<String> services = r.values("service")
        return services
    }

    void actorSelfDescribe() {
        def r = conn.execute("actor.self.describe")
        println r
    }

    void assetExists(def assetId) {
        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.service(name: 'asset.exists') {
            id(assetId)
        }

        // HELP: asset.get: call to service 'asset.get' failed: No permission to access metadata for asset (id) 97485468
        def ele = conn.execute('service.execute', writer.toString())
        println ele
    }

    void assetGet(def assetId) {
        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.service(name: 'asset.get') {
            id(assetId)
//            xpath(ename: 'path', 'path')
        }

        def ele = conn.execute('service.execute', writer.toString())
        println ele
    }

    void assetShareableList() {
        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.service(name: 'asset.shareable.list')

        // HELP: asset.get: call to service 'asset.get' failed: No permission to access metadata for asset (id) 97485468
        def ele = conn.execute('service.execute', writer.toString())
        println ele
        def res = new XmlSlurper().parseText(ele.toString())
        assert res
    }

    void archive() {
        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.id('path=/mcri/group/BIOI1/tommyl/9d6deb86a0cc2b861bd93813eabb97fca43f9c34/results/site_199704_00002-60adf7d_family_NA24385-WGS-THREEGENES-PROBAND.ped')
        xml.'notification-email-address'('tommy.li@mcri.edu.au')
        xml.'send-notification-email'(true)
        xml.'migrate'(store: 'bioi1_01_gpfs', 'offline')
        xml.'asset-service'(name: 'asset.content.migrate') {
            store('bioi1_01_prim')
        }
        xml.'where'('csize>0')

        def re = conn.execute("asset.preparation.request.create", writer.toString())
        int recallId = re.intValue("id")
        println("migrate-preparation-id: " + recallId);

        Thread.sleep(2000)

        Writer writerStatus = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writerStatus)
        mb.id(recallId)
        def statusRe = conn.execute("asset.preparation.request.describe", writerStatus.toString())
        assert statusRe
    }

    void recall() {
//        java -jar /hpc/scripts/online-recall.jar \
//          -host datastore.mcri.edu.au -port 443 -ssl true -mode start \
//          -mount BIOI1 \
//          -path tommyl/9d6deb86a0cc2b861bd93813eabb97fca43f9c34/results/site_199704_00002-60adf7d_family_NA24385-WGS-THREEGENES-PROBAND.ped \
//          -store bioi1_01_prim \
//          -destination-store bioi1_01_gpfs \
//          -notification-email tommy.li@mcri.edu.au

        Writer writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.id('path=/mcri/group/BIOI1/tommyl/9d6deb86a0cc2b861bd93813eabb97fca43f9c34/results/site_199704_00002-60adf7d_family_NA24385-WGS-THREEGENES-PROBAND.ped')
        xml.'notification-email-address'('tommy.li@mcri.edu.au')
        xml.'send-notification-email'(true)
        xml.'migrate'(store: 'bioi1_01_prim', 'online')
        xml.'asset-service'(name: 'asset.content.copy.create') {
            store('bioi1_01_gpfs')
        }
        xml.'where'('csize>0')

        def re = conn.execute("asset.preparation.request.create", writer.toString())
        int recallId = re.intValue("id")
        println("migrate-preparation-id: " + recallId);

        Thread.sleep(2000)

        Writer writerStatus = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(writerStatus)
        mb.id(recallId)
        def statusRe = conn.execute("asset.preparation.request.describe", writerStatus.toString())
        assert statusRe
    }
}
