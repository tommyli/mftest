package mcri.mf

static void main(String[] args) {
    MfClient underTest = MfClient.instance
    println "services: ${underTest.systemServiceList().join('\n')}"
    underTest.actorSelfDescribe()
    underTest.showAsset(747719513)
}
