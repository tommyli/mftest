package mcri.mf

static void main(String[] args) {
    MfClient underTest = MfClient.instance
//    println "services: ${underTest.systemServiceList().join('\n')}"
//    underTest.actorSelfDescribe()

    underTest.assetGet(97485468)
    String pathId = 'path=/mcri/group/BIOI1/tommyl/9d6deb86a0cc2b861bd93813eabb97fca43f9c34/results/site_199704_00002-60adf7d_family_NA24385-WGS-THREEGENES-PROBAND.ped'
//    underTest.assetShareableList()
//    underTest.assetMetaGet()
    underTest.assetExists(pathId)
    underTest.archive(pathId)
//    underTest.recall(pathId)
}
