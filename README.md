# Testing mfclient.jar

```bash
export MF_USERNAME=username
export MF_PASSWORD=secret

./gradlew shadowJar

java -jar build/libs/mftest-1.0-SNAPSHOT-all.jar
```
