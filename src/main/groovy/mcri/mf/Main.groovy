package mcri.mf

static void main(String[] args) {
    MfClient underTest = MfClient.instance
//    println "services: ${underTest.systemServiceList().join('\n')}"
//    underTest.actorSelfDescribe()

    underTest.assetGet(97485468)
    underTest.assetExists('path=/mcri/group/BIOI1/tommyl/9d6deb86a0cc2b861bd93813eabb97fca43f9c34/results/site_199704_00002-60adf7d_family_NA24385-WGS-THREEGENES-PROBAND.ped')
    underTest.archive()
//    underTest.recall()
//    underTest.assetShareableList()
//    underTest.assetMetaGet()
}
